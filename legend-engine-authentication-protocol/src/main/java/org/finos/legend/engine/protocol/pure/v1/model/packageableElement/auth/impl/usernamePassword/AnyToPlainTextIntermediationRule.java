package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.usernamePassword;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.IntermediationRule;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.credential.PlainTextCredential;
import org.finos.legend.engine.shared.core.identity.Credential;

public class AnyToPlainTextIntermediationRule extends IntermediationRule<UsernamePasswordAuthenticationSpec,Credential, PlainTextCredential> {
    @Override
    public PlainTextCredential makeCredential(UsernamePasswordAuthenticationSpec spec, Credential credential) throws Exception {
        return new PlainTextCredential(spec.password.vaultReference);// TODO: add vaultImplementations
    }
}
