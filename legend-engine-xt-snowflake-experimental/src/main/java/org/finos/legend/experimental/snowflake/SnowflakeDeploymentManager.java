package org.finos.legend.experimental.snowflake;

import org.eclipse.collections.api.RichIterable;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.SingleExecutionPlan;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.ExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.RelationalTdsInstantiationExecutionNode;
import org.finos.legend.engine.protocol.pure.v1.model.executionPlan.nodes.SQLExecutionNode;

import java.util.HashMap;
import java.util.Map;

public class SnowflakeDeploymentManager
{
    public DeploymentArtifacts generateDeploymentArtifactsForService() throws Exception
    {
        DeploymentArtifacts deploymentArtifacts = new DeploymentArtifacts();
        SingleExecutionPlan singleExecutionPlan = this.generatePlan();
        this.generateArtifacts(singleExecutionPlan, deploymentArtifacts);
        return deploymentArtifacts;
    }

    public SingleExecutionPlan generatePlan() throws Exception
    {
        // TODO
        return null;
    }

    public void generateArtifacts(SingleExecutionPlan singleExecutionPlan, DeploymentArtifacts deploymentArtifacts)
    {
        ExecutionNode rootExecutionNode = singleExecutionPlan.rootExecutionNode;
        RichIterable<ExecutionNode> executionNodes = rootExecutionNode.executionNodes();
        RichIterable<ExecutionNode> tdsNodes = executionNodes.select(node -> node instanceof RelationalTdsInstantiationExecutionNode);
        RelationalTdsInstantiationExecutionNode firstTdsNode = (RelationalTdsInstantiationExecutionNode)tdsNodes.getFirst();

        RichIterable<ExecutionNode> tdsExecutionNodes = firstTdsNode.executionNodes();
        RichIterable<ExecutionNode> sqlExecutionNodes = tdsExecutionNodes.select(node -> node instanceof SQLExecutionNode);
        SQLExecutionNode firstSQLExecutionNode = (SQLExecutionNode) sqlExecutionNodes.getFirst();

        deploymentArtifacts.addRawSQL(firstSQLExecutionNode.sqlQuery);

        deploymentArtifacts.addFoo("foo");
        deploymentArtifacts.addBar("bar");
    }

    public DeploymentResult deployService() throws Exception
    {
        DeploymentArtifacts deploymentArtifacts = this.generateDeploymentArtifactsForService();
        DeploymentResult deploymentResult = this.deploy(deploymentArtifacts);
        return deploymentResult;
    }

    public DeploymentResult deploy(DeploymentArtifacts deploymentArtifacts)
    {
        return new DeploymentResult();
    }

    public static class DeploymentArtifacts
    {
        public Map<String, String> artifacts = new HashMap<>();

        public DeploymentArtifacts()
        {
            // Jackson
        }

        public Map<String, String> getArtifacts()
        {
            return artifacts;
        }

        public void setArtifacts(Map<String, String> artifacts)
        {
            this.artifacts = artifacts;
        }

        public void addRawSQL(String rawSQL)
        {
            this.artifacts.put("RAW_SQL", rawSQL);
        }

        public void addFoo(String foo)
        {
            this.artifacts.put("FOO", foo);
        }

        public void addBar(String bar)
        {
            this.artifacts.put("BAR", bar);
        }
    }

    public static class DeploymentResult
    {
        public Map<String, String> keyValues = new HashMap<>();
    }
}
