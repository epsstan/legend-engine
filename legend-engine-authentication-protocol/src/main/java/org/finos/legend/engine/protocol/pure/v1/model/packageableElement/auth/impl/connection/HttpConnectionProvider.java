package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.connection;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.AuthenticationMethod;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.ConnectionProvider;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.connection.HttpConnectionSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.provider.AuthenticationMethodProvider;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;

import java.net.HttpURLConnection;
import java.net.URL;

//TODO: This class to extend connectionProvider
public class HttpConnectionProvider //extends ConnectionProvider<HttpURLConnection>
{
    private static AuthenticationMethodProvider authenticationMethodProvider;

    public HttpConnectionProvider(AuthenticationMethodProvider authenticationMethodProvider) {
        this.authenticationMethodProvider = authenticationMethodProvider;
    }

    public static HttpURLConnection makeConnection(Object connectionSpec, AuthenticationSpec authenticationSpec, Identity identity) throws Exception
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

//        if (credential instanceof PlaintextUserPasswordCredential)
//        {
//            PlaintextUserPasswordCredential cred = (PlaintextUserPasswordCredential)credential;
//            String encoding = Base64.encodeBase64String((cred.getUser()+ ":" + cred.getPassword()).getBytes());
//            connection.setRequestProperty("Authorization", "Basic " + encoding);
//        }

        return connection;
    }


}
