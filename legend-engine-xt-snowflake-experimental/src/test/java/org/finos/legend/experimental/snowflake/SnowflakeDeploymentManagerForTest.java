package org.finos.legend.experimental.snowflake;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.SingleExecutionPlan;
import org.finos.legend.engine.shared.core.ObjectMapperFactory;

public class SnowflakeDeploymentManagerForTest extends SnowflakeDeploymentManager
{
    private static final ObjectMapper objectMapper = ObjectMapperFactory.getNewStandardObjectMapperWithPureProtocolExtensionSupports();
    private String planJSON;

    public SnowflakeDeploymentManagerForTest(String planJSON)
    {
        this.planJSON = planJSON;
    }

    @Override
    public SingleExecutionPlan generatePlan() throws Exception
    {
        return objectMapper.readValue(this.planJSON, SingleExecutionPlan.class);
    }
}
