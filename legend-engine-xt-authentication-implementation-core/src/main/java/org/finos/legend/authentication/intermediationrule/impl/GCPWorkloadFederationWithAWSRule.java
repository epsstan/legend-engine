/*
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

package org.finos.legend.authentication.intermediationrule.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.authentication.configuration.GCPWorkloadFederationWithAWSConfiguration;
import org.finos.legend.authentication.intermediationrule.IntermediationRule;
import org.finos.legend.authentication.vault.CredentialVault;
import org.finos.legend.authentication.vault.CredentialVaultProvider;
import org.finos.legend.authentication.vault.CredentialVaultSecretConfiguration;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.credential.OAuthCredential;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.utils.http.SdkHttpUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;

public class GCPWorkloadFederationWithAWSRule extends IntermediationRule<GCPWorkloadFederationWithAWSConfiguration, Credential, OAuthCredential>
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final String STS = "sts";
    public static final String HTTPS = "https";
    public static final String AWS_STS_HOST = "sts.amazonaws.com";
    public static final String GCP_STS_HOST = "sts.googleapis.com";
    public static final String GCP_IAM_CREDENTIALS_HOST = "iamcredentials.googleapis.com";
    public static final String ISO8601BasicFormat = "yyyyMMdd'T'HHmmss'Z'";

    public GCPWorkloadFederationWithAWSRule(CredentialVaultProvider credentialVaultProvider)
    {
        super(credentialVaultProvider);
    }

    @Override
    public OAuthCredential makeCredential(GCPWorkloadFederationWithAWSConfiguration AuthenticationSpecification, Credential credential) throws Exception
    {
        GCPWorkloadFederationWithAWSConfiguration.AWSIdentityProviderConfiguration awsIdentityProviderConfiguration = AuthenticationSpecification.getAwsIdentityProviderConfiguration();
        GCPWorkloadFederationWithAWSConfiguration.GCPWorkloadConfiguration gcpWorkloadConfiguration = AuthenticationSpecification.getGcpWorkloadConfiguration();
        Credentials awsCredentials = this.assumeAWSRoleAndGetCredentials(awsIdentityProviderConfiguration);
        Date date = new Date();
        String currentDate = getUTCDate(date);
        String canonicalAWSRequestSignature = computeCanonicalAWSRequestSignature(awsCredentials, date, awsIdentityProviderConfiguration.getRegion());
        String gcpTargetResource = String.format(
                "//iam.googleapis.com/projects/%s/locations/global/workloadIdentityPools/%s/providers/%s",
                gcpWorkloadConfiguration.getProjectNumber(),
                gcpWorkloadConfiguration.getPoolId(),
                gcpWorkloadConfiguration.getProviderId()
        );
        String subjectTokenType = "urn:ietf:params:aws:token-type:aws4_request";
        String callerIdentityToken = makeAWSCallerIdentityToken(awsCredentials, currentDate, canonicalAWSRequestSignature, gcpTargetResource);
        String federatedAccessToken = getGCPFederatedAccessToken(SdkHttpUtils.urlEncode(callerIdentityToken), gcpTargetResource, subjectTokenType);
        String serviceAccountAccessToken = getGCPServiceAccountAccessToken(federatedAccessToken, AuthenticationSpecification.serviceAccountEmail, AuthenticationSpecification.additionalGcpScopes);
        return new OAuthCredential(serviceAccountAccessToken);
    }

    private Credentials assumeAWSRoleAndGetCredentials(GCPWorkloadFederationWithAWSConfiguration.AWSIdentityProviderConfiguration awsIdentityProviderConfiguration) throws Exception
    {
        String roleArn = awsIdentityProviderConfiguration.getRole();
        String roleSessionName = awsIdentityProviderConfiguration.getRole();
        String region = awsIdentityProviderConfiguration.getRegion();
        CredentialVaultSecretConfiguration awsAccessKeyIdVaultReferenceConfiguration = awsIdentityProviderConfiguration.getAccessKeyIdVaultReference();
        CredentialVaultSecretConfiguration secretAccessKeyVaultReferenceConfiguration = awsIdentityProviderConfiguration.getSecretAccessKeyVaultReference();

        CredentialVault vault = super.credentialVaultProvider.getVault(awsAccessKeyIdVaultReferenceConfiguration.vaultType);
        String awsAccessKeyId = vault.getSecret(awsAccessKeyIdVaultReferenceConfiguration.vaultReference);
        String awsSecretAccessKeyId = vault.getSecret(secretAccessKeyVaultReferenceConfiguration.vaultReference);

        StsClient stsClient = StsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        awsAccessKeyId,
                        awsSecretAccessKeyId
                )))
                .build();
        AssumeRoleRequest roleRequest = AssumeRoleRequest.builder()
                .roleArn(roleArn)
                .roleSessionName(roleSessionName)
                .build();
        AssumeRoleResponse roleResponse = stsClient.assumeRole(roleRequest);
        return roleResponse.credentials();
    }

    private String getUTCDate(Date date)
    {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(ISO8601BasicFormat);
        dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
        return dateTimeFormat.format(date);
    }

    private String computeCanonicalAWSRequestSignature(Credentials credentials, Date date, String awsRegion)
    {
        Aws4Signer aws4Signer = Aws4Signer.create();
        Aws4SignerParams aws4SignerParams = Aws4SignerParams.builder()
                .signingRegion(Region.of(awsRegion))
                .signingName(STS)
                .awsCredentials(software.amazon.awssdk.auth.credentials.AwsSessionCredentials.create(
                        credentials.accessKeyId(),
                        credentials.secretAccessKey(),
                        credentials.sessionToken())
                ).signingClockOverride(Clock.fixed(date.toInstant(), ZoneOffset.UTC))
                .build();
        SdkHttpFullRequest sdkHttpFullRequest = SdkHttpFullRequest.builder()
                .method(SdkHttpMethod.POST)
                .host(AWS_STS_HOST)
                .appendRawQueryParameter("Action", "GetCallerIdentity")
                .appendRawQueryParameter("Version", "2011-06-15")
                .protocol(HTTPS)
                .build();
        SdkHttpFullRequest signedSdkHttpFullRequest = aws4Signer.sign(sdkHttpFullRequest, aws4SignerParams);
        return signedSdkHttpFullRequest.headers().get("Authorization").get(0);
    }

    private String makeAWSCallerIdentityToken(Credentials credentials, String signingDate, String signature, String gcpTargetResource)
    {
        return "{" +
                "\"url\": \"https://sts.amazonaws.com?Action=GetCallerIdentity&Version=2011-06-15\"," +
                "\"method\": \"POST\"," +
                "\"headers\": [" +
                "{ \"key\": \"Authorization\", \"value\": \"" + signature + "\" }," +
                "{ \"key\": \"host\", \"value\" : \"" + AWS_STS_HOST + "\" }," +
                "{ \"key\": \"x-amz-date\", \"value\": \"" + signingDate + "\"}," +
                "{ \"key\": \"x-goog-cloud-target-resource\", \"value\": \"" + gcpTargetResource + "\" }," +
                "{ \"key\": \"x-amz-security-token\", \"value\": \"" + credentials.sessionToken() + "\" }" +
                "]" +
                "}";
    }

    public String getGCPFederatedAccessToken(String subjectToken, String audience, String subjectTokenType) throws IOException, URISyntaxException
    {
        String body = "{" +
                "\"audience\": \"" + audience + "\"," +
                "\"grantType\": \"urn:ietf:params:oauth:grant-type:token-exchange\"," +
                "\"requestedTokenType\": \"urn:ietf:params:oauth:token-type:access_token\"," +
                "\"scope\": \"https://www.googleapis.com/auth/cloud-platform\"," +
                "\"subjectTokenType\": \"" + subjectTokenType + "\"," +
                "\"subjectToken\": \"" + subjectToken + "\"" +
                "}";
        HttpPost request = new HttpPost(new URIBuilder()
                .setScheme(HTTPS)
                .setHost(GCP_STS_HOST)
                .setPath("v1beta/token")
                .build());
        StringEntity stringEntity = new StringEntity(body);
        stringEntity.setContentType("application/json");
        request.setEntity(stringEntity);
        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            try (CloseableHttpResponse response = httpClient.execute(request))
            {
                JsonNode responseData = OBJECT_MAPPER.readTree(response.getEntity().getContent());
                return responseData.path("access_token").asText();
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failed to get Federated Access Token", ex);
        }
    }

    public String getGCPServiceAccountAccessToken(String federatedAccessToken, String serviceAccountEmail, List<String> additionalGcpScopes) throws URISyntaxException, UnsupportedEncodingException, JsonProcessingException
    {
        List<String> gcpScopes;
        if (additionalGcpScopes == null)
        {
            gcpScopes = FastList.newList();
        }
        else
        {
            gcpScopes = FastList.newList(additionalGcpScopes);
        }
        gcpScopes.add("https://www.googleapis.com/auth/bigquery");
        Map<String, List<String>> map = new HashMap<>();
        map.put("scope", gcpScopes);
        String body = OBJECT_MAPPER.writeValueAsString(map);
        HttpPost request = new HttpPost(new URIBuilder()
                .setScheme(HTTPS)
                .setHost(GCP_IAM_CREDENTIALS_HOST)
                .setPath(String.format("v1/projects/-/serviceAccounts/%s:generateAccessToken", serviceAccountEmail))
                .build());
        StringEntity stringEntity = new StringEntity(body);
        stringEntity.setContentType("application/json");
        request.setEntity(stringEntity);
        request.setHeader("Authorization", "Bearer " + federatedAccessToken);
        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            try (CloseableHttpResponse response = httpClient.execute(request))
            {
                JsonNode responseData = OBJECT_MAPPER.readTree(response.getEntity().getContent());
                return responseData.path("accessToken").asText();
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failed to get Service Account Access Token", ex);
        }
    }
}
*/
