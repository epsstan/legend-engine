package org.finos.legend.engine.flow.registry;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.finos.legend.engine.credentials.FakeIdentityWithAWSCredential;
import org.finos.legend.engine.credentials.FakeIdentityWithKereberosCredential;
import org.finos.legend.engine.credentials.credential.ImmutableLegendOAuthCredential;
import org.finos.legend.engine.credentials.credential.LegendAwsCredential;
import org.finos.legend.engine.credentials.flow.authenticated.*;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class TestFlowDiscovery {

    @Test
    public void registry1() throws Exception
    {
        ImmutableList<KerberosToAWSCredentialFlow> flows = Lists.immutable.of(
                new KerberosToAWSCredentialFlow(ImmutableKerberosToAWSCredentialFlow.ConfigurationParams.builder()
                        .build()));

        FlowRegistry1 flowRegistry1 = new FlowRegistry1(flows);

        AuthenticatedCredentialsProviderFlow<LegendKerberosCredential, LegendAwsCredential, Object> flow = flowRegistry1.lookup(LegendKerberosCredential.class, LegendAwsCredential.class).get();
        assertNotNull(flow);

        Identity identity = new FakeIdentityWithKereberosCredential("fake");
        Supplier<LegendAwsCredential> credentialSupplier = flow.makeCredential(identity, ImmutableKerberosToAWSCredentialFlow.ConfigurationParams.builder().build());
    }

    @Test
    public void registry2() throws Exception {

        FlowRegistry2 flowRegistry2 = new FlowRegistry2();
        flowRegistry2.register(
                FlowRegistry2.DatabaseType.Snowflake,
                new KerberosToOAuthCredentialFlow(ImmutableKerberosToOAuthCredentialFlow.ConfigurationParams.builder().build()));

        // No flow for Postgres
        assertFalse(flowRegistry2.lookup(FlowRegistry2.DatabaseType.Postgres, new FakeIdentityWithKereberosCredential("fake")).isPresent());

        // No flow for Snowflake and LegndAWSCredential
        assertFalse(flowRegistry2.lookup(FlowRegistry2.DatabaseType.Snowflake, new FakeIdentityWithAWSCredential("fake")).isPresent());

        // Flow found for Snowflake and LegendKerberosCredential
        FakeIdentityWithKereberosCredential identity = new FakeIdentityWithKereberosCredential("fake");
        Optional<? extends AuthenticatedCredentialsProviderFlow<Credential, Credential, Object>> holder = flowRegistry2.lookup(FlowRegistry2.DatabaseType.Snowflake, identity);
        assertTrue(holder.isPresent());

        Supplier<Credential> credentialSupplier = holder.get().makeCredential(identity, ImmutableLegendOAuthCredential.CredentialRequestParams.builder().oauthScopes("scope1").build());
    }
}
