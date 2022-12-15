package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.usernamePassword;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.IntermediationRule;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.credential.PlainTextCredential;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;

public class AnyToPlainUsernamePasswordTextIntermediationRule extends IntermediationRule<UsernamePasswordAuthenticationSpec,Credential, PlaintextUserPasswordCredential> {
    @Override
    public PlaintextUserPasswordCredential makeCredential(UsernamePasswordAuthenticationSpec spec, Credential credential) throws Exception {
        return new PlaintextUserPasswordCredential(spec.username,spec.password.vaultReference);// TODO: add vaultImplementations
    }
}
