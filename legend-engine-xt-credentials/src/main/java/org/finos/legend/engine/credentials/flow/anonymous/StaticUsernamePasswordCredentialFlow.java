package org.finos.legend.engine.credentials.flow.anonymous;

import org.finos.legend.engine.credentials.credential.LegendPlaintextUserPasswordCredential;
import org.finos.legend.engine.shared.core.vault.Vault;
import org.immutables.value.Value;

import java.util.function.Supplier;

@Value.Enclosing
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public class StaticUsernamePasswordCredentialFlow extends AbstractAnonymousCredentialsProviderFlow<
        LegendPlaintextUserPasswordCredential,
        LegendPlaintextUserPasswordCredential.CredentialRequestParams>
{
    private StaticUsernamePasswordCredentialFlow.ConfigurationParams configurationParams;

    @Value.Immutable
    interface ConfigurationParams
    {

    }

    public StaticUsernamePasswordCredentialFlow(ConfigurationParams configurationParams)
    {
        super(LegendPlaintextUserPasswordCredential.class, LegendPlaintextUserPasswordCredential.CredentialRequestParams.class);
        this.configurationParams = configurationParams;
    }

    @Override
    public Supplier<LegendPlaintextUserPasswordCredential> makeCredential(LegendPlaintextUserPasswordCredential.CredentialRequestParams requestParams) throws Exception {
        String vaultSecretReference = requestParams.name();
        String password = Vault.INSTANCE.getValue(vaultSecretReference);
        return () -> new LegendPlaintextUserPasswordCredential(requestParams.name(), password);
    }


}
