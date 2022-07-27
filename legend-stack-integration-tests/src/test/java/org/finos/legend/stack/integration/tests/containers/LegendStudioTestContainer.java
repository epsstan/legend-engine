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
import java.util.stream.Collectors;

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
        URL resource = LegendStudioTestContainer.class.getResource("/container-configs/studio-httpConfig.template.json");
        String template = Files.readAllLines(Paths.get(resource.toURI())).stream().collect(Collectors.joining("\n"));
        return this.parameterizeConfig(mongoTestContainer, template);
    }

    private String assembleUiConfig(LegendMongoTestContainer mongoTestContainer) throws Exception {
        URL resource = LegendStudioTestContainer.class.getResource("/container-configs/studio-uiConfig.template.json");
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
}
