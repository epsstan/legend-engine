package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.runtime.connection.authentication;

public interface AuthenticationSpecVisitor<T>
{
    T visit(AuthenticationSpec authenticationSpec);
}

