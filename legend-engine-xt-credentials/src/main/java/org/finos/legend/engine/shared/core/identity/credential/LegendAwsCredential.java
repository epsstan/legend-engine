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
import org.immutables.value.Value;
import software.amazon.awssdk.auth.credentials.AwsCredentials;

// TODO - Move all credentials into the same module. Either into this module or legend-engine-shared-core

@Value.Enclosing
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public class LegendAwsCredential implements Credential
{
    private AwsCredentials underlying;

    public LegendAwsCredential(AwsCredentials underlying)
    {
        this.underlying = underlying;
    }

    public AwsCredentials getUnderlying()
    {
        return underlying;
    }

    @Value.Immutable
    public interface Params
    {
    }
}
