package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.connection;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import javax.ws.rs.HttpMethod;
import java.net.URI;
import java.util.List;

public class HttpConnectionSpec {

    public URI uri;
    public String httpMethod;
    public List<Header> headers;
    public StringEntity requestBodyDescription;
    public String mimeType;
    public StoreType storeType;

    public static enum StoreType
    {
        SERVICE_STORE
    }

    public HttpConnectionSpec(URI uri, String httpMethod, List<Header> headers, StringEntity requestBodyDescription, String mimeType, StoreType storeType) {
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.headers = headers;
        this.requestBodyDescription = requestBodyDescription;
        this.mimeType = mimeType;
        this.storeType = storeType;
    }
}
