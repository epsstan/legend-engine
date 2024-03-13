package org.finos.legend.engine.identity.core.identity;

import org.finos.legend.engine.identity.core.identity.Identity;
import org.finos.legend.engine.shared.core.extension.LegendExtension;

public interface IdentityTransformer<O> extends LegendExtension
{
    O transform(Identity identity) throws Exception;
}
