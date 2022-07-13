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

package org.finos.legend.engine.credentials.provider.registry;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.finos.legend.engine.credentials.provider.CredentialsProvider;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;

import java.util.Optional;

public class FlowRegistry2
{
    public enum DatabaseType
    {
        Snowflake,
        Postgres
    }

    private MutableMap<DatabaseType, MutableList<? super CredentialsProvider>> authenticatedCredentialsProviderFlows = Maps.mutable.empty();

    public <F extends CredentialsProvider> void register(DatabaseType databaseType, F flow)
    {
        this.authenticatedCredentialsProviderFlows.putIfAbsent(databaseType, Lists.mutable.empty());
        this.authenticatedCredentialsProviderFlows.get(databaseType).add(flow);
    }

    public <I extends Credential, O extends Credential, P> Optional<? extends CredentialsProvider<I, O, P>>
        lookup(DatabaseType databaseType, Identity identity) throws Exception
    {

        Class<? extends Credential> inputCredentialType = identity.getFirstCredential().getClass();

        if (!this.authenticatedCredentialsProviderFlows.containsKey(databaseType))
        {
            return Optional.empty();
        }

        MutableList<? super CredentialsProvider> flowsForDatabase = this.authenticatedCredentialsProviderFlows.get(databaseType);

        MutableList<? extends CredentialsProvider> matches = flowsForDatabase.collect(o -> (CredentialsProvider) o)
                .select(flow -> flow.inboundCredentialType().isAssignableFrom(inputCredentialType));

        if (matches.size() > 1) 
        {
            throw new RuntimeException("Too many matches. Don't know which one to use");
        }

        if (matches.size() == 0) 
        {
            return Optional.empty();
        }

        Optional<? extends CredentialsProvider> flow = Optional.of(matches.get(0));
        return (Optional<? extends CredentialsProvider<I, O, P>>) flow;
    }
}
