package org.finos.legend.engine.credentials.provider;

import org.finos.legend.engine.credentials.credential.LegendPlaintextUserPasswordCredential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.function.Supplier;

@Value.Enclosing
public class KerberosToUsernamePasswordCredentialFlow extends AbstractCredentialsProviderFlow<KerberosToUsernamePasswordCredentialFlow.ConfigurationParams, LegendKerberosCredential, LegendPlaintextUserPasswordCredential, LegendPlaintextUserPasswordCredential.CredentialRequestParams>
{
    private KerberosToUsernamePasswordCredentialFlow.ConfigurationParams configurationParams;

    @Value.Immutable
    interface ConfigurationParams
    {

    }

    public KerberosToUsernamePasswordCredentialFlow()
    {
        super(LegendKerberosCredential.class, LegendPlaintextUserPasswordCredential.class);
    }

    @Override
    public CredentialsProviderFlow<ConfigurationParams, LegendKerberosCredential, LegendPlaintextUserPasswordCredential, LegendPlaintextUserPasswordCredential.CredentialRequestParams> configure(ConfigurationParams configurationParams) {
        this.configurationParams = configurationParams;
        return this;
    }

    @Override
    public Supplier<LegendPlaintextUserPasswordCredential> makeCredential(Identity identity, Class<LegendKerberosCredential> inboundClass, LegendPlaintextUserPasswordCredential.CredentialRequestParams requestParams) throws Exception {
        Optional<LegendKerberosCredential> inboundCredential = identity.getCredential(inboundClass);
        // implement auth transformation
        String generated = "fake-user-" + identity.getName() + "-" + requestParams.someConfig();
        return () -> new LegendPlaintextUserPasswordCredential(generated, generated);
    }


}
