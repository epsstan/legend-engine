package org.finos.legend.engine.connection;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.finos.legend.authentication.credentialprovider.CredentialProviderProvider;
import org.finos.legend.authentication.credentialprovider.impl.UserPasswordCredentialProvider;
import org.finos.legend.authentication.intermediationrule.impl.UserPasswordFromVaultRule;
import org.finos.legend.authentication.vault.CredentialVaultProvider;
import org.finos.legend.authentication.vault.PlatformCredentialVaultProvider;
import org.finos.legend.authentication.vault.impl.PropertiesFileCredentialVault;
import org.finos.legend.engine.connection.jdbc.LegendSparkJdbcConnectionProvider;
import org.finos.legend.engine.plan.execution.stores.relational.connection.postgres.test.PostgresTestContainerWrapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TestSparkIntegration
{
    private static PostgresTestContainerWrapper POSTGRES = null;
    private static Connection POSTGRES_CONNECTION;

    @BeforeClass
    public static void setup() throws Exception
    {
        Class.forName("org.postgresql.Driver");
        POSTGRES = PostgresTestContainerWrapper.build();
        POSTGRES.start();
        setupDatabaseObjects();
    }

    @AfterClass
    public static void cleanup()
    {
        if (POSTGRES == null)
        {
            return;
        }
        POSTGRES.stop();
    }

    public static void setupDatabaseObjects() throws Exception
    {
        POSTGRES_CONNECTION = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUser(),
                POSTGRES.getPassword()
        );

        Statement stmt = POSTGRES_CONNECTION.createStatement();
        MutableList<String> setupSqls = Lists.mutable.with(
                "create schema legend"
                    //"drop table if exists legend.PERSON;",
                    //"create table legend.PERSON(firstName VARCHAR(200), lastName VARCHAR(200));"
        );
        for (String sql : setupSqls)
        {
            stmt.executeUpdate(sql);
        }
        stmt.close();
    }

    // In main code, this should be driven off of application config
    public CredentialProviderProvider configureCredentialProvider()
    {
        Properties properties = new Properties();
        properties.put("myVaultReference", POSTGRES.getPassword());
        PropertiesFileCredentialVault propertiesFileCredentialVault = new PropertiesFileCredentialVault(properties);

        PlatformCredentialVaultProvider platformCredentialVaultProvider = PlatformCredentialVaultProvider.builder()
                .with(propertiesFileCredentialVault)
                .build();

        CredentialVaultProvider credentialVaultProvider = CredentialVaultProvider.builder()
                .with(platformCredentialVaultProvider)
                .build();

        UserPasswordCredentialProvider userPasswordCredentialProvider = new UserPasswordCredentialProvider(
                org.eclipse.collections.api.factory.Lists.mutable.with(
                        new UserPasswordFromVaultRule(credentialVaultProvider)
                )
        );
        CredentialProviderProvider credentialProviderProvider = CredentialProviderProvider.builder()
                .with(userPasswordCredentialProvider)
                .build();

        return credentialProviderProvider;
    }

    public void configureLegendSpark()
    {
        CredentialProviderProvider credentialProviderProvider = this.configureCredentialProvider();
        LegendSparkJdbcConnectionProvider.buildInstance(credentialProviderProvider);
    }

    @Test
    public void testSparkJdbc() throws ClassNotFoundException, SQLException
    {
        this.configureLegendSpark();

        // some glue code for Spark/hadoop on windows
        System.setProperty("hadoop.home.dir", "D:\\ephrim-sw\\winutils\\winutils-master\\hadoop-3.0.0\\");

        ImmutableList<Person> dataToWrite = Lists.immutable.of(
                new Person("jane", "doe"),
                new Person("john", "doe")
        );

        this.writeWithSpark(dataToWrite, Encoders.bean(Person.class));

        ImmutableList<Person> dataFromDatabase = this.readFromDatabase();

        assertEquals(dataToWrite.toSortedList(), dataFromDatabase.toSortedList());
    }

    private void writeWithSpark(ImmutableList<Person> dataToWrite, Encoder<Person> encoder)
    {
        SparkSession spark = SparkSession
                .builder()
                .appName("legend-spark-jdbc")
                .master("local[*]")
                .config("spark.sql.sources.disabledJdbcConnProviderList", "basic")
                .getOrCreate();


        Dataset<Person> dataset = spark.createDataset(dataToWrite.castToList(), encoder);

        dataset.write()
                .format("jdbc")
                // TODO - we should inject an instance of AuthenticationSpecification instead of individual params
                .option("username", "test")
                .option("passwordReference", "myVaultReference")
                // TODO - we should inject an instance of ConnectionSpecification
                //.option("url", "jdbc:postgresql:dbserver")
                .option("url", POSTGRES.getJdbcUrl())
                // TODO - we should inject an instance of a ConnectionProvider<T> and not a JdbcConnectionProvider
                .option("connectionProvider", "legend")
                .option("dbtable", "legend.PERSON")
                // TODO - inject identity
                .save();
    }

    public ImmutableList<Person> readFromDatabase() throws SQLException
    {
        MutableList<Person> persons = Lists.mutable.empty();
        Statement statement = POSTGRES_CONNECTION.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from legend.PERSON");
        while (resultSet.next())
        {
            String firstName = resultSet.getString(1);
            String lastName = resultSet.getString(2);
            persons.add(new Person(firstName, lastName));
        }
        resultSet.close();
        statement.close();

        return persons.toImmutable();
    }

    public static class Person implements Serializable, Comparable<Person>
    {
        private String firstName;
        private String lastName;

        public Person(String firstName, String lastName)
        {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName()
        {
            return firstName;
        }

        public void setFirstName(String firstName)
        {
            this.firstName = firstName;
        }

        public String getLastName()
        {
            return lastName;
        }

        public void setLastName(String lastName)
        {
            this.lastName = lastName;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return firstName.equals(person.firstName) && lastName.equals(person.lastName);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(firstName, lastName);
        }

        @Override
        public String toString()
        {
            return "Person{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }

        @Override
        public int compareTo(TestSparkIntegration.Person that)
        {
            String thisName = this.lastName + this.firstName;
            String thatName = that.lastName + that.firstName;
            return thisName.compareTo(thatName);
        }
    }
}
