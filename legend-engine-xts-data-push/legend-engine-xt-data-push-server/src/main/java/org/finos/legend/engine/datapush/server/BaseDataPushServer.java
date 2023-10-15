// Copyright 2020 Goldman Sachs
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

import io.dropwizard.setup.Environment;
import org.finos.legend.connection.AuthenticationConfigurationProvider;
import org.finos.legend.connection.ConnectionFactory;
import org.finos.legend.connection.IdentityFactory;
import org.finos.legend.connection.LegendEnvironment;
import org.finos.legend.connection.StoreInstanceProvider;
import org.finos.legend.engine.datapush.DataPusher;
import org.finos.legend.engine.datapush.DataPusherProvider;
import org.finos.legend.engine.datapush.DataStager;
import org.finos.legend.engine.datapush.server.configuration.DataPushServerConfiguration;
import org.finos.legend.engine.datapush.server.resources.DataPushResource;
import org.finos.legend.engine.datapush.server.resources.RegistryResource;
import org.finos.legend.engine.server.support.server.BaseServer;

public abstract class BaseDataPushServer extends BaseServer<DataPushServerConfiguration>
{
    protected LegendEnvironment environment;
    protected IdentityFactory identityFactory;
    protected StoreInstanceProvider storeInstanceProvider;
    protected AuthenticationConfigurationProvider authenticationConfigurationProvider;
    protected ConnectionFactory connectionFactory;
    protected DataPusherProvider dataPushProvider;

    @Override
    public void run(DataPushServerConfiguration configuration, Environment environment)
    {
        this.environment = this.buildLegendEnvironment(configuration);
        this.identityFactory = this.buildIdentityFactory(configuration, this.environment);
        this.storeInstanceProvider = this.buildStoreInstanceProvider(configuration, this.environment);
        this.authenticationConfigurationProvider = this.buildAuthenticationConfigurationProvider(configuration, this.storeInstanceProvider, this.environment);
        this.connectionFactory = this.buildConnectionFactory(configuration, this.storeInstanceProvider, this.environment);
        this.dataPushProvider = this.buildDataPushProvider();
        super.run(configuration, environment);
    }

    public abstract DataPusherProvider buildDataPushProvider();
    @Override
    protected void configureServerCore(DataPushServerConfiguration configuration, Environment environment)
    {
        environment.jersey().register(new DataPushResource(this.environment, this.identityFactory,
                this.storeInstanceProvider, this.authenticationConfigurationProvider,
                this.connectionFactory,
                this.dataPushProvider));
        environment.jersey().register(new RegistryResource(this.environment, this.storeInstanceProvider));
    }

    @Override
    protected void configureServerExtension(DataPushServerConfiguration configuration, Environment environment)
    {
        super.configureServerExtension(configuration, environment);
    }

    public abstract LegendEnvironment buildLegendEnvironment(DataPushServerConfiguration configuration);

    public abstract IdentityFactory buildIdentityFactory(DataPushServerConfiguration configuration, LegendEnvironment environment);

    public abstract StoreInstanceProvider buildStoreInstanceProvider(DataPushServerConfiguration configuration, LegendEnvironment environment);

    public abstract AuthenticationConfigurationProvider buildAuthenticationConfigurationProvider(DataPushServerConfiguration configuration, StoreInstanceProvider storeInstanceProvider, LegendEnvironment environment);

    public abstract ConnectionFactory buildConnectionFactory(DataPushServerConfiguration configuration, StoreInstanceProvider storeInstanceProvider, LegendEnvironment environment);

}
