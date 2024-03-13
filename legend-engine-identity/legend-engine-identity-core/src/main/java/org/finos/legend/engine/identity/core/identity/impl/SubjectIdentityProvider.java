package org.finos.legend.engine.identity.core.identity.impl;

import org.finos.legend.engine.identity.core.identity.Identity;
import org.finos.legend.engine.identity.core.identity.credential.impl.LegendKerberosCredential;
import org.finos.legend.engine.identity.core.identity.IdentityProvider;

import javax.security.auth.Subject;
import java.security.Principal;

public class SubjectIdentityProvider implements IdentityProvider<Subject>
{
    public Identity make(Subject subject)
    {
        if (subject == null)
        {
            throw new IllegalArgumentException("Subject is null");
        }
        // TODO  - implement
        Principal principal = null;
        if (principal == null)
        {
            throw new IllegalArgumentException("Subject does not contain a KerberosPrincipal");
        }
        String name = principal != null ? principal.getName().split("@")[0] : null;
        return new Identity(name, new LegendKerberosCredential(subject));
    }
}