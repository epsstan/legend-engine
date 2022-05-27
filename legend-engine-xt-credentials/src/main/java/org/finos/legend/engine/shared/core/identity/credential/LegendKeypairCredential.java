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

package org.finos.legend.engine.shared.core.identity.credential;

import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.credential.PrivateKeyCredential;
import org.immutables.value.Value;

@Value.Enclosing
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public class LegendKeypairCredential implements Credential
{
    private PrivateKeyCredential underlying;

    public LegendKeypairCredential(PrivateKeyCredential privateKeyCredential)
    {
        this.underlying = privateKeyCredential;
    }

    public PrivateKeyCredential getUnderlying()
    {
        return underlying;
    }

    @Value.Immutable
    public interface Params
    {
        String userName();

        String privateKeyVaultReference();

        String passphraseVaultReference();
    }
}
