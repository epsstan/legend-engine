package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.provider;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.IntermediationRule;

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

