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
package org.finos.legend.engine.plan.execution.authentication.connection;

import org.eclipse.collections.impl.list.mutable.FastList;

import org.finos.legend.engine.plan.execution.authentication.AuthenticationMethod;
import org.finos.legend.engine.plan.execution.authentication.ConnectionProvider;
import org.finos.legend.engine.plan.execution.authentication.ConnectionSpec;
import org.finos.legend.engine.plan.execution.authentication.provider.AuthenticationMethodProvider;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.runtime.connection.authentication.AuthenticationSpec;
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
    public Connection makeConnection(ConnectionSpec connectionSpec, AuthenticationSpec authenticationSpec, Identity identity) throws Exception
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
