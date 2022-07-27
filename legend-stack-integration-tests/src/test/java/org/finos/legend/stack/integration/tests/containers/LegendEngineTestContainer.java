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

package org.finos.legend.stack.integration.tests.containers;

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
        URL resource = LegendEngineTestContainer.class.getResource("/container-configs/engine-config.template.json");
        String template = Files.readAllLines(Paths.get(resource.toURI())).stream().collect(Collectors.joining("\n"));
        return this.parameterizeConfig(mongoTestContainer, template);
    }

    private String parameterizeConfig(LegendMongoTestContainer mongoTestContainer, String config)
    {
        return config
                .replaceAll("__MONGO_URI__", mongoTestContainer.getContainerNetworkAccessibleMongoUri())
                .replaceAll("__LEGEND_ENGINE_PORT__", String.valueOf(portMapping.containerPort));
    }

    public String getExternallyAccessibleBaseUrl() {
        return String.format("http://%s:%d/exec", "localhost", portMapping.hostPort);
    }
}
