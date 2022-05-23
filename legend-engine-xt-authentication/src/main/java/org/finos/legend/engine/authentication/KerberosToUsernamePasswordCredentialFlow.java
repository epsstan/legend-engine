package org.finos.legend.engine.authentication;

import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;

public class KerberosToUsernamePasswordCredentialFlow implements AuthenticationFlow<LegendKerberosCredential, PlaintextUserPasswordCredential>
{
    private Object configurationParams;

    @Override
    public Class<LegendKerberosCredential> inboundCredentialType() {
        return LegendKerberosCredential.class;
    }

    @Override
    public Class<PlaintextUserPasswordCredential> outboundCredentialType() {
        return PlaintextUserPasswordCredential.class;
    }

    @Override
    public void configure(Object configurationParams)
    {
        this.configurationParams = configurationParams;
    }


    @Override
    public PlaintextUserPasswordCredential makeCredential(Identity identity, LegendKerberosCredential input) throws Exception
    {
        // implement auth transformation
        String config = (String)this.configurationParams;
        String generated = "fake-user-" + identity.getName() + "-" + config;
        return new PlaintextUserPasswordCredential(generated, generated);
    }
}
