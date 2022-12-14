package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.apiKey;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.IntermediationRule;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.credential.PlainTextCredential;
import org.finos.legend.engine.shared.core.identity.Credential;

public class AnyToPlainTextIntermediationRule extends IntermediationRule<ApiKeyAuthenticationSpec,Credential, PlainTextCredential> {
    @Override
    public PlainTextCredential makeCredential(ApiKeyAuthenticationSpec spec, Credential credential) throws Exception {
        return new PlainTextCredential(spec.value);// TODO: add vaultImplementations
    }
}
