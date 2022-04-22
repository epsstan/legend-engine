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
import java.time.Duration;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LegendSDLCTestContainer extends AbstractLegendTestContainer
{
    public static LegendSDLCTestContainer build(String containerImageName, Network network, LegendMongoTestContainer mongoTestContainer) throws Exception {
        return new LegendSDLCTestContainer(containerImageName, network, mongoTestContainer);
    }

    public final PortMapping portMapping = new PortMapping(7070, 7170);
    public final PortMapping adminPortMapping = new PortMapping(7071, 7171);

    public LegendSDLCTestContainer(String containerImageName, Network network, LegendMongoTestContainer mongoTestContainer) throws Exception
    {
        super(containerImageName);
        this.writeConfigurations(mongoTestContainer);

        super.testContainer = new GenericContainer(DockerImageName.parse(this.imageName()))
                // container config
                .withFileSystemBind(hostWorkingDirectory.toAbsolutePath().toString(), "/config", BindMode.READ_ONLY)
                // wait config
                .withStartupTimeout(Duration.ofSeconds(30))
                .waitingFor(Wait.forLogMessage("(?i).*org.eclipse.jetty.server.Server - Started*.*", 1))
                // network config
                .withNetwork(network)
                .withNetworkAliases(this.containerNetworkName())
                .withAccessToHost(true)
                .withExposedPorts(portMapping.containerPort, adminPortMapping.containerPort)
                .withCreateContainerCmdModifier(super.buildPortMappingModifier(portMapping, adminPortMapping));
    }

    @Override
    public String containerNetworkName() {
        return "sdlc";
    }

    private void writeConfigurations(LegendMongoTestContainer mongoTestContainer) throws Exception {
        this.writeFile(this.assembleSDLCConfigYaml(mongoTestContainer), this.hostWorkingDirectory.resolve("config.json"));
    }

    private String assembleSDLCConfigYaml(LegendMongoTestContainer mongoTestContainer) throws Exception {
        URL resource = LegendSDLCTestContainer.class.getResource("/e2e/sdlc-confg.template.yml");
        String template = Files.readAllLines(Paths.get(resource.toURI())).stream().collect(Collectors.joining("\n"));
        return this.parameterizeConfig(mongoTestContainer, template);
    }

    private String parameterizeConfig(LegendMongoTestContainer mongoTestContainer, String config)
    {
        return config
                .replaceAll("__MONGO_URI__", mongoTestContainer.getContainerNetworkAccessibleMongoUri())
                .replaceAll("__LEGEND_SDLC_PORT__", String.valueOf(portMapping.containerPort))
                .replaceAll("__LEGEND_SDLC_ADMIN_PORT__", String.valueOf(adminPortMapping.containerPort))
                .replaceAll("__LEGEND_SDLC_URL__", this.getExternallyAccessibleUrl());
    }

    public String getExternallyAccessibleUrl() {
        return String.format("http://%s:%d/sdlc", "localhost", portMapping.hostPort);
    }

    @Override
    public void testBeforeUse() throws Exception {
        HttpClient httpClient = HttpClientBuilder.getHttpClient(new BasicCookieStore());
        HttpGet httpGet = new HttpGet(this.getExternallyAccessibleUrl() + "/api/info");
        HttpResponse healthCheckResponse = httpClient.execute(httpGet);
        assertEquals(HttpStatus.SC_OK, healthCheckResponse.getStatusLine().getStatusCode());

        String healthCheckResponseText = EntityUtils.toString(healthCheckResponse.getEntity());
        JsonNode healthCheckData = new ObjectMapper().readTree(healthCheckResponseText);
        assertNotNull(healthCheckData.get("hostName").asText());
    }

}
