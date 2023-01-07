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

package org.finos.legend.engine.language.pure.dsl.authentication.grammar.from;

import org.finos.legend.engine.language.pure.grammar.from.ParseTreeWalkerSourceInformation;
import org.finos.legend.engine.language.pure.grammar.from.PureGrammarParserContext;
import org.finos.legend.engine.language.pure.grammar.from.PureGrammarParserUtility;
import org.finos.legend.engine.language.pure.grammar.from.antlr4.authentication.AuthenticationParserGrammar;
import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;
import org.finos.legend.engine.protocol.pure.v1.model.context.EngineErrorType;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.PackageableElement;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.ApiKeyAuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.AuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.EncryptedPrivateKeyPairAuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.GCPWIFWithAWSIdPAuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.UserPasswordAuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.AWSSecretsManagerCredentialVaultSecret;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.CredentialVaultSecret;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.EnvironmentCredentialVaultSecret;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.PropertiesFileCredentialVaultSecret;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.SystemPropertiesCredentialVaultSecret;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.section.ImportAwareCodeSection;
import org.finos.legend.engine.shared.core.operational.errorManagement.EngineException;

import java.util.function.Consumer;

public class AuthenticationParseTreeWalker
{
    private final ParseTreeWalkerSourceInformation walkerSourceInformation;
    private final Consumer<PackageableElement> elementConsumer;
    private final ImportAwareCodeSection section;
    private final PureGrammarParserContext context;

    public AuthenticationParseTreeWalker(ParseTreeWalkerSourceInformation walkerSourceInformation, Consumer<PackageableElement> elementConsumer, ImportAwareCodeSection section, PureGrammarParserContext context)
    {
        this.walkerSourceInformation = walkerSourceInformation;
        this.elementConsumer = elementConsumer;
        this.section = section;
        this.context = context;
    }


    private AuthenticationSpecification visitAuthenticationSpecification(AuthenticationParserGrammar.AuthenticationContext ctx)
    {
        SourceInformation sourceInformation = walkerSourceInformation.getSourceInformation(ctx);
        AuthenticationParserGrammar.UserPasswordAuthenticationContext userPasswordAuthenticationContext = ctx.userPasswordAuthentication();
        if (userPasswordAuthenticationContext != null)
        {
            return this.visitUserPasswordAuthentication(userPasswordAuthenticationContext);
        }
        AuthenticationParserGrammar.ApiKeyAuthenticationContext apiKeyAuthenticationContext = ctx.apiKeyAuthentication();
        if (apiKeyAuthenticationContext != null)
        {
            return this.visitApiKeyAuthentication(apiKeyAuthenticationContext);
        }
        AuthenticationParserGrammar.EncryptedPrivateKeyAuthenticationContext encryptedPrivateKeyAuthenticationContext = ctx.encryptedPrivateKeyAuthentication();
        if (encryptedPrivateKeyAuthenticationContext != null)
        {
            return this.visitEncryptedKeyPairAuthentication(encryptedPrivateKeyAuthenticationContext);
        }

        AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthenticationContext gcpWIFWithAWSIdPAuthenticationContext = ctx.gcpWIFWithAWSIdPAuthentication();
        if (gcpWIFWithAWSIdPAuthenticationContext != null)
        {
            return this.visitGcpWIFWithAWSIdPAuthenticationContext(gcpWIFWithAWSIdPAuthenticationContext);
        }

        throw new EngineException("Unsupported authentication", sourceInformation, EngineErrorType.PARSER);
    }

    private AuthenticationSpecification visitGcpWIFWithAWSIdPAuthenticationContext(AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthenticationContext ctx)
    {
        GCPWIFWithAWSIdPAuthenticationSpecification authenticationSpecification = new GCPWIFWithAWSIdPAuthenticationSpecification();
        authenticationSpecification.sourceInformation = walkerSourceInformation.getSourceInformation(ctx);

        AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_serviceAccountEmailContext serviceAccountEmailContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.gcpWIFWithAWSIdPAuthentication_serviceAccountEmail(), "serviceAccountEmail", authenticationSpecification.sourceInformation);
        String serviceAccountEmail = PureGrammarParserUtility.fromGrammarString(serviceAccountEmailContext.STRING().getText(), true);
        authenticationSpecification.serviceAccountEmail = serviceAccountEmail;

        AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_awsIdpContext idpContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.gcpWIFWithAWSIdPAuthentication_awsIdp(), "idP", authenticationSpecification.sourceInformation);
        authenticationSpecification.idPConfiguration = this.visitGCPWithAWSIdPIdp(idpContext);

        AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_gcpWorkloadContext workloadContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.gcpWIFWithAWSIdPAuthentication_gcpWorkload(), "workload", authenticationSpecification.sourceInformation);
        authenticationSpecification.workloadConfiguration = this.visitGCPWithAWSIdPWorkload(workloadContext);

        return authenticationSpecification;
    }

    private GCPWIFWithAWSIdPAuthenticationSpecification.IdPConfiguration visitGCPWithAWSIdPIdp(AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_awsIdpContext ctx)
    {
        GCPWIFWithAWSIdPAuthenticationSpecification.IdPConfiguration configuration = new GCPWIFWithAWSIdPAuthenticationSpecification.IdPConfiguration();

        AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_awsIdp_accountIdContext accountIdContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.gcpWIFWithAWSIdPAuthentication_awsIdp_accountId(), "accountId", walkerSourceInformation.getSourceInformation(ctx));
        String accountId = PureGrammarParserUtility.fromGrammarString(accountIdContext.STRING().getText(), true);
        configuration.accountId  = accountId;

        AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_awsIdp_regionContext regionContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.gcpWIFWithAWSIdPAuthentication_awsIdp_region(), "region", walkerSourceInformation.getSourceInformation(ctx));
        String region = PureGrammarParserUtility.fromGrammarString(regionContext.STRING().getText(), true);
        configuration.region = region;

        AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_awsIdp_roleContext roleContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.gcpWIFWithAWSIdPAuthentication_awsIdp_role(), "role", walkerSourceInformation.getSourceInformation(ctx));
        String role = PureGrammarParserUtility.fromGrammarString(roleContext.STRING().getText(), true);
        configuration.role = role;

        AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_awsIdp_access_keyContext accessKeyContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.gcpWIFWithAWSIdPAuthentication_awsIdp_access_key(), "awsAccessKey", walkerSourceInformation.getSourceInformation(ctx));
        configuration.accessKeyIdVaultReference = this.visitCredentialVaultSecret(accessKeyContext.secret_value());

        AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_awsIdp_secret_access_keyContext secretAccessKeyContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.gcpWIFWithAWSIdPAuthentication_awsIdp_secret_access_key(), "awsSecretAccessKey", walkerSourceInformation.getSourceInformation(ctx));
        configuration.secretAccessKeyIdVaultReference = this.visitCredentialVaultSecret(secretAccessKeyContext.secret_value());

        return configuration;
    }

    private GCPWIFWithAWSIdPAuthenticationSpecification.WorkloadConfiguration visitGCPWithAWSIdPWorkload(AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_gcpWorkloadContext ctx)
    {
        GCPWIFWithAWSIdPAuthenticationSpecification.WorkloadConfiguration workloadConfiguration = new GCPWIFWithAWSIdPAuthenticationSpecification.WorkloadConfiguration();

        AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_gcpWorkload_projectNumberContext projectNumberContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.gcpWIFWithAWSIdPAuthentication_gcpWorkload_projectNumber(), "projectNumber", walkerSourceInformation.getSourceInformation(ctx));
        String projectNumber = PureGrammarParserUtility.fromGrammarString(projectNumberContext.STRING().getText(), true);
        workloadConfiguration.projectNumber  = projectNumber;

        AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_gcpWorkload_providerIdContext providerIdContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.gcpWIFWithAWSIdPAuthentication_gcpWorkload_providerId(), "providerId", walkerSourceInformation.getSourceInformation(ctx));
        String providerId = PureGrammarParserUtility.fromGrammarString(providerIdContext.STRING().getText(), true);
        workloadConfiguration.providerId  = providerId;

        AuthenticationParserGrammar.GcpWIFWithAWSIdPAuthentication_gcpWorkload_poolIdContext poolIdContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.gcpWIFWithAWSIdPAuthentication_gcpWorkload_poolId(), "poolId", walkerSourceInformation.getSourceInformation(ctx));
        String poolId = PureGrammarParserUtility.fromGrammarString(poolIdContext.STRING().getText(), true);
        workloadConfiguration.poolId  = poolId;

        return workloadConfiguration;
    }

    private AuthenticationSpecification visitApiKeyAuthentication(AuthenticationParserGrammar.ApiKeyAuthenticationContext ctx)
    {
        ApiKeyAuthenticationSpecification authenticationSpecification = new ApiKeyAuthenticationSpecification();
        authenticationSpecification.sourceInformation = walkerSourceInformation.getSourceInformation(ctx);

        AuthenticationParserGrammar.ApiKeyAuthentication_locationContext locationContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.apiKeyAuthentication_location(), "location", authenticationSpecification.getSourceInformation());
        String location = PureGrammarParserUtility.fromGrammarString(locationContext.STRING().getText(), true);
        authenticationSpecification.location = ApiKeyAuthenticationSpecification.Location.valueOf(location);

        AuthenticationParserGrammar.ApiKeyAuthentication_keyNameContext keyNameContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.apiKeyAuthentication_keyName(), "keyName", authenticationSpecification.getSourceInformation());
        String keyName = PureGrammarParserUtility.fromGrammarString(keyNameContext.STRING().getText(), true);
        authenticationSpecification.keyName = keyName;

        AuthenticationParserGrammar.ApiKeyAuthentication_valueContext valueContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.apiKeyAuthentication_value(), "value", authenticationSpecification.getSourceInformation());
        authenticationSpecification.value = visitCredentialVaultSecret(valueContext.secret_value());

        return authenticationSpecification;
    }

    private AuthenticationSpecification visitUserPasswordAuthentication(AuthenticationParserGrammar.UserPasswordAuthenticationContext ctx)
    {
        UserPasswordAuthenticationSpecification authenticationSpecification = new UserPasswordAuthenticationSpecification();
        authenticationSpecification.sourceInformation = walkerSourceInformation.getSourceInformation(ctx);

        AuthenticationParserGrammar.UserPasswordAuthentication_usernameContext usernameContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.userPasswordAuthentication_username(), "userName", authenticationSpecification.getSourceInformation());
        String userNameValue = PureGrammarParserUtility.fromGrammarString(usernameContext.STRING().getText(), true);
        authenticationSpecification.userName = userNameValue;

        AuthenticationParserGrammar.UserPasswordAuthentication_passwordContext passwordContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.userPasswordAuthentication_password(), "password", authenticationSpecification.getSourceInformation());
        authenticationSpecification.password = visitCredentialVaultSecret(passwordContext.secret_value());

        return authenticationSpecification;
    }

    private EncryptedPrivateKeyPairAuthenticationSpecification visitEncryptedKeyPairAuthentication(AuthenticationParserGrammar.EncryptedPrivateKeyAuthenticationContext ctx)
    {
        EncryptedPrivateKeyPairAuthenticationSpecification authenticationSpecification = new EncryptedPrivateKeyPairAuthenticationSpecification();
        authenticationSpecification.sourceInformation = walkerSourceInformation.getSourceInformation(ctx);

        AuthenticationParserGrammar.EncryptedPrivateKeyAuthentication_privateKeyContext privateKeyContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.encryptedPrivateKeyAuthentication_privateKey(), "privateKey", authenticationSpecification.getSourceInformation());
        authenticationSpecification.privateKey = visitCredentialVaultSecret(privateKeyContext.secret_value());

        AuthenticationParserGrammar.EncryptedPrivateKeyAuthentication_passphraseContext passphraseContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.encryptedPrivateKeyAuthentication_passphrase(), "passphrase", authenticationSpecification.getSourceInformation());
        authenticationSpecification.passphrase = visitCredentialVaultSecret(privateKeyContext.secret_value());

        return authenticationSpecification;
    }

    private CredentialVaultSecret visitCredentialVaultSecret(AuthenticationParserGrammar.Secret_valueContext secretContext)
    {
        SourceInformation sourceInformation = walkerSourceInformation.getSourceInformation(secretContext);
        if (secretContext.propertiesVaultSecret() != null)
        {
            return this.visitPropertiesFileCredentialVaultSecret(secretContext);
        }
        else if (secretContext.environmentVaultSecret() != null)
        {
            return this.visitEnvironmentVaultCredentialVaultSecret(secretContext);
        }
        else if (secretContext.systemPropertiesVaultSecret() != null)
        {
            return this.visitSystemPropertiesCredentialVaultSecret(secretContext);
        }
        else if (secretContext.awsSecretsManagerVaultSecret() != null)
        {
            return this.awsSecretsManagerCredentialVaultSecret(secretContext);
        }
        throw new EngineException("Unrecognized secret", sourceInformation, EngineErrorType.PARSER);
    }

    private AWSSecretsManagerCredentialVaultSecret awsSecretsManagerCredentialVaultSecret(AuthenticationParserGrammar.Secret_valueContext secretContext)
    {
        AWSSecretsManagerCredentialVaultSecret credentialVaultSecret = new AWSSecretsManagerCredentialVaultSecret();
        AuthenticationParserGrammar.VaultReferenceContext vaultReferenceContext = PureGrammarParserUtility.validateAndExtractRequiredField(secretContext.awsSecretsManagerVaultSecret().vaultReference(), "reference", credentialVaultSecret.sourceInformation);
        credentialVaultSecret.reference = PureGrammarParserUtility.fromGrammarString(vaultReferenceContext.STRING().getText(), true);

        AuthenticationParserGrammar.VersionIdContext versionIdContext = PureGrammarParserUtility.validateAndExtractRequiredField(secretContext.awsSecretsManagerVaultSecret().versionId(), "versionId", credentialVaultSecret.sourceInformation);
        credentialVaultSecret.versionId = PureGrammarParserUtility.fromGrammarString(versionIdContext.STRING().getText(), true);

        AuthenticationParserGrammar.VersionStageContext versionStage = PureGrammarParserUtility.validateAndExtractRequiredField(secretContext.awsSecretsManagerVaultSecret().versionStage(), "versionStage", credentialVaultSecret.sourceInformation);
        credentialVaultSecret.versionStage = PureGrammarParserUtility.fromGrammarString(versionStage.STRING().getText(), true);

        return credentialVaultSecret;
    }

    private CredentialVaultSecret visitSystemPropertiesCredentialVaultSecret(AuthenticationParserGrammar.Secret_valueContext secretContext)
    {
        SystemPropertiesCredentialVaultSecret credentialVaultSecret = new SystemPropertiesCredentialVaultSecret();
        credentialVaultSecret.sourceInformation = walkerSourceInformation.getSourceInformation(secretContext);
        AuthenticationParserGrammar.VaultReferenceContext vaultReferenceContext = PureGrammarParserUtility.validateAndExtractRequiredField(secretContext.systemPropertiesVaultSecret().vaultReference(), "reference", credentialVaultSecret.sourceInformation);
        credentialVaultSecret.reference = PureGrammarParserUtility.fromGrammarString(vaultReferenceContext.STRING().getText(), true);

        return credentialVaultSecret;
    }


    private EnvironmentCredentialVaultSecret visitEnvironmentVaultCredentialVaultSecret(AuthenticationParserGrammar.Secret_valueContext secretContext)
    {
        EnvironmentCredentialVaultSecret credentialVaultSecret = new EnvironmentCredentialVaultSecret();
        credentialVaultSecret.sourceInformation = walkerSourceInformation.getSourceInformation(secretContext);
        AuthenticationParserGrammar.VaultReferenceContext vaultReferenceContext = PureGrammarParserUtility.validateAndExtractRequiredField(secretContext.environmentVaultSecret().vaultReference(), "reference", credentialVaultSecret.sourceInformation);
        credentialVaultSecret.reference = PureGrammarParserUtility.fromGrammarString(vaultReferenceContext.STRING().getText(), true);

        return credentialVaultSecret;
    }

    private PropertiesFileCredentialVaultSecret visitPropertiesFileCredentialVaultSecret(AuthenticationParserGrammar.Secret_valueContext secretContext)
    {
        PropertiesFileCredentialVaultSecret credentialVaultSecret = new PropertiesFileCredentialVaultSecret();
        credentialVaultSecret.sourceInformation = walkerSourceInformation.getSourceInformation(secretContext);
        AuthenticationParserGrammar.VaultReferenceContext vaultReferenceContext = PureGrammarParserUtility.validateAndExtractRequiredField(secretContext.propertiesVaultSecret().vaultReference(), "reference", credentialVaultSecret.sourceInformation);
        credentialVaultSecret.reference = PureGrammarParserUtility.fromGrammarString(vaultReferenceContext.STRING().getText(), true);

        return credentialVaultSecret;
    }
}
