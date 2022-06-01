package org.finos.legend.engine.flow.registry;

import org.eclipse.collections.api.list.ImmutableList;
import org.finos.legend.engine.credentials.flow.authenticated.AuthenticatedCredentialsProviderFlow;
import org.finos.legend.engine.shared.core.identity.Credential;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlowRegistry1
{
    private ImmutableList<? extends AuthenticatedCredentialsProviderFlow> authenticatedCredentialsProviderFlows;

    public FlowRegistry1(ImmutableList<? extends AuthenticatedCredentialsProviderFlow> authenticatedCredentialsProviderFlows) {
        this.authenticatedCredentialsProviderFlows = authenticatedCredentialsProviderFlows;
    }

    public <InboundCredential extends Credential, OutboundCredential extends Credential, CredentialRequestParams> Optional<? extends AuthenticatedCredentialsProviderFlow<InboundCredential, OutboundCredential, CredentialRequestParams>>
        lookup(Class<InboundCredential> input, Class<OutboundCredential> output)
    {
        List<? extends AuthenticatedCredentialsProviderFlow> matches = this.authenticatedCredentialsProviderFlows.stream()
                .filter(flow -> flow.inboundCredentialType().isAssignableFrom(input) && flow.outboundCredentialType().isAssignableFrom(output))
                .collect(Collectors.toList());
        if (matches.size() > 1) {
            throw new RuntimeException("Too many matches. Don't know which one to use");
        }
        if (matches.size() == 0) {
            return Optional.empty();
        }
        Optional<? extends AuthenticatedCredentialsProviderFlow> flow = Optional.of(matches.get(0));
        return (Optional<? extends AuthenticatedCredentialsProviderFlow<InboundCredential, OutboundCredential, CredentialRequestParams>>) flow;
    }
}
