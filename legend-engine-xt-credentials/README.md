# Overview 

Sketch of a module to abstract various functionality related to credential acquisition/management.

# Goals

* Consolidate code for credential acquisition/management such that it can be reused across the platform in different contexts. E.g relational stores, service stores, persistence, service execution jars etc.
* Consolidate code for different vault implementations 
* Allow for platform specific customizations. i.e Allow Legend installations to extend/modify or provide their own implementations.
* TODO : Minimize Java library dependencies

# Anti Goals

* Since the clients for this module are very diverse, this module does not offer APIs that integrate natively with other client libraries. E.g AWS's Java SDK requires a ```oftware.amazon.awssdk.auth.credential.AwsCredentialsProvider```. This module does not provide a ```AwsCredentialsProvider```.

# Example Usage 

See [CredentialsProviderTest](src/test/java/org/finos/legend/engine/credentials/provider/CredentialsProviderTest.java)


# API Design / Implementation Choices  

### Identity API

An ```Identity``` represents an entity that has an identity :). An ```Identity``` carries one/more ```Credential```s as proof of its identity.

This API approximates the ```javax.security.auth.Subject``` API. We decided not to use the ```javax.secruity.auth.Subject``` API to give us more API design/implementation flexbility.

See Identity.java.

### Credential API

Every SDK (AWS S3, Google etc) has its own Java type system for credentials. So we have Legend types that wrap the native credentials. 

See org.finos.legend.engine.credentials.credential package.

### Credentials Provider vs Flow 
 
A provider provides a credential. A provider implements business logic for providing or creating a credential. 

A flow is similar to a provider except that the flow accepts an ```Identity``` as input. This is so that the flow can implement business logic based on the caller indentity. E.g exchange a Kerberos ticket for an OAuth token.

### Usage

See TestDirectUseOfCredentialProviders.

See TestDirectUseOfFlows.

### Lookup

While a provider or flow can be instantiated and used directly, in most cases, the provider/flow to be used depends on some runtime context.

For e.g during a relational plan execution, configuration in the plan might indicate that an OAuth credential for Snowflake is needed. In another case it might be OAuth but with say Google specific OAuth behavior.

A ```registry``` provides a way for looking up a flow given a runtime context. 

__Given that we cannot meaningfully abstract over the runtime context, this module does not provide a registry implementation. Clients of this module can build their custom registries.__ 

For e.g FlowRegistry2 uses database type and the caller's identity to resolve a credential.

### Configuration

TODO 

### Runtime discovery

TODO : How do we configure and load these flows dynamically ?

### Credential Creation Lifecycle

TODO : Lazy creation, credential refresh

### Legend API Backwards compatibility

TODO 


