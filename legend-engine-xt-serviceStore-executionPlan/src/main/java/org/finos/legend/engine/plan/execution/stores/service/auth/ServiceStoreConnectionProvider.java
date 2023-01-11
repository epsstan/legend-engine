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

import org.apache.commons.codec.binary.Base64;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.tuple.Tuples;
import org.eclipse.collections.impl.utility.ListIterate;
import org.finos.legend.engine.plan.execution.authentication.AuthenticationMethod;
import org.finos.legend.engine.plan.execution.authentication.ConnectionProvider;
import org.finos.legend.engine.plan.execution.authentication.ConnectionSpec;
import org.finos.legend.engine.plan.execution.authentication.IntermediationRule;
import org.finos.legend.engine.plan.execution.authentication.provider.AuthenticationMethodProvider;
import org.finos.legend.engine.plan.execution.authentication.provider.IntermediationRuleProvider;
import org.finos.legend.engine.plan.execution.stores.service.IServiceStoreExecutionExtension;
import org.finos.legend.engine.protocol.pure.v1.model.context.EngineErrorType;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.runtime.connection.authentication.AuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.ApiKeySecurityScheme;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.CompositeSecurityScheme;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SecurityScheme;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SimpleHttpSecurityScheme;
import org.finos.legend.engine.shared.core.function.Function4;
import org.finos.legend.engine.shared.core.function.Function5;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextCredential;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;
import org.finos.legend.engine.shared.core.operational.errorManagement.EngineException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;

//TODO: HttpURLConnection API is not feature rich + hard to maintain, Refactor to use apache HttpClient api
public class ServiceStoreConnectionProvider extends ConnectionProvider<Pair<HttpClientBuilder,RequestBuilder>>
{
    private static AuthenticationMethodProvider authenticationMethodProvider;

    public ServiceStoreConnectionProvider(AuthenticationMethodProvider authenticationMethodProvider) {
        this.authenticationMethodProvider = authenticationMethodProvider;
    }

    public Pair<HttpClientBuilder,RequestBuilder> makeConnection(ConnectionSpec connectionSpec, AuthenticationSpec authenticationSpec, Identity identity) throws Exception
    {
        assert (connectionSpec instanceof ServiceStoreConnectionSpec);
        ServiceStoreConnectionSpec serviceStoreConnectionSpec = (ServiceStoreConnectionSpec) connectionSpec;

        assert(authenticationSpec instanceof ServiceStoreAuthenticationSpec);
        ServiceStoreAuthenticationSpec serviceStoreAuthenticationSpec = (ServiceStoreAuthenticationSpec) authenticationSpec;

        Map<String, SecurityScheme> securitySchemeMap = serviceStoreAuthenticationSpec.securitySchemes;
        Map<String, AuthenticationSpec> authenticationSpecMap = serviceStoreAuthenticationSpec.authSpecs;

        for (Map.Entry<String, SecurityScheme> entry : securitySchemeMap.entrySet())
        {
            Credential cred = processSecurityScheme(entry.getKey(),entry.getValue(),authenticationSpecMap,identity);
            HttpClientBuilder httpClientBuilder = HttpClients.custom();
            RequestBuilder builder = makeRequestUtil(serviceStoreConnectionSpec,serviceStoreAuthenticationSpec,identity);
            configureAuthentication(builder,httpClientBuilder,entry.getKey(),entry.getValue(), authenticationSpecMap, cred);
            return Tuples.pair(httpClientBuilder,builder);
        }
        return Tuples.pair(HttpClients.custom(),makeRequestUtil(serviceStoreConnectionSpec,serviceStoreAuthenticationSpec,identity));
    }

    public static RequestBuilder makeRequestUtil(ServiceStoreConnectionSpec serviceStoreConnectionSpec, AuthenticationSpec authenticationSpec, Identity identity) throws Exception
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

    private static Credential processSecurityScheme(String schemeId, SecurityScheme securityScheme, Map<String,AuthenticationSpec> authenticationSpecMap, Identity identity) throws Exception
    {
        FastList<AuthenticationMethod> allMethods = FastList.newList(ServiceLoader.load(AuthenticationMethod.class));
        FastList<IntermediationRule> allRules = FastList.newList(ServiceLoader.load(IntermediationRule.class));
        AuthenticationMethodProvider authenticationMethodProvider = new AuthenticationMethodProvider(allMethods,new IntermediationRuleProvider(allRules));

        if (securityScheme instanceof CompositeSecurityScheme)
        {
           Map<String,Credential> credentialMap = Maps.mutable.empty();
           Arrays.asList(schemeId.split("::")).forEach( id -> {
               try
               {
                   Credential cred = makeCredential(id,authenticationMethodProvider,authenticationSpecMap.get(id),identity);
                   credentialMap.put(id,cred);
               }
               catch (Exception e)
               {
                   throw new RuntimeException(e);
               }
           } );
           return new CompositeCredential(credentialMap);
        }
        else
        {
            return makeCredential(schemeId,authenticationMethodProvider,authenticationSpecMap.get(schemeId),identity);
        }

    }

    private static Credential makeCredential(String securitySchemeId, AuthenticationMethodProvider authenticationMethodProvider, AuthenticationSpec authenticationSpec, Identity identity)
    {
        FastList<AuthenticationMethod> supportedAuthenticationMethods = authenticationMethodProvider.getSupportedMethodFor(authenticationSpec.getClass());
        AuthenticationMethod chosenAuthenticationMethod = supportedAuthenticationMethods.get(0);

        try
        {
            return chosenAuthenticationMethod.makeCredential(authenticationSpec,identity);
        }
        catch (Exception e)
        {
            throw new RuntimeException(String.format("Unable to obtain cred for %s security scheme",securitySchemeId),e);

        }
    }
    private static void configureAuthentication(RequestBuilder builder,HttpClientBuilder httpClientBuilder, String securitySchemeId, SecurityScheme scheme,Map<String,AuthenticationSpec> authenticationSpecMap, Credential credential) throws IOException
    {
        if (scheme instanceof CompositeSecurityScheme)
        {
            CompositeSecurityScheme compositeSecurityScheme = (CompositeSecurityScheme) scheme;
            assert(credential instanceof CompositeCredential);
            CompositeCredential compositeCredential = (CompositeCredential) credential;
            Arrays.asList(securitySchemeId.split("::")).forEach( id -> {
                    try
                    {
                        authenticateUtil(builder,httpClientBuilder,id,compositeSecurityScheme.securitySchemes.get(id),authenticationSpecMap.get(id),compositeCredential.getCredentials().get(id));
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                } );

        }
        else
        {
            authenticateUtil(builder, httpClientBuilder, securitySchemeId, scheme, authenticationSpecMap.get(securitySchemeId), credential);
        }
    }

    private static void authenticateUtil(RequestBuilder builder, HttpClientBuilder httpClientBuilder,String securitySchemeId, SecurityScheme scheme, AuthenticationSpec authenticationSpec,Credential credential) throws IOException
    {
        List<Function5<SecurityScheme, AuthenticationSpec, Credential, RequestBuilder, HttpClientBuilder, Boolean>> processors = ListIterate.flatCollect(IServiceStoreExecutionExtension.getExtensions(), ext -> ext.getExtraSecuritySchemeProcessors());

            ListIterate
                    .collect(processors,processor -> processor.value(scheme,authenticationSpec,credential,builder,httpClientBuilder))
                    .select(Objects::nonNull)
                    .getFirstOptional()
                    .orElseThrow(() -> new EngineException(" Error using security scheme " + securitySchemeId,authenticationSpec.sourceInformation, EngineErrorType.COMPILATION));

    }

}
