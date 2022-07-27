// Copyright 2021 Goldman Sachs
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

package org.finos.legend.engine.plan.execution.stores.relational.connection.test;

import com.jayway.jsonpath.JsonPath;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers.AbstractLegendTestContainer;
import org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers.LegendEngineTestContainer;
import org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers.LegendMongoTestContainer;
import org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers.LegendSDLCTestContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;

import static org.junit.Assert.assertEquals;

public class LegendStackIntegrationTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LegendStackIntegrationTest.class);
    private static LegendMongoTestContainer MONGO_CONTAINER;
    private static LegendEngineTestContainer LEGEND_ENGINE_CONTAINER;
    private static LegendSDLCTestContainer LEGEND_SDLC_CONTAINER;
    private static Path logDirectory;

    @BeforeClass
    public static void setup() throws Exception
    {
        logDirectory = Files.createTempDirectory("e2e-logs");
        LOGGER.info("Container logs are in {}", logDirectory.toAbsolutePath());

        LOGGER.info("Starting mongo container");
        Network network = Network.newNetwork();
        MONGO_CONTAINER = LegendMongoTestContainer.build(network);
        MONGO_CONTAINER.start();
        MONGO_CONTAINER.testBeforeUse();

        LOGGER.info("Starting engine container");
        LEGEND_ENGINE_CONTAINER = LegendEngineTestContainer.build("finos/legend-engine-server:3.9.3", network, MONGO_CONTAINER);
        LEGEND_ENGINE_CONTAINER.start();

        LOGGER.info("Starting sdlc container");
        LEGEND_SDLC_CONTAINER = LegendSDLCTestContainer.build("finos/legend-sdlc-server:0.84.2", network, MONGO_CONTAINER);
        LEGEND_SDLC_CONTAINER.start();
    }

    @AfterClass
    public static void teardown() throws Exception
    {
        MONGO_CONTAINER.stopAndIgnoreErrors();
        dumpLogs(LEGEND_ENGINE_CONTAINER, logDirectory);
        dumpLogs(LEGEND_SDLC_CONTAINER, logDirectory);
    }

    private static void dumpLogs(AbstractLegendTestContainer container, Path logDir) throws Exception
    {
        if (container != null)
        {
            container.dumpLogs(logDir);
        }
    }

    @Test
    public void testEngineInfoAPI() throws Exception
    {
        HttpClient httpClient = getHttpClient(new BasicCookieStore());

        String infoUrl = LEGEND_ENGINE_CONTAINER.getExternallyAccessibleBaseUrl() + "/api/server/v1/info";
        HttpGet engineGet = new HttpGet(infoUrl);
        HttpResponse response = httpClient.execute(engineGet);

        this.assertAll(
                () -> assertEquals("incorrect status code", HttpStatus.SC_OK, response.getStatusLine().getStatusCode()),
                () ->
                {
                        String json = EntityUtils.toString(response.getEntity());
                        String version = JsonPath.read(json, "$.info.legendSDLC['git.build.version']");
                        assertEquals("version number", "3.9.3", version);
                }
        );
    }

    @Test
    public void testEngineExecutionAPI() throws Exception
    {
        HttpClient httpClient = getHttpClient(new BasicCookieStore());

        String planJson = new String(Files.readAllBytes(Paths.get(LegendStackIntegrationTest.class.getResource("/engine-plans/plan1.json").toURI())), Charset.defaultCharset());
        String planExecutionAPI = LEGEND_ENGINE_CONTAINER.getExternallyAccessibleBaseUrl() + "/api/executionPlan/v1/execution/executePlan";

        HttpPost httpPost = new HttpPost(planExecutionAPI);
        httpPost.setEntity(new StringEntity(planJson));
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

        HttpResponse response = httpClient.execute(httpPost);
        String json = EntityUtils.toString(response.getEntity());

        String expectedResponse = "{\"builder\":{\"_type\":\"json\"},\"values\":[{\"firstName\":\"Johny\",\"lastName\":\"Doe\"},{\"firstName\":\"Jane\",\"lastName\":\"Doe\"}]}";
        this.assertAll(
                () -> assertEquals("incorrect status code", HttpStatus.SC_OK, response.getStatusLine().getStatusCode()),
                () -> assertEquals("incorrect response", expectedResponse, json)
        );
    }

    @Test
    public void testSDLCInfoAPI() throws Exception
    {
        HttpClient httpClient = getHttpClient(new BasicCookieStore());

        String infoUrl = LEGEND_SDLC_CONTAINER.getExternallyAccessibleBaseUrl() + "/api/info";
        HttpGet engineGet = new HttpGet(infoUrl);
        HttpResponse response = httpClient.execute(engineGet);

        this.assertAll(
                () -> assertEquals("incorrect status code", HttpStatus.SC_OK, response.getStatusLine().getStatusCode()),
                () ->
                {
                    String json = EntityUtils.toString(response.getEntity());
                    String version = JsonPath.read(json, "$.platform.version");
                    assertEquals("version number", null, version);
                }
        );
    }

    private AssertionData executeAssertion(AssertionProcedure assertion)
    {
        try
        {
            assertion.executeAssertion();
            return new AssertionData();
        }
        catch (Exception e)
        {
            return new AssertionData(e);
        }
        catch (AssertionError e)
        {
            return new AssertionData(e);
        }
    }

    private void assertAll(AssertionProcedure... assertions) throws Exception
    {
        FastList<AssertionData> failedAssertions = FastList.newListWith(assertions)
                .collect(runnable -> executeAssertion(runnable))
                .select(assertionData -> assertionData.failed());

        if (!failedAssertions.isEmpty())
        {
            failedAssertions.forEach(assertionData -> assertionData.log());
            throw new Exception(String.format("%d assertions failed. See log %s", failedAssertions.size(), logDirectory.toAbsolutePath()));
        }
    }

    public static class AssertionData
    {
        private AssertionError error;
        private Exception exception;

        public AssertionData()
        {
        }

        public AssertionData(AssertionError error)
        {
            this.error = error;
        }

        public AssertionData(Exception exception)
        {
            this.exception = exception;
        }

        public boolean failed()
        {
            return this.exception != null || this.error != null;
        }

        public void log()
        {
            if (exception != null )
            {
                LOGGER.error("assertion exception", exception);
            }
            if (error != null)
            {
                LOGGER.error("assertion failure", error);
            }
        }
    }

    public static HttpClient getHttpClient(CookieStore cookieStore)
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        Credentials credentials = new Credentials() {
            public String getPassword() {
                return null;
            }

            public Principal getUserPrincipal() {
                return null;
            }
        };
        httpclient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, -1, AuthScope.ANY_REALM), credentials);
        httpclient.setCookieStore(cookieStore);
        return httpclient;
    }
}
