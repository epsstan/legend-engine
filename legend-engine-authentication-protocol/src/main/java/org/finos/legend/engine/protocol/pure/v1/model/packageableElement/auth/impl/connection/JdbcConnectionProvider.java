package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.connection;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.AuthenticationMethod;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.ConnectionProvider;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.provider.AuthenticationMethodProvider;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.PlaintextUserPasswordCredential;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class JdbcConnectionProvider extends ConnectionProvider<Connection> {

    private AuthenticationMethodProvider authenticationMethodProvider;

    public JdbcConnectionProvider(AuthenticationMethodProvider authenticationMethodProvider) {
        this.authenticationMethodProvider = authenticationMethodProvider;
    }

    @Override
    public Connection makeConnection(Object connectionSpec, AuthenticationSpec authenticationSpec, Identity identity) throws Exception
    {
        assert(connectionSpec instanceof JdbcConnectionSpec);
        JdbcConnectionSpec jdbcConnectionSpec = (JdbcConnectionSpec) connectionSpec;

        FastList<AuthenticationMethod> supportedAuthenticationMethods = authenticationMethodProvider.getSupportedMethodFor(authenticationSpec.getClass());
        AuthenticationMethod chosenAuthenticationMethod = supportedAuthenticationMethods.get(0);

        switch (jdbcConnectionSpec.dbType)
        {
            case H2:
                return connectToH2(jdbcConnectionSpec,authenticationSpec,chosenAuthenticationMethod,identity);
            default:
                throw new UnsupportedOperationException("Unsupported Db Type " + jdbcConnectionSpec.dbType);
        }

    }

    private Connection connectToH2(JdbcConnectionSpec jdbcConnectionSpec, AuthenticationSpec authenticationSpec, AuthenticationMethod chosenAuthenticationMethod, Identity identity) throws Exception
    {
        Credential credential = chosenAuthenticationMethod.makeCredential(authenticationSpec,identity);
        if (credential instanceof PlaintextUserPasswordCredential)
        {
            PlaintextUserPasswordCredential plaintextUserPasswordCredential = (PlaintextUserPasswordCredential) credential;
            Class.forName("org.h2.Driver");
            Properties properties = new Properties();
            properties.setProperty("user",plaintextUserPasswordCredential.getUser());
            properties.setProperty("password",plaintextUserPasswordCredential.getPassword());
            String url = "jdbc:h2:tcp://" + jdbcConnectionSpec.dbHostname + ":" + jdbcConnectionSpec.dbPort + "/mem:" + "db1";
            Connection connection = DriverManager.getConnection(url);
            return connection;
        }

        return null;

    }
}
