package org.finos.legend.engine.credentials.credential;

import org.finos.legend.engine.shared.core.identity.Credential;
import org.immutables.value.Value;
import software.amazon.awssdk.auth.credentials.AwsCredentials;

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
    @Value.Style(typeImmutable = "LegendAwsCredential*")
    public interface CredentialRequestParams
    {
    }
}
