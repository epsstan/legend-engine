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

public abstract class AbstractCredentialsProviderImpl<I extends Credential, O extends  Credential, P>
        implements CredentialsProvider<I, O, P>
{
    private Class<I> inputCredentialType;
    private Class<O> outputCredentialType;
    private Class<P> credentialParamsType;

    public AbstractCredentialsProviderImpl(Class<I> inputCredentialType, Class<O> outputCredentialType, Class<P> credentialParamsType)
    {
        this.inputCredentialType = inputCredentialType;
        this.outputCredentialType = outputCredentialType;
        this.credentialParamsType = credentialParamsType;
    }

    @Override
    public Class<I> inboundCredentialType()
    {
        return this.inputCredentialType;
    }

    @Override
    public Class<O> outboundCredentialType()
    {
        return this.outputCredentialType;
    }

    @Override
    public Class<P> credentialParamsType()
    {
        return this.credentialParamsType;
    }
}