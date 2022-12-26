lexer grammar AuthenticationLexerGrammar;

import CoreLexerGrammar;

OAUTH_AUTHENTICATION:                                       'OauthAuthenticationSpec';
GRANT_TYPE:                                                 'grantType';
CLIENT_ID:                                                  'clientId';
CLIENT_SECRET_VAULT_REFERENCE:                              'clientSecretVaultReference';
AUTH_SERVER_URL:                                            'authorizationServerUrl';

BASIC_AUTHENTICATION:                                       'UsernamePasswordAuthenticationSpec';
USERNAME:                                                   'username';
PASSWORD:                                                   'password';

API_KEY_AUTHENTICATION:                                     'ApiKeyAuthenticationSpec';
VALUE:                                                      'value';
LOCATION:                                                   'Location';
KEYNAME:                                                    'keyName';

BRACKET_OPEN:                                               '[';
BRACKET_CLOSE:                                              ']';

// -------------------------------------- ISLAND ---------------------------------------
PAREN_OPEN:                    '(' -> pushMode (CREDENTIAL_ISLAND_MODE);


mode CREDENTIAL_ISLAND_MODE;
CREDENTIAL_ISLAND_OPEN: '(' -> pushMode (CREDENTIAL_ISLAND_MODE);
CREDENTIAL_ISLAND_CLOSE: ')' -> popMode;
CREDENTIAL_CONTENT: (~[()])+;