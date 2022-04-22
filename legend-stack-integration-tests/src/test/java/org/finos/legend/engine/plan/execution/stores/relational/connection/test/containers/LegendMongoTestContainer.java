package org.finos.legend.engine.plan.execution.stores.relational.connection.test.containers;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class LegendMongoTestContainer extends AbstractLegendTestContainer
{
    public static final String MONGO_ADMIN_USER = "admin";
    public static final String MONGO_ADMIN_PASSWORD = "admin";
    public static final int MONGO_LISTEN_PORT = 27017;
    public static final String MONGO_CONTAINER_SERVICE_NAME = "mongo";

    public static LegendMongoTestContainer build(Network network) throws Exception
    {
        return new LegendMongoTestContainer(network);
    }

    public LegendMongoTestContainer(Network network) throws Exception
    {
        super.testContainer = new GenericContainer(DockerImageName.parse(this.imageName()))
                // container config
                .withEnv("MONGO_INITDB_ROOT_USERNAME", MONGO_ADMIN_USER)
                .withEnv("MONGO_INITDB_ROOT_PASSWORD", MONGO_ADMIN_PASSWORD)
                .withEnv("MONGO_INITDB_DATABASE", "legend")
                // wait config
                .waitingFor(Wait.forLogMessage("(?i).*Waiting for connections*.*", 1))
                // network config
                .withAccessToHost(true)
                .withNetwork(network).withNetworkAliases(this.containerNetworkName())
                .withExposedPorts(MONGO_LISTEN_PORT);
    }

    @Override
    public String imageName() {
        return "mongo:latest";
    }

    @Override
    public String containerNetworkName() {
        return "mongo";
    }

    public String getHostAccessibleMongoUri()
    {
        return String.format("mongodb://%s:%s@%s:%d", MONGO_ADMIN_USER, MONGO_ADMIN_PASSWORD, super.testContainer.getHost(), super.testContainer.getMappedPort(MONGO_LISTEN_PORT));
    }

    public String getContainerNetworkAccessibleMongoUri()
    {
        return String.format("mongodb://%s:%s@%s:%d", MONGO_ADMIN_USER, MONGO_ADMIN_PASSWORD, MONGO_CONTAINER_SERVICE_NAME, MONGO_LISTEN_PORT);
    }

    @Override
    public void testBeforeUse() throws Exception
    {
        MongoClient mongoClient = new MongoClient(new MongoClientURI(this.getHostAccessibleMongoUri()));
        MongoDatabase database = mongoClient.getDatabase("legend");
        database.createCollection("test");
        MongoCollection<Document> collection = database.getCollection("tets");

        Document foobarDoc = new Document("foo", "bar");
        collection.insertOne(foobarDoc);

        FindIterable<Document> documents = collection.find(new Document("foo", "bar"));
        Document queryResult = documents.iterator().next();
    }
}
