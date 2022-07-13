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

import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.AnonymousCredential;
import org.finos.legend.engine.shared.core.identity.credential.LegendPlaintextUserPasswordCredential;
import org.finos.legend.engine.shared.core.vault.Vault;
import org.immutables.value.Value;

import java.util.function.Supplier;

@Value.Enclosing
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public class StaticUsernamePasswordCredentialProvider extends AbstractCredentialsProviderImpl<
        AnonymousCredential,
        LegendPlaintextUserPasswordCredential,
        LegendPlaintextUserPasswordCredential.Params>
{
    private Configuration configuration;

    @Value.Immutable
    interface Configuration
    {

    }

    public StaticUsernamePasswordCredentialProvider(Configuration configuration)
    {
        super(AnonymousCredential.class, LegendPlaintextUserPasswordCredential.class, LegendPlaintextUserPasswordCredential.Params.class);
        this.configuration = configuration;
    }

    @Override
    public Supplier<LegendPlaintextUserPasswordCredential> makeCredential(Identity identity, LegendPlaintextUserPasswordCredential.Params params) throws Exception
    {
        String vaultSecretReference = params.name();
        String password = Vault.INSTANCE.getValue(vaultSecretReference);
        return () -> new LegendPlaintextUserPasswordCredential(params.name(), password);
    }

}
