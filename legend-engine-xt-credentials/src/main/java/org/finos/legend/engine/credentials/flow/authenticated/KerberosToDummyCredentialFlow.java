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

package org.finos.legend.engine.credentials.flow.authenticated;

import org.finos.legend.engine.credentials.credential.LegendDummyCredential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.function.Supplier;

@Value.Enclosing
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public class KerberosToDummyCredentialFlow extends AbstractAuthenticatedCredentialsProviderFlow<
        LegendKerberosCredential,
        LegendDummyCredential,
        LegendDummyCredential.CredentialRequestParams>
{
    private KerberosToDummyCredentialFlow.ConfigurationParams configurationParams;

    @Value.Immutable
    interface ConfigurationParams
    {

    }

    public KerberosToDummyCredentialFlow(KerberosToDummyCredentialFlow.ConfigurationParams configurationParams)
    {
        super(LegendKerberosCredential.class, LegendDummyCredential.class, LegendDummyCredential.CredentialRequestParams.class);
        this.configurationParams = configurationParams;
    }

    @Override
    public Supplier<LegendDummyCredential> makeCredential(Identity identity, LegendDummyCredential.CredentialRequestParams requestParams) throws Exception
    {
        Optional<LegendKerberosCredential> inboundCredential = identity.getCredential(this.inboundCredentialType());
        return () -> new LegendDummyCredential(identity.getName());
    }
}
