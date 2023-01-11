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

package org.finos.legend.engine.language.pure.compiler.toPureGraph;

import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.finos.legend.engine.language.pure.compiler.toPureGraph.extension.Processor;
import org.finos.legend.pure.runtime.java.compiled.generation.processors.support.map.PureMap;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;
import org.finos.legend.engine.protocol.pure.v1.model.context.EngineErrorType;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.runtime.connection.authentication.*;
import org.finos.legend.engine.shared.core.operational.errorManagement.EngineException;
import org.finos.legend.pure.generated.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AuthenticationCompilerExtension implements IAuthenticationCompilerExtension
{

    @Override
    public Iterable<? extends Processor<?>> getExtraProcessors()
    {
        return Lists.immutable.empty();
    }

    public List<Function2<AuthenticationSpec, CompileContext, ? extends Root_meta_pure_runtime_connection_authentication_AuthenticationSpec_Impl>> getExtraAuthenticationProcessors()
    {
        return Lists.mutable.with((authSpec, context) ->
        {
            if (authSpec instanceof UsernamePasswordAuthenticationSpec)
            {
                UsernamePasswordAuthenticationSpec usernamePasswordAuthenticationSpec = (UsernamePasswordAuthenticationSpec) authSpec;
                return (Root_meta_pure_runtime_connection_authentication_AuthenticationSpec_Impl) new Root_meta_pure_runtime_connection_authentication_UsernamePasswordAuthenticationSpec_Impl("")
                        ._username(usernamePasswordAuthenticationSpec.username)
                        ._password(new Root_meta_pure_runtime_connection_authentication_CredentialVault_Impl("")
                                ._vaultReference(((CredentialVault)usernamePasswordAuthenticationSpec.password).vaultReference)); //TODO: Validate Authenticaton  Credential Combos


            }
            else if (authSpec instanceof OAuthAuthenticationSpec)
            {
                OAuthAuthenticationSpec oAuthAuthenticationSpec = (OAuthAuthenticationSpec) authSpec;
                return (Root_meta_pure_runtime_connection_authentication_AuthenticationSpec_Impl) new Root_meta_pure_runtime_connection_authentication_OauthAuthenticationSpec_Impl("")
                        ._grantType(context.pureModel.getEnumValue("meta::pure::runtime::connection::authentication::OauthGrantType", oAuthAuthenticationSpec.grantType.toString()))
                        ._clientId(oAuthAuthenticationSpec.clientId)
                        ._clientSecretVaultReference(oAuthAuthenticationSpec.clientSecretVaultReference)
                        ._authServerUrl(oAuthAuthenticationSpec.authServerUrl)
                        ._scopes(Lists.mutable.withAll(oAuthAuthenticationSpec.scopes));
            }
            else if (authSpec instanceof ApiKeyAuthenticationSpec)
            {
                ApiKeyAuthenticationSpec apiKeyAuthenticationSpec = (ApiKeyAuthenticationSpec) authSpec;
                return (Root_meta_pure_runtime_connection_authentication_AuthenticationSpec_Impl) new Root_meta_pure_runtime_connection_authentication_ApiKeyAuthenticationSpec_Impl("")
                        ._value(new Root_meta_pure_runtime_connection_authentication_CredentialVault_Impl("")
                                ._vaultReference(((CredentialVault)apiKeyAuthenticationSpec.value).vaultReference));

            }
            return null;
        });
    }

}
