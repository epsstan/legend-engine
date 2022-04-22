package org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.GenericContainer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractLegendTestContainer implements LegendTestContainer
{
    protected Path hostWorkingDirectory;
    protected GenericContainer testContainer;
    protected String containerImageName;

    public AbstractLegendTestContainer() throws Exception
    {
        this.hostWorkingDirectory = Files.createTempDirectory(this.containerNetworkName());
    }

    public AbstractLegendTestContainer(String containerImageName) throws Exception
    {
        this.hostWorkingDirectory = Files.createTempDirectory(this.containerNetworkName());
        this.containerImageName = containerImageName;
    }


    @Override
    public void start() throws Exception
    {
        this.testContainer.start();
    }

    @Override
    public void stop() throws Exception
    {
        this.testContainer.stop();
    }

    @Override
    public String imageName() {
        return this.containerImageName;
    }

    protected void writeFile(String fileContent, Path filePath) throws Exception
    {
        Files.write(filePath, fileContent.getBytes(Charset.defaultCharset()));
    }

    public String getContainerLogs()
    {
        return this.testContainer.getLogs();
    }

    protected Consumer<CreateContainerCmd> buildPortMappingModifier(PortMapping ...portMappings)
    {
        List<PortBinding> portBindings = Arrays
                .stream(portMappings)
                .map(portMapping -> new PortBinding(Ports.Binding.bindPort(portMapping.hostPort), new ExposedPort(portMapping.containerPort)))
                .collect(Collectors.toList());


        Consumer<CreateContainerCmd> commandModifier = e -> e.withPortBindings(portBindings);
        return commandModifier;
    }

    public void dumpLogs(Path logDirectory) throws Exception
    {
        if (this.testContainer == null)
        {
            return;
        }
        Path filePath = logDirectory.resolve(this.containerNetworkName()+".container.logs.txt");
        Files.write(filePath, this.testContainer.getLogs().getBytes(StandardCharsets.UTF_8));
    }

    static class PortMapping
    {
        int hostPort;
        int containerPort;

        public PortMapping(int hostPort, int containerPort)
        {
            this.hostPort = hostPort;
            this.containerPort = containerPort;
        }
    }
}
