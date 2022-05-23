package org.finos.legend.engine.authentication;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.finos.legend.engine.shared.core.identity.Credential;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LegendDefaultAuthenticationFlows
{
    private ImmutableList<AuthenticationFlow> flows;

    public <T> LegendDefaultAuthenticationFlows(ImmutableList<AuthenticationFlow> flows)
    {
        this.flows = flows;
    }

    public static LegendDefaultAuthenticationFlows defaultFlows()
    {
        return new LegendDefaultAuthenticationFlows(Lists.immutable.of(
                new KerberosToUsernamePasswordCredentialFlow(),
                new KereberosToOAuthCredentialFlow()
        ));
    }

    public Optional<AuthenticationFlow> lookup(Class<? extends Credential> inbound, Class<? extends Credential> outboud)
    {
        List<AuthenticationFlow> matches = this.flows.stream()
                .filter(flow -> flow.inboundCredentialType().isAssignableFrom(inbound) && flow.outboundCredentialType().isAssignableFrom(outboud))
                .collect(Collectors.toList());
        // TODO - validate during construction
        if (matches.size() > 1 )
        {
            throw new RuntimeException("Too many matches. Don't know which one to use");
        }
        if (matches.size() == 0)
        {
            return Optional.empty();
        }

        // TODO : return a new flow - flow should not hold state
        return Optional.of(matches.get(0));
    }
}
