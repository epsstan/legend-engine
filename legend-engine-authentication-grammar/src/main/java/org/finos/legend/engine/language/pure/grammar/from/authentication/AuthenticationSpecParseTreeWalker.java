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

package org.finos.legend.engine.language.pure.grammar.from.authentication;

import org.finos.legend.engine.language.pure.grammar.from.PureGrammarParserUtility;
import org.finos.legend.engine.language.pure.grammar.from.antlr4.authentication.AuthenticationParserGrammar;
import org.finos.legend.engine.language.pure.grammar.from.antlr4.authentication.CredentialParserGrammar;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.*;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.OAuthAuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.ApiKeyAuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.UsernamePasswordAuthenticationSpec;
import org.finos.legend.engine.language.pure.grammar.from.ParseTreeWalkerSourceInformation;
import org.finos.legend.engine.language.pure.grammar.from.extensions.IAuthenticationSpecGrammarParserExtension;
import org.finos.legend.engine.shared.core.operational.errorManagement.EngineException;
import org.finos.legend.engine.protocol.pure.v1.model.context.EngineErrorType;
import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;
import org.eclipse.collections.impl.utility.ListIterate;
import java.util.List;

public class AuthenticationSpecParseTreeWalker
{

    private final ParseTreeWalkerSourceInformation walkerSourceInformation;

    public AuthenticationSpecParseTreeWalker(ParseTreeWalkerSourceInformation walkerSourceInformation)
    {
        this.walkerSourceInformation = walkerSourceInformation;
    }

    public UsernamePasswordAuthenticationSpec visitUsernamePasswordAuthenticationSpec(AuthenticationSpecSourceCode code, AuthenticationParserGrammar.BasicAuthenticationContext ctx)
    {
        UsernamePasswordAuthenticationSpec u = new UsernamePasswordAuthenticationSpec();
        u.sourceInformation = code.getSourceInformation();

        AuthenticationParserGrammar.UsernameContext usernameContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.username(),"username", u.sourceInformation);
        u.username = PureGrammarParserUtility.fromGrammarString(usernameContext.STRING().getText(), true);

        AuthenticationParserGrammar.PasswordContext passwordContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.password(),"password", u.sourceInformation);

        //TODO: Validate type of credential
        CredentialVault v = (CredentialVault) this.visitCredentialProvider(passwordContext.credential());

        u.password = v;
        return u;
    }

    public ApiKeyAuthenticationSpec visitApiKeyAuthenticationSpec(AuthenticationSpecSourceCode code, AuthenticationParserGrammar.ApiKeyAuthenticationContext ctx)
    {
        ApiKeyAuthenticationSpec u = new ApiKeyAuthenticationSpec();
        u.sourceInformation = code.getSourceInformation();

        AuthenticationParserGrammar.ValueContext valueContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.value(),"value", u.sourceInformation);
        u.value = PureGrammarParserUtility.fromGrammarString(valueContext.STRING().getText(), true);

        return u;
    }

    public OAuthAuthenticationSpec visitOAuthAuthenticationSpec(AuthenticationSpecSourceCode code, AuthenticationParserGrammar.OauthAuthenticationContext ctx)
    {
        OAuthAuthenticationSpec o = new OAuthAuthenticationSpec();
        o.sourceInformation = code.getSourceInformation();

        AuthenticationParserGrammar.GrantTypeContext grantTypeContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.grantType(), "grantType", o.sourceInformation);
        //o.grantType = OauthGrantType.valueOf(PureGrammarParserUtility.fromGrammarString(grantTypeContext.STRING().getText(), true));
        o.grantType = PureGrammarParserUtility.fromGrammarString(grantTypeContext.STRING().getText(), true);

        AuthenticationParserGrammar.ClientIdContext clientIdContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.clientId(), "clientId", o.sourceInformation);
        o.clientId = PureGrammarParserUtility.fromGrammarString(clientIdContext.STRING().getText(), true);

        AuthenticationParserGrammar.ClientSecretContext clientSecretContext = PureGrammarParserUtility.validateAndExtractOptionalField(ctx.clientSecret(), "clientSecret", o.sourceInformation);
        if (clientSecretContext != null)
        {
            o.clientSecretVaultReference = PureGrammarParserUtility.fromGrammarString(clientSecretContext.STRING().getText(), true);
        }

        AuthenticationParserGrammar.AuthServerUrlContext authServerUrlContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.authServerUrl(), "authServerUrl", o.sourceInformation);
        o.authServerUrl = PureGrammarParserUtility.fromGrammarString(authServerUrlContext.STRING().getText(), true);

        return o;
    }

    public CredentialProvider visitCredentialProvider(AuthenticationParserGrammar.CredentialContext ctx)
    {
        SourceInformation sourceInformation = walkerSourceInformation.getSourceInformation(ctx);

        CredentialProviderSourceCode code = new CredentialProviderSourceCode(
                ctx.getText(),
                ctx.credentialType().getText(),
                sourceInformation,
                ParseTreeWalkerSourceInformation.offset(walkerSourceInformation, ctx.getStart())
        );

        List<IAuthenticationSpecGrammarParserExtension> extensions = IAuthenticationSpecGrammarParserExtension.getExtensions();
        CredentialProvider credentialProvider = IAuthenticationSpecGrammarParserExtension.process(code, ListIterate.flatCollect(extensions, IAuthenticationSpecGrammarParserExtension::getExtraCredentialParsers));

        if (credentialProvider == null)
        {
            throw new EngineException("Unsupported syntax", this.walkerSourceInformation.getSourceInformation(ctx), EngineErrorType.PARSER);
        }

        return credentialProvider;
    }
}
