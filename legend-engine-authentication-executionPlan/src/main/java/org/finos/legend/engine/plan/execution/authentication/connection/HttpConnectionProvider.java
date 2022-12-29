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

package org.finos.legend.engine.plan.execution.authentication.connection;

import org.apache.commons.codec.binary.Base64;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.plan.execution.authentication.AuthenticationMethod;
import org.finos.legend.engine.plan.execution.authentication.ConnectionProvider;
import org.finos.legend.engine.plan.execution.authentication.ConnectionSpec;
import org.finos.legend.engine.plan.execution.authentication.provider.AuthenticationMethodProvider;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.runtime.connection.authentication.AuthenticationSpec;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;

import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnectionProvider extends ConnectionProvider<HttpURLConnection>
{
    private static AuthenticationMethodProvider authenticationMethodProvider;

    public HttpConnectionProvider(AuthenticationMethodProvider authenticationMethodProvider) {
        this.authenticationMethodProvider = authenticationMethodProvider;
    }

    public HttpURLConnection makeConnection(ConnectionSpec connectionSpec, AuthenticationSpec authenticationSpec, Identity identity) throws Exception
    {

        assert(connectionSpec instanceof HttpConnectionSpec);
        HttpConnectionSpec httpConnectionSpec = (HttpConnectionSpec) connectionSpec;

        FastList<AuthenticationMethod> supportedAuthenticationMethods = authenticationMethodProvider.getSupportedMethodFor(authenticationSpec.getClass());
        AuthenticationMethod chosenAuthenticationMethod = supportedAuthenticationMethods.get(0);

        return connectToServiceStore(httpConnectionSpec,authenticationSpec,chosenAuthenticationMethod,identity);

    }

    private static HttpURLConnection connectToServiceStore(HttpConnectionSpec httpConnectionSpec,AuthenticationSpec authenticationSpec, AuthenticationMethod chosenAuthenticationMethod, Identity identity) throws Exception
    {
        HttpURLConnection connection = (HttpURLConnection) (new URL(httpConnectionSpec.uri.toString()).openConnection());
        switch (httpConnectionSpec.httpMethod.toString())
        {
            case "GET":
                connection.setRequestMethod("GET");
                break;
            case "POST":
                connection.setRequestMethod("POST");
                break;
            default:
                throw new UnsupportedOperationException("The HTTP method " + httpConnectionSpec.httpMethod + " is not supported");
        }
        httpConnectionSpec.headers.forEach( header -> connection.setRequestProperty(header.getName(),header.getValue()));

        Credential credential = chosenAuthenticationMethod.makeCredential(authenticationSpec,identity);

        if (credential instanceof PlaintextUserPasswordCredential)
        {
            PlaintextUserPasswordCredential cred = (PlaintextUserPasswordCredential)credential;
            String encoding = Base64.encodeBase64String((cred.getUser()+ ":" + cred.getPassword()).getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encoding);
        }

        return connection;
    }


}
