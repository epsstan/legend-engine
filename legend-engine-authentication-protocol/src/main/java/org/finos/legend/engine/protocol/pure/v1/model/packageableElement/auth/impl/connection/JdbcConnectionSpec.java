package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.connection;

public class JdbcConnectionSpec {

    public String dbHostname;
    public int dbPort;
    public DbType dbType;

    public static enum DbType
    {
        H2
    }

}
