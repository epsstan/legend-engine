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

package org.finos.legend.engine.plan.execution.stores.service;

import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.utility.ListIterate;
import org.finos.legend.engine.plan.execution.nodes.helpers.freemarker.FreeMarkerExecutor;
import org.finos.legend.engine.plan.execution.nodes.state.ExecutionState;
import org.finos.legend.engine.plan.execution.result.ConstantResult;
import org.finos.legend.engine.plan.execution.result.InputStreamResult;
import org.finos.legend.engine.plan.execution.result.Result;
import org.finos.legend.engine.plan.execution.result.StreamingResult;
import org.finos.legend.engine.plan.execution.result.serialization.SerializationFormat;
import org.finos.legend.engine.plan.execution.stores.service.activity.ServiceStoreExecutionActivity;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.RequestBodyDescription;
import org.finos.legend.engine.plan.execution.authentication.AuthenticationMethod;
import org.finos.legend.engine.plan.execution.authentication.IntermediationRule;
import org.finos.legend.engine.plan.execution.authentication.connection.HttpConnectionProvider;
import org.finos.legend.engine.plan.execution.authentication.connection.HttpConnectionSpec;
import org.finos.legend.engine.plan.execution.stores.service.auth.ServiceStoreAuthenticationSpec;
import org.finos.legend.engine.plan.execution.authentication.provider.AuthenticationMethodProvider;
import org.finos.legend.engine.plan.execution.authentication.provider.IntermediationRuleProvider;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.HttpMethod;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.Location;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SecurityScheme;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.ServiceParameter;
import org.finos.legend.engine.shared.core.identity.factory.IdentityFactoryProvider;
import org.pac4j.core.profile.CommonProfile;
import org.finos.legend.engine.plan.execution.authentication.AuthenticationMethod;
import org.finos.legend.engine.plan.execution.authentication.IntermediationRule;
import org.finos.legend.engine.plan.execution.authentication.connection.HttpConnectionProvider;
import org.finos.legend.engine.plan.execution.authentication.connection.HttpConnectionSpec;
import org.finos.legend.engine.plan.execution.stores.service.auth.ServiceStoreAuthenticationSpec;
import org.finos.legend.engine.plan.execution.authentication.provider.AuthenticationMethodProvider;
import org.finos.legend.engine.plan.execution.authentication.provider.IntermediationRuleProvider;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.shared.core.identity.factory.IdentityFactoryProvider;
import java.net.HttpURLConnection;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServiceExecutor
{
    public static InputStreamResult executeHttpService(String url, List<Header> headers, StringEntity requestBodyEntity, HttpMethod httpMethod, String mimeType, Map<String,SecurityScheme> securitySchemes, Map<String, AuthenticationSpec> authSpecs, MutableList<CommonProfile> profiles)
    {
        URI uri;
        try
        {
            URIBuilder uriBuilder = new URIBuilder(url);
            uri = uriBuilder.build();
        }
        catch (URISyntaxException e)
        {
            String errMsg = String.format("Cannot build URI from url (%s)", url);
            throw new RuntimeException(errMsg, e);
        }

        InputStream response = executeRequest(httpMethod, uri, headers, requestBodyEntity, mimeType, securitySchemes,authSpecs, profiles);
        return new InputStreamResult(response, org.eclipse.collections.api.factory.Lists.mutable.with(new ServiceStoreExecutionActivity(url)));
    }

    public static InputStream executeRequest(HttpMethod httpMethod, URI uri, List<Header> headers, StringEntity requestBodyDescription, String mimeType, Map<String,SecurityScheme> securitySchemes, Map<String, AuthenticationSpec> authSpecs, MutableList<CommonProfile> profiles)
    {
        Span span = GlobalTracer.get().activeSpan();

        try
        {
            FastList<AuthenticationMethod> allMethods = FastList.newList(ServiceLoader.load(AuthenticationMethod.class));
            FastList<IntermediationRule> allRules = FastList.newList(ServiceLoader.load(IntermediationRule.class));
            AuthenticationMethodProvider authenticationMethodProvider = new AuthenticationMethodProvider(allMethods,new IntermediationRuleProvider(allRules));
            HttpURLConnection connection = new HttpConnectionProvider(authenticationMethodProvider).makeConnection(new HttpConnectionSpec(uri,httpMethod.toString(),headers,requestBodyDescription,mimeType),new ServiceStoreAuthenticationSpec(securitySchemes,authSpecs), IdentityFactoryProvider.getInstance().makeIdentity(profiles));

            InputStream responseStream = connection.getInputStream();

            //TODO: FIX
//            ObjectMapper mapper = new ObjectMapper();
//            int statusCode = 200;// httpResponse.getStatusLine().getStatusCode();
//
//            if (span != null)
//            {
//                span.setTag("Status code", statusCode);
//            }
//
//            if (statusCode != HttpStatus.SC_OK)
//            {
//                String explanation = httpResponse.getEntity() == null ? "" : EntityUtils.toString(httpResponse.getEntity());
//
//                if (span != null)
//                {
//                    span.setTag("Failure message", explanation);
//                }
//                throw new RuntimeException("HTTP request [" + request.toString() + "] failed with error - " + explanation);
//            }
//
//            return httpResponse.getEntity().getContent();
            return responseStream;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String getProcessedUrl(String url, List<ServiceParameter> params, List<String> mappedParameters, ExecutionState state)
    public InputStreamResult executeHttpService(String url, List<ServiceParameter> params, RequestBodyDescription requestBodyDescription, HttpMethod httpMethod, String mimeType, List<SecurityScheme> securitySchemes, Map<String, AuthenticationSpec> authSpecs, ExecutionState state, MutableList<CommonProfile> profiles)
    public static InputStreamResult executeHttpService(String url, List<ServiceParameter> params, RequestBodyDescription requestBodyDescription, HttpMethod httpMethod, String mimeType, List<SecurityScheme> securitySchemes, Map<String, AuthenticationSpec> authSpecs, ExecutionState state, MutableList<CommonProfile> profiles)
    {
        Span span = GlobalTracer.get().activeSpan();

        List<ServiceParameter> pathParams = params == null ? Lists.mutable.empty() : ListIterate.select(params, param -> param.location == Location.PATH);
        List<ServiceParameter> queryParams = params == null ? Lists.mutable.empty() : ListIterate.select(params, param -> param.location == Location.QUERY);

        String urlProcessedWithPathParams = processUrlWithPathParams(url, pathParams, state);
        String urlProcessedWithQueryParams = processUrlWithQueryParams(urlProcessedWithPathParams, queryParams, mappedParameters, state);

        if (span != null)
        {
            span.setTag("processed url", urlProcessedWithQueryParams);
        }

        return urlProcessedWithQueryParams;
    }

    public static List<Header> getProcessedHeaders(List<ServiceParameter> params, List<String> mappedParameters, ExecutionState state)
    {
        List<ServiceParameter> headerParams = params == null ? Lists.mutable.empty() : ListIterate.select(params, param -> param.location == Location.HEADER);
        return processHeaderParams(headerParams, mappedParameters, state);
    }

    public static StringEntity getRequestBodyEntity(RequestBodyDescription requestBodyDescription, ExecutionState state)
    {
        StringEntity requestBodyEntity = null;
        if (requestBodyDescription != null)
        {
            String requestBody;
            try
            {
                StreamingResult streamingResult = (StreamingResult) state.getResult(requestBodyDescription.resultKey);
                requestBody = streamingResult.flush(streamingResult.getSerializer(SerializationFormat.RAW));
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error serializing requestBody value.\n" + e);
            }
            ContentType contentType = ContentType.create(requestBodyDescription.mimeType, StandardCharsets.UTF_8);
            requestBodyEntity = new StringEntity(requestBody, contentType);
        }
        return requestBodyEntity;
    }

    public static InputStream executeRequest(HttpMethod httpMethod, URI uri, List<Header> headers, StringEntity requestBodyDescription, String mimeType, List<SecurityScheme> securitySchemes, Map<String, AuthenticationSpec> authSpecs, MutableList<CommonProfile> profiles)
    {
        Span span = GlobalTracer.get().activeSpan();

        try
        {
            FastList<AuthenticationMethod> allMethods = FastList.newList(ServiceLoader.load(AuthenticationMethod.class));
            FastList<IntermediationRule> allRules = FastList.newList(ServiceLoader.load(IntermediationRule.class));
            AuthenticationMethodProvider authenticationMethodProvider = new AuthenticationMethodProvider(allMethods,new IntermediationRuleProvider(allRules));
            HttpURLConnection connection = new HttpConnectionProvider(authenticationMethodProvider).makeConnection(new HttpConnectionSpec(uri,httpMethod.toString(),headers,requestBodyDescription,mimeType),new ServiceStoreAuthenticationSpec(securitySchemes,authSpecs), IdentityFactoryProvider.getInstance().makeIdentity(profiles));

            InputStream responseStream = connection.getInputStream();

//            ObjectMapper mapper = new ObjectMapper();
//            int statusCode = 200;// httpResponse.getStatusLine().getStatusCode();
//
//            if (span != null)
//            {
//                span.setTag("Status code", statusCode);
//            }
//
//            if (statusCode != HttpStatus.SC_OK)
//            {
//                String explanation = httpResponse.getEntity() == null ? "" : EntityUtils.toString(httpResponse.getEntity());
//
//                if (span != null)
//                {
//                    span.setTag("Failure message", explanation);
//                }
//                throw new RuntimeException("HTTP request [" + request.toString() + "] failed with error - " + explanation);
//            }
//
//            return httpResponse.getEntity().getContent();
            return responseStream;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static String processUrlWithPathParams(String url, List<ServiceParameter> pathParams, ExecutionState state)
    {
        Map<String, String> pathVarValueMap = Maps.mutable.empty();
        for (ServiceParameter param : pathParams)
        {
            url = url.replace("{" + param.name + "}", "${.data_model[\"" + param.name + "\"]}");

            Result paramResult = state.getResult(param.name);
            if (paramResult == null)
            {
                throw new RuntimeException("No value found for parameter '" + param.name + "'");
            }
            if (!(paramResult instanceof ConstantResult))
            {
                throw new RuntimeException("Expected Constant Result for parameter '" + param.name + "'. Found : " + paramResult.getClass().getSimpleName());
            }

            pathVarValueMap.put(param.name, serializePathParameter(((ConstantResult) paramResult).getValue(), param));
        }

        return FreeMarkerExecutor.processRecursively(url, pathVarValueMap, "");
    }

    private static String processUrlWithQueryParams(String url, List<ServiceParameter> queryParams, ExecutionState state)
    {
        if (queryParams == null || queryParams.isEmpty())
        {
            return url;
        }
        return url + "?" + String.join("&", ListIterate.collectIf(queryParams, param -> (state.getResult(param.name) != null), param -> serializeQueryParameter(((ConstantResult) state.getResult(param.name)).getValue(), param)));
    }

    private static List<Header> processHeaderParams(List<ServiceParameter> headerParams, ExecutionState state)
    {
        if (headerParams == null || headerParams.isEmpty())
        {
            return Collections.emptyList();
        }
        return ListIterate.collectIf(headerParams, param -> (mappedParameters.contains(param.name) && state.getResult(param.name) != null), param -> new BasicHeader(param.name, serializeHeaderParameter(((ConstantResult) state.getResult(param.name)).getValue(), param)));
        return ListIterate.collectIf(headerParams, param -> (state.getResult(param.name) != null), param -> new BasicHeader(param.name, serializeHeaderParameter(((ConstantResult) state.getResult(param.name)).getValue(), param)));
    }



    private static String serializePathParameter(Object value, ServiceParameter parameter)
    {
        String result;
        if (!(value instanceof List))
        {
            result = value.toString();
        }
        else
        {
            String serializationFormat = parameter.serializationFormat.style + "_" + parameter.serializationFormat.explode;

            switch (serializationFormat)
            {
                case "simple_false":
                case "simple_true":
                    result = String.join(",", ListIterate.collect((List) value, Object::toString));
                    break;
                case "label_false":
                    result = "." + String.join(",", ListIterate.collect((List) value, Object::toString));
                    break;
                case "label_true":
                    result = "." + String.join(".", ListIterate.collect((List) value, Object::toString));
                    break;
                case "matrix_false":
                    result = ";" + parameter.name + "=" + String.join(",", ListIterate.collect((List) value, Object::toString));
                    break;
                case "matrix_true":
                    result = ";" + String.join(";", ListIterate.collect((List) value, val -> parameter.name + "=" + val.toString()));
                    break;
                default:
                    throw new RuntimeException("Serialization Format [style : " + parameter.serializationFormat.style + ", explode : " + parameter.serializationFormat.explode + "] not supported for path parameter");
            }
        }
        return encodeParameterValue(parameter.name, result, parameter.allowReserved);
    }

    private static String serializeQueryParameter(Object value, ServiceParameter parameter)
    {
        String result;
        if (!(value instanceof List))
        {
            result = parameter.name + "=" + encodeParameterValue(parameter.name, value.toString(), parameter.allowReserved);
        }
        else
        {
            String serializationFormat = parameter.serializationFormat.style + "_" + parameter.serializationFormat.explode;

            switch (serializationFormat)
            {
                case "form_false":
                    result = parameter.name + "=" + String.join(",", ListIterate.collect((List) value, val -> encodeParameterValue(parameter.name, val.toString(), parameter.allowReserved)));
                    break;
                case "spaceDelimited_false":
                    result = parameter.name + "=" + String.join("%20", ListIterate.collect((List) value, val -> encodeParameterValue(parameter.name, val.toString(), parameter.allowReserved)));
                    break;
                case "pipeDelimited_false":
                    result = parameter.name + "=" + String.join("|", ListIterate.collect((List) value, val -> encodeParameterValue(parameter.name, val.toString(), parameter.allowReserved)));
                    break;
                case "form_true":
                case "spaceDelimited_true":
                case "pipeDelimited_true":
                    String paramName = parameter.name;
                    result = String.join("&", ListIterate.collect((List) value, val -> paramName + "=" + encodeParameterValue(parameter.name, val.toString(), parameter.allowReserved)));
                    break;
                default:
                    throw new RuntimeException("Serialization Format [style : " + parameter.serializationFormat.style + ", explode : " + parameter.serializationFormat.explode + "] not supported for query parameter");
            }
        }
        return result;
    }

    private static String serializeHeaderParameter(Object value, ServiceParameter parameter)
    {
        String result;
        if (!(value instanceof List))
        {
            result = encodeParameterValue(parameter.name, value.toString(), parameter.allowReserved);
        }
        else
        {
            String serializationFormat = parameter.serializationFormat.style + "_" + parameter.serializationFormat.explode;

            switch (serializationFormat)
            {
                case "simple_false":
                case "simple_true":
                    result = String.join(",", ListIterate.collect((List) value, val -> encodeParameterValue(parameter.name, val.toString(), parameter.allowReserved)));
                    break;
                default:
                    throw new RuntimeException("Serialization Format [style : " + parameter.serializationFormat.style + ", explode : " + parameter.serializationFormat.explode + "] not supported for query parameter");
            }
        }
        return result;
    }

    private static String encodeParameterValue(String name, String value, Boolean allowReserved)
    {
        if (allowReserved != null && allowReserved)
        {
            return value;
        }
        else
        {
            try
            {
                return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException("Error encoding parameter : " + name + ". Found value - " + value);
            }
        }
    }
}
