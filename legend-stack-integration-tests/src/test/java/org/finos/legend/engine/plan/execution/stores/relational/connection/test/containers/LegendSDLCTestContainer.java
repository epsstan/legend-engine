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

package org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers;

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
        URL resource = LegendSDLCTestContainer.class.getResource("/container-configs/sdlc-confg.template.yml");
        String template = Files.readAllLines(Paths.get(resource.toURI())).stream().collect(Collectors.joining("\n"));
        return this.parameterizeConfig(mongoTestContainer, template);
    }

    private String parameterizeConfig(LegendMongoTestContainer mongoTestContainer, String config)
    {
        return config
                .replaceAll("__MONGO_URI__", mongoTestContainer.getContainerNetworkAccessibleMongoUri())
                .replaceAll("__LEGEND_SDLC_PORT__", String.valueOf(portMapping.containerPort))
                .replaceAll("__LEGEND_SDLC_ADMIN_PORT__", String.valueOf(adminPortMapping.containerPort))
                .replaceAll("__LEGEND_SDLC_URL__", this.getExternallyAccessibleBaseUrl());
    }

    public String getExternallyAccessibleBaseUrl() {
        return String.format("http://%s:%d/sdlc", "localhost", portMapping.hostPort);
    }
}
