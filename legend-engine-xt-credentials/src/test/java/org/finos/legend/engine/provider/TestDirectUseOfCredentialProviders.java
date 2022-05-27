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

package org.finos.legend.engine.provider;

import org.finos.legend.engine.credentials.provider.ImmutableStaticUsernamePasswordCredentialProvider;
import org.finos.legend.engine.credentials.provider.StaticUsernamePasswordCredentialProvider;
import org.finos.legend.engine.shared.core.identity.credential.ImmutableLegendPlaintextUserPasswordCredential;
import org.finos.legend.engine.shared.core.identity.credential.LegendPlaintextUserPasswordCredential;
import org.finos.legend.engine.shared.core.vault.PropertiesVaultImplementation;
import org.finos.legend.engine.shared.core.vault.Vault;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class TestDirectUseOfCredentialProviders
{
    private PropertiesVaultImplementation propertiesVaultImplementation;

    @Before
    public void setup()
    {
        Properties properties = new Properties();
        properties.put("user1", "password1");
        this.propertiesVaultImplementation = new PropertiesVaultImplementation(properties);
        Vault.INSTANCE.registerImplementation(propertiesVaultImplementation);
    }

    @Test
    public void userPasswordFlow() throws Exception
    {
        StaticUsernamePasswordCredentialProvider flow = new StaticUsernamePasswordCredentialProvider(
                ImmutableStaticUsernamePasswordCredentialProvider.Configuration.builder()
                        .build());

        Supplier<LegendPlaintextUserPasswordCredential> supplier =
                flow.makeCredential(
                        ImmutableLegendPlaintextUserPasswordCredential.Params.builder().name("user1").build()
                );

        LegendPlaintextUserPasswordCredential credential = supplier.get();
        assertEquals("user1", credential.getUser());
        assertEquals("password1", credential.getPassword());
    }

}
