parser grammar CredentialParserGrammar;

import CoreParserGrammar;

options
{
    tokenVocab = CredentialLexerGrammar;
}

identifier:                      VALID_STRING
;



credentialVault:          CREDENTIAL_VAULT
                                            PAREN_OPEN
                                                   (vaultReference)*
                                            PAREN_CLOSE
;

vaultReference:             VAULT_REFERENCE COLON STRING SEMI_COLON
;

