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

package org.finos.legend.engine.datapush.server;

import io.dropwizard.setup.Bootstrap;
import org.finos.legend.authentication.vault.impl.EnvironmentCredentialVault;
import org.finos.legend.authentication.vault.impl.SystemPropertiesCredentialVault;
import org.finos.legend.connection.AuthenticationConfigurationProvider;
import org.finos.legend.connection.AuthenticationMechanismConfiguration;
import org.finos.legend.connection.ConnectionFactory;
import org.finos.legend.connection.DatabaseType;
import org.finos.legend.connection.IdentityFactory;
import org.finos.legend.connection.LegendEnvironment;
import org.finos.legend.connection.RelationalDatabaseStoreSupport;
import org.finos.legend.connection.StoreInstance;
import org.finos.legend.connection.StoreInstanceProvider;
import org.finos.legend.connection.impl.EncryptedPrivateKeyPairAuthenticationConfiguration;
import org.finos.legend.connection.impl.InstrumentedAuthenticationConfigurationProvider;
import org.finos.legend.connection.impl.InstrumentedStoreInstanceProvider;
import org.finos.legend.connection.impl.KerberosCredentialExtractor;
import org.finos.legend.connection.impl.KeyPairCredentialBuilder;
import org.finos.legend.connection.impl.SnowflakeConnectionBuilder;
import org.finos.legend.connection.impl.StaticJDBCConnectionBuilder;
import org.finos.legend.connection.impl.UserPasswordAuthenticationConfiguration;
import org.finos.legend.connection.impl.UserPasswordCredentialBuilder;
import org.finos.legend.connection.protocol.AuthenticationMechanismType;
import org.finos.legend.connection.protocol.SnowflakeConnectionSpecification;
import org.finos.legend.connection.protocol.StaticJDBCConnectionSpecification;
import org.finos.legend.engine.datapush.DataPusher;
import org.finos.legend.engine.datapush.DataPusherProvider;
import org.finos.legend.engine.datapush.impl.S3DataStager;
import org.finos.legend.engine.datapush.impl.SnowflakeJdbcDataPusher;
import org.finos.legend.engine.datapush.server.configuration.DataPushServerConfiguration;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.SystemPropertiesSecret;
import org.finos.legend.engine.server.support.server.config.BaseServerConfiguration;
import org.finos.legend.server.pac4j.LegendPac4jBundle;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

public class DataPushServer extends BaseDataPushServer
{
    @Override
    protected ServerPlatformInfo newServerPlatformInfo()
    {
        return new ServerPlatformInfo(null, null, null);
    }

    @Override
    public void initialize(Bootstrap<DataPushServerConfiguration> bootstrap)
    {
        super.initialize(bootstrap);

        bootstrap.addBundle(new LegendPac4jBundle<>(BaseServerConfiguration::getPac4jConfiguration));
    }

    public static void main(String... args) throws Exception
    {
        new DataPushServer().run(args);
    }

    @Override
    public DataPusherProvider buildDataPushProvider()
    {
        return new DataPusherProviderImpl();
    }

    @Override
    public LegendEnvironment buildLegendEnvironment(DataPushServerConfiguration configuration)
    {
        return new LegendEnvironment.Builder()
                .withVaults(
                        new SystemPropertiesCredentialVault(),
                        new EnvironmentCredentialVault()
                )
                .withStoreSupports(
                        new RelationalDatabaseStoreSupport.Builder(DatabaseType.POSTGRES)
                                .withIdentifier("Postgres")
                                .withAuthenticationMechanismConfigurations(
                                        new AuthenticationMechanismConfiguration.Builder(AuthenticationMechanismType.USER_PASSWORD).withAuthenticationConfigurationTypes(
                                                UserPasswordAuthenticationConfiguration.class
                                        ).build()
                                )
                                .build(),
                        new RelationalDatabaseStoreSupport.Builder(DatabaseType.SNOWFLAKE)
                                .withIdentifier("Snowflake")
                                .withAuthenticationMechanismConfigurations(
                                        new AuthenticationMechanismConfiguration.Builder(AuthenticationMechanismType.KEY_PAIR).withAuthenticationConfigurationTypes(
                                                EncryptedPrivateKeyPairAuthenticationConfiguration.class
                                        ).build()
                                )
                                .build()
                ).build();
    }

    @Override
    public IdentityFactory buildIdentityFactory(DataPushServerConfiguration configuration, LegendEnvironment environment)
    {
        return new IdentityFactory.Builder(environment)
                .build();
    }

    @Override
    public StoreInstanceProvider buildStoreInstanceProvider(DataPushServerConfiguration configuration, LegendEnvironment environment)
    {
        InstrumentedStoreInstanceProvider storeInstanceProvider = new InstrumentedStoreInstanceProvider();

        StoreInstance localPostgres = new StoreInstance.Builder(this.environment)
                .withIdentifier("test-postgres")
                .withStoreSupportIdentifier("Postgres")
                .withConnectionSpecification(new StaticJDBCConnectionSpecification(
                        "localhost",
                        5432,
                        "legend"
                ))
                .build();
        storeInstanceProvider.injectStoreInstance(localPostgres
        );

        SnowflakeConnectionSpecification summitSnowflake = new SnowflakeConnectionSpecification();
        summitSnowflake.databaseName = "SUMMIT_DEV";
        summitSnowflake.accountName = "ki79827";
        summitSnowflake.warehouseName = "SUMMIT_DEV";
        summitSnowflake.region = "us-east-2";
        summitSnowflake.cloudType = "aws";
        summitSnowflake.role = "SUMMIT_DEV";
        storeInstanceProvider.injectStoreInstance(new StoreInstance.Builder(this.environment)
                .withIdentifier("test-snowflake")
                .withStoreSupportIdentifier("Snowflake")
                .withConnectionSpecification(summitSnowflake)
                .build()
        );

        SnowflakeConnectionSpecification finosSnowflake = new SnowflakeConnectionSpecification();
        finosSnowflake.databaseName = "DPSH_DB1";
        finosSnowflake.accountName = "ki79827";
        finosSnowflake.warehouseName = "PUSH_WH1";
        finosSnowflake.region = "us-east-2";
        finosSnowflake.cloudType = "aws";
        finosSnowflake.role = "PUSH_ROLE1";
        storeInstanceProvider.injectStoreInstance(new StoreInstance.Builder(this.environment)
                .withIdentifier("finosSF")
                .withStoreSupportIdentifier("Snowflake")
                .withConnectionSpecification(finosSnowflake)
                .build()
        );

        return storeInstanceProvider;
    }

    @Override
    public AuthenticationConfigurationProvider buildAuthenticationConfigurationProvider(DataPushServerConfiguration configuration, StoreInstanceProvider storeInstanceProvider, LegendEnvironment environment)
    {
        InstrumentedAuthenticationConfigurationProvider authenticationConfigProvider = new InstrumentedAuthenticationConfigurationProvider(this.storeInstanceProvider, this.environment);
        authenticationConfigProvider.injectAuthenticationConfiguration(
                "test-postgres",
                new UserPasswordAuthenticationConfiguration("newuser", new SystemPropertiesSecret("passwordRef")));
        authenticationConfigProvider.injectAuthenticationConfiguration(
                "test-snowflake",
                new EncryptedPrivateKeyPairAuthenticationConfiguration(
                        "SUMMIT_DEV1",
                        new SystemPropertiesSecret("snowflakePkRef"),
                        new SystemPropertiesSecret("snowflakePkPassphraseRef")
                ));
        authenticationConfigProvider.injectAuthenticationConfiguration(
                "finosSF",
                new EncryptedPrivateKeyPairAuthenticationConfiguration(
                        "DEVELOPER_USER1",
                        new SystemPropertiesSecret("snowflakePkRef"),
                        new SystemPropertiesSecret("snowflakePkPassphraseRef")
                ));

        // TODO - add secrets
        System.setProperty("snowflakePkPassphraseRef", "xxxxx");
        System.setProperty("snowflakePkRef", "yyyy");

        return authenticationConfigProvider;
    }

    @Override
    public ConnectionFactory buildConnectionFactory(DataPushServerConfiguration configuration, StoreInstanceProvider storeInstanceProvider, LegendEnvironment environment)
    {
        return new ConnectionFactory.Builder(environment, storeInstanceProvider)
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

    public static class DataPusherProviderImpl implements DataPusherProvider
    {
        @Override
        public DataPusher getDataPusher(StoreInstance connectionInstance)
        {
            String storeSupport = connectionInstance.getStoreSupport().getIdentifier();
            if (storeSupport.equals("Snowflake"))
            {
                return this.buildDataPusherForSnowflake();
            }
            throw new UnsupportedOperationException("Unsupported store support : " + storeSupport);
        }

        // Add secrets
        private DataPusher buildDataPusherForSnowflake()
        {
            // TODO - lookup from some config ??
            S3DataStager s3DataStager = new S3DataStager(
                    "legend-dpsh1",
                    "https://s3.us-east-1/",
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(
                                    "xxxxx",
                                    "yyyyy")
                    )
            );
            String tableName = "DPSH_DB1.SCHEMA1.TABLE1";
            String stageName = "DPSH_DB1.SCHEMA1.STAGE1";
            DataPusher dataPusher = new SnowflakeJdbcDataPusher(s3DataStager, tableName, stageName);
            return dataPusher;
        }
    }
}
