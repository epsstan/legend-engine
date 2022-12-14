lexer grammar AuthenticationLexerGrammar;

import CoreLexerGrammar;

OAUTH_AUTHENTICATION:                                       'OauthAuthenticationSpec';
TOKEN:                                                      'token';

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