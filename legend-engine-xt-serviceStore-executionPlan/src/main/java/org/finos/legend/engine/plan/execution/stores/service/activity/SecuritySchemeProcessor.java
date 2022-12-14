package org.finos.legend.engine.plan.execution.stores.service.activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import org.apache.commons.codec.binary.Base64;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.apiKey.ApiKeyAuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.auth.impl.usernamePassword.UsernamePasswordAuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.*;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.*;
import org.finos.legend.engine.shared.core.vault.Vault;
import org.pac4j.core.profile.CommonProfile;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecuritySchemeProcessor
{
    private HttpClientBuilder httpClientBuilder;
    private AuthenticationSpec authSpecification;
    private RequestBuilder requestBuilder;
    private MutableList<CommonProfile> profiles;

    public SecuritySchemeProcessor(AuthenticationSpec authSpecification, HttpClientBuilder httpClientBuilder, RequestBuilder requestBuilder, MutableList<CommonProfile> profiles)
    {
        this.authSpecification = authSpecification;
        this.httpClientBuilder = httpClientBuilder;
        this.requestBuilder = requestBuilder;
        this.profiles = profiles;
    }

    public Boolean visit(SecurityScheme securityScheme)
    {
        try
        {
            if (securityScheme instanceof SimpleHttpSecurityScheme)
           {
                UsernamePasswordAuthenticationSpec spec = (UsernamePasswordAuthenticationSpec) this.authSpecification;
                //String password = Vault.INSTANCE.getValue(((VaultCredential)spec.password).vaultReference);
                String password = "password";
                String encoding = Base64.encodeBase64String((spec.username+ ":" + password).getBytes());
                requestBuilder.addHeader("Authorization", "Basic " + encoding);
                return true;
            }
            else if (securityScheme instanceof OauthSecurityScheme)
            {
                OAuthAuthenticationSpec spec = (OAuthAuthenticationSpec) this.authSpecification;
                //TODO: get token of valid scopes
                String oauthToken = getOAuthToken(spec.credential.grantType,spec.credential.clientId,spec.credential.clientSecretVaultReference,spec.credential.authServerUrl);
                requestBuilder.addHeader("Authorization", "Bearer " + oauthToken);
                return true;
            }
            else if (securityScheme instanceof ApiKeySecurityScheme)
            {
                ApiKeySecurityScheme scheme = (ApiKeySecurityScheme) securityScheme;
                ApiKeyAuthenticationSpec spec = (ApiKeyAuthenticationSpec) this.authSpecification;
                if (scheme.location.equals("Header"))
                {
                    //TODO
                }
                else if (scheme.location.equals("Cookie"))
                {
                    //TODO
                }
                else if (scheme.location.equals("QueryParam"))
                {
                    //TODO: does this conflict with service parameters ?
                    //URI updatedUri = new URI(requestBuilder.getUri()+"?"+scheme.keyName+"="+spec.value);
                    //requestBuilder.setUri(updatedUri);
                }
                throw new RuntimeException(String.format("ApiKey location %s not supported",scheme.location));
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return null;
    }

    private String getOAuthToken(String grantType, String clientId, String clientSecretVaultReference, String authServerUrl) {

        if (grantType.equals("client_credentials"))
        {
            try
            {
                Map<String, List<String>> customParams = new HashMap<>();
                customParams.put("grant_type", Lists.mutable.of("client_credentials").toList());

                //TODO: resolve clientSecretVaultReference
                TokenRequest tokenRequest = new TokenRequest(new URI(authServerUrl),
                        new ClientSecretBasic(new ClientID(clientId), new Secret(clientSecretVaultReference)),
                        new ClientCredentialsGrant(), null, null, customParams);

                HTTPRequest request = tokenRequest.toHTTPRequest();
                request.setFollowRedirects(true);
                HTTPResponse response = request.send();
                String content = response.getContent();
                return (String) new ObjectMapper().readValue(content,Map.class).get("access_token");
            }
            catch(Exception e)
            {
                throw  new RuntimeException("Unable to obtain OAuth token");
            }
        }
         throw new RuntimeException(String.format("OAuth GrantType %s not supported",grantType));

    }
}
