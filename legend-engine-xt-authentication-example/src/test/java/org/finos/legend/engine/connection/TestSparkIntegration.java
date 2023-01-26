package org.finos.legend.engine.connection;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.Test;

import java.io.Serializable;

public class TestSparkIntegration
{
    @Test
    public void testSparkJdbc()
    {
        System.setProperty("hadoop.home.dir", "D:\\ephrim-sw\\winutils\\winutils-master\\hadoop-3.0.0\\");
        //System.setProperty("java.specification.version", "11");

        SparkSession spark = SparkSession
                .builder()
                .appName("spark-legend")
                .master("local[*]")
                .getOrCreate();

        Encoder<Person> personEncoder = Encoders.bean(Person.class);
        Dataset<Person> dataset = spark.createDataset(
                Lists.immutable.of(
                        new Person("jane", "doe")
                ).castToList(), personEncoder);

        dataset.write()
                .format("jdbc")
                .option("url", "jdbc:postgresql:dbserver")
                .option("connectionProvider", "legend");
    }

    public static class Person implements Serializable
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
    }
}
