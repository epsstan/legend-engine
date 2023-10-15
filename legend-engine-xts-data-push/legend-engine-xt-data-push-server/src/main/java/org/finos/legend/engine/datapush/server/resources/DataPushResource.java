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

package org.finos.legend.engine.datapush.server.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.finos.legend.connection.*;
import org.finos.legend.connection.protocol.AuthenticationConfiguration;
import org.finos.legend.engine.datapush.DataPusher;
import org.finos.legend.engine.datapush.DataPusherProvider;
import org.finos.legend.engine.datapush.data.CSVData;
import org.finos.legend.engine.server.support.server.resources.BaseResource;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.kerberos.ProfileManagerHelper;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jax.rs.annotations.Pac4JProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/data-push")
@Api("Data Push")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DataPushResource extends BaseResource
{
    private final LegendEnvironment environment;
    private final IdentityFactory identityFactory;
    private final StoreInstanceProvider storeInstanceProvider;
    private final AuthenticationConfigurationProvider authenticationConfigurationProvider;
    private final ConnectionFactory connectionFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger(DataPushResource.class);
    private final DataPusherProvider dataPusherProvider;

    public DataPushResource(LegendEnvironment environment, IdentityFactory identityFactory,
                            StoreInstanceProvider storeInstanceProvider,
                            AuthenticationConfigurationProvider authenticationConfigurationProvider,
                            ConnectionFactory connectionFactory,
                            DataPusherProvider dataPusherProvider
                            )
    {
        this.environment = environment;
        this.identityFactory = identityFactory;
        this.storeInstanceProvider = storeInstanceProvider;
        this.authenticationConfigurationProvider = authenticationConfigurationProvider;
        this.connectionFactory = connectionFactory;
        this.dataPusherProvider = dataPusherProvider;
    }

    @Path("/push/{connectionRef}/{dataRef}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation("Push data")
    public Response pushData(
            @PathParam("connectionRef") String connectionRef,
            @PathParam("dataRef") String dataRef,
            String rawData,
            @ApiParam(hidden = true) @Pac4JProfileManager ProfileManager<CommonProfile> profileManager
    )
    {
        Identity identity = this.identityFactory.createIdentity(
                new IdentitySpecification.Builder().withProfiles(ProfileManagerHelper.extractProfiles(profileManager)).build()
        );

        CSVData csvData = new CSVData();
        csvData.name = dataRef;
        csvData.value = rawData;

        return executeWithLogging(
                "pushing data to connection " + connectionRef,
                () ->
                {
                    this.pushCSVData(identity, connectionRef, csvData);
                    return Response.noContent().build();
                }
        );
    }

    private void pushCSVData(Identity identity, String connectionInstanceRef, CSVData csvData)
    {
        try
        {
            StoreInstance connectionInstance = this.storeInstanceProvider.lookup(connectionInstanceRef);
            AuthenticationConfiguration authenticationConfiguration = this.authenticationConfigurationProvider.lookup(identity, connectionInstance);
            DataPusher dataPusher = this.dataPusherProvider.getDataPusher(connectionInstance);
            dataPusher.configure(this.connectionFactory);
            dataPusher.writeCSV(identity, connectionInstance, authenticationConfiguration, csvData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
