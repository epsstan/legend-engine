package org.finos.legend.engine.authentication;

import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.finos.legend.engine.shared.core.identity.credential.OAuthCredential;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;

public class KereberosToOAuthCredentialFlow implements AuthenticationFlow<LegendKerberosCredential, OAuthCredential>
{
    private Object configurationParams;

    @Override
    public Class<LegendKerberosCredential> inboundCredentialType() {
        return LegendKerberosCredential.class;
    }

    @Override
    public Class<OAuthCredential> outboundCredentialType() {
        return OAuthCredential.class;
    }

    @Override
    public void configure(Object configurationParams)
    {
        this.configurationParams = configurationParams;
    }


    @Override
    public OAuthCredential makeCredential(Identity identity, LegendKerberosCredential input) throws Exception
    {
        // implement auth transformation
        String config = (String)this.configurationParams;
        String generated = "fake-token-" + identity.getName() + "-" + config;
        return new OAuthCredential(generated);
    }
}
