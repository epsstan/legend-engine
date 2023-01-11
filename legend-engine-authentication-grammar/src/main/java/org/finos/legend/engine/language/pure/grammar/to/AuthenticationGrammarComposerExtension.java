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

import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.list.mutable.ListAdapter;
import org.eclipse.collections.impl.utility.LazyIterate;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.runtime.connection.authentication.*;



import java.util.Collections;
import java.util.List;

import static org.finos.legend.engine.language.pure.grammar.to.PureGrammarComposerUtility.convertString;
import static org.finos.legend.engine.language.pure.grammar.to.PureGrammarComposerUtility.getTabString;

public class AuthenticationGrammarComposerExtension implements IAuthenticationGrammarComposerExtension
{
    @Override
    public List<Function2<Pair<String,AuthenticationSpec>,Integer,String>> getExtraAuthenticationComposers()
    {
        return Lists.mutable.with((authenticationSpecPair, baseIndentation) ->
        {
            String securitySchemeId = authenticationSpecPair.getOne();
            AuthenticationSpec a = authenticationSpecPair.getTwo();
            if (a instanceof OAuthAuthenticationSpec)
            {
                OAuthAuthenticationSpec spec = (OAuthAuthenticationSpec) a;
                return getTabString(baseIndentation) + securitySchemeId +
                        " : Oauth\n" +
                        getTabString(baseIndentation) + "{\n" +
                        getTabString(baseIndentation + 1) + "grantType : " + convertString(spec.grantType.toString(), true) + ";\n" +
                        getTabString(baseIndentation + 1) + "clientId : " + convertString(spec.clientId, true) + ";\n" +
                        getTabString(baseIndentation + 1) + "clientSecretVaultReference : " + convertString(spec.clientSecretVaultReference, true) + ";\n" +
                        getTabString(baseIndentation + 1) + "authorizationServerUrl : " + convertString(spec.authServerUrl, true) + ";\n" +
                        getTabString(baseIndentation + 1) + "scopes : [" + ListAdapter.adapt(spec.scopes).makeString(",") + "];\n" +
                        getTabString(baseIndentation) + "}";
            }
            else if (a instanceof UsernamePasswordAuthenticationSpec)
            {
                UsernamePasswordAuthenticationSpec spec = (UsernamePasswordAuthenticationSpec) a;
                return  getTabString(baseIndentation) + securitySchemeId +
                        " : UsernamePassword\n" +
                        getTabString(baseIndentation) + "{\n" +
                        getTabString(baseIndentation + 1) + "username : " + convertString(spec.username.toString(), true) + ";\n" +
                        getTabString(baseIndentation + 1) + "password : VaultCredential" +
                        getTabString(baseIndentation + 1) + "(\n" +
                        getTabString(baseIndentation + 2) + "vaultReference : " + convertString(spec.password.vaultReference.toString(), true) + ";\n" +
                        getTabString(baseIndentation + 1) + ");\n" +
                        getTabString(baseIndentation) + "}";
            }
            else if (a instanceof ApiKeyAuthenticationSpec)
            {
                ApiKeyAuthenticationSpec spec = (ApiKeyAuthenticationSpec) a;
                return  getTabString(baseIndentation) + securitySchemeId +
                        " : ApiKey\n" +
                        getTabString(baseIndentation) + "{\n" +
                        getTabString(baseIndentation + 1) + "location : " + convertString(spec.location, true) + ";\n" +
                        getTabString(baseIndentation + 1) + "keyName : " + convertString(spec.keyName, true) + ";\n" +
                        getTabString(baseIndentation + 1) + "value : VaultCredential" +
                        getTabString(baseIndentation + 1) + "(\n" +
                        getTabString(baseIndentation + 2) + "vaultReference : " + convertString(spec.value.vaultReference.toString(), true) + ";\n" +
                        getTabString(baseIndentation + 1) + ");\n" +
                        getTabString(baseIndentation) + "}";
            }
            return null;
        });
    }
}
