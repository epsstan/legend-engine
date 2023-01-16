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
package org.finos.legend.engine.plan.execution.stores.service.auth;

import org.finos.legend.engine.shared.core.identity.Credential;

import java.util.Map;

public class CompositeCredential implements Credential {

    private Map<String,Credential> credentials;

    public CompositeCredential(Map<String,Credential> credentials) {
        this.credentials = credentials;
    }

    public Map<String,Credential> getCredentials() {
        return credentials;
    }
}
