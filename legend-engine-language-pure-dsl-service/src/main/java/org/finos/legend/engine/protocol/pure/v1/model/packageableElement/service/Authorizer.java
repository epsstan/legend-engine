package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.service;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AuthorizerConfigPointer.class, name = "authorizerPointer"),
})
public abstract class Authorizer
{
    public String element;
    public SourceInformation elementSourceInformation;
    public SourceInformation sourceInformation;

    public abstract <T> T accept(AuthorizerVisitor<T> authorizerVisitor);
}
