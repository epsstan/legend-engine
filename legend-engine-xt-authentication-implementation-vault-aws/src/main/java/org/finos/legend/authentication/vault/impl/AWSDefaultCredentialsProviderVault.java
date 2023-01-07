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

package org.finos.legend.authentication.vault.impl;

import org.finos.legend.authentication.vault.CredentialVault;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.CredentialVaultSecret;
import org.finos.legend.engine.shared.core.identity.Identity;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

// TODO - epsstan - refactor and add parameters
public class AWSDefaultCredentialsProviderVault extends CredentialVault
{
    @Override
    public String lookupSecret(CredentialVaultSecret vaultSecret, Identity identity) throws Exception
    {
        // TODO - epsstan - Implement secret resolution from AWS Secrets Manager
        String reference = vaultSecret.reference;
        DefaultCredentialsProvider defaultCredentialsProvider = DefaultCredentialsProvider.builder().build();
        AwsCredentials awsCredentials = defaultCredentialsProvider.resolveCredentials();
        switch (reference)
        {
            case "ACCESS_KEY_ID":
                return awsCredentials.accessKeyId();
            case "SECRET_ACCESS_KEY":
                return awsCredentials.secretAccessKey();
            default:
                throw new UnsupportedOperationException("Unsupported reference '" + reference + "'");
        }
    }
}
