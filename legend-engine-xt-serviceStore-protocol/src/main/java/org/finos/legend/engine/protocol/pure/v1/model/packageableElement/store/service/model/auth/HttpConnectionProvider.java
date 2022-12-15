package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.auth;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.ConnectionProvider;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.connection.HttpConnectionSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.shared.core.identity.Identity;

import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnectionProvider //extends ConnectionProvider<HttpURLConnection>
{
    public static HttpURLConnection makeConnection(Object connectionSpec, AuthenticationSpec authenticationSpec, Identity identity) throws Exception {

        assert(connectionSpec instanceof HttpConnectionSpec);
        HttpConnectionSpec httpConnectionSpec = (HttpConnectionSpec) connectionSpec;

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

        switch (httpConnectionSpec.storeType)
        {
            case SERVICE_STORE:
                return connection;
            default:
                throw new UnsupportedOperationException("Store Type " + httpConnectionSpec.storeType + " is not supported");

        }
    }


}
