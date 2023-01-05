package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model;

import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;

import java.util.List;

public class IdentifiedSecurityScheme extends SecurityScheme {

    public String id;
    public SourceInformation sourceInformation;

    public IdentifiedSecurityScheme() {
    }

    public IdentifiedSecurityScheme(String id, SourceInformation sourceInformation)
    {
        this.id = id;
        this.sourceInformation = sourceInformation;
    }

    @Override
    public <T> T accept(SecuritySchemeVisitor<T> securitySchemeVisitor) {
        return null;
    }
}
