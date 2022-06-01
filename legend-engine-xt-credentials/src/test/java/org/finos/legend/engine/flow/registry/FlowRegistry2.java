package org.finos.legend.engine.flow.registry;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.finos.legend.engine.credentials.flow.authenticated.AuthenticatedCredentialsProviderFlow;
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

    private MutableMap<DatabaseType, MutableList<? super AuthenticatedCredentialsProviderFlow>> authenticatedCredentialsProviderFlows = Maps.mutable.empty();

    public <F extends AuthenticatedCredentialsProviderFlow> void register(DatabaseType databaseType, F flow)
    {
        this.authenticatedCredentialsProviderFlows.putIfAbsent(databaseType, Lists.mutable.empty());
        this.authenticatedCredentialsProviderFlows.get(databaseType).add(flow);
    }

    public <InboundCredential extends Credential, OutboundCredential extends Credential, CredentialRequestParams> Optional<? extends AuthenticatedCredentialsProviderFlow<InboundCredential, OutboundCredential, CredentialRequestParams>>
        lookup(DatabaseType databaseType, Identity identity) throws Exception
    {

        Class<? extends Credential> inputCredentialType = identity.getFirstCredential().getClass();

        if (!this.authenticatedCredentialsProviderFlows.containsKey(databaseType))
        {
            return Optional.empty();
        }

        MutableList<? super AuthenticatedCredentialsProviderFlow> flowsForDatabase = this.authenticatedCredentialsProviderFlows.get(databaseType);

        MutableList<? extends AuthenticatedCredentialsProviderFlow> matches = flowsForDatabase.collect(o -> (AuthenticatedCredentialsProviderFlow) o)
                .select(flow -> flow.inboundCredentialType().isAssignableFrom(inputCredentialType));

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
