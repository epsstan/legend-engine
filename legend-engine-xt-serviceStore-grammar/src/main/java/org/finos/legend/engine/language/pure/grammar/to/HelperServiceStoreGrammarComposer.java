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

package org.finos.legend.engine.language.pure.grammar.to;

import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.list.mutable.ListAdapter;
import org.eclipse.collections.impl.tuple.Tuples;
import org.eclipse.collections.impl.utility.LazyIterate;
import org.eclipse.collections.impl.utility.ListIterate;
import org.eclipse.collections.impl.utility.MapIterate;
import org.finos.legend.engine.language.pure.compiler.toPureGraph.CompileContext;
import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;
import org.finos.legend.engine.protocol.pure.v1.model.context.EngineErrorType;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.specification.AuthenticationSpecification;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SecurityScheme;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.connection.ServiceStoreConnection;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.mapping.LocalMappingProperty;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.mapping.RootServiceStoreClassMapping;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.mapping.ServiceMapping;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.mapping.ServiceRequestBodyBuildInfo;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.mapping.ServiceRequestBuildInfo;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.mapping.ServiceRequestParameterBuildInfo;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.mapping.ServiceRequestParametersBuildInfo;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.*;
import org.finos.legend.engine.shared.core.operational.errorManagement.EngineException;
import org.finos.legend.pure.generated.Root_meta_external_store_service_metamodel_SecurityScheme;
import org.finos.legend.pure.generated.Root_meta_external_store_service_metamodel_ServiceStore;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.finos.legend.engine.language.pure.grammar.from.ServiceStoreParseTreeWalker.SERVICE_MAPPING_PATH_PREFIX;
import static org.finos.legend.engine.language.pure.grammar.to.PureGrammarComposerUtility.convertString;
import static org.finos.legend.engine.language.pure.grammar.to.PureGrammarComposerUtility.getTabString;
import static org.finos.legend.engine.language.pure.grammar.to.PureGrammarComposerUtility.unsupported;

public class HelperServiceStoreGrammarComposer
{
    public static String renderServiceStore(ServiceStore serviceStore)
    {
        int baseIndentation = 1;

        StringBuilder builder = new StringBuilder();
        builder.append("ServiceStore ").append(PureGrammarComposerUtility.convertPath(serviceStore.getPath())).append("\n(\n");

        if (serviceStore.description != null)
        {
            builder.append("description : ").append("'").append(serviceStore.description).append("'").append(";\n\n");
        }

        renderSecuritySchemes(serviceStore.securitySchemes, builder, PureGrammarComposerContext.Builder.newInstance().withIndentation(baseIndentation).build());
        renderServiceStoreElements(serviceStore.elements, builder, baseIndentation);

        builder.append(")");
        return builder.toString();
    }

    public static void renderSecuritySchemes(Map<String,SecurityScheme> securitySchemes, StringBuilder builder, PureGrammarComposerContext context)
    {
        if (securitySchemes!=null  && !securitySchemes.isEmpty())
        {
            builder.append(context.getIndentationString()).append(PureGrammarComposerUtility.getTabString(1)).append("securitySchemes ").append(": ").append("{\n").append(MapIterate.toListOfPairs(securitySchemes).collect(pair -> renderSecurityScheme(pair.getOne(),pair.getTwo(), context)).makeString(",\n")).append("\n").append(getTabString()).append("};\n");
        }
    }
    public static String renderAuthSpecs (ServiceStoreConnection serviceStoreConnection, PureGrammarComposerContext context)
    {
        if (serviceStoreConnection.authSpecs!=null)
        {
            return "\n" + context.getIndentationString() + getTabString() + "auth: {\n" +
                    serviceStoreConnection. authSpecs.entrySet().stream().map(entry
                            -> renderAuthenticationSpecification(entry.getKey(), entry.getValue(), 2))
                            .collect (Collectors. joining(",\n")) +
                          "\n" + context.getIndentationString() + getTabString() + "};";
        }
        return "";
    }

    private static void renderServiceStoreElements(List<ServiceStoreElement> elements, StringBuilder builder, int baseIndentation)
    {
        List<Service> serviceList = ListIterate.selectInstancesOf(elements, Service.class);
        List<ServiceGroup> serviceGroupList = ListIterate.selectInstancesOf(elements, ServiceGroup.class);

        ListIterate.forEach(serviceList, service -> renderService(service, builder, baseIndentation));
        ListIterate.forEach(serviceGroupList, serviceGroup -> renderServiceGroup(serviceGroup, builder, baseIndentation));
    }

    private static void renderServiceGroup(ServiceGroup serviceGroup, StringBuilder builder, int baseIndentation)
    {
        builder.append(getTabString(baseIndentation)).append("ServiceGroup ").append(serviceGroup.id).append("\n");
        builder.append(getTabString(baseIndentation)).append("(\n");
        builder.append(getTabString(baseIndentation + 1)).append("path : ").append("'").append(serviceGroup.path).append("'").append(";\n\n");
        renderServiceStoreElements(serviceGroup.elements, builder, baseIndentation + 1);
        builder.append(getTabString(baseIndentation)).append(")\n");
    }

    private static void renderService(Service service, StringBuilder builder, int baseIndentation)
    {
        builder.append(getTabString(baseIndentation)).append("Service ").append(service.id).append("\n");
        builder.append(getTabString(baseIndentation)).append("(\n");
        builder.append(getTabString(baseIndentation + 1)).append("path : ").append("'").append(service.path).append("'").append(";\n");
        if (service.requestBody != null)
        {
            builder.append(getTabString(baseIndentation + 1)).append("requestBody : ").append(renderTypeReference(service.requestBody)).append(";\n");
        }
        builder.append(getTabString(baseIndentation + 1)).append("method : ").append(service.method).append(";\n");
        if (service.parameters != null && !service.parameters.isEmpty())
        {
            builder.append(getTabString(baseIndentation + 1)).append("parameters :\n")
                    .append(getTabString(baseIndentation + 1)).append("(\n")
                    .append(String.join(",\n", ListIterate.collect(service.parameters, param -> renderServiceParameter(param, baseIndentation + 2))))
                    .append("\n")
                    .append(getTabString(baseIndentation + 1)).append(");\n");
        }
        builder.append(getTabString(baseIndentation + 1)).append("response : ").append(renderTypeReference(service.response)).append(";\n");
        builder.append(getTabString(baseIndentation + 1)).append("security : [")
                .append(String.join(",", ListIterate.collect(service.security, s -> renderSecurity(s,baseIndentation))))
                .append("];\n");

        builder.append(getTabString(baseIndentation)).append(")\n");
    }

    private static String renderServiceParameter(ServiceParameter param, int baseIndentation)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(getTabString(baseIndentation))
                .append(renderServiceParameterName(param.name))
                .append(" : ")
                .append(renderTypeReference(param.type))
                .append(" ( ")
                .append("location = ")
                .append(param.location.toString().toLowerCase());

        if (param.serializationFormat != null && param.serializationFormat.style != null)
        {
            builder.append(", ").append("style = ").append(param.serializationFormat.style);
        }

        if (param.serializationFormat != null && param.serializationFormat.explode != null)
        {
            builder.append(", ").append("explode = ").append(param.serializationFormat.explode);
        }

        if (param.enumeration != null)
        {
            builder.append(", ").append("enum = ").append(param.enumeration);
        }

        if (param.allowReserved != null)
        {
            builder.append(", ").append("allowReserved = ").append(param.allowReserved);
        }

        if (param.required != null)
        {
            builder.append(", ").append("required = ").append(param.required);
        }

        builder.append(" )");

        return builder.toString();
    }

    private static String renderTypeReference(TypeReference typeReference)
    {
        StringBuilder builder = new StringBuilder();

        if (typeReference.list)
        {
            builder.append("[");
        }

        if (typeReference instanceof BooleanTypeReference)
        {
            builder.append("Boolean");
        }
        else if (typeReference instanceof FloatTypeReference)
        {
            builder.append("Float");
        }
        else if (typeReference instanceof IntegerTypeReference)
        {
            builder.append("Integer");
        }
        else if (typeReference instanceof StringTypeReference)
        {
            builder.append("String");
        }
        else if (typeReference instanceof ComplexTypeReference)
        {
            builder.append(((ComplexTypeReference) typeReference).type).append(" <- ").append(((ComplexTypeReference) typeReference).binding);
        }
        else
        {
            throw new EngineException("ServiceStore Composer does not support " + typeReference.getClass().getSimpleName() + " Parameter Value Type", SourceInformation.getUnknownSourceInformation(), EngineErrorType.PARSER);
        }

        if (typeReference.list)
        {
            builder.append("]");
        }

        return builder.toString();
    }

    private static String renderSecurityScheme(String id, SecurityScheme securityScheme, PureGrammarComposerContext context)
    {
        List<Function2<Pair<String,SecurityScheme>, PureGrammarComposerContext,String>> processors = ListIterate.flatCollect(IServiceStoreGrammarComposerExtension.getExtensions(), ext -> ext.getExtraSecuritySchemesComposers());

        return ListIterate.collect(processors, processor -> processor.value(Tuples.pair(id, securityScheme),context))
                .select(Objects::nonNull)
                .getFirstOptional()
                .orElseThrow(() -> new EngineException("Unsupported securityScheme - " + securityScheme.getClass().getSimpleName(), securityScheme.sourceInformation,EngineErrorType.PARSER));
    }

    private static String renderAuthenticationSpecification(String id, AuthenticationSpecification authenticationSpec, int baseIndentation)
    {
        return "";
//        List<Function2<Pair<String,AuthenticationSpecification>,Integer,String>> processors = ListIterate.flatCollect(IAuthenticationGrammarComposerExtension.getExtensions(), ext -> ext.getExtraAuthenticationComposers());
//
//        return ListIterate.collect(processors, processor -> processor.value(Tuples.pair(id, authenticationSpec),baseIndentation))
//                .select(Objects::nonNull)
//                .getFirstOptional()
//                .orElseThrow(() -> new EngineException("Unsupported authenticationSpec corresponding to securityScheme - " + id,authenticationSpec.sourceInformation,EngineErrorType.PARSER));
    }

    private static String renderSecurity(SecurityScheme securityScheme, int baseIndentation)
    {
        if (securityScheme instanceof IdentifiedSecurityScheme)
        {
            String id = ((IdentifiedSecurityScheme) securityScheme).id;
            if (id.contains("::"))
            {
               return "(" + ListAdapter.adapt(Arrays.asList(id.split("::"))).makeString(" && ") + ")";
            }

            return id;
        }

        return unsupported(securityScheme.getClass(), "Security Scheme type");

    }

    // -------------------------------------- CLASS MAPPING --------------------------------------

    public static void visitRootServiceClassMappingContents(RootServiceStoreClassMapping rootServiceStoreClassMapping, StringBuilder builder)
    {
        int baseIndentation = 2;
        ListIterate.forEach(rootServiceStoreClassMapping.localMappingProperties, prop -> visitLocalMappingProperty(prop, builder, baseIndentation));
        if (rootServiceStoreClassMapping.localMappingProperties != null && !rootServiceStoreClassMapping.localMappingProperties.isEmpty())
        {
            builder.append("\n");
        }
        ListIterate.forEach(rootServiceStoreClassMapping.servicesMapping, sm -> visitServiceMapping(sm, builder, baseIndentation));
    }

    private static void visitLocalMappingProperty(LocalMappingProperty localMappingProperty, StringBuilder builder, int baseIndentation)
    {
        builder.append(getTabString(baseIndentation))
                .append("+")
                .append(PureGrammarComposerUtility.convertIdentifier(localMappingProperty.name))
                .append(" : ")
                .append(localMappingProperty.type).append("[").append(HelperDomainGrammarComposer.renderMultiplicity(localMappingProperty.multiplicity)).append("]")
                .append(";\n");
    }

    private static void visitServiceMapping(ServiceMapping serviceMapping, StringBuilder builder, int baseIndentation)
    {
        builder.append(getTabString(baseIndentation)).append("~service ").append(renderServicePtr(serviceMapping.service)).append("\n");

        if ((serviceMapping.pathOffset != null && !(serviceMapping.pathOffset.path.isEmpty())) || (serviceMapping.requestBuildInfo != null))
        {
            builder.append(getTabString(baseIndentation)).append("(\n");

            if (serviceMapping.pathOffset != null && !(serviceMapping.pathOffset.path.isEmpty()))
            {
                builder.append(getTabString(baseIndentation + 1)).append("~path " + SERVICE_MAPPING_PATH_PREFIX + ".")
                        .append(ListAdapter.adapt(serviceMapping.pathOffset.path).collect(p -> HelperValueSpecificationGrammarComposer.renderPathElement(p, DEPRECATED_PureGrammarComposerCore.Builder.newInstance().build())).makeString("."))
                        .append("\n");
            }

            if (serviceMapping.requestBuildInfo != null)
            {
                visitServiceRequestBuildInfo(serviceMapping.requestBuildInfo, builder, baseIndentation + 1);
            }

            builder.append(getTabString(baseIndentation)).append(")\n");
        }
    }

    private static void visitServiceRequestBuildInfo(ServiceRequestBuildInfo requestBuildInfo, StringBuilder builder, int baseIndentation)
    {
        builder.append(getTabString(baseIndentation)).append("~request\n")
                .append(getTabString(baseIndentation)).append("(\n");

        if (requestBuildInfo.requestParametersBuildInfo != null)
        {
            visitServiceRequestParametersBuildInfo(requestBuildInfo.requestParametersBuildInfo, builder, baseIndentation + 1);
        }

        if (requestBuildInfo.requestBodyBuildInfo != null)
        {
            visitServiceRequestBodyBuildInfo(requestBuildInfo.requestBodyBuildInfo, builder, baseIndentation + 1);
        }

        builder.append(getTabString(baseIndentation)).append(")\n");
    }

    private static void visitServiceRequestParametersBuildInfo(ServiceRequestParametersBuildInfo requestParametersBuildInfo, StringBuilder builder, int baseIndentation)
    {
        builder.append(getTabString(baseIndentation)).append("parameters\n")
                .append(getTabString(baseIndentation)).append("(\n");

        builder.append(String.join(",\n", ListIterate.collect(requestParametersBuildInfo.parameterBuildInfoList, paramBuildInfo -> composeServiceRequestParameterBuildInfo(paramBuildInfo, baseIndentation + 1))));
        builder.append("\n");

        builder.append(getTabString(baseIndentation)).append(")\n");
    }

    private static String composeServiceRequestParameterBuildInfo(ServiceRequestParameterBuildInfo requestParameterBuildInfo, int baseIndentation)
    {
        return getTabString(baseIndentation) + renderServiceParameterName(requestParameterBuildInfo.serviceParameter) + " = " + requestParameterBuildInfo.transform.accept(DEPRECATED_PureGrammarComposerCore.Builder.newInstance().build()).replaceFirst("\\|", "");
    }

    private static void visitServiceRequestBodyBuildInfo(ServiceRequestBodyBuildInfo requestBodyBuildInfo, StringBuilder builder, int baseIndentation)
    {
        builder.append(getTabString(baseIndentation)).append("body = ")
                .append(requestBodyBuildInfo.transform.accept(DEPRECATED_PureGrammarComposerCore.Builder.newInstance().build()).replaceFirst("\\|", ""))
                .append("\n");
    }

    private static String renderServiceParameterName(String serviceParameter)
    {
        return PureGrammarComposerUtility.convertIdentifier(serviceParameter, true);
    }

    private static String renderServicePtr(ServicePtr servicePtr)
    {
        return "[" + servicePtr.serviceStore + "] " + renderServicePath(servicePtr);
    }

    public static String renderServicePath(ServicePtr servicePtr)
    {
        return (servicePtr.parent == null ? "" : renderServiceGroupPath(servicePtr.parent) + ".") + servicePtr.service;
    }

    public static String renderServiceGroupPath(ServiceGroupPtr serviceGroupPtr)
    {
        if (serviceGroupPtr.parent == null)
        {
            return serviceGroupPtr.serviceGroup;
        }
        else
        {
            return renderServiceGroupPath(serviceGroupPtr.parent) + "." + serviceGroupPtr.serviceGroup;
        }
    }
}
