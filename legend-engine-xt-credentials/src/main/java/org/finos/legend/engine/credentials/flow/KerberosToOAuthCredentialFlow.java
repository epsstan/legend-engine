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

package org.finos.legend.engine.credentials.flow;

import org.finos.legend.engine.shared.core.identity.credential.LegendOAuthCredential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.immutables.value.Value;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

@Value.Enclosing
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public class KerberosToOAuthCredentialFlow extends AbstractCredentialsProviderFlowImpl<
        LegendKerberosCredential,
        LegendOAuthCredential,
        LegendOAuthCredential.Params>
{
    @Value.Immutable
    interface Configuration
    {

    }

    private Configuration configuration;


    public KerberosToOAuthCredentialFlow(Configuration configuration)
    {
        super(LegendKerberosCredential.class, LegendOAuthCredential.class, LegendOAuthCredential.Params.class);
        this.configuration = configuration;
    }


    @Override
    public Supplier<LegendOAuthCredential> makeCredential(Identity identity, LegendOAuthCredential.Params params) throws Exception
    {
        Optional<LegendKerberosCredential> inboundCredential = identity.getCredential(this.inboundCredentialType());
        // use the inbound credential - for e.g authenticate with an STS service using the inbound credential and obtain an LegendOAuthCredential
        String generated = "fake-token-" + identity.getName() + "-" + Arrays.toString(params.oauthScopes());
        return () -> new LegendOAuthCredential(generated);
    }
}
