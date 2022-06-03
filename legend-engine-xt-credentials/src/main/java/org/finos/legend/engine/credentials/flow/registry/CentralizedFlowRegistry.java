//  Copyright 2022 Goldman Sachs
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.finos.legend.engine.credentials.flow.registry;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.finos.legend.engine.credentials.flow.authenticated.AnonymousToDummyCredentialFlow;
import org.finos.legend.engine.credentials.flow.authenticated.AuthenticatedCredentialsProviderFlow;
import org.finos.legend.engine.credentials.flow.authenticated.ImmutableAnonymousToDummyCredentialFlow;
import org.finos.legend.engine.credentials.flow.authenticated.ImmutableKerberosToAWSCredentialFlow;
import org.finos.legend.engine.credentials.flow.authenticated.ImmutableKerberosToDummyCredentialFlow;
import org.finos.legend.engine.credentials.flow.authenticated.KerberosToAWSCredentialFlow;
import org.finos.legend.engine.credentials.flow.authenticated.KerberosToDummyCredentialFlow;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: Possibly we can maintain a centralized registry which brings together all the flows loaded dynamically (Currently hard coded)
public class CentralizedFlowRegistry
{
    private static final CentralizedFlowRegistry INSTANCE = new CentralizedFlowRegistry();

    private final ImmutableList<? extends AuthenticatedCredentialsProviderFlow> authenticatedCredentialsProviderFlows;

    private CentralizedFlowRegistry()
    {
        this.authenticatedCredentialsProviderFlows = Lists.immutable.of(new KerberosToAWSCredentialFlow(ImmutableKerberosToAWSCredentialFlow.ConfigurationParams.builder().build()),
                new KerberosToDummyCredentialFlow(ImmutableKerberosToDummyCredentialFlow.ConfigurationParams.builder().build()),
                new AnonymousToDummyCredentialFlow(ImmutableAnonymousToDummyCredentialFlow.ConfigurationParams.builder().build()));
    }

    public static CentralizedFlowRegistry getRegistry()
    {
        return INSTANCE;
    }

    public <InboundCredential extends Credential, OutboundCredential extends Credential, CredentialRequestParams> Optional<? extends AuthenticatedCredentialsProviderFlow<InboundCredential, OutboundCredential, CredentialRequestParams>>
    lookupByRequiredCredentialTypeAndAvailableCredentials(Class<OutboundCredential> output, Identity identity)
    {
        List<Class<? extends Credential>> availableCredentialTypes = identity.getCredentials().stream().map(Credential::getClass).collect(Collectors.toList());

        Stream<? extends AuthenticatedCredentialsProviderFlow> authenticatedCredentialsProviderFlowsMatches = this.authenticatedCredentialsProviderFlows.stream()
                .filter(flow -> flow.outboundCredentialType().isAssignableFrom(output) && availableCredentialTypes.contains(flow.inboundCredentialType()));

        return (Optional<? extends AuthenticatedCredentialsProviderFlow<InboundCredential, OutboundCredential, CredentialRequestParams>>) authenticatedCredentialsProviderFlowsMatches.findFirst();
    }
}
