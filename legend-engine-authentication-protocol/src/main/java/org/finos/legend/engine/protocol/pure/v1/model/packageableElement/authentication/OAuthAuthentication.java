package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication;

import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;
public class OAuthAuthentication extends Authentication
{
    public OauthCredential credential;
    public SourceInformation sourceInformation;
}
