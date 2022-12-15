package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.shared.core.identity.Identity;

public abstract class ConnectionProvider<T> {

    public abstract T makeConnection(Object connectionSpec, AuthenticationSpec authenticationSpec, Identity identity) throws Exception;

}
