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

import org.finos.legend.engine.credentials.FakeIdentityWithKerberosCredential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.ImmutableLegendAwsCredential;
import org.finos.legend.engine.shared.core.identity.credential.ImmutableLegendKeypairCredential;
import org.finos.legend.engine.shared.core.identity.credential.ImmutableLegendOAuthCredential;
import org.finos.legend.engine.shared.core.identity.credential.LegendAwsCredential;
import org.finos.legend.engine.shared.core.identity.credential.LegendKeypairCredential;
import org.finos.legend.engine.shared.core.identity.credential.LegendOAuthCredential;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class TestDirectUseOfFlows
{
    private Identity fakeKerberosIdentity;

    @Before
    public void setup()
    {
        this.fakeKerberosIdentity = new FakeIdentityWithKerberosCredential("fred@EXAMPLE.COM");
    }

    @Test
    public void kerberosToOAuth() throws Exception
    {

        KerberosToOAuthCredentialFlow flow = new KerberosToOAuthCredentialFlow(ImmutableKerberosToOAuthCredentialFlow.Configuration.builder()
                .build());

        Supplier<LegendOAuthCredential> supplier =
                flow.makeCredential(
                        fakeKerberosIdentity,
                        ImmutableLegendOAuthCredential.Params.builder().oauthScopes("scope1").build()
                );
        assertEquals("fake-token-fred@EXAMPLE.COM-[scope1]", supplier.get().getAccessToken());
    }

    @Test
    public void kerberosToAWS() throws Exception
    {

        KerberosToAWSCredentialFlow flow = new KerberosToAWSCredentialFlow(ImmutableKerberosToAWSCredentialFlow.Configuration.builder()
                .build());

        Supplier<LegendAwsCredential> supplier = flow.makeCredential(
                fakeKerberosIdentity,
                ImmutableLegendAwsCredential.Params.builder().build()
        );
        AwsCredentials underlying = supplier.get().getUnderlying();


        S3Client s3 = S3Client.builder().region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(underlying))
                .build();


        ListBucketsResponse listBucketsResponse = s3.listBuckets();
    }

    // TODO - Can this be made generic and not tied to Snowflake ?
    @Test
    public void kerberosToSnowflakeKeyPair() throws Exception
    {

        KerberosToKeyPairFlow flow = new KerberosToKeyPairFlow(ImmutableKerberosToKeyPairFlow.Configuration.builder()
                .build());

        Supplier<LegendKeypairCredential> supplier = flow.makeCredential(
                fakeKerberosIdentity,
                ImmutableLegendKeypairCredential.Params.builder()
                        .userName("foo")
                        .passphraseVaultReference("ref1")
                        .privateKeyVaultReference("ref2")
                        .build()
        );

        LegendKeypairCredential legendKeypairCredential = supplier.get();
    }
}
