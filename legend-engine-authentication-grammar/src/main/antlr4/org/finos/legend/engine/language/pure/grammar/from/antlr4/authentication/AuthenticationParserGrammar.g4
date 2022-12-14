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
                                                (token)*
                                            BRACE_CLOSE
;

token:                          TOKEN COLON credential SEMI_COLON
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
                                               location
                                               | keyName
                                               | value
                                           )*
                                           BRACE_CLOSE
;
location:                                  LOCATION VALUE STRING SEMI_COLON
;

keyName:                                   KEYNAME VALUE STRING SEMI_COLON
;

value:                                     VALUE COLON STRING SEMI_COLON
;

credential:     credentialType (credentialObject)?
;

credentialType:       VALID_STRING
;

credentialObject:           PAREN_OPEN (credentialValue)*
;

credentialValue:      CREDENTIAL_ISLAND_OPEN | CREDENTIAL_CONTENT | CREDENTIAL_ISLAND_CLOSE
;