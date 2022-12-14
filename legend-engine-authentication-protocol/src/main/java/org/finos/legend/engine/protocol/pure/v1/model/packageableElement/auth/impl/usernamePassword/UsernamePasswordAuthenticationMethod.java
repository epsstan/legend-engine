package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.usernamePassword;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.AuthenticationMethod;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.IntermediationRule;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.credential.PlainTextCredential;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;

public class UsernamePasswordAuthenticationMethod extends AuthenticationMethod<UsernamePasswordAuthenticationSpec, PlainTextCredential> {
    @Override
    public PlainTextCredential makeCredential(UsernamePasswordAuthenticationSpec spec, Identity identity) throws Exception {

        if (!this.intermediationRules.isEmpty())
        {
            for (Credential credential : identity.getCredentials())
            {
                FastList<IntermediationRule> matchingRules = FastList.newList(intermediationRules).select(rule -> rule.matchesInputAndOutput(spec.getClass(), credential.getClass(), PlainTextCredential.class));
                if (!matchingRules.isEmpty())
                {
                    IntermediationRule intermediationRule = matchingRules.get(0);
                    return (PlainTextCredential) intermediationRule.makeCredential(spec,credential);
                }
            }
        }
        throw new UnsupportedOperationException("Unsupported Authentication spec type " + spec.getClass());
    }
}
