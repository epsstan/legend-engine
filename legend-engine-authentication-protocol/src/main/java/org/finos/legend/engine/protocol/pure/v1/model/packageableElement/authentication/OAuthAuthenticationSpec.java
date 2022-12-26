package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication;

import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;
public class OAuthAuthenticationSpec extends AuthenticationSpec
{
    public String grantType;
    public String clientId;
    public String clientSecretVaultReference;
    public String authServerUrl;

    public SourceInformation sourceInformation;
}
