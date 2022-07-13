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

import org.finos.legend.engine.credentials.provider.StaticUsernamePasswordCredentialProvider;
import org.finos.legend.engine.shared.core.identity.credential.ImmutableLegendAwsCredential;
import org.finos.legend.engine.shared.core.identity.credential.ImmutableLegendKeypairCredential;
import org.finos.legend.engine.shared.core.identity.credential.ImmutableLegendOAuthCredential;
import org.finos.legend.engine.shared.core.identity.credential.ImmutableLegendPlaintextUserPasswordCredential;
import org.finos.legend.engine.shared.core.identity.credential.LegendAwsCredential;
import org.finos.legend.engine.shared.core.identity.credential.LegendKeypairCredential;
import org.finos.legend.engine.shared.core.identity.credential.LegendOAuthCredential;
import org.finos.legend.engine.shared.core.identity.credential.LegendPlaintextUserPasswordCredential;
import org.finos.legend.engine.shared.core.vault.PropertiesVaultImplementation;
import org.finos.legend.engine.shared.core.vault.Vault;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.util.Properties;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class TestDirectUseOfCredentialProviders
{
    private PropertiesVaultImplementation propertiesVaultImplementation;
    private FakeIdentityWithKerberosCredential fakeKerberosIdentity;

    @Before
    public void setup()
    {
        Properties properties = new Properties();
        properties.put("user1", "password1");
        this.propertiesVaultImplementation = new PropertiesVaultImplementation(properties);
        Vault.INSTANCE.registerImplementation(propertiesVaultImplementation);

        this.fakeKerberosIdentity = new FakeIdentityWithKerberosCredential("fred@EXAMPLE.COM");
    }

    @Test
    public void createUserPasswordCredential() throws Exception
    {
        StaticUsernamePasswordCredentialProvider credentialProvider = new StaticUsernamePasswordCredentialProvider(
                ImmutableStaticUsernamePasswordCredentialProvider.Configuration.builder().build()
        );

        Supplier<LegendPlaintextUserPasswordCredential> supplier = credentialProvider.makeCredential(
                fakeKerberosIdentity,
                ImmutableLegendPlaintextUserPasswordCredential.Params.builder().name("user1").build()
        );

        LegendPlaintextUserPasswordCredential credential = supplier.get();
        assertEquals("user1", credential.getUser());
        assertEquals("password1", credential.getPassword());
    }

    @Test
    public void createOAuthCredentialFromKerberos() throws Exception
    {
        KerberosToOAuthCredentialProvider flow = new KerberosToOAuthCredentialProvider(ImmutableKerberosToOAuthCredentialProvider.Configuration.builder()
                .build());

        Supplier<LegendOAuthCredential> supplier =
                flow.makeCredential(
                        fakeKerberosIdentity,
                        ImmutableLegendOAuthCredential.Params.builder().oauthScopes("scope1").build()
                );
        assertEquals("fake-token-fred@EXAMPLE.COM-[scope1]", supplier.get().getAccessToken());
    }

    @Test
    public void createS3CredentialFromKerberos() throws Exception
    {
        KerberosToAWSCredentialProvider flow = new KerberosToAWSCredentialProvider(ImmutableKerberosToAWSCredentialProvider.Configuration.builder()
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

    @Test
    public void createSnowflakeKeypairFromKerberos() throws Exception
    {

        KerberosToKeyPairCredentialProvider flow = new KerberosToKeyPairCredentialProvider(ImmutableKerberosToKeyPairCredentialProvider.Configuration.builder()
                .build());

        Supplier<LegendKeypairCredential> supplier = flow.makeCredential(
                fakeKerberosIdentity,
                ImmutableLegendKeypairCredential.Params.builder()
                        .userName("foo")
                        .passphraseVaultReference("ref1")
                        .privateKeyVaultReference("ref2")
                        .build()
        );

        supplier.get();
    }
}
