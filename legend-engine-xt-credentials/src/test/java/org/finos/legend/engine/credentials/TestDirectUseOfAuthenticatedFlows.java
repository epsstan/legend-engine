package org.finos.legend.engine.credentials;

import org.finos.legend.engine.credentials.credential.*;
import org.finos.legend.engine.credentials.flow.authenticated.*;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class TestDirectUseOfAuthenticatedFlows
{
    private Identity fakeKerberosIdentity;

    @Before
    public void setup() {
        this.fakeKerberosIdentity = new FakeIdentityWithKereberosCredential("fred@EXAMPLE.COM");
    }

    @Test
    public void kerberosToOAuth() throws Exception {

        KerberosToOAuthCredentialFlow flow = new KerberosToOAuthCredentialFlow(ImmutableKerberosToOAuthCredentialFlow.ConfigurationParams.builder()
                .build());

        Supplier<LegendOAuthCredential> supplier =
                flow.makeCredential(
                        fakeKerberosIdentity,
                        ImmutableLegendOAuthCredential.CredentialRequestParams.builder().oauthScopes("scope1").build()
                );
        assertEquals("fake-token-fred@EXAMPLE.COM-[scope1]", supplier.get().getAccessToken());
    }

    @Test
    public void kerberosToAWS() throws Exception {

        KerberosToAWSCredentialFlow flow = new KerberosToAWSCredentialFlow(ImmutableKerberosToAWSCredentialFlow.ConfigurationParams.builder()
                .build());

        Supplier<LegendAwsCredential> supplier = flow.makeCredential(
                fakeKerberosIdentity,
                ImmutableLegendAwsCredential.CredentialRequestParams.builder().build()
        );
        AwsCredentials underlying = supplier.get().getUnderlying();


        S3Client s3 = S3Client.builder().region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(underlying))
                .build();


        ListBucketsResponse listBucketsResponse = s3.listBuckets();
    }

    // TODO - Can this be made generic and not tied to Snowflake ?
    @Test
    public void kerberosToSnowflakeKeyPair() throws Exception {

        KerberosToKeyPairFlow flow = new KerberosToKeyPairFlow(ImmutableKerberosToKeyPairFlow.ConfigurationParams.builder()
                .build());

        Supplier<LegendKeypairCredential> supplier = flow.makeCredential(
                fakeKerberosIdentity,
                ImmutableLegendKeypairCredential.CredentialRequestParams.builder()
                        .userName("foo")
                        .passphraseVaultReference("ref1")
                        .privateKeyVaultReference("ref2")
                        .build()
        );

        LegendKeypairCredential legendKeypairCredential = supplier.get();
    }
}
