package org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.util.EntityUtils;
import org.finos.legend.engine.shared.core.kerberos.HttpClientBuilder;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LegendStudioTestContainer extends AbstractLegendTestContainer
{
    public static LegendStudioTestContainer build(String containerImageName, Network network, LegendMongoTestContainer mongoTestContainer) throws Exception {
        return new LegendStudioTestContainer(containerImageName, network, mongoTestContainer);
    }

    private final PortMapping portMapping = new PortMapping(8080, 8180);

    public LegendStudioTestContainer(String containerImageName, Network network, LegendMongoTestContainer mongoTestContainer) throws Exception {

        super(containerImageName);
        this.writeConfigurations(mongoTestContainer);

        this.testContainer = new GenericContainer(DockerImageName.parse(this.imageName()))
                // container config
                .withFileSystemBind(hostWorkingDirectory.toAbsolutePath().toString(), "/config", BindMode.READ_ONLY)
                // wait config
                .waitingFor(Wait.forLogMessage("(?i).*org.eclipse.jetty.server.Server: Started*.*", 1))
                // network config
                .withNetwork(network)
                .withNetworkAliases(this.containerNetworkName())
                .withAccessToHost(true)
                .withExposedPorts(portMapping.containerPort)
                .withCreateContainerCmdModifier(super.buildPortMappingModifier(portMapping));
    }

    @Override
    public String containerNetworkName() {
        return "studio";
    }

    private void writeConfigurations(LegendMongoTestContainer mongoTestContainer) throws Exception {
        this.writeFile(this.assembleStudioConfig(mongoTestContainer), this.hostWorkingDirectory.resolve("httpConfig.json"));
        this.writeFile(this.assembleUiConfig(mongoTestContainer), this.hostWorkingDirectory.resolve("uiConfig.json"));
    }

    private String assembleStudioConfig(LegendMongoTestContainer mongoTestContainer) throws Exception {
        URL resource = LegendStudioTestContainer.class.getResource("/e2e/studio-httpConfig.template.json");
        String template = Files.readAllLines(Paths.get(resource.toURI())).stream().collect(Collectors.joining("\n"));
        return this.parameterizeConfig(mongoTestContainer, template);
    }

    private String assembleUiConfig(LegendMongoTestContainer mongoTestContainer) throws Exception {
        URL resource = LegendStudioTestContainer.class.getResource("/e2e/studio-uiConfig.template.json");
        String template = Files.readAllLines(Paths.get(resource.toURI())).stream().collect(Collectors.joining("\n"));
        return this.parameterizeConfig(mongoTestContainer, template);
    }

    private String parameterizeConfig(LegendMongoTestContainer mongoTestContainer, String config)
    {
        return config
                .replaceAll("__MONGO_URI__", mongoTestContainer.getContainerNetworkAccessibleMongoUri())
                .replaceAll("__LEGEND_STUDIO_PORT__", String.valueOf(portMapping.containerPort));
    }

    public String getExternallyAccessibleUrl() {
        return String.format("http://%s:%d/studio", testContainer.getHost(), portMapping.hostPort);
    }

    public String getExternallyAccessibleHealthCheckUrl() {
        return String.format("%s/admin/healthcheck", this.getExternallyAccessibleUrl());
    }

    @Override
    public void testBeforeUse() throws Exception {
        String logs = this.testContainer.getLogs();
        System.out.println(logs);

        HttpClient httpClient = HttpClientBuilder.getHttpClient(new BasicCookieStore());
        HttpGet httpGet = new HttpGet(this.getExternallyAccessibleHealthCheckUrl());
        HttpResponse healthCheckResponse = httpClient.execute(httpGet);
        assertEquals(HttpStatus.SC_OK, healthCheckResponse.getStatusLine().getStatusCode());

        String healthCheckResponseText = EntityUtils.toString(healthCheckResponse.getEntity());
        JsonNode healthCheckData = new ObjectMapper().readTree(healthCheckResponseText);
        assertTrue(healthCheckData.get("Static").get("healthy").asBoolean());
    }
}
