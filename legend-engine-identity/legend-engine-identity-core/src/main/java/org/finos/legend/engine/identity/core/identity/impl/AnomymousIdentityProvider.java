package org.finos.legend.engine.identity.core.identity.impl;

import org.finos.legend.engine.identity.core.identity.Identity;
import org.finos.legend.engine.identity.core.identity.IdentityProvider;

public class AnomymousIdentityProvider implements IdentityProvider<Void>
{
    @Override
    public Identity make(Void input)
    {
        return new Identity("anonymous");
    }
}
