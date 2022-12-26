package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.apiKey;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;

public class ApiKeyAuthenticationSpec extends AuthenticationSpec {

    //TODO: Refactor value to be a vault credential
    public String value;
}
