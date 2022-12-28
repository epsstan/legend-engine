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
package org.finos.legend.engine.plan.execution.authentication.apiKey;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.plan.execution.authentication.AuthenticationMethod;
import org.finos.legend.engine.plan.execution.authentication.IntermediationRule;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.ApiKeyAuthenticationSpec;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextCredential;

public class ApikeyAuthenticationMethod extends AuthenticationMethod<ApiKeyAuthenticationSpec, PlaintextCredential> {
    @Override
    public PlaintextCredential makeCredential(ApiKeyAuthenticationSpec spec, Identity identity) throws Exception {

        if (!this.intermediationRules.isEmpty())
        {
            for (Credential credential : identity.getCredentials())
            {
                FastList<IntermediationRule> matchingRules = FastList.newList(intermediationRules).select(rule -> rule.matchesInputAndOutput(spec.getClass(), credential.getClass(), PlaintextCredential.class));
                if (!matchingRules.isEmpty())
                {
                    IntermediationRule intermediationRule = matchingRules.get(0);
                    return (PlaintextCredential) intermediationRule.makeCredential(spec,credential);
                }
            }
        }
        throw new UnsupportedOperationException("Unsupported Authentication spec type " + spec.getClass());
    }
}
