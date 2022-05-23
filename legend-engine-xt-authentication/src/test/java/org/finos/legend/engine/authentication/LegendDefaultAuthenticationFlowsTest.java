package org.finos.legend.engine.authentication;

import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.finos.legend.engine.shared.core.identity.credential.OAuthCredential;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LegendDefaultAuthenticationFlowsTest
{
    @Test
    public void kerberosToUserPassword() throws Exception {

        // registry of transformation rules
        LegendDefaultAuthenticationFlows legendDefaultAuthenticationFlows = LegendDefaultAuthenticationFlows.defaultFlows();

        // choose a rule based on inbound and outbound types
        AuthenticationFlow flow = legendDefaultAuthenticationFlows.lookup(LegendKerberosCredential.class, PlaintextUserPasswordCredential.class).get();

        // configure the flow
        flow.configure("someconfig");

        // get an identity
        Identity identity = new FakeKerberosIdentity("fred@EXMAPLE.COM");

        // ask the flow to make a credential
        Credential credential = flow.makeCredential(identity, identity.getCredential(LegendKerberosCredential.class).get());
        PlaintextUserPasswordCredential plaintextUserPasswordCredential = (PlaintextUserPasswordCredential) credential;
        assertEquals("fake-user-fred@EXMAPLE.COM-someconfig", plaintextUserPasswordCredential.getPassword());
    }

    @Test
    public void kerberosToOAuth() throws Exception {

        // registry of transformation rules
        LegendDefaultAuthenticationFlows legendDefaultAuthenticationFlows = LegendDefaultAuthenticationFlows.defaultFlows();

        // choose a rule based on inbound and outbound types
        AuthenticationFlow flow = legendDefaultAuthenticationFlows.lookup(LegendKerberosCredential.class, OAuthCredential.class).get();

        // configure the flow
        flow.configure("someconfig");

        // get an identity
        Identity identity = new FakeKerberosIdentity("fred@EXMAPLE.COM");

        // ask the flow to make a credential
        Credential credential = flow.makeCredential(identity, identity.getCredential(LegendKerberosCredential.class).get());
        OAuthCredential oAuthCredential = (OAuthCredential) credential;
        assertEquals("fake-token-fred@EXMAPLE.COM-someconfig", oAuthCredential.getAccessToken());
    }

    static class FakeKerberosIdentity extends Identity
    {
        public FakeKerberosIdentity(String name) {
            super(name, new LegendKerberosCredential(null));
        }
    }
}