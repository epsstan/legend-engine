package org.finos.legend.engine.credentials.flow.authenticated;

import org.finos.legend.engine.credentials.credential.LegendOAuthCredential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.immutables.value.Value;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

@Value.Enclosing
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public class KerberosToOAuthCredentialFlow extends AbstractAuthenticatedCredentialsProviderFlow<
        LegendKerberosCredential,
        LegendOAuthCredential,
        LegendOAuthCredential.CredentialRequestParams>
{

    private ConfigurationParams configurationParams;

    @Value.Immutable
    interface ConfigurationParams {

    }

    public KerberosToOAuthCredentialFlow(ConfigurationParams configurationParams) {
        super(LegendKerberosCredential.class, LegendOAuthCredential.class, LegendOAuthCredential.CredentialRequestParams.class);
        this.configurationParams = configurationParams;
    }


    @Override
    public Supplier<LegendOAuthCredential> makeCredential(Identity identity, LegendOAuthCredential.CredentialRequestParams requestParams) throws Exception {
        Optional<LegendKerberosCredential> inboundCredential = identity.getCredential(this.inboundCredentialType());
        // use the inbound credential - for e.g authenticate with an STS service using the inbound credential and obtain an LegendOAuthCredential
        String generated = "fake-token-" + identity.getName() + "-" + Arrays.toString(requestParams.oauthScopes());
        return () -> new LegendOAuthCredential(generated);
    }
}
