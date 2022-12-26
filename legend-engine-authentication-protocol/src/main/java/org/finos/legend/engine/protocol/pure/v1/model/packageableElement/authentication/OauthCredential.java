package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication;

import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;
public class OauthCredential extends Credential{

    public String token;
    public SourceInformation sourceInformation;
}
