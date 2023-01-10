package org.finos.legend.engine.plan.execution.stores.service.securitySchemes;

import org.finos.legend.engine.language.pure.grammar.from.PureGrammarParser;
import org.finos.legend.engine.plan.execution.stores.service.utils.ServiceStoreTestSuite;
import org.finos.legend.engine.plan.execution.stores.service.utils.ServiceStoreTestUtils;
import org.finos.legend.engine.protocol.pure.v1.model.context.PureModelContextData;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.SingleExecutionPlan;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.finos.legend.engine.plan.execution.stores.service.utils.ServiceStoreTestUtils.buildPlanForQuery;
import static org.finos.legend.engine.plan.execution.stores.service.utils.ServiceStoreTestUtils.executePlan;

public class TestBasicAuthWithServiceStore extends ServiceStoreTestSuite
{
    private static String pureGrammar;

    @BeforeClass
    public static void setup()
    {
        setupServer("securitySchemes");

        String serviceStore =
                "###ServiceStore\n" +
                        "ServiceStore meta::external::store::service::showcase::store::TradeProductServiceStore\n" +
                        "(\n" +
                        "   description : 'Showcase Service Store';\n" +
                        "   securitySchemes : {\n" +
                        "       http : Http\n" +
                        "               {\n" +
                        "                   scheme : 'basic';\n" +
                        "               },\n" +
                        "       api1 : ApiKey\n" +
                        "               {\n" +
                        "                   location : 'cookie';\n" +
                        "                   keyName : 'apiKey1';\n" +
                        "               },\n" +
                        "       api2 : ApiKey\n" +
                        "               {\n" +
                        "                   location : 'cookie';\n" +
                        "                   keyName : 'apiKey2';\n" +
                        "               },\n" +
                        "       oauth : Oauth\n" +
                        "               {\n" +
                        "                   scopes : ['read','openid'];\n" +
                        "               }\n" +
                        "   };\n" +
                        "   ServiceGroup TradeServices\n" +
                        "   (\n" +
                        "      path : '/trades';\n" +
                        "\n" +
                        "      Service AllTradeService\n" +
                        "      (\n" +
                        "         path : '/allTradesService';\n" +
                        "         method : GET;\n" +
                        "         security : [http];\n" +
                        "         response : [meta::external::store::service::showcase::domain::S_Trade <- meta::external::store::service::showcase::store::tradeServiceStoreSchemaBinding];\n" +
                        "      )\n" +
                        "   )\n" +
                        ")";

        pureGrammar = ServiceStoreTestUtils.readGrammarFromPureFile("/securitySchemes/testGrammar.pure").replace("port",String.valueOf(getPort())) + "\n\n" + serviceStore;
    }

    @Test
    public void testAuthentication()
    {
        SingleExecutionPlan plan = buildPlanForQuery(pureGrammar);
        String result = executePlan(plan);
        Assert.assertEquals("{\"builder\":{\"_type\":\"json\"},\"values\":[{\"s_tradeId\":\"1\",\"s_traderDetails\":\"abc:F_Name_1:L_Name_1\",\"s_tradeDetails\":\"30:100\"},{\"s_tradeId\":\"2\",\"s_traderDetails\":\"abc:F_Name_1:L_Name_1\",\"s_tradeDetails\":\"31:200\"},{\"s_tradeId\":\"3\",\"s_traderDetails\":\"abc:F_Name_2:L_Name_2\",\"s_tradeDetails\":\"30:300\"},{\"s_tradeId\":\"4\",\"s_traderDetails\":\"abc:F_Name_2:L_Name_2\",\"s_tradeDetails\":\"31:400\"}]}",result);
    }



}
