package org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers;


import java.nio.file.Files;
import java.nio.file.Path;

public interface LegendTestContainer
{
    String imageName();
    String containerNetworkName();
    void start() throws Exception;
    void stop() throws Exception;
    void testBeforeUse() throws Exception;

    default void stopAndIgnoreErrors()
    {
        try {
            this.stop();
        }
        catch (Throwable e)
        {
            System.out.println("Failed to stop container gracefully");
        }
    }
}
