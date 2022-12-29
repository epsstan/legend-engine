package org.finos.legend.engine.plan.execution.stores.service.auth;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.runtime.connection.authentication.AuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SecurityScheme;

import java.util.Map;

public class ServiceStoreAuthenticationSpec extends AuthenticationSpec {

    public Map<String,SecurityScheme> securitySchemes;
    public Map<String, AuthenticationSpec> authSpecs;

    public ServiceStoreAuthenticationSpec(Map<String,SecurityScheme> securitySchemes, Map<String, AuthenticationSpec> authSpecs) {
        this.securitySchemes = securitySchemes;
        this.authSpecs = authSpecs;
    }
}
