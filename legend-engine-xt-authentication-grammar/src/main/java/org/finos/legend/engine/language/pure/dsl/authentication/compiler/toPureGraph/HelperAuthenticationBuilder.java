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

package org.finos.legend.engine.language.pure.dsl.authentication.compiler.toPureGraph;

import org.finos.legend.engine.language.pure.compiler.toPureGraph.CompileContext;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.ApiKeyAuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.AuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.AuthenticationSpecificationVisitor;
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
import org.finos.legend.pure.generated.Root_meta_pure_runtime_connection_authentication_ApiKeyAuthenticationSpecification_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_runtime_connection_authentication_AuthenticationSpecification;
import org.finos.legend.pure.generated.Root_meta_pure_runtime_connection_authentication_CredentialVaultSecret;
import org.finos.legend.pure.generated.Root_meta_pure_runtime_connection_authentication_PropertiesFileVaultSecret_Impl;
import org.finos.legend.pure.generated.Root_meta_pure_runtime_connection_authentication_UserPasswordAuthenticationSpecification_Impl;

public class HelperAuthenticationBuilder
{
    public static Root_meta_pure_runtime_connection_authentication_AuthenticationSpecification buildAuthenticationSpecification(AuthenticationSpecification authenticationSpecification, CompileContext context)
    {
        return authenticationSpecification.accept(new AuthenticationSpecificationBuilder(context));
    }

    private static class AuthenticationSpecificationBuilder implements AuthenticationSpecificationVisitor<Root_meta_pure_runtime_connection_authentication_AuthenticationSpecification>
    {
        private CompileContext context;

        public AuthenticationSpecificationBuilder(CompileContext context)
        {
            this.context = context;
        }

        @Override
        public Root_meta_pure_runtime_connection_authentication_AuthenticationSpecification visit(ApiKeyAuthenticationSpecification apiKeyAuthenticationSpecification)
        {
            String ENUM_PATH = "meta::pure::runtime::connection::authentication::Location";
            return new Root_meta_pure_runtime_connection_authentication_ApiKeyAuthenticationSpecification_Impl("", null, context.pureModel.getClass("meta::pure::runtime::connection::authentication::ApiKeyAuthenticationSpecification"))
                    ._location(context.resolveEnumValue(ENUM_PATH, apiKeyAuthenticationSpecification.location.name()))
                    ._keyName(apiKeyAuthenticationSpecification.keyName)
                    ._value(buildSecret(apiKeyAuthenticationSpecification.value, context));
        }

        @Override
        public Root_meta_pure_runtime_connection_authentication_AuthenticationSpecification visit(UserPasswordAuthenticationSpecification userPasswordAuthenticationSpecification)
        {
            return new Root_meta_pure_runtime_connection_authentication_UserPasswordAuthenticationSpecification_Impl("", null, context.pureModel.getClass("meta::pure::runtime::connection::authentication::UserPasswordAuthenticationSpecification"))
                    ._username(userPasswordAuthenticationSpecification.userName)
                    ._password(buildSecret(userPasswordAuthenticationSpecification.password, context));
        }

        @Override
        public Root_meta_pure_runtime_connection_authentication_AuthenticationSpecification visit(EncryptedPrivateKeyPairAuthenticationSpecification encryptedPrivateKeyPairAuthenticationSpecification)
        {
            throw new UnsupportedOperationException("TODO - epsstan");
        }

        @Override
        public Root_meta_pure_runtime_connection_authentication_AuthenticationSpecification visit(GCPWIFWithAWSIdPAuthenticationSpecification gcpwifWithAWSIdPAuthenticationSpecification)
        {
            throw new UnsupportedOperationException("TODO - epsstan");
        }

        @Override
        public Root_meta_pure_runtime_connection_authentication_AuthenticationSpecification visit(OAuthAuthenticationSpecification oAuthAuthenticationSpecification)
        {
            throw new UnsupportedOperationException("TODO - epsstan");
        }
    }

    public static Root_meta_pure_runtime_connection_authentication_CredentialVaultSecret buildSecret(CredentialVaultSecret credentialVaultSecret, CompileContext context)
    {
        return credentialVaultSecret.accept(new CredentialVaultSecretBuilder(context));
    }

    public static class CredentialVaultSecretBuilder implements CredentialVaultSecretVisitor<Root_meta_pure_runtime_connection_authentication_CredentialVaultSecret>
    {
        private final CompileContext context;

        public CredentialVaultSecretBuilder(CompileContext context)
        {
            this.context = context;
        }

        @Override
        public Root_meta_pure_runtime_connection_authentication_CredentialVaultSecret visit(PropertiesFileCredentialVaultSecret propertiesFileCredentialVaultSecret)
        {
            return new Root_meta_pure_runtime_connection_authentication_PropertiesFileVaultSecret_Impl("", null, context.pureModel.getClass("meta::pure::runtime::connection::authentication::PropertiesFileVaultSecret"))
                    ._reference(propertiesFileCredentialVaultSecret.reference);
        }

        @Override
        public Root_meta_pure_runtime_connection_authentication_CredentialVaultSecret visit(EnvironmentCredentialVaultSecret environmentCredentialVaultSecret)
        {
            throw new UnsupportedOperationException("TODO - epsstan");
        }

        @Override
        public Root_meta_pure_runtime_connection_authentication_CredentialVaultSecret visit(SystemPropertiesCredentialVaultSecret systemPropertiesCredentialVaultSecret)
        {
            throw new UnsupportedOperationException("TODO - epsstan");
        }

        @Override
        public Root_meta_pure_runtime_connection_authentication_CredentialVaultSecret visit(AWSSecretsManagerCredentialVaultSecret awsSecretsManagerCredentialVaultSecret)
        {
            throw new UnsupportedOperationException("TODO - epsstan");
        }
    }
}
