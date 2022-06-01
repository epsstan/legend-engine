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

### Credentials Provider Flow 
 
A flow abstracts the business logic of providing or creating a credential.

An ```AuthenticatedCredentialsProviderFlow``` uses the identity of the caller to create a credential. Therefore, the API accepts ```Identity``` as an input.

In many cases, creating a credential requires runtime input. For e.g an OAuth scope the value for which is obtained from configuration in a Legend execution plan. Therefore, the API accepts a POJO.
```
public interface AuthenticatedCredentialsProviderFlow
{
    
    Supplier<OutboundCredential> makeCredential(Identity identity, CredentialRequestParams requestParams) throws Exception;
}
```

### Credentials Provider Flow - Usage

A flow can be directly instantiated and invoked. 

```java
@Test
    public void kerberosToOAuth() throws Exception {

        KerberosToOAuthCredentialFlow flow = new KerberosToOAuthCredentialFlow(ImmutableKerberosToOAuthCredentialFlow.ConfigurationParams.builder()
                .build());

        Supplier<LegendOAuthCredential> supplier =
                flow.makeCredential(
                        fakeKerberosIdentity,
                        ImmutableLegendOAuthCredential.CredentialRequestParams.builder().oauthScopes("scope1").build()
                );
        assertEquals("fake-token-fred@EXAMPLE.COM-[scope1]", supplier.get().getAccessToken());
    }
```

See TestDirectUseOfAuthenticatedFlows.java

### Credentials Provider Flow - Intermediation 

We have use cases where the caller does not have a credential that can be used directly with a target authentication system. E.g The caller has a Kerberos credential but is trying to connect to a database that supports OAuth. 

In some of these use cases, the platform can bridge this cap by exchanging the caller's credential for one that is compatible with the target.

A flow is how we implement this bridge. 

### Credentials Provider Flow - Lookup

While a flow can be instantiated and used directly, in most cases, the flow to be used depends on some runtime context.

For e.g during a relational plan execution, configuration in the plan might indicate that an OAuth credential for Snowflake is needed. In another case it might be OAuth but with say Google specific OAuth behavior.

A ```registry``` provides a way for looking up a flow given a runtime context. 

__Given that we cannot meaningfully abstract over the runtime context, this module does not provide a registry implementation. Clients of this module can build their custom registries.__ 

For e.g FlowRegistry2 uses database type and the caller's identity to resolve a credential.

### Credentials Provider Flow - Configuration

TODO 

### Credentials Provider Flow - Runtime discovery

TODO : How do we configure and load these flows dynamically ?

### Credential Creation Lifecycle

TODO : Lazy creation, credential refresh

### Legend API Backwards compatibility

TODO 


