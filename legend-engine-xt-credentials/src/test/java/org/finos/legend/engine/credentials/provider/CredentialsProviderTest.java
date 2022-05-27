package org.finos.legend.engine.credentials.provider;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.finos.legend.engine.credentials.credential.*;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class CredentialsProviderTest {
    private Identity fakeKerberosIdentity;
    private CredentialsProviderFlowRegistry flowRegistry;

    @Before
    public void setup() {
        ImmutableList<CredentialsProviderFlow> flows = Lists.immutable.of(
                new KerberosToUsernamePasswordCredentialFlow()
                        .configure(KerberosToUsernamePasswordCredentialFlowConfigurationParams.builder()
                                .build()),
                new KerberosToAWSCredentialFlow()
                        .configure(KerberosToAWSCredentialFlowConfigurationParams.builder()
                                .build()),
                new KerberosToKeyPairFlow()
                        .configure(KerberosToKeyPairFlowConfigurationParams.builder()
                                .build()),
                new KerberosToOAuthCredentialFlow()
                        .configure(KerberosToOAuthCredentialFlowConfigurationParams.builder()
                                .build())
        );

        this.flowRegistry = new CredentialsProviderFlowRegistry(flows);
        this.fakeKerberosIdentity = new FakeKerberosIdentity("fred@EXAMPLE.COM");
    }


    @Test
    public void kerberosToUserPassword() throws Exception {

        Supplier<LegendPlaintextUserPasswordCredential> supplier =
                this.flowRegistry.lookup(
                        LegendKerberosCredential.class,
                        LegendPlaintextUserPasswordCredential.class
                 ).get()
                .makeCredential(
                    fakeKerberosIdentity,
                    LegendKerberosCredential.class,
                    LegendPlaintextUserPasswordCredentialCredentialRequestParams.builder().someConfoig("someconfig").build()
        );

        assertEquals("fake-user-fred@EXAMPLE.COM-someconfig", supplier.get().getPassword());
    }

    @Test
    public void kerberosToOAuth() throws Exception
    {
        Supplier<LegendOAuthCredential> supplier =
                this.flowRegistry.lookup(
                                LegendKerberosCredential.class,
                                LegendOAuthCredential.class
                        ).get()
                        .makeCredential(
                                fakeKerberosIdentity,
                                LegendKerberosCredential.class,
                                LegendOAuthCredentialCredentialRequestParams.builder().oauthScopes("scope1").build()
                        );
        assertEquals("fake-token-fred@EXAMPLE.COM-[scope1]", supplier.get().getAccessToken());
    }

    @Test
    public void kerberosToAWS() throws Exception {

        Supplier<LegendAwsCredential> supplier = this.flowRegistry.lookup(
                        LegendKerberosCredential.class,
                        LegendAwsCredential.class
                ).get()
                .makeCredential(
                        fakeKerberosIdentity,
                        LegendKerberosCredential.class,
                        LegendAwsCredentialCredentialRequestParams.builder().build()
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
        Supplier<LegendKeypairCredential> supplier = this.flowRegistry.lookup(
                        LegendKerberosCredential.class,
                        LegendKeypairCredential.class
                ).get()
                .makeCredential(
                        fakeKerberosIdentity,
                        LegendKerberosCredential.class,
                        LegendKeypairCredentialRequestParams.builder()
                                .userName("foo")
                                .passphraseVaultReference("ref1")
                                .privateKeyVaultReference("ref2")
                                .build()
                );

        LegendKeypairCredential legendKeypairCredential = supplier.get();
    }


    static class FakeKerberosIdentity extends Identity {
        public FakeKerberosIdentity(String name) {
            super(name, new LegendKerberosCredential(null));
        }
    }
}
