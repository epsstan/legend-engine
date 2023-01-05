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
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.plan.execution.authentication.AuthenticationMethod;
import org.finos.legend.engine.plan.execution.authentication.ConnectionProvider;
import org.finos.legend.engine.plan.execution.authentication.ConnectionSpec;
import org.finos.legend.engine.plan.execution.authentication.IntermediationRule;
import org.finos.legend.engine.plan.execution.authentication.provider.AuthenticationMethodProvider;
import org.finos.legend.engine.plan.execution.authentication.provider.IntermediationRuleProvider;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.runtime.connection.authentication.AuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.ApiKeySecurityScheme;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.CompositeSecurityScheme;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SecurityScheme;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SimpleHttpSecurityScheme;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextCredential;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.ServiceLoader;

//TODO: HttpURLConnection API is not feature rich + hard to maintain, Refactor to use apache HttpClient api
public class ServiceStoreConnectionProvider extends ConnectionProvider<HttpURLConnection>
{
    private static AuthenticationMethodProvider authenticationMethodProvider;

    public ServiceStoreConnectionProvider(AuthenticationMethodProvider authenticationMethodProvider) {
        this.authenticationMethodProvider = authenticationMethodProvider;
    }

    public HttpURLConnection makeConnection(ConnectionSpec connectionSpec, AuthenticationSpec authenticationSpec, Identity identity) throws Exception
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
            HttpURLConnection conn = makeConnectionUtil(serviceStoreConnectionSpec,serviceStoreAuthenticationSpec,identity);
            configureAuthentication(conn,entry.getKey(),entry.getValue(),cred);
            if (conn.getResponseCode() == HttpStatus.SC_OK)
            {
                return conn;
            }
        }
        return makeConnectionUtil(serviceStoreConnectionSpec,serviceStoreAuthenticationSpec,identity);
    }

    public HttpURLConnection makeConnectionUtil(ServiceStoreConnectionSpec serviceStoreConnectionSpec, AuthenticationSpec authenticationSpec, Identity identity) throws Exception
    {
        HttpURLConnection connection = (HttpURLConnection) (new URL(serviceStoreConnectionSpec.uri.toString()).openConnection());
        switch (serviceStoreConnectionSpec.httpMethod.toString())
        {
            case "GET":
                connection.setRequestMethod("GET");
                break;
            case "POST":
                connection.setRequestMethod("POST");
                break;
            default:
                throw new UnsupportedOperationException("The HTTP method " + serviceStoreConnectionSpec.httpMethod + " is not supported");
        }
        serviceStoreConnectionSpec.headers.forEach(header -> connection.setRequestProperty(header.getName(),header.getValue()));

        return connection;
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
    private static void configureAuthentication(HttpURLConnection connection, String securitySchemeId, SecurityScheme scheme, Credential credential) throws IOException
    {
        if (scheme instanceof CompositeSecurityScheme)
        {
            CompositeSecurityScheme compositeSecurityScheme = (CompositeSecurityScheme) scheme;
            CompositeCredential compositeCredential = (CompositeCredential) credential;
            assert(credential instanceof CompositeCredential);
            Arrays.asList(securitySchemeId.split("::")).forEach( id -> {
                    try
                    {
                        authenticateUtil(connection,id,compositeSecurityScheme.securitySchemes.get(id),compositeCredential.getCredentials().get(id));
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                } );

        }
        authenticateUtil(connection,securitySchemeId,scheme,credential);
    }

    private static void authenticateUtil(HttpURLConnection connection,String securitySchemeId, SecurityScheme scheme,Credential credential) throws IOException
    {
        if (scheme instanceof SimpleHttpSecurityScheme)
        {
            assert(credential instanceof PlaintextUserPasswordCredential);
            PlaintextUserPasswordCredential cred = (PlaintextUserPasswordCredential)credential;
            String encoding = Base64.encodeBase64String((cred.getUser()+ ":" + cred.getPassword()).getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encoding);
        }
        else if (scheme instanceof ApiKeySecurityScheme)
        {
            assert(credential instanceof PlaintextCredential);
            PlaintextCredential cred = (PlaintextCredential) credential;
            ApiKeySecurityScheme apiKeySecurityScheme = (ApiKeySecurityScheme) scheme;
            if (apiKeySecurityScheme.location.equals("cookie"))
            {
                String cookie = connection.getRequestProperty("Cookie");
                if (cookie!=null)
                {
                    String newCookieString = cookie + ";" + String.format("%s=%s",apiKeySecurityScheme.keyName,cred.getValue());
                    connection.setRequestProperty("Cookie",newCookieString);
                }
                else
                {
                    connection.setRequestProperty("Cookie", String.format("%s=%s", apiKeySecurityScheme.keyName, cred.getValue()));
                }
            }
        }
    }

}
