package org.finos.legend.engine.plan.execution.stores.service.auth;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SecurityScheme;

import java.util.List;
import java.util.Map;

public class ServiceStoreAuthenticationSpec extends AuthenticationSpec {

    public List<SecurityScheme> securitySchemes;
    public Map<String, AuthenticationSpec> authSpecs;

    public ServiceStoreAuthenticationSpec(List<SecurityScheme> securitySchemes, Map<String, AuthenticationSpec> authSpecs) {
        this.securitySchemes = securitySchemes;
        this.authSpecs = authSpecs;
    }
}
