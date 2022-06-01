package org.finos.legend.engine.credentials.credential;

import org.finos.legend.engine.shared.core.identity.credential.OAuthCredential;
import org.immutables.value.Value;

// TODO - deprecate/refactor use of OAuthCredential from legend-shared-core
@Value.Enclosing
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public class LegendOAuthCredential extends OAuthCredential
{
    public LegendOAuthCredential(String accessToken) {
        super(accessToken);
    }

    @Value.Immutable
    public
    interface CredentialRequestParams
    {
        String[] oauthScopes();
    }
}
