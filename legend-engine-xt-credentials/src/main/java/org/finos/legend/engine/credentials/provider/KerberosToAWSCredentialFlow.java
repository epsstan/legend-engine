package org.finos.legend.engine.credentials.provider;

import org.finos.legend.engine.credentials.credential.LegendAwsCredential;
import org.finos.legend.engine.credentials.credential.LegendOAuthCredential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.immutables.value.Value;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

import java.util.Optional;
import java.util.function.Supplier;

public class KerberosToAWSCredentialFlow extends AbstractCredentialsProviderFlow<
        LegendKerberosCredential,
        LegendAwsCredential,
        KerberosToAWSCredentialFlow.ConfigurationParams,
        LegendAwsCredential.CredentialRequestParams>
{
    private ConfigurationParams configurationParams;

    @Value.Immutable
    @Value.Style(typeImmutable = "KerberosToAWSCredentialFlow*")
    interface ConfigurationParams
    {

    }

    public KerberosToAWSCredentialFlow() {
        super(LegendKerberosCredential.class, LegendAwsCredential.class);
    }

    @Override
    public CredentialsProviderFlow<LegendKerberosCredential, LegendAwsCredential, ConfigurationParams, LegendAwsCredential.CredentialRequestParams> configure(KerberosToAWSCredentialFlow.ConfigurationParams configurationParams)
    {
        this.configurationParams = configurationParams;
        return this;
    }

    @Override
    public Supplier<LegendAwsCredential> makeCredential(Identity identity, Class<LegendKerberosCredential> inboundClass, LegendAwsCredential.CredentialRequestParams requestParams) throws Exception {
        Optional<LegendKerberosCredential> inboundCredential = identity.getCredential(inboundClass);
        // implement auth transformation
        String generated = "fake-token-" + identity.getName() + "-";
        return () -> new LegendAwsCredential(AwsBasicCredentials.create("fakeAccessKeyId", "fakeSecretAccessKey"));
    }
}
