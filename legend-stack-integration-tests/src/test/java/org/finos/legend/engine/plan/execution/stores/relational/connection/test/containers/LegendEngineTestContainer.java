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

public class LegendEngineTestContainer extends AbstractLegendTestContainer
{
    public static LegendEngineTestContainer build(String containerImageName, Network network, LegendMongoTestContainer mongoTestContainer) throws Exception
    {
        return new LegendEngineTestContainer(containerImageName, network, mongoTestContainer);
    }

    // Force a static port mapping as the Gitlab.com OAuth redirect urls are static
    public final PortMapping portMapping = new PortMapping(6060, 6160);

    public LegendEngineTestContainer(String containerImageName, Network network, LegendMongoTestContainer mongoTestContainer) throws Exception
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
                .withExposedPorts(portMapping.containerPort)
                .withCreateContainerCmdModifier(super.buildPortMappingModifier(portMapping));
    }

    @Override
    public String containerNetworkName() {
        return "engine";
    }

    private void writeConfigurations(LegendMongoTestContainer mongoTestContainer) throws Exception {
        this.writeFile(this.assembleSDLCConfigYaml(mongoTestContainer), this.hostWorkingDirectory.resolve("config.json"));
    }

    private String assembleSDLCConfigYaml(LegendMongoTestContainer mongoTestContainer) throws Exception {
        URL resource = LegendEngineTestContainer.class.getResource("/e2e/engine-config.template.json");
        String template = Files.readAllLines(Paths.get(resource.toURI())).stream().collect(Collectors.joining("\n"));
        return this.parameterizeConfig(mongoTestContainer, template);
    }

    private String parameterizeConfig(LegendMongoTestContainer mongoTestContainer, String config)
    {
        return config
                .replaceAll("__MONGO_URI__", mongoTestContainer.getContainerNetworkAccessibleMongoUri())
                .replaceAll("__LEGEND_ENGINE_PORT__", String.valueOf(portMapping.containerPort));
    }

    public String getExternallyAccessibleUrl() {
        return String.format("http://%s:%d/exec", "localhost", portMapping.hostPort);
    }

    @Override
    public void testBeforeUse() throws Exception {
        HttpClient httpClient = HttpClientBuilder.getHttpClient(new BasicCookieStore());
        HttpGet httpGet = new HttpGet(this.getExternallyAccessibleUrl() + "/api/server/v1/info");
        HttpResponse healthCheckResponse = httpClient.execute(httpGet);
        assertEquals(HttpStatus.SC_OK, healthCheckResponse.getStatusLine().getStatusCode());

        String healthCheckResponseText = EntityUtils.toString(healthCheckResponse.getEntity());
        JsonNode healthCheckData = new ObjectMapper().readTree(healthCheckResponseText);
        assertNotNull(healthCheckData.asText());
    }

}
