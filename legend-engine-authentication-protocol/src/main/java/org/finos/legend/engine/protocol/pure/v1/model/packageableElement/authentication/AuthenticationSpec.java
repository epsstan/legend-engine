package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.apiKey.ApiKeyAuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.usernamePassword.UsernamePasswordAuthenticationSpec;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_type")
@JsonSubTypes({
            @JsonSubTypes.Type(value = OAuthAuthenticationSpec.class, name = "oauth"),
            @JsonSubTypes.Type(value = UsernamePasswordAuthenticationSpec.class, name = "basic" ),
            @JsonSubTypes.Type(value = ApiKeyAuthenticationSpec.class, name = "apiKey")
    })
public class AuthenticationSpec {

    public SourceInformation sourceInformation;
}
