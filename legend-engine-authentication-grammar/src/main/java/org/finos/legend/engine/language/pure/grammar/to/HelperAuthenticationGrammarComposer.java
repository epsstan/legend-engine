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

package org.finos.legend.engine.language.pure.grammar.to;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.apiKey.ApiKeyAuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.usernamePassword.UsernamePasswordAuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.OAuthAuthenticationSpec;

import static org.finos.legend.engine.language.pure.grammar.to.PureGrammarComposerUtility.convertString;
import static org.finos.legend.engine.language.pure.grammar.to.PureGrammarComposerUtility.getTabString;

public class HelperAuthenticationGrammarComposer
{
    public static String renderAuthenticationSpec(String securityScheme, AuthenticationSpec a, int baseIndentation)
    {
        if (a instanceof OAuthAuthenticationSpec)
        {
            OAuthAuthenticationSpec spec = (OAuthAuthenticationSpec) a;
            return getTabString(baseIndentation) + securityScheme +
                    " : OauthAuthenticationSpec\n" +
                    getTabString(baseIndentation) + "{\n" +
                    getTabString(baseIndentation + 1) + "grantType : " + convertString(spec.grantType.toString(), true) + ";\n" +
                    getTabString(baseIndentation + 1) + "clientId : " + convertString(spec.clientId, true) + ";\n" +
                    getTabString(baseIndentation + 1) + "clientSecretVaultReference : " + convertString(spec.clientSecretVaultReference, true) + ";\n" +
                    getTabString(baseIndentation + 1) + "authorizationServerUrl : " + convertString(spec.authServerUrl, true) + ";\n" +
                    getTabString(baseIndentation) + "}";
        }
        else if (a instanceof UsernamePasswordAuthenticationSpec)
        {
            UsernamePasswordAuthenticationSpec spec = (UsernamePasswordAuthenticationSpec) a;
            return  getTabString(baseIndentation) + securityScheme +
                    " : UsernamePasswordAuthenticationSpec\n" +
                    getTabString(baseIndentation) + "{\n" +
                    getTabString(baseIndentation + 1) + "username : " + convertString(spec.username.toString(), true) + ";\n" +
                    getTabString(baseIndentation + 1) + "password : VaultCredential" +
                    getTabString(baseIndentation + 1) + "(\n" +
                    getTabString(baseIndentation + 2) + "vaultReference : " + convertString(spec.password.toString(), true) + ";\n" +
                    getTabString(baseIndentation + 1) + ");\n" +
                    getTabString(baseIndentation) + "}";
        }
        else if (a instanceof ApiKeyAuthenticationSpec)
        {
            ApiKeyAuthenticationSpec spec = (ApiKeyAuthenticationSpec) a;
            return  getTabString(baseIndentation) + securityScheme +
                    " : ApiKeyAuthenticationSpec\n" +
                    getTabString(baseIndentation) + "{\n" +
                    getTabString(baseIndentation + 1) + "location : " + convertString(spec.value.toString(), true) + ";\n" +
                    getTabString(baseIndentation + 1) + "keyName : " + convertString(spec.value.toString(), true) + ";\n" +
                    getTabString(baseIndentation + 1) + "value : " + convertString(spec.value.toString(), true) + ";\n" +
                    getTabString(baseIndentation) + "}";
        }
        return null;
    }
}
