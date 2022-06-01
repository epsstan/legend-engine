package org.finos.legend.engine.credentials.flow.authenticated;

import org.finos.legend.engine.credentials.credential.LegendAwsCredential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.immutables.value.Value;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

import java.util.Optional;
import java.util.function.Supplier;

@Value.Enclosing
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public class KerberosToAWSCredentialFlow extends AbstractAuthenticatedCredentialsProviderFlow<
        LegendKerberosCredential,
        LegendAwsCredential,
        LegendAwsCredential.CredentialRequestParams>
{
    private ConfigurationParams configurationParams;

    @Value.Immutable
    interface ConfigurationParams
    {

    }

    public KerberosToAWSCredentialFlow(ConfigurationParams configurationParams) {
        super(LegendKerberosCredential.class, LegendAwsCredential.class, LegendAwsCredential.CredentialRequestParams.class);
        this.configurationParams = configurationParams;
    }

    @Override
    public Supplier<LegendAwsCredential> makeCredential(Identity identity, LegendAwsCredential.CredentialRequestParams requestParams) throws Exception {
        Optional<LegendKerberosCredential> inboundCredential = identity.getCredential(this.inboundCredentialType());
        // use the inbound credential - for e.g authenticate with an STS service using the inbound credential and obtain an AwsCredential
        String generated = "fake-token-" + identity.getName() + "-";
        return () -> new LegendAwsCredential(AwsBasicCredentials.create("fakeAccessKeyId", "fakeSecretAccessKey"));
    }
}
