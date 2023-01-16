// Copyright 2021 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.engine.plan.execution.stores.service.auth;

import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.tuple.Tuples;
import org.eclipse.collections.impl.utility.ListIterate;
import org.finos.legend.authentication.credentialprovider.CredentialProvider;
import org.finos.legend.authentication.credentialprovider.CredentialProviderProvider;
import org.finos.legend.authentication.credentialprovider.impl.ApikeyCredentialProvider;
import org.finos.legend.authentication.credentialprovider.impl.UserPasswordCredentialProvider;
import org.finos.legend.authentication.intermediationrule.IntermediationRuleProvider;
import org.finos.legend.authentication.intermediationrule.impl.ApiKeyFromVaultRule;
import org.finos.legend.authentication.intermediationrule.impl.UserPasswordFromVaultRule;
import org.finos.legend.authentication.vault.CredentialVaultProvider;
import org.finos.legend.authentication.vault.impl.CredentialVaultProviderForTest;
import org.finos.legend.engine.connection.ConnectionProvider;
import org.finos.legend.engine.connection.ConnectionSpecification;
import org.finos.legend.engine.plan.execution.stores.service.IServiceStoreExecutionExtension;
import org.finos.legend.engine.protocol.pure.v1.model.context.EngineErrorType;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.AuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SecurityScheme;
import org.finos.legend.engine.shared.core.function.Function5;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.operational.errorManagement.EngineException;

import java.io.IOException;
import java.util.*;

public class ServiceStoreConnectionProvider extends ConnectionProvider<Pair<HttpClientBuilder,RequestBuilder>>
{
    public ServiceStoreConnectionProvider(CredentialProviderProvider credentialProviderProvider) 
    {
        super(credentialProviderProvider);
    }

    public Pair<HttpClientBuilder,RequestBuilder> makeConnection(ConnectionSpecification connectionSpec, AuthenticationSpecification authenticationSpec, Identity identity) throws Exception
    {
        assert (connectionSpec instanceof ServiceStoreConnectionSpec);
        ServiceStoreConnectionSpec serviceStoreConnectionSpec = (ServiceStoreConnectionSpec) connectionSpec;

        assert(authenticationSpec instanceof ServiceStoreAuthenticationSpecification);
        ServiceStoreAuthenticationSpecification serviceStoreAuthenticationSpecification = (ServiceStoreAuthenticationSpecification) authenticationSpec;

        Map<String, SecurityScheme> securitySchemeMap = serviceStoreAuthenticationSpecification.securitySchemes;
        Map<String, AuthenticationSpecification> authenticationSpecMap = serviceStoreAuthenticationSpecification.authSpecs;

        for (Map.Entry<String, SecurityScheme> entry : securitySchemeMap.entrySet())
        {
            Credential cred = processSecurityScheme(entry.getKey(),entry.getValue(),authenticationSpecMap,identity);
            HttpClientBuilder httpClientBuilder = HttpClients.custom();
            RequestBuilder builder = makeRequestUtil(serviceStoreConnectionSpec, serviceStoreAuthenticationSpecification,identity);
            configureAuthentication(builder,httpClientBuilder,entry.getKey(),entry.getValue(), authenticationSpecMap, cred);
            return Tuples.pair(httpClientBuilder,builder);
        }
        return Tuples.pair(HttpClients.custom(),makeRequestUtil(serviceStoreConnectionSpec, serviceStoreAuthenticationSpecification,identity));
    }

    public static RequestBuilder makeRequestUtil(ServiceStoreConnectionSpec serviceStoreConnectionSpec, AuthenticationSpecification authenticationSpec, Identity identity) throws Exception
    {
        RequestBuilder builder = null;
        switch (serviceStoreConnectionSpec.httpMethod.toString())
        {
            case "GET":
                builder = RequestBuilder.get(serviceStoreConnectionSpec.uri);
                break;
            case "POST":
                builder = RequestBuilder.post(serviceStoreConnectionSpec.uri);
                break;
            default:
                throw new UnsupportedOperationException("The HTTP method " + serviceStoreConnectionSpec.httpMethod + " is not supported");
        }
        serviceStoreConnectionSpec.headers.forEach(builder::addHeader);

        return builder;
    }

    private static Credential processSecurityScheme(String schemeId, SecurityScheme securityScheme, Map<String,AuthenticationSpecification> authenticationSpecMap, Identity identity) throws Exception
    {
        ApikeyCredentialProvider apikeyCredentialProvider = new ApikeyCredentialProvider();
        UserPasswordCredentialProvider userPasswordCredentialProvider = new UserPasswordCredentialProvider();
        FastList<org.finos.legend.authentication.credentialprovider.CredentialProvider> credentialProviders = FastList.newListWith(apikeyCredentialProvider, userPasswordCredentialProvider);

        CredentialVaultProvider credentialVaultProvider = CredentialVaultProviderForTest.buildForTest()
                .withProperties("reference1", "key1")
                .build();
        ApiKeyFromVaultRule apiKeyRule = new ApiKeyFromVaultRule(credentialVaultProvider);
        UserPasswordFromVaultRule userPasswordFromVaultRule = new UserPasswordFromVaultRule(credentialVaultProvider);
        IntermediationRuleProvider intermediationRuleProvider = new IntermediationRuleProvider(FastList.newListWith(apiKeyRule,userPasswordFromVaultRule));

        CredentialProviderProvider credentialProviderProvider = new CredentialProviderProvider(credentialProviders, intermediationRuleProvider);


        return makeCredential(schemeId,credentialProviderProvider,authenticationSpecMap.get(schemeId),identity);


    }

    private static Credential makeCredential(String securitySchemeId, CredentialProviderProvider CredentialProviderProvider, AuthenticationSpecification authenticationSpecification, Identity identity)
    {
        ImmutableSet<? extends Class<? extends Credential>> inputCredentialTypes = identity.getCredentials().collect(c -> c.getClass()).toSet().toImmutable();
        Optional<org.finos.legend.authentication.credentialprovider.CredentialProvider> supportedCredentialProviders = CredentialProviderProvider.findMatchingCredentialProvider(authenticationSpecification.getClass(),inputCredentialTypes);
        String message = String.format("Did not find a credential provider for specification type=%s, input credential types=%s", authenticationSpecification.getClass(), inputCredentialTypes);

        try
        {
            CredentialProvider credentialProvider = supportedCredentialProviders.orElseThrow(() -> new RuntimeException(message));
            return credentialProvider.makeCredential(authenticationSpecification, identity);
        }
        catch (Exception e)
        {
            throw new RuntimeException(String.format("Unable to obtain cred for %s security scheme",securitySchemeId),e);

        }
    }
    private static void configureAuthentication(RequestBuilder builder,HttpClientBuilder httpClientBuilder, String securitySchemeId, SecurityScheme scheme,Map<String,AuthenticationSpecification> authenticationSpecMap, Credential credential) throws IOException
    {
        authenticateUtil(builder, httpClientBuilder, securitySchemeId, scheme, authenticationSpecMap.get(securitySchemeId), credential);
    }

    private static void authenticateUtil(RequestBuilder builder, HttpClientBuilder httpClientBuilder,String securitySchemeId, SecurityScheme scheme, AuthenticationSpecification authenticationSpec,Credential credential) throws IOException
    {
        List<Function5<SecurityScheme, AuthenticationSpecification, Credential, RequestBuilder, HttpClientBuilder, Boolean>> processors = ListIterate.flatCollect(IServiceStoreExecutionExtension.getExtensions(), ext -> ext.getExtraSecuritySchemeProcessors());

            ListIterate
                    .collect(processors,processor -> processor.value(scheme,authenticationSpec,credential,builder,httpClientBuilder))
                    .select(Objects::nonNull)
                    .getFirstOptional()
                    .orElseThrow(() -> new EngineException(" Error using security scheme " + securitySchemeId,authenticationSpec.sourceInformation, EngineErrorType.COMPILATION));

    }

}
