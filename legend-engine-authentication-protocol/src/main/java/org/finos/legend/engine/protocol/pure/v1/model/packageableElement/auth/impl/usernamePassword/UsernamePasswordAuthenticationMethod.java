package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.usernamePassword;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.AuthenticationMethod;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.IntermediationRule;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.credential.PlainTextCredential;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;

public class UsernamePasswordAuthenticationMethod extends AuthenticationMethod<UsernamePasswordAuthenticationSpec, PlaintextUserPasswordCredential> {
    @Override
    public PlaintextUserPasswordCredential makeCredential(UsernamePasswordAuthenticationSpec spec, Identity identity) throws Exception {

        if (!this.intermediationRules.isEmpty())
        {
            for (Credential credential : identity.getCredentials())
            {
                FastList<IntermediationRule> matchingRules = FastList.newList(intermediationRules).select(rule -> rule.matchesOutput(spec.getClass(), PlaintextUserPasswordCredential.class));
                if (!matchingRules.isEmpty())
                {
                    IntermediationRule intermediationRule = matchingRules.get(0);
                    return (PlaintextUserPasswordCredential) intermediationRule.makeCredential(spec,credential);
                }
            }
        }
        throw new UnsupportedOperationException("Unsupported Authentication spec type " + spec.getClass());
    }
}
