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

import org.finos.legend.engine.shared.core.identity.credential.LegendAwsCredential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.immutables.value.Value;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

import java.util.Optional;
import java.util.function.Supplier;

@Value.Enclosing
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public class KerberosToAWSCredentialProvider extends AbstractCredentialsProviderImpl<
        LegendKerberosCredential,
        LegendAwsCredential,
        LegendAwsCredential.Params>
{
    private Configuration configuration;

    @Value.Immutable
    interface Configuration
    {

    }

    public KerberosToAWSCredentialProvider(Configuration configuration)
    {
        super(LegendKerberosCredential.class, LegendAwsCredential.class, LegendAwsCredential.Params.class);
        this.configuration = configuration;
    }

    @Override
    public Supplier<LegendAwsCredential> makeCredential(Identity identity, LegendAwsCredential.Params params) throws Exception
    {
        Optional<LegendKerberosCredential> inboundCredential = identity.getCredential(this.inboundCredentialType());
        // use the inbound credential - for e.g authenticate with an STS service using the inbound credential and obtain an AwsCredential
        String generated = "fake-token-" + identity.getName() + "-";
        return () -> new LegendAwsCredential(AwsBasicCredentials.create("fakeAccessKeyId", "fakeSecretAccessKey"));
    }
}
