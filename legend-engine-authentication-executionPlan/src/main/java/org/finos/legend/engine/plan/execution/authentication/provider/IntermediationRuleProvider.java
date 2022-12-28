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
import org.finos.legend.engine.plan.execution.authentication.IntermediationRule;

import java.util.List;

//TODO: Load rules from classpath using ServiceLoader or other extension
public class IntermediationRuleProvider {
    private FastList<IntermediationRule> allRules;

    public IntermediationRuleProvider()
    {
        this.allRules = FastList.newList();
    }

    public IntermediationRuleProvider(FastList<IntermediationRule> allRules)
    {
        this.allRules = allRules;
    }

    public FastList<IntermediationRule> getAllSupportedRules() {
        return allRules;
    }
}

