// Copyright 2022 Goldman Sachs
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

package org.finos.legend.engine.language.pure.dsl.authentication.grammar.to;

import org.finos.legend.engine.language.pure.grammar.to.PureGrammarComposerContext;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.AuthenticationSpecificationVisitor;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.ApiKeyAuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.EncryptedPrivateKeyPairAuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.GCPWIFWithAWSIdPAuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.OAuthAuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.UserPasswordAuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.AWSSecretsManagerCredentialVaultSecret;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.CredentialVaultSecret;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.CredentialVaultSecretVisitor;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.EnvironmentCredentialVaultSecret;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.PropertiesFileCredentialVaultSecret;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.SystemPropertiesCredentialVaultSecret;

import static org.finos.legend.engine.language.pure.grammar.to.PureGrammarComposerUtility.getTabString;

public class AuthenticationSpecificationComposer implements AuthenticationSpecificationVisitor<String>
{
    private final int indentLevel;
    private final PureGrammarComposerContext context;

    public AuthenticationSpecificationComposer(int indentLevel, PureGrammarComposerContext context)
    {
        this.indentLevel = indentLevel;
        this.context = context;
    }

    @Override
    public String visit(ApiKeyAuthenticationSpecification authenticationSpecification)
    {
        return getTabString(indentLevel) + "authentication: ApiKey\n" +
                getTabString(indentLevel) + "{\n" +
                getTabString(indentLevel + 1) + "location: '" + authenticationSpecification.location + "';\n" +
                getTabString(indentLevel + 1) + "keyName: '" + authenticationSpecification.keyName + "';\n" +
                getTabString(indentLevel + 1) + "value: " + renderCredentialVaultSecret(authenticationSpecification.value, indentLevel + 1, context) + "\n" +
                getTabString(indentLevel) + "}\n";
    }

    @Override
    public String visit(UserPasswordAuthenticationSpecification authenticationSpecification)
    {
        CredentialVaultSecret credentialVaultSecret = authenticationSpecification.password;
        return getTabString(indentLevel) + "authentication: UserPassword\n" +
                getTabString(indentLevel) + "{\n" +
                getTabString(indentLevel + 1) + "username: '" + authenticationSpecification.userName + "';\n" +
                getTabString(indentLevel + 1) + "password: " + renderCredentialVaultSecret(credentialVaultSecret, indentLevel + 1, context) + "\n" +
                getTabString(indentLevel) + "}\n";
    }

    @Override
    public String visit(EncryptedPrivateKeyPairAuthenticationSpecification authenticationSpecification)
    {
        return getTabString(indentLevel) + "authentication: EncryptedPrivateKey\n" +
                getTabString(indentLevel) + "{\n" +
                getTabString(indentLevel + 1) + "privateKey: " + renderCredentialVaultSecret(authenticationSpecification.privateKey, indentLevel + 1, context) + "\n" +
                getTabString(indentLevel + 1) + "passphrase: " + renderCredentialVaultSecret(authenticationSpecification.passphrase, indentLevel + 1, context) + "\n" +
                getTabString(indentLevel) + "}\n";
    }

    @Override
    public String visit(GCPWIFWithAWSIdPAuthenticationSpecification authenticationSpecification)
    {
        return getTabString(indentLevel) + "authentication: GCPWIFWithAWSIdP\n" +
                getTabString(indentLevel) + "{\n" +
                getTabString(indentLevel + 1) + "serviceAccountEmail: '" + authenticationSpecification.serviceAccountEmail + "';\n" +
                getTabString(indentLevel + 1) + "idP: " + this.renderGCPWIFWithAWSIdPIdP(authenticationSpecification.idPConfiguration, indentLevel + 1, context) + "\n" +
                getTabString(indentLevel + 1) + "workload: " + this.renderGCPWIFWithAWSIdPWorkload(authenticationSpecification.workloadConfiguration, indentLevel + 1, context) + "\n" +
                getTabString(indentLevel) + "}\n";
    }

    private String renderGCPWIFWithAWSIdPWorkload(GCPWIFWithAWSIdPAuthenticationSpecification.WorkloadConfiguration workloadConfiguration, int indentLevel, PureGrammarComposerContext context)
    {
        return "GCPWorkload\n" +
                getTabString(indentLevel) + "{\n" +
                getTabString(indentLevel + 1) + "projectNumber: '" + workloadConfiguration.projectNumber + "';\n" +
                getTabString(indentLevel + 1) + "providerId: '" + workloadConfiguration.providerId + "';\n" +
                getTabString(indentLevel + 1) + "poolId: '" + workloadConfiguration.poolId + "';\n" +
                getTabString(indentLevel) + "}";
    }

    private String renderGCPWIFWithAWSIdPIdP(GCPWIFWithAWSIdPAuthenticationSpecification.IdPConfiguration idPConfiguration, int indentLevel, PureGrammarComposerContext context)
    {
        return "AWSIdP\n" +
                getTabString(indentLevel) + "{\n" +
                getTabString(indentLevel + 1) + "accountId: '" + idPConfiguration.accountId + "';\n" +
                getTabString(indentLevel + 1) + "region: '" + idPConfiguration.region + "';\n" +
                getTabString(indentLevel + 1) + "role: '" + idPConfiguration.role + "';\n" +
                getTabString(indentLevel + 1) + "awsAccessKeyReference: " + renderCredentialVaultSecret(idPConfiguration.accessKeyIdVaultReference, indentLevel + 1, context) + "\n" +
                getTabString(indentLevel + 1) + "awsSecretAccessKeyReference: " + renderCredentialVaultSecret(idPConfiguration.secretAccessKeyIdVaultReference, indentLevel + 1, context) + "\n" +
                getTabString(indentLevel) + "}";
    }

    @Override
    public String visit(OAuthAuthenticationSpecification authenticationSpecification)
    {
        // TODO - epsstan
        throw new UnsupportedOperationException("todo");
    }

    private String renderCredentialVaultSecret(CredentialVaultSecret credentialVaultSecret, int indentLevel, PureGrammarComposerContext context)
    {
        CredentialVaultSecretComposer composer = new CredentialVaultSecretComposer(indentLevel, context);
        return credentialVaultSecret.accept(composer);
    }

    public static class CredentialVaultSecretComposer implements CredentialVaultSecretVisitor<String>
    {
        private final int indentLevel;
        private final PureGrammarComposerContext context;

        CredentialVaultSecretComposer(int indentLevel, PureGrammarComposerContext context)
        {
            this.indentLevel = indentLevel;
            this.context = context;
        }

        @Override
        public String visit(PropertiesFileCredentialVaultSecret propertiesFileCredentialVaultSecret)
        {
            return "PropertiesFileSecret\n" +
                    getTabString(indentLevel) + "{\n" +
                    getTabString(indentLevel + 1) + "reference: '" + propertiesFileCredentialVaultSecret.reference + "';\n" +
                    getTabString(indentLevel) + "}";
        }

        @Override
        public String visit(EnvironmentCredentialVaultSecret environmentCredentialVaultSecret)
        {
            return "EnvironmentSecret\n" +
                    getTabString(indentLevel) + "{\n" +
                    getTabString(indentLevel + 1) + "reference: '" + environmentCredentialVaultSecret.reference + "';\n" +
                    getTabString(indentLevel) + "}";
        }

        @Override
        public String visit(SystemPropertiesCredentialVaultSecret systemPropertiesCredentialVaultSecret)
        {
            return "SystemPropertiesSecret\n" +
                    getTabString(indentLevel) + "{\n" +
                    getTabString(indentLevel + 1) + "reference: '" + systemPropertiesCredentialVaultSecret.reference + "';\n" +
                    getTabString(indentLevel) + "}";
        }

        @Override
        public String visit(AWSSecretsManagerCredentialVaultSecret awsSecretsManagerCredentialVaultSecret)
        {
            return "AWSSecretsManagerSecret\n" +
                    getTabString(indentLevel) + "{\n" +
                    getTabString(indentLevel + 1) + "reference: '" + awsSecretsManagerCredentialVaultSecret.reference + "';\n" +
                    getTabString(indentLevel + 1) + "versionId: '" + awsSecretsManagerCredentialVaultSecret.versionId + "';\n" +
                    getTabString(indentLevel + 1) + "versionStage: '" + awsSecretsManagerCredentialVaultSecret.versionStage + "';\n" +
                    getTabString(indentLevel) + "}";
        }
    }
}
