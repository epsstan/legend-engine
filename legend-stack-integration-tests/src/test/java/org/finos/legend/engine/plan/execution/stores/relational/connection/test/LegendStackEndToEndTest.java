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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.util.EntityUtils;
import org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers.LegendEngineTestContainer;
import org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers.LegendMongoTestContainer;
import org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers.LegendSDLCTestContainer;
import org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers.LegendStudioTestContainer;
import org.finos.legend.engine.shared.core.kerberos.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.Network;

import javax.ws.rs.core.Response;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class LegendStackEndToEndTest
{
    private static LegendMongoTestContainer MONGO_CONTAINER;
    private static LegendStudioTestContainer LEGEND_STUDIO_CONTAINER;
    private static LegendSDLCTestContainer LEGEND_SDLC_CONTAINER;
    private static LegendEngineTestContainer LEGEND_ENGINE_CONTAINER;

    @BeforeClass
    public static void setup() throws Exception
    {
        Network network = Network.newNetwork();
        MONGO_CONTAINER = LegendMongoTestContainer.build(network);
        MONGO_CONTAINER.start();
        MONGO_CONTAINER.testBeforeUse();

        LEGEND_ENGINE_CONTAINER = LegendEngineTestContainer.build("finos/legend-engine-server:2.59.2", network, MONGO_CONTAINER);
        LEGEND_ENGINE_CONTAINER.start();
        LEGEND_ENGINE_CONTAINER.testBeforeUse();

        LEGEND_SDLC_CONTAINER = LegendSDLCTestContainer.build("finos/legend-sdlc-server:0.71.2", network, MONGO_CONTAINER);
        LEGEND_SDLC_CONTAINER.start();
        LEGEND_SDLC_CONTAINER.testBeforeUse();

        LEGEND_STUDIO_CONTAINER = LegendStudioTestContainer.build("finos/legend-studio:4.18.0", network, MONGO_CONTAINER);
        LEGEND_STUDIO_CONTAINER.start();
        LEGEND_STUDIO_CONTAINER.testBeforeUse();
    }

    @AfterClass
    public static void teardown() throws Exception
    {
        Path logDirectory = Files.createTempDirectory("e2e-logs");
        MONGO_CONTAINER.stopAndIgnoreErrors();
        LEGEND_SDLC_CONTAINER.dumpLogs(logDirectory);
        LEGEND_ENGINE_CONTAINER.dumpLogs(logDirectory);
        LEGEND_STUDIO_CONTAINER.dumpLogs(logDirectory);
        System.out.println("See container logs in " + logDirectory.toAbsolutePath().toString());
    }

    /*
        Add the following redirect urls to your Gitlab.com OAuth application

        http://localhost:6060/callback
        http://localhost:7070/api/auth/callback
        http://localhost:7070/api/pac4j/login/callback
        http://localhost:8080/studio/log.in/callback
     */

    @Test
    public void test() throws Exception
    {
        System.out.println(LEGEND_ENGINE_CONTAINER.getExternallyAccessibleUrl());
        System.out.println(LEGEND_SDLC_CONTAINER.getExternallyAccessibleUrl());
        System.out.println(LEGEND_STUDIO_CONTAINER.getExternallyAccessibleUrl());

        BasicCookieStore cookieStore = new BasicCookieStore();
        HttpClient httpClient = HttpClientBuilder.getHttpClient(cookieStore);
        HttpGet sdlcHttpGet = new HttpGet(LEGEND_SDLC_CONTAINER.getExternallyAccessibleUrl());
        sdlcHttpGet.setHeader("PRIVATE-TOKEN", "YOUR PAT");
        HttpResponse sdlcHttpGetResponse = httpClient.execute(sdlcHttpGet);
        EntityUtils.consume(sdlcHttpGetResponse.getEntity());

        // TODO : Something is broken here .. This should result in a successful auth
        HttpGet engineGet = new HttpGet(LEGEND_ENGINE_CONTAINER.getExternallyAccessibleUrl() + "/api/server/v1/currentUser");
        HttpResponse engineGetResponse = httpClient.execute(engineGet);
        assertEquals(Response.Status.OK.getStatusCode(), engineGetResponse.getStatusLine().getStatusCode());
    }
}
