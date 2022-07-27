package org.finos.legend.stack.integration.tests.containers;


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
