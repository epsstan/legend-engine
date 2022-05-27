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

package org.finos.legend.engine.credentials.flow.registry;

import org.eclipse.collections.api.list.ImmutableList;
import org.finos.legend.engine.credentials.flow.CredentialsProviderFlow;
import org.finos.legend.engine.shared.core.identity.Credential;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlowRegistry1
{
    private ImmutableList<? extends CredentialsProviderFlow> authenticatedCredentialsProviderFlows;

    public FlowRegistry1(ImmutableList<? extends CredentialsProviderFlow> authenticatedCredentialsProviderFlows)
    {
        this.authenticatedCredentialsProviderFlows = authenticatedCredentialsProviderFlows;
    }

    public <I extends Credential, O extends Credential, P> Optional<? extends CredentialsProviderFlow<I, O, P>>
        lookup(Class<I> input, Class<O> output)
    {
        List<? extends CredentialsProviderFlow> matches = this.authenticatedCredentialsProviderFlows.stream()
                .filter(flow -> flow.inboundCredentialType().isAssignableFrom(input) && flow.outboundCredentialType().isAssignableFrom(output))
                .collect(Collectors.toList());
        if (matches.size() > 1) 
        {
            throw new RuntimeException("Too many matches. Don't know which one to use");
        }
        if (matches.size() == 0) 
        {
            return Optional.empty();
        }
        Optional<? extends CredentialsProviderFlow> flow = Optional.of(matches.get(0));
        return (Optional<? extends CredentialsProviderFlow<I, O, P>>) flow;
    }
}
