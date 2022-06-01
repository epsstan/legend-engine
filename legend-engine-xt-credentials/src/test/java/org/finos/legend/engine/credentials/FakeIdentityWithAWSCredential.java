package org.finos.legend.engine.credentials;

import org.finos.legend.engine.credentials.credential.LegendAwsCredential;
import org.finos.legend.engine.shared.core.identity.Identity;
import org.finos.legend.engine.shared.core.identity.credential.LegendKerberosCredential;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

public class FakeIdentityWithAWSCredential extends Identity {
    public FakeIdentityWithAWSCredential(String name) {
        super(name, new LegendAwsCredential(AwsBasicCredentials.create("falke", "fake")));
    }
}