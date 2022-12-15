package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.provider;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.AuthenticationMethod;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.IntermediationRule;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.Credential;

import java.util.List;

public class AuthenticationMethodProvider
{
    private FastList<AuthenticationMethod> methodList;
    private IntermediationRuleProvider intermediationRuleProvider;

    public AuthenticationMethodProvider(FastList<AuthenticationMethod> methodList, IntermediationRuleProvider intermediationRuleProvider) {
        this.methodList = methodList;
        this.intermediationRuleProvider = intermediationRuleProvider;
    }

    public FastList<AuthenticationMethod> getAllSupportedAuthenticationMethods()
    {
        FastList<IntermediationRule> ruleList = this.intermediationRuleProvider.getAllSupportedRules();
        return match(methodList,ruleList);
    }

    private FastList<AuthenticationMethod> match(FastList<AuthenticationMethod> allSupportedMethods, FastList<IntermediationRule> allSupportedRules)
    {
        FastList<AuthenticationMethod> authenticationMethods = FastList.newList();
        for (AuthenticationMethod authenticationMethod : allSupportedMethods)
        {
            Class <? extends AuthenticationSpec> authenticationMethodSpecClass = authenticationMethod.getAuthenticationSpecClass();
            Class <? extends Credential> authenticationMethodOutputCredentialClass = authenticationMethod.getOutputCredentialClass();
            FastList<IntermediationRule> rulesForMethod = allSupportedRules.select(rule -> rule.getAuthenticationSpecClass().equals(authenticationMethodSpecClass) && rule.getOutputCredentialClass().equals(authenticationMethodOutputCredentialClass));
            authenticationMethod.addIntermediatonRules(rulesForMethod);
            authenticationMethods.add(authenticationMethod);
        }
        return authenticationMethods;
    }

    public FastList<AuthenticationMethod> getSupportedMethodFor(Class <? extends AuthenticationSpec> authenticationSpecClass)
    {
        FastList<AuthenticationMethod> allSupportedMethods = this.getAllSupportedAuthenticationMethods();
        return  allSupportedMethods.select(method -> method.getAuthenticationSpecClass().equals(authenticationSpecClass));
    }
}
