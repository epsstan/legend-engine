package org.finos.legend.engine.shared.core.identity.credential;

import org.finos.legend.engine.shared.core.identity.Credential;

public class PlaintextCredential implements Credential {

    private String value;

    public PlaintextCredential(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
