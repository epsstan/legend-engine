package org.finos.legend.engine.connection.jdbc;

import org.apache.spark.sql.jdbc.JdbcConnectionProvider;
import org.finos.legend.authentication.credentialprovider.CredentialBuilder;
import org.finos.legend.authentication.credentialprovider.CredentialProviderProvider;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.UserPasswordAuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.PropertiesFileSecret;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.AnonymousCredential;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;
import scala.collection.immutable.Map;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

public class LegendSparkJdbcConnectionProvider extends JdbcConnectionProvider
{
    private CredentialProviderProvider credentialProvider;

    public LegendSparkJdbcConnectionProvider(CredentialProviderProvider credentialProvider)
    {
        this.credentialProvider = credentialProvider;
    }

    @Override
    public String name()
    {
        return "legend-spark-jdbc";
    }

    @Override
    public boolean canHandle(Driver driver, Map<String, String> options)
    {
        return true;
    }

    @Override
    public Connection getConnection(Driver driver, Map<String, String> options)
    {
        String url = options.get("url").get();
        // TODO - epsstan - integrate with Legend db types ??
        if (url.startsWith("jdbc:postgresql:"))
        {
            return this.buildPostgresConnection(driver, options);
        }
        throw new UnsupportedOperationException("Unsupported database. Url=" + url);
    }

    private Connection buildPostgresConnection(Driver driver, Map<String, String> options)
    {
        String url = options.get("url").get();
        try
        {
            UserPasswordAuthenticationSpecification userPasswordAuthenticationSpecification = new UserPasswordAuthenticationSpecification();
            userPasswordAuthenticationSpecification.username = options.get("username").get();
            userPasswordAuthenticationSpecification.password = new PropertiesFileSecret(options.get("passwordReference").get());

            Identity identity = new Identity("anonymous", new AnonymousCredential());
            PlaintextUserPasswordCredential credential = (PlaintextUserPasswordCredential) CredentialBuilder.makeCredential(
                    this.credentialProvider,
                    userPasswordAuthenticationSpecification,
                    identity
            );

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, credential.getUser(), credential.getPassword());
            return connection;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static LegendSparkJdbcConnectionProvider INSTANCE = null;

    public synchronized static LegendSparkJdbcConnectionProvider buildInstance(CredentialProviderProvider credentialProviderProvider)
    {
        if (INSTANCE == null)
        {
            INSTANCE = new LegendSparkJdbcConnectionProvider(credentialProviderProvider);
        }
        return INSTANCE;
    }

    public synchronized static LegendSparkJdbcConnectionProvider getInstance()
    {
        return INSTANCE;
    }
}
