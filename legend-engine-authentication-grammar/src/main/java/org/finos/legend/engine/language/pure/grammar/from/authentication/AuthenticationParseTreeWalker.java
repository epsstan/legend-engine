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
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.Authentication;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.UsernamePasswordAuthentication;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.ApiKeyAuthentication;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.OAuthAuthentication;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.OauthCredential;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.VaultCredential;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.Credential;
import org.finos.legend.engine.language.pure.grammar.from.ParseTreeWalkerSourceInformation;
import org.finos.legend.engine.language.pure.grammar.from.extensions.IAuthenticationGrammarParserExtension;
import org.finos.legend.engine.shared.core.operational.errorManagement.EngineException;
import org.finos.legend.engine.protocol.pure.v1.model.context.EngineErrorType;
import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;
import org.eclipse.collections.impl.utility.ListIterate;
import java.util.List;

public class AuthenticationParseTreeWalker
{

    private final ParseTreeWalkerSourceInformation walkerSourceInformation;

    public AuthenticationParseTreeWalker(ParseTreeWalkerSourceInformation walkerSourceInformation)
    {
        this.walkerSourceInformation = walkerSourceInformation;
    }

    public UsernamePasswordAuthentication visitUsernamePasswordAuthentication(AuthenticationSourceCode code, AuthenticationParserGrammar.BasicAuthenticationContext ctx)
    {
        UsernamePasswordAuthentication u = new UsernamePasswordAuthentication();
        u.sourceInformation = code.getSourceInformation();

        AuthenticationParserGrammar.UsernameContext usernameContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.username(),"username", u.sourceInformation);
        u.username = PureGrammarParserUtility.fromGrammarString(usernameContext.STRING().getText(), true);

        AuthenticationParserGrammar.PasswordContext passwordContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.password(),"password", u.sourceInformation);

        //TODO: Validate type of credential
        VaultCredential v = (VaultCredential) this.visitCredential(passwordContext.credential());
        v.sourceInformation = code.getSourceInformation();

        u.password = v;
        return u;
    }

    public ApiKeyAuthentication visitApiKeyAuthentication(AuthenticationSourceCode code, AuthenticationParserGrammar.ApiKeyAuthenticationContext ctx)
    {
        ApiKeyAuthentication u = new ApiKeyAuthentication();
        u.sourceInformation = code.getSourceInformation();

        AuthenticationParserGrammar.ValueContext valueContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.value(),"value", u.sourceInformation);
        u.value = PureGrammarParserUtility.fromGrammarString(valueContext.STRING().getText(), true);

        return u;
    }

    public OAuthAuthentication visitOAuthAuthentication(AuthenticationSourceCode code, AuthenticationParserGrammar.OauthAuthenticationContext ctx)
    {
        OAuthAuthentication oAuthAuthentication = new OAuthAuthentication();

        OauthCredential o = new OauthCredential();
        o.sourceInformation = code.getSourceInformation();

        AuthenticationParserGrammar.OauthCredentialContext oauthCredentialContext = ctx.oauthCredential();

        AuthenticationParserGrammar.GrantTypeContext grantTypeContext = PureGrammarParserUtility.validateAndExtractRequiredField(oauthCredentialContext.grantType(), "grantType", o.sourceInformation);
        //o.grantType = OauthGrantType.valueOf(PureGrammarParserUtility.fromGrammarString(grantTypeContext.STRING().getText(), true));
        o.grantType = PureGrammarParserUtility.fromGrammarString(grantTypeContext.STRING().getText(), true);

        AuthenticationParserGrammar.ClientIdContext clientIdContext = PureGrammarParserUtility.validateAndExtractRequiredField(oauthCredentialContext.clientId(), "clientId", o.sourceInformation);
        o.clientId = PureGrammarParserUtility.fromGrammarString(clientIdContext.STRING().getText(), true);

        AuthenticationParserGrammar.ClientSecretContext clientSecretContext = PureGrammarParserUtility.validateAndExtractOptionalField(oauthCredentialContext.clientSecret(), "clientSecret", o.sourceInformation);
        if (clientSecretContext != null)
        {
            o.clientSecretVaultReference = PureGrammarParserUtility.fromGrammarString(clientSecretContext.STRING().getText(), true);
        }

        AuthenticationParserGrammar.AuthServerUrlContext authServerUrlContext = PureGrammarParserUtility.validateAndExtractRequiredField(oauthCredentialContext.authServerUrl(), "authServerUrl", o.sourceInformation);
        o.authServerUrl = PureGrammarParserUtility.fromGrammarString(authServerUrlContext.STRING().getText(), true);

        oAuthAuthentication.credential = o;
        return oAuthAuthentication;
    }

    public Credential visitCredential(AuthenticationParserGrammar.CredentialContext ctx)
    {
        SourceInformation sourceInformation = walkerSourceInformation.getSourceInformation(ctx);

        CredentialSourceCode code = new CredentialSourceCode(
                ctx.getText(),
                ctx.credentialType().getText(),
                sourceInformation,
                ParseTreeWalkerSourceInformation.offset(walkerSourceInformation, ctx.getStart())
        );

        List<IAuthenticationGrammarParserExtension> extensions = IAuthenticationGrammarParserExtension.getExtensions();
        Credential cred = IAuthenticationGrammarParserExtension.process(code, ListIterate.flatCollect(extensions, IAuthenticationGrammarParserExtension::getExtraCredentialParsers));

        if (cred == null)
        {
            throw new EngineException("Unsupported syntax", this.walkerSourceInformation.getSourceInformation(ctx), EngineErrorType.PARSER);
        }

        return cred;
    }
}
