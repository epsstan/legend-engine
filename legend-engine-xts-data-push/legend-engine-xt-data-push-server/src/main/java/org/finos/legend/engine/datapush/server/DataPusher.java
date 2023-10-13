// Copyright 2023 Goldman Sachs
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

package org.finos.legend.engine.datapush.server;

import org.finos.legend.connection.ConnectionFactory;
import org.finos.legend.connection.StoreInstance;
import org.finos.legend.connection.protocol.AuthenticationConfiguration;
import org.finos.legend.engine.shared.core.identity.Identity;

public abstract class DataPusher
{
    protected final ConnectionFactory connectionFactory;

    public DataPusher(ConnectionFactory connectionFactory)
    {
        this.connectionFactory = connectionFactory;
    }

    public abstract void write(Identity identity, StoreInstance storeInstance, AuthenticationConfiguration authenticationConfiguration, Data data) throws Exception;
}
