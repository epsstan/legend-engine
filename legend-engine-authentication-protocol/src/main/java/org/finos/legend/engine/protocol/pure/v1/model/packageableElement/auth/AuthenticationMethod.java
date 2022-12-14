package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;

import java.util.List;

public abstract class AuthenticationMethod<Spec, Cred extends Credential>
{
    protected FastList<IntermediationRule> intermediationRules = FastList.newList();

    public AuthenticationMethod()
    {

    }

    public AuthenticationMethod addIntermediatonRules(List<IntermediationRule> intermediationRules)
    {
        this.intermediationRules.addAll(intermediationRules);
        return this;
    }

    public abstract Cred makeCredential(Spec spec, Identity identity) throws Exception;

}
