package org.finos.legend.engine.plan.execution.stores.service.auth;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.finos.legend.engine.plan.execution.authentication.AuthenticationMethod;
import org.finos.legend.engine.plan.execution.authentication.IntermediationRule;
import org.finos.legend.engine.plan.execution.authentication.provider.AuthenticationMethodProvider;
import org.finos.legend.engine.plan.execution.authentication.provider.IntermediationRuleProvider;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.AuthenticationSpec;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.model.SecurityScheme;
import org.finos.legend.engine.shared.core.identity.Credential;
import org.finos.legend.engine.shared.core.identity.Identity;

import java.util.Map;
import java.util.ServiceLoader;

public class ServiceStoreAuthenticationMethod extends AuthenticationMethod<ServiceStoreAuthenticationSpec, Credential>
{
    @Override
    public Credential makeCredential(ServiceStoreAuthenticationSpec serviceStoreAuthenticationSpec, Identity identity) throws Exception
    {
        if (!serviceStoreAuthenticationSpec.securitySchemes.isEmpty())
        {
            //TODO: Process all the security schemes
            Map.Entry<String,SecurityScheme> s = serviceStoreAuthenticationSpec.securitySchemes.entrySet().iterator().next();
            String id = s.getKey();
            SecurityScheme scheme = s.getValue();
            return processSecurityScheme(scheme,serviceStoreAuthenticationSpec.authSpecs.get(id),identity);
        }

        return null;
    }

    private Credential processSecurityScheme(SecurityScheme securityScheme, AuthenticationSpec authenticationSpec, Identity identity) throws Exception
    {
        FastList<AuthenticationMethod> allMethods = FastList.newList(ServiceLoader.load(AuthenticationMethod.class));
        FastList<IntermediationRule> allRules = FastList.newList(ServiceLoader.load(IntermediationRule.class));
        AuthenticationMethodProvider authenticationMethodProvider = new AuthenticationMethodProvider(allMethods,new IntermediationRuleProvider(allRules));

        FastList<AuthenticationMethod> supportedAuthenticationMethods = authenticationMethodProvider.getSupportedMethodFor(authenticationSpec.getClass());
        AuthenticationMethod chosenAuthenticationMethod = supportedAuthenticationMethods.get(0);

        return chosenAuthenticationMethod.makeCredential(authenticationSpec,identity);
    }
}
