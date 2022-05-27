package org.finos.legend.engine.credentials.provider;

import org.finos.legend.engine.credentials.credential.LegendOAuthCredential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.immutables.value.Value;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

public class KerberosToOAuthCredentialFlow extends AbstractCredentialsProviderFlow<
        LegendKerberosCredential,
        LegendOAuthCredential,
        KerberosToOAuthCredentialFlow.ConfigurationParams,
        LegendOAuthCredential.CredentialRequestParams
        >
{

    private ConfigurationParams configurationParams;

    @Value.Immutable
    @Value.Style(typeImmutable = "KerberosToOAuthCredentialFlow*")
    interface ConfigurationParams {

    }

    public KerberosToOAuthCredentialFlow() {
        super(LegendKerberosCredential.class, LegendOAuthCredential.class);
    }

    @Override
    public CredentialsProviderFlow<LegendKerberosCredential, LegendOAuthCredential, ConfigurationParams, LegendOAuthCredential.CredentialRequestParams> configure(ConfigurationParams configurationParams) {
        this.configurationParams = configurationParams;
        return this;
    }

    @Override
    public Supplier<LegendOAuthCredential> makeCredential(Identity identity, Class<LegendKerberosCredential> inboundClass, LegendOAuthCredential.CredentialRequestParams requestParams) throws Exception {
        Optional<LegendKerberosCredential> inboundCredential = identity.getCredential(inboundClass);
       // implement auth transformation
        String generated = "fake-token-" + identity.getName() + "-" + Arrays.toString(requestParams.oauthScopes());
        return () -> new LegendOAuthCredential(generated);
    }
}
