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

package org.finos.legend.engine.credentials.provider;

import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;

import java.util.function.Supplier;


public interface CredentialsProviderFlow<IC extends Credential, OC extends  Credential, C, CR> {
    /*
        A flow implements a transformation rule where an inbound identity and credential can be transformed into an outbound credential.

        The flow is intended to be a generic construct. Example usages include :
        - Creating a credential to connect to a database
        - Creating a credential to connect to a REST API
     */

    Class<IC> inboundCredentialType();

    Class<OC> outboundCredentialType();

    CredentialsProviderFlow<IC, OC, C, CR> configure(C configurationParams);

    Supplier<OC> makeCredential(Identity identity, Class<IC> inboundClass, CR requestParams) throws Exception;

}