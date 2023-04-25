package org.finos.legend.experimental.snowflake;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jax.rs.annotations.Pac4JProfileManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

@Api(tags = "Application - Query")
@Path("experimental/snowflake")
@Produces(MediaType.APPLICATION_JSON)
public class SnowflakeDeploymentApi
{
    private SnowflakeDeploymentManager snowflakeDeploymentManager;

    public SnowflakeDeploymentApi(SnowflakeDeploymentManager snowflakeDeploymentManager)
    {
        this.snowflakeDeploymentManager = snowflakeDeploymentManager;
    }

    @GET
    @Path("ping")
    @ApiOperation(value = "Ping")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response ping(@ApiParam(hidden = true) @Context HttpServletRequest request, @Pac4JProfileManager ProfileManager<CommonProfile> profileManager)
    {
        HashMap<String, String> responseData = new HashMap<>();
        responseData.put("health", "ok");
        Response response = Response.ok(responseData).build();
        return response;
    }

    @GET
    @Path("generateArtifactsForService")
    @ApiOperation(value = "Generate Artifacts")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response generateArtifacts(HttpServletRequest mockRequest, @Pac4JProfileManager ProfileManager<CommonProfile> profileManager)
    {
        try
        {
            SnowflakeDeploymentManager.DeploymentArtifacts deploymentArtifacts = this.snowflakeDeploymentManager.generateDeploymentArtifactsForService();
            Response response = Response.ok(deploymentArtifacts).build();
            return response;
        }
        catch (Exception e)
        {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("deployArtifactsForService")
    @ApiOperation(value = "Generate And Deploy Artifacts")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response generateAndDeployArtifacts(HttpServletRequest mockRequest, @Pac4JProfileManager ProfileManager<CommonProfile> profileManager)
    {
        try
        {
            SnowflakeDeploymentManager.DeploymentResult deploymentResult = this.snowflakeDeploymentManager.deployService();
            Response response = Response.ok(deploymentResult).build();
            return response;
        }
        catch (Exception e)
        {
            return Response.serverError().build();
        }
    }
}


