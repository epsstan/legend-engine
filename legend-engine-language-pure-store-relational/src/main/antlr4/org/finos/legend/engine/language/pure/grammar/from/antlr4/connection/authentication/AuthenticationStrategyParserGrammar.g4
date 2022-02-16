parser grammar AuthenticationStrategyParserGrammar;

import CoreParserGrammar;

options
{
    tokenVocab = AuthenticationStrategyLexerGrammar;
}

identifier:                      VALID_STRING
;

// ----------------------------- RELATIONAL DATABASE CONNECTION AUTH STRATEGY -----------------------------

defaultH2Auth:                          H2_DEFAULT_AUTH
;
testDBAuth:                             TEST_DB_AUTH
;
delegatedKerberosAuth:                  DELEGATED_KERBEROS_AUTH delegatedKerberosAuthConfig?
;
delegatedKerberosAuthConfig:            BRACE_OPEN
                                            (
                                                serverPrincipalConfig
                                            )*
                                        BRACE_CLOSE
;
serverPrincipalConfig:                  SERVER_PRINCIPAL COLON STRING SEMI_COLON
;
userNamePasswordAuth:                   USERNAME_PASSWORD_AUTH
                                            BRACE_OPEN
                                                (
                                                    userNamePasswordAuthBaseVaultRef
                                                    | userNamePasswordAuthUserNameVaultRef
                                                    | userNamePasswordAuthPasswordVaultRef
                                                )*
                                            BRACE_CLOSE
;
userNamePasswordAuthBaseVaultRef:       USERNAME_PASSWORD_AUTH_BASE_VAULT_REF COLON STRING SEMI_COLON
;
userNamePasswordAuthUserNameVaultRef:   USERNAME_PASSWORD_AUTH_USERNAME_VAULT_REF COLON STRING SEMI_COLON
;
userNamePasswordAuthPasswordVaultRef:   USERNAME_PASSWORD_AUTH_PASSWORD_VAULT_REF COLON STRING SEMI_COLON
;

snowflakePublicAuth:                    SNOWFLAKE_PUBLIC_AUTH
                                            BRACE_OPEN
                                                (
                                                    snowflakePublicAuthKeyVaultRef
                                                    | snowflakePublicAuthPassPhraseVaultRef
                                                    | snowflakePublicAuthUserName
                                                )*
                                            BRACE_CLOSE
;

snowflakePublicAuthKeyVaultRef:         SNOWFLAKE_AUTH_KEY_VAULT_REFERENCE COLON STRING SEMI_COLON
;

snowflakePublicAuthPassPhraseVaultRef:  SNOWFLAKE_AUTH_PASSPHRASE_VAULT_REFERENCE COLON STRING SEMI_COLON
;

snowflakePublicAuthUserName:  SNOWFLAKE_AUTH_PUBLIC_USERNAME COLON STRING SEMI_COLON
;

gcpApplicationDefaultCredentialsAuth : GCP_APPLICATION_DEFAULT_CREDENTIALS_AUTH SEMI_COLON
;

gcpWorkloadIdentityFederationAuth: GCP_WORKLOAD_IDENTITY_FEDERATION_AUTH
                                    BRACE_OPEN
                                        (
                                            workloadProjectNumberRef
                                            | serviceAccountEmailRef
                                            | gcpScopeRef
                                            | workloadPoolIdRef
                                            | workloadProviderIdRef
                                            | discoveryUrlRef
                                            | clientIdRef
                                        )*
                                    BRACE_CLOSE
;

workloadProjectNumberRef:                WORKLOAD_PROJECT_NUMBER COLON STRING SEMI_COLON
;

serviceAccountEmailRef:          SERVICE_ACCOUNT_EMAIL COLON STRING SEMI_COLON
;

gcpScopeRef:                            GCP_SCOPE COLON STRING SEMI_COLON
;

workloadPoolIdRef:                       WORKLOAD_POOL_ID COLON STRING SEMI_COLON
;

workloadProviderIdRef:                   WORKLOAD_PROVIDER_ID COLON STRING SEMI_COLON
;

discoveryUrlRef:                 DISCOVERY_URL COLON STRING SEMI_COLON
;

clientIdRef:                     CLIENT_ID COLON STRING SEMI_COLON
;
