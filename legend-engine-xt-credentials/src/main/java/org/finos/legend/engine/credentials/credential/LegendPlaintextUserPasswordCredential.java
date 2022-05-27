package org.finos.legend.engine.credentials.credential;

import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;
import org.immutables.value.Value;

// TODO - deprecate or refactor PlaintextUserPasswordCredential from legend-engine-shared-core

@Value.Enclosing
public class LegendPlaintextUserPasswordCredential extends PlaintextUserPasswordCredential
{
    public LegendPlaintextUserPasswordCredential(String user, String password)
    {
        super(user, password);
    }

    @Value.Immutable
    public interface CredentialRequestParams
    {
        String someConfig();
    }
}
