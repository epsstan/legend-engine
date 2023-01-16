package org.finos.legend.engine.plan.execution.stores.service.auth;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.AuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.AuthenticationSpecificationVisitor;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SecurityScheme;

import java.util.Map;

public class ServiceStoreAuthenticationSpecification extends AuthenticationSpecification {

    public Map<String,SecurityScheme> securitySchemes;
    public Map<String, AuthenticationSpecification> authSpecs;

    public ServiceStoreAuthenticationSpecification(Map<String,SecurityScheme> securitySchemes, Map<String, AuthenticationSpecification> authSpecs) {
        this.securitySchemes = securitySchemes;
        this.authSpecs = authSpecs;
    }

    @Override
    public <T> T accept(AuthenticationSpecificationVisitor<T> visitor) {
        return null;
    }
}
