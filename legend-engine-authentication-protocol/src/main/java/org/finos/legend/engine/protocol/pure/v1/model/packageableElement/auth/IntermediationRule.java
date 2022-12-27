package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.shared.core.identity.Credential;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class IntermediationRule<Spec, InputCred extends Credential, OutputCred extends Credential>
{
    public abstract OutputCred makeCredential(Spec spec, InputCred cred) throws Exception;

    public Class<? extends AuthenticationSpec> getAuthenticationSpecClass()
    {
        Type[] actualTypeArguments = getActualTypeArguments();
        return (Class<? extends AuthenticationSpec>) actualTypeArguments[0];
    }

    public Class<? extends Credential> getInputCredentialClass()
    {
        Type[] actualTypeArguments = getActualTypeArguments();
        return (Class<? extends Credential>) actualTypeArguments[1];
    }

    public Class<? extends Credential> getOutputCredentialClass()
    {
        Type[] actualTypeArguments = getActualTypeArguments();
        return (Class<? extends Credential>) actualTypeArguments[2];
    }

    private Type[] getActualTypeArguments()
    {
        Type genericSuperClass = this.getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperClass;
        return parameterizedType.getActualTypeArguments();
    }
    public boolean matchesInputAndOutput(Class<? extends AuthenticationSpec> authenticationSpecClass, Class<? extends Credential> inputCredentialClass, Class<? extends Credential> outputCredentialClass)
    {
        return this.getAuthenticationSpecClass().equals(authenticationSpecClass) && this.getInputCredentialClass().equals(inputCredentialClass) && this.getOutputCredentialClass().equals(outputCredentialClass);
    }

    public boolean matchesOutput(Class<? extends AuthenticationSpec> authenticationSpecClass, Class<? extends Credential> outputCredentialClass)
    {
        return this.getAuthenticationSpecClass().equals(authenticationSpecClass) && this.getOutputCredentialClass().equals(outputCredentialClass);
    }
}
