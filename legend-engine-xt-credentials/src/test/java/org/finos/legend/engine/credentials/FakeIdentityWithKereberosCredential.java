package org.finos.legend.engine.credentials;

import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;

public class FakeIdentityWithKereberosCredential extends Identity {
    public FakeIdentityWithKereberosCredential(String name) {
        super(name, new LegendKerberosCredential(null));
    }
}