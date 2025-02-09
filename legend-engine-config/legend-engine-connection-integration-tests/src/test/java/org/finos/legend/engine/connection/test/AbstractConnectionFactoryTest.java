// Copyright 2023 Goldman Sachs
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

package org.finos.legend.engine.connection.test;

import org.finos.legend.authentication.vault.CredentialVault;
import org.finos.legend.authentication.vault.impl.EnvironmentCredentialVault;
import org.finos.legend.authentication.vault.impl.SystemPropertiesCredentialVault;
import org.finos.legend.connection.Authenticator;
import org.finos.legend.connection.ConnectionFactory;
import org.finos.legend.connection.DatabaseType;
import org.finos.legend.connection.EnvironmentConfiguration;
import org.finos.legend.connection.IdentityFactory;
import org.finos.legend.connection.IdentitySpecification;
import org.finos.legend.connection.RelationalDatabaseStoreSupport;
import org.finos.legend.connection.StoreInstance;
import org.finos.legend.connection.impl.KerberosCredentialExtractor;
import org.finos.legend.connection.impl.KeyPairCredentialBuilder;
import org.finos.legend.connection.impl.SnowflakeConnectionBuilder;
import org.finos.legend.connection.impl.UserPasswordCredentialBuilder;
import org.finos.legend.connection.jdbc.StaticJDBCConnectionBuilder;
import org.finos.legend.connection.protocol.AuthenticationConfiguration;
import org.finos.legend.connection.protocol.AuthenticationMechanismType;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractConnectionFactoryTest<T>
{
    protected static final String TEST_STORE_INSTANCE_NAME = "test-store";

    protected EnvironmentConfiguration environmentConfiguration;
    protected IdentityFactory identityFactory;
    protected ConnectionFactory connectionFactory;

    @BeforeEach
    public void initialize()
    {
        this.setup();

        EnvironmentConfiguration.Builder environmentConfigurationBuilder = new EnvironmentConfiguration.Builder()
                .withVaults(
                        new SystemPropertiesCredentialVault(),
                        new EnvironmentCredentialVault()
                )
                .withStoreSupports(
                        new RelationalDatabaseStoreSupport.Builder()
                                .withIdentifier("Postgres")
                                .withDatabase(DatabaseType.POSTGRES)
                                .withAuthenticationMechanisms(
                                        AuthenticationMechanismType.USER_PASSWORD
                                )
                                .build(),
                        new RelationalDatabaseStoreSupport.Builder()
                                .withIdentifier("Snowflake")
                                .withDatabase(DatabaseType.SNOWFLAKE)
                                .withAuthenticationMechanisms(
                                        AuthenticationMechanismType.KEY_PAIR
//                                        AuthenticationMechanismType.OAUTH
                                )
                                .build()
                )
                .withAuthenticationMechanisms(
                        AuthenticationMechanismType.USER_PASSWORD,
                        AuthenticationMechanismType.API_KEY,
                        AuthenticationMechanismType.KEY_PAIR,
                        AuthenticationMechanismType.KERBEROS
                );

        CredentialVault credentialVault = this.getCredentialVault();
        if (credentialVault != null)
        {
            environmentConfigurationBuilder.withVault(credentialVault);
        }

        this.environmentConfiguration = environmentConfigurationBuilder.build();

        this.identityFactory = new IdentityFactory.Builder(environmentConfiguration)
                .build();

        this.connectionFactory = new ConnectionFactory.Builder(environmentConfiguration)
                .withCredentialBuilders(
                        new KerberosCredentialExtractor(),
                        new UserPasswordCredentialBuilder(),
                        new KeyPairCredentialBuilder()
                )
                .withConnectionBuilders(
                        new StaticJDBCConnectionBuilder.WithPlaintextUsernamePassword(),
                        new SnowflakeConnectionBuilder.WithKeyPair()
                )
                .build();
    }

    @AfterEach
    public void shutdown()
    {
        this.cleanup();
    }

    public abstract void setup();

    public abstract void cleanup();

    public CredentialVault getCredentialVault()
    {
        return null;
    }

    public abstract StoreInstance getStoreInstance();

    public abstract Identity getIdentity();

    public abstract AuthenticationConfiguration getAuthenticationConfiguration();

    public abstract void runTestWithConnection(T connection) throws Exception;

    @Test
    public void runTest() throws Exception
    {
        this.connectionFactory.injectStoreInstance(this.getStoreInstance());
        Identity identity = this.getIdentity();
        AuthenticationConfiguration authenticationConfiguration = this.getAuthenticationConfiguration();

        Authenticator authenticator = this.connectionFactory.getAuthenticator(identity, TEST_STORE_INSTANCE_NAME, authenticationConfiguration);
        T connection = this.connectionFactory.getConnection(authenticator);

        this.runTestWithConnection(connection);
        System.out.println("Successfully established and checked connection!");
    }

    // ------------------------------ Utilities ---------------------------------

    protected static Identity getAnonymousIdentity(IdentityFactory identityFactory)
    {
        return identityFactory.createIdentity(
                new IdentitySpecification.Builder()
                        .withName("test-user")
                        .build()
        );
    }
}
