// Copyright 2021 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.finos.legend.engine.plan.execution.authentication.provider;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.plan.execution.authentication.AuthenticationMethod;
import org.finos.legend.engine.plan.execution.authentication.IntermediationRule;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.shared.core.identity.Credential;


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
