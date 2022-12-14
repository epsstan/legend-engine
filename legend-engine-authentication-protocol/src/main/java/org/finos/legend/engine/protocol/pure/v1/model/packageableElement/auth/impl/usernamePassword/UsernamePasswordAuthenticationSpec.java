package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.usernamePassword;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.VaultCredential;

public class UsernamePasswordAuthenticationSpec extends AuthenticationSpec {

    public String username;
    public VaultCredential password;
}
