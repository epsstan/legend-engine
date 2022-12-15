package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

    public Class<? extends AuthenticationSpec> getAuthenticationSpecClass()
    {
        Type[] actualTypeArguments = getActualTypeArguments();
        return (Class<? extends AuthenticationSpec>) actualTypeArguments[0];
    }

    public Class<? extends Credential> getOutputCredentialClass()
    {
        Type[] actualTypeArguments = getActualTypeArguments();
        return (Class<? extends Credential>) actualTypeArguments[1];
    }

    private Type[] getActualTypeArguments()
    {
        Type genericSuperClass = this.getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperClass;
        return parameterizedType.getActualTypeArguments();
    }
}
