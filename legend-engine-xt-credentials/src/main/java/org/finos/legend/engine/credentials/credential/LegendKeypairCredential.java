package org.finos.legend.engine.credentials.credential;

import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.credential.PrivateKeyCredential;
import org.immutables.value.Value;

@Value.Enclosing
public class LegendKeypairCredential implements Credential
{
    private PrivateKeyCredential underlying;

    public LegendKeypairCredential(PrivateKeyCredential privateKeyCredential) {
        this.underlying = privateKeyCredential;
    }

    public PrivateKeyCredential getUnderlying() {
        return underlying;
    }

    @Value.Immutable
    public interface CredentialRequestParams
    {
        String userName();
        String privateKeyVaultReference();
        String passphraseVaultReference();
    }
}
