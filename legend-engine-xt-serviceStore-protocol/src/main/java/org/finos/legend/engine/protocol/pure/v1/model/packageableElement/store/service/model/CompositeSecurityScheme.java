package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model;

import java.util.Map;

public class CompositeSecurityScheme extends SecurityScheme{

    public String operation;
    public Map<String,SecurityScheme> securitySchemes;


    @Override
    public <T> T accept(SecuritySchemeVisitor<T> securitySchemeVisitor) {
        return null;
    }
}
