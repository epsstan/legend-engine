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

package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.connection;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.connection.Connection;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.connection.ConnectionVisitor;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.runtime.connection.authentication.AuthenticationSpec;
import java.util.LinkedHashMap;

public class ServiceStoreConnection extends Connection
{
    public String baseUrl;
    //TODO: Order of iterating should be same as the order of inserting authSpecs into the map
    public LinkedHashMap<String, AuthenticationSpec> authSpecs;

    @Override
    public <T> T accept(ConnectionVisitor<T> connectionVisitor)
    {
        return connectionVisitor.visit(this);
    }
}
