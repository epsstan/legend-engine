/*
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

package org.finos.legend.engine.credentials.provider;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.finos.legend.engine.credentials.provider.registry.FlowRegistry1;
import org.finos.legend.engine.credentials.provider.registry.FlowRegistry2;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.ImmutableLegendOAuthCredential;
import org.finos.legend.engine.shared.core.identity.credential.LegendAwsCredential;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestFlowDiscovery
{

    @Test
    public void registry1() throws Exception
    {
        ImmutableList<KerberosToAWSCredentialProvider> flows = Lists.immutable.of(
                new KerberosToAWSCredentialProvider(ImmutableKerberosToAWSCredentialFlow.Configuration.builder()
                        .build()));

        FlowRegistry1 flowRegistry1 = new FlowRegistry1(flows);

        CredentialsProvider<LegendKerberosCredential, LegendAwsCredential, Object> flow = flowRegistry1.lookup(LegendKerberosCredential.class, LegendAwsCredential.class).get();
        assertNotNull(flow);

        Identity identity = new FakeIdentityWithKerberosCredential("fake");
        Supplier<LegendAwsCredential> credentialSupplier = flow.makeCredential(identity, ImmutableKerberosToAWSCredentialFlow.Configuration.builder().build());
    }

    @Test
    public void registry2() throws Exception
    {
        FlowRegistry2 flowRegistry2 = new FlowRegistry2();
        flowRegistry2.register(
                FlowRegistry2.DatabaseType.Snowflake,
                new KerberosToOAuthCredentialProvider(ImmutableKerberosToOAuthCredentialFlow.Configuration.builder().build()));

        // No flow for Postgres
        assertFalse(flowRegistry2.lookup(FlowRegistry2.DatabaseType.Postgres, new FakeIdentityWithKerberosCredential("fake")).isPresent());

        // No flow for Snowflake and LegndAWSCredential
        assertFalse(flowRegistry2.lookup(FlowRegistry2.DatabaseType.Snowflake, new FakeIdentityWithAWSCredential("fake")).isPresent());

        // Flow found for Snowflake and LegendKerberosCredential
        FakeIdentityWithKerberosCredential identity = new FakeIdentityWithKerberosCredential("fake");
        Optional<? extends CredentialsProvider<Credential, Credential, Object>> holder = flowRegistry2.lookup(FlowRegistry2.DatabaseType.Snowflake, identity);
        assertTrue(holder.isPresent());

        Supplier<Credential> credentialSupplier = holder.get().makeCredential(identity, ImmutableLegendOAuthCredential.Params.builder().oauthScopes("scope1").build());
    }
}
*/
