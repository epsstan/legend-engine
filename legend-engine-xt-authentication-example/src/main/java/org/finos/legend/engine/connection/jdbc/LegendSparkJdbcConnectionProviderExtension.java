package org.finos.legend.engine.connection.jdbc;

import org.apache.spark.sql.jdbc.JdbcConnectionProvider;
import scala.collection.immutable.Map;

import java.sql.Connection;
import java.sql.Driver;

public class LegendSparkJdbcConnectionProviderExtension extends JdbcConnectionProvider
{
    private final LegendSparkJdbcConnectionProvider delegate;

    public LegendSparkJdbcConnectionProviderExtension()
    {
        this.delegate = LegendSparkJdbcConnectionProvider.getInstance();
    }

    @Override
    public String name()
    {
        return this.delegate.name();
    }

    @Override
    public boolean canHandle(Driver driver, Map<String, String> options)
    {
        return this.delegate.canHandle(driver, options);
    }

    @Override
    public Connection getConnection(Driver driver, Map<String, String> options)
    {
        return this.delegate.getConnection(driver, options);
    }
}
