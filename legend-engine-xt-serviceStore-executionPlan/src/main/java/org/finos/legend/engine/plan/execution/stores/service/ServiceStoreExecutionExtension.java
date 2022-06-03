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

package org.finos.legend.engine.plan.execution.stores.service;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.collections.api.list.MutableList;
import org.finos.legend.engine.credentials.credential.ImmutableLegendDummyCredential;
import org.finos.legend.engine.credentials.credential.LegendDummyCredential;
import org.finos.legend.engine.credentials.flow.anonymous.ImmutableDummyCredentialFlow;
import org.finos.legend.engine.credentials.flow.registry.CentralizedFlowRegistry;
import org.finos.legend.engine.plan.execution.nodes.state.ExecutionState;
import org.finos.legend.engine.plan.execution.result.Result;
import org.finos.legend.engine.plan.execution.stores.StoreType;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.ExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.RestServiceExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.ServiceParametersResolutionExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.DummySecurityScheme;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SecurityScheme;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.pac4j.core.profile.CommonProfile;

import java.util.Collections;
import java.util.List;

public class ServiceStoreExecutionExtension implements IServiceStoreExecutionExtension
{
    @Override
    public List<Function3<ExecutionNode, MutableList<CommonProfile>, ExecutionState, Result>> getExtraNodeExecutors()
    {
        return Collections.singletonList(((executionNode, profiles, executionState) ->
        {
            if (executionNode instanceof RestServiceExecutionNode || executionNode instanceof ServiceParametersResolutionExecutionNode)
            {
                return executionNode.accept(executionState.getStoreExecutionState(StoreType.Service).getVisitor(profiles, executionState));
            }
            return null;
        }));
    }

    @Override
    public List<Function3<SecurityScheme, HttpClientBuilder, Identity, Boolean>> getExtraSecuritySchemeProcessors()
    {
        return Collections.singletonList(((securityScheme, httpClientBuilder, identity) ->
        {
            if(securityScheme instanceof DummySecurityScheme)
            {
                CentralizedFlowRegistry centralizedFlowRegistry = CentralizedFlowRegistry.getRegistry();
                try
                {
                    LegendDummyCredential dummyCredential = centralizedFlowRegistry.lookupByRequiredCredentialTypeAndAvailableCredentials(LegendDummyCredential.class, identity).get().makeCredential(identity, ImmutableLegendDummyCredential.CredentialRequestParams.builder().build()).get();
                    httpClientBuilder.setDefaultHeaders(Collections.singletonList(new BasicHeader("dummyCredential", dummyCredential.getId())));
                    return true;
                }
                catch (Exception ignores)
                {
                    return false;
                }
            }
            return null;
        }));
    }
}
