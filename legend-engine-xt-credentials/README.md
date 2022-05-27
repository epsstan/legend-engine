# Overview 

Sketch of a module to abstract various functionality related to credential acquisition/management.

# Goals

* Consolidate code for credential acquisition/management such that it can be reused across the platform in different contexts. E.g relational stores, service stores, persistence etc.
* Similarly consolidate code for vault implementations.
* Allow for platform specific customizations. i.e Allow Legend installations to extend/modify or provide their own implementations.

# Anti Goals

* Since the clients for this module are very diverse, this module does not offer APIs that integrate natively with other client libraries. E.g AWS's Java SDK requires a ```oftware.amazon.awssdk.auth.credential.AwsCredentialsProvider```. This module does not provide a ```AwsCredentialsProvider```.

# Example Usage 

See [CredentialsProviderTest](src/test/java/org/finos/legend/engine/credentials/provider/CredentialsProviderTest.java)


# API Design / Implementation Choices  

### Identity/Credential API

An ```Identity``` represents an entity that has an identity :). An ```Identity``` carries one/more ```Credential```s as proof of its identity.

See [Identity.java](../legend-engine-shared-core/src/main/java/org/finos/legend/engine/shared/core/identity/Identity.java)

This API approximates the ```javax.security.auth.Subject``` API. We decided not to use the ```javax.secruity.auth.Subject``` API to give us more API design/implementation flexbility.

### Credential API

Since every SDK has its own Java types for credentials, we have a Java type system for ```LegendCredential```s.  

A ```LegendCredential``` is a simple POJO. It can directly contain credential data like in ```LegendPlaintextUserPasswordCredential``` or wrap other credential types like in ```LegendAwsCredential```.

### Credential Provider
 
A ```CredentialsProviderFlow``` abstracts the business logic of providing or creating a credential. 

See [CredentialsProviderFlow.java](src/main/java/org/finos/legend/engine/credentials/provider/CredentialsProviderFlow.java)

### Credential Provider Lookup

The platform provides a registry of credential provider flows. Clients look up a provider by passing in the current caller's identity and the desired target credential. 

In some cases, acquiring a credential requires custom logic (e.g token exchange, brokered credential acquisition etc), which by itself might require a credential. 

So callers are expected to choose one of the identity's credential to be used for this custom logic.

The lookup API therefore is

```java
TODO - fix generics
Optional<CredentialsProviderFlow<LegendKerberosCredential, LegendPlaintextUserPasswordCredential, Object, Object>> flow = this.flowRegistry.lookup(
        LegendKerberosCredential.class,
        LegendPlaintextUserPasswordCredential.class
)
```

###  Credential Provider Configuration

In some cases, creating a credential requires static configuration. This configuration is static in the sense that it is not specific to a credential creation request.

For e.g, a Legend installation might use an identity provider like Ping and we might not want Ping specific configuration to be included in every Legend model.

So, a credential provider exposes a ```configure``` life cycle method by which this configuration can be injected.

```java
    new KerberosToKeyPairFlow()
        .configure(KerberosToKeyPairFlowConfigurationParams.builder()
            .foo("foo")
            .bar("bar")        
            .build()
        )
```
Flows are configured during platform initialization. 

### Credential Creation Configuration

In many cases, credential creation requires dynamic configuration. For e.g creating an OAuth token with specific scopes the values for which are sourced from a Legend model. 

So, the credential creation API accepts a configuration object. 

```java
    flow
        .makeCredential(
            fakeKerberosIdentity,
            LegendKerberosCredential.class,
            LegendOAuthCredentialCredentialRequestParams.builder()
                .oauthScopes("scope1")
                .build()
        );
```

### Delayed Credential Creation

TODO 


# Open Design Questions

* Should flow lookup always require a credential ? 

* For e.g a "public" credential provider might not care about the identity of the caller. But the provider might be used in multiple contexts where the runtime identity has different credentials.

To support this use case, the same flow code has to be registered multiple times with different inbound credentials. 

```
class MyPublicFlow1 extends AbstractCredentialsProviderFlow<LegendKerberosCredential, MyPublicCredential, Object, Object>
{
}

class MyPublicFlow2 extends AbstractCredentialsProviderFlow<LegendOAuthCredential, MyPublicCredential, Object, Object>
{
}
```
Do we want to support a different lookup API ??

* Should ```makeCredential``` return a ```Suplier``` or the actual credential ?

In some cases we want to delay credential acquisition. So a ```Supplier``` lets us defer the creation of the credential. But can/should we just the pass the ```CredentialsProviderFlow``` ?

* How to handle credential refresh 


