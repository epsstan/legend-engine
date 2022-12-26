parser grammar CredentialParserGrammar;

import CoreParserGrammar;

options
{
    tokenVocab = CredentialLexerGrammar;
}

identifier:                      VALID_STRING
;



vaultCredential:          VAULT_CREDENTIAL
                                            PAREN_OPEN
                                                   (vaultReference)*
                                            PAREN_CLOSE
;

vaultReference:             VAULT_REFERENCE COLON STRING SEMI_COLON
;

