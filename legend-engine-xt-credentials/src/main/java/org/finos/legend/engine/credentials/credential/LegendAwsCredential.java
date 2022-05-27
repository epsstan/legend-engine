package org.finos.legend.engine.credentials.credential;

import org.finos.legend.engine.shared.core.identity.Credential;
import org.immutables.value.Value;
import software.amazon.awssdk.auth.credentials.AwsCredentials;

@Value.Enclosing
public class LegendAwsCredential implements Credential
{
    private AwsCredentials underlying;

    public LegendAwsCredential(AwsCredentials underlying) {
        this.underlying = underlying;
    }

    public AwsCredentials getUnderlying() {
        return underlying;
    }

    @Value.Immutable
    public interface CredentialRequestParams
    {
    }
}
