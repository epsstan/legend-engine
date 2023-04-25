package org.finos.legend.experimental.snowflake;

import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestSnowflakeDeploymentApi
{
    @Test
    public void testPing()
    {
        SnowflakeDeploymentApi snowflakeDeploymentApi = new SnowflakeDeploymentApi(null);

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[0]);

        Response response = snowflakeDeploymentApi.ping(mockRequest, null);
        Map map = (Map)response.getEntity();
        assertEquals("ok", map.get("health"));
    }

    @Test
    public void testGenerateArtifacts() throws URISyntaxException, IOException
    {
        String planJSON = new String(Files.readAllBytes(Paths.get(TestSnowflakeDeploymentApi.class.getResource("/plans/plan1.json").toURI())), StandardCharsets.UTF_8);
        SnowflakeDeploymentManagerForTest snowflakeDeploymentManagerForTest = new SnowflakeDeploymentManagerForTest(planJSON);
        SnowflakeDeploymentApi snowflakeDeploymentApi = new SnowflakeDeploymentApi(snowflakeDeploymentManagerForTest);

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[0]);

        Response response = snowflakeDeploymentApi.generateArtifacts(mockRequest, null);
        SnowflakeDeploymentManager.DeploymentArtifacts deploymentArtifacts = (SnowflakeDeploymentManager.DeploymentArtifacts) response.getEntity();

        assertEquals("select \"root\".LEGAL_NAME as \"Legal Name\", \"person_0\".FIRST_NAME as \"Employees/First Name\", \"person_0\".LAST_NAME as \"Employees/Last Name\" from SCHEMA1.FIRM as \"root\" left outer join SCHEMA1.PERSON as \"person_0\" on (\"person_0\".FIRMID = \"root\".ID)", deploymentArtifacts.getArtifacts().get("RAW_SQL"));

    }
}
