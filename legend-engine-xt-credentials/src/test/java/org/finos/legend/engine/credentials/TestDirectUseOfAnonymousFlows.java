package org.finos.legend.engine.credentials;

import org.finos.legend.engine.credentials.credential.ImmutableLegendPlaintextUserPasswordCredential;
import org.finos.legend.engine.credentials.credential.LegendPlaintextUserPasswordCredential;
import org.finos.legend.engine.credentials.flow.anonymous.ImmutableStaticUsernamePasswordCredentialFlow;
import org.finos.legend.engine.credentials.flow.anonymous.StaticUsernamePasswordCredentialFlow;
import org.finos.legend.engine.shared.core.vault.PropertiesVaultImplementation;
import org.finos.legend.engine.shared.core.vault.Vault;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class TestDirectUseOfAnonymousFlows
{
    private PropertiesVaultImplementation propertiesVaultImplementation;

    @Before
    public void setup()
    {
        Properties properties = new Properties();
        properties.put("user1", "password1");
        this.propertiesVaultImplementation = new PropertiesVaultImplementation(properties);
        Vault.INSTANCE.registerImplementation(propertiesVaultImplementation);
    }

    @Test
    public void userPasswordFlow() throws Exception
    {
        StaticUsernamePasswordCredentialFlow flow = new StaticUsernamePasswordCredentialFlow(
                ImmutableStaticUsernamePasswordCredentialFlow.ConfigurationParams.builder()
                        .build());

        Supplier<LegendPlaintextUserPasswordCredential> supplier =
                flow.makeCredential(
                        ImmutableLegendPlaintextUserPasswordCredential.CredentialRequestParams.builder().name("user1").build()
                );

        LegendPlaintextUserPasswordCredential credential = supplier.get();
        assertEquals("user1", credential.getUser());
        assertEquals("password1", credential.getPassword());
    }

}
