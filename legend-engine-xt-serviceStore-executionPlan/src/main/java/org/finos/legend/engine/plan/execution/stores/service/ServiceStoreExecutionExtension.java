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

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.block.function.Function3;
import org.finos.legend.engine.shared.core.function.Function5;
import org.eclipse.collections.api.list.MutableList;
import org.finos.legend.engine.plan.execution.nodes.state.ExecutionState;
import org.finos.legend.engine.plan.execution.result.Result;
import org.finos.legend.engine.plan.execution.stores.StoreType;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.ExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.LimitExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.RestServiceExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.ServiceParametersResolutionExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.runtime.connection.authentication.AuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.ApiKeySecurityScheme;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SecurityScheme;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SimpleHttpSecurityScheme;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextCredential;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;
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
            if (executionNode instanceof RestServiceExecutionNode || executionNode instanceof ServiceParametersResolutionExecutionNode || executionNode instanceof LimitExecutionNode)
            {
                return executionNode.accept(executionState.getStoreExecutionState(StoreType.Service).getVisitor(profiles, executionState));
            }
            return null;
        }));
    }

    public List<Function5<SecurityScheme, AuthenticationSpec, Credential, RequestBuilder, HttpClientBuilder, Boolean>> getExtraSecuritySchemeProcessors()
    {
        return Collections.singletonList((securityScheme,authenticationSpec,credential,requestBuilder,httpClientBuilder) ->
        {
            if (securityScheme instanceof SimpleHttpSecurityScheme && credential instanceof PlaintextUserPasswordCredential)
            {
                PlaintextUserPasswordCredential cred = (PlaintextUserPasswordCredential)credential;
                String encoding = Base64.encodeBase64String((cred.getUser()+ ":" + cred.getPassword()).getBytes());
                requestBuilder.addHeader("Authorization", "Basic " + encoding);
                return true;
            }

            else if (securityScheme instanceof ApiKeySecurityScheme && credential instanceof PlaintextCredential)
            {
                PlaintextCredential cred = (PlaintextCredential) credential;
                ApiKeySecurityScheme apiKeySecurityScheme = (ApiKeySecurityScheme) securityScheme;
                if (apiKeySecurityScheme.location.equals("cookie"))
                {
                    requestBuilder.addHeader("Cookie", String.format("%s=%s",apiKeySecurityScheme.keyName,cred.getValue()));
//                    if (cookie!=null)
//                    {
//                        String newCookieString = cookie + ";" + String.format("%s=%s",apiKeySecurityScheme.keyName,cred.getValue());
//                        connection.setRequestProperty("Cookie",newCookieString);
//                    }
//                    else
//                    {
//                        connection.setRequestProperty("Cookie", String.format("%s=%s", apiKeySecurityScheme.keyName, cred.getValue()));
//                    }
                }
                return true;
            }
            return null;
        });
    }
}
