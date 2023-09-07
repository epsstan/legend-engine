```
        LegendEnvironment legendEnvironment = new LegendEnvironment()
                .vaults()
                    .with(new PropertiesFileCredentialVault(new Properties()))
                    .build()
                .credentialProviders()
                    .with(new KerberosToGSSOCredentialProvider()) // K -> G
                    .with(new OneGSToGSSOCredentialProvider())    // O -> G
                    .with(new AToBCredentialProvider())    // O -> G
                    .with(new BToCCredentialProvider())    // O -> G
                    .build()
                .flows()
                    .with(new SnowflakeGSSOFlow())
                    .with(new SnowflakeKerberosFlow())
                    .with(new MemSQLKerberosFlow())
                    .with(new PostgresUserPasswordFlow())
                    .build();

        // 1
        Identity identity =
                legendEnvironment.identityFactory()
                    .withName("pierre")
                    .with(Credentials.plainText().with(new PropertiesFileSecret("secret1")).build())
                    .with(Credentials.plainText().with(new AWSSecretsManagerSecret("secret2", "version1", "stage1", null)).build())
                    .build();

        // 2
        // scopes
        DatabaseCapability  capability  = DatabaseCapability.for(SybaseIQ).withAuthn(SecureConnect).withAuthn(Kerberos)
/*
        Authentication authentication1 =
                Authentication.userPassword()
                    .withSpec(new UserPasswordAuthenticationSpecification("pierre", new PropertiesFileSecret("prop1")))
                    .withIdentity(identity);*/

        ConnectionSpecification postgreSpec = new StaticJDBCConnectionSpecification("a", 1, DatabaseType.Postgres, "a");

        // 3
        Authentication authentication = MagicFlows
                .forDbType(Postres)
                .withIdentity(identity);

        // 4
        Connection connection1 = ConnectionFactory.connect(postgreSpec, new UserPasswordAuthenticationSpecification("pierre", new PropertiesFileSecret("prop1")), identity);

        Authentication authentication2 =
                Authentication.gssso()
                        .withSpec(new GSSOAuthenticationSpecification())
                        .withIdentity(identity);

        ConnectionSpecification snowflakeSpec =  null;
        Connection connection2 = ConnectionFactory.connect(snowflakeSpec, authentication2);
```
