package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.credential;

import org.finos.legend.engine.shared.core.identity.Credential;

public class PlainTextCredential implements Credential {

    private String value;

    public PlainTextCredential(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
