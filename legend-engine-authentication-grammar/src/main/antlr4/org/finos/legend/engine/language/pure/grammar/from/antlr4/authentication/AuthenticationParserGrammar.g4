parser grammar AuthenticationParserGrammar;

import CoreParserGrammar;

options
{
    tokenVocab = AuthenticationLexerGrammar;
}

identifier:                      VALID_STRING
;


oauthAuthentication:          OAUTH_AUTHENTICATION
                                            BRACE_OPEN
                                              TOKEN COLON oauthCredential SEMI_COLON
                                            BRACE_CLOSE
;

oauthCredential:                           OAUTH_CREDENTIAL
                                           BRACE_OPEN
                                            (
                                                   grantType
                                                   | clientId
                                                   | clientSecret
                                                   |  authServerUrl
                                            )*
                                            BRACE_CLOSE
;

grantType:                                 GRANT_TYPE COLON STRING SEMI_COLON
;

clientId:                                   CLIENT_ID COLON STRING SEMI_COLON
;

clientSecret:                               CLIENT_SECRET_VAULT_REFERENCE COLON STRING SEMI_COLON
;

authServerUrl:                              AUTH_SERVER_URL COLON STRING SEMI_COLON
;

basicAuthentication:                        BASIC_AUTHENTICATION
                                            BRACE_OPEN
                                            (
                                                   username
                                                   | password
                                            )*
                                            BRACE_CLOSE
;

username:                                  USERNAME COLON STRING SEMI_COLON
;

password:                                  PASSWORD COLON credential SEMI_COLON
;

apiKeyAuthentication:                      API_KEY_AUTHENTICATION
                                           BRACE_OPEN
                                           (
                                               value
                                           )*
                                           BRACE_CLOSE
;

value:                                     VALUE COLON STRING SEMI_COLON
;

credential:     credentialType (credentialObject)?
;

credentialType:       VALID_STRING
;

credentialObject:           BRACE_OPEN (credentialValue)*
;

credentialValue:      CREDENTIAL_ISLAND_OPEN | CREDENTIAL_CONTENT | CREDENTIAL_ISLAND_CLOSE
;