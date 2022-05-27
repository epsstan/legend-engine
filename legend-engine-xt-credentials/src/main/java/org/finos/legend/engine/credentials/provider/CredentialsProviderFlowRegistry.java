package org.finos.legend.engine.credentials.provider;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.finos.legend.engine.shared.core.identity.Credential;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CredentialsProviderFlowRegistry
{
    private ImmutableList<CredentialsProviderFlow> flows;

    public CredentialsProviderFlowRegistry(ImmutableList<CredentialsProviderFlow> flows) {
        this.flows = flows;
    }


    public <IC extends Credential, OC extends Credential, C, CR> Optional<CredentialsProviderFlow<IC, OC, C, CR>> lookup(Class<IC> input, Class<OC> output) {
        List<CredentialsProviderFlow> matches = this.flows.stream()
                .filter(flow -> flow.inboundCredentialType().isAssignableFrom(input) && flow.outboundCredentialType().isAssignableFrom(output))
                .collect(Collectors.toList());
        // TODO - validate during construction
        if (matches.size() > 1) {
            throw new RuntimeException("Too many matches. Don't know which one to use");
        }
        if (matches.size() == 0) {
            return Optional.empty();
        }

        // TODO : return a new flow - flow should not hold state
        return Optional.of(matches.get(0));
    }
}
