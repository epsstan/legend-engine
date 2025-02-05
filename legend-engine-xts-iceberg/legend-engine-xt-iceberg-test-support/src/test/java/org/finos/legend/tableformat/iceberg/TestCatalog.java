package org.finos.legend.tableformat.iceberg;

import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionData;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.GenericParquetWriter;
import org.apache.iceberg.gcp.bigquery.BigQueryMetastoreCatalog;
import org.apache.iceberg.gcp.gcs.GCSFileIO;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.types.Types;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// This class does not compile out the box. This requires BigQuery jars not in Maven Central.
public class TestBigQuery
{
    @Test
    public void testBigQuery() throws Exception
    {
        Catalog catalog = this.initCatalog();
        Namespace namespace = this.listTables(catalog, "blmt_dataset");
        String tableName = "table_" + System.currentTimeMillis();
        Table table = this.createTable(catalog, namespace, tableName);
        this.addRowsToTable(table);
        this.readTable(table);
    }

    private Namespace listTables(Catalog catalog, String namespaceName)
    {
        Namespace namespace = Namespace.of(namespaceName);
        List<String> tableNames = catalog.listTables(namespace).stream().map(tableIdentifier -> tableIdentifier.namespace() + ":" + tableIdentifier.name()).collect(Collectors.toList());
        System.out.println(tableNames);
        return namespace;
    }

    private Catalog initCatalog()
    {
        MutableMap<String, String> properties = Maps.mutable.empty();
        properties.put(CatalogProperties.CATALOG_IMPL, BigQueryMetastoreCatalog.class.getCanonicalName());
        properties.put("gcp_project", "__GCP_PROJECT__NAME__");
        properties.put("gcp_location", "us");
        properties.put("warehouse", "__GCS_BUCKET_NAME__");
        properties.put(CatalogProperties.FILE_IO_IMPL, GCSFileIO.class.getCanonicalName());

        String adcEnvParam = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (adcEnvParam == null || adcEnvParam.trim().isEmpty())
        {
            throw new RuntimeException("GOOGLE_APPLICATION_CREDENTIALS env not set");
        }
        Catalog catalog = new BigQueryMetastoreCatalog();
        catalog.initialize("demo", properties);
        return catalog;
    }

    public Table createTable(Catalog catalog, Namespace namespace, String tableName)
    {
        TableIdentifier name = TableIdentifier.of(namespace, tableName);
        Schema schema = new Schema(
                Types.NestedField.required(1, "level", Types.StringType.get()),
                Types.NestedField.required(2, "event_time", Types.TimestampType.withZone()),
                Types.NestedField.required(3, "message", Types.StringType.get()),
                Types.NestedField.optional(4, "call_stack", Types.ListType.ofRequired(5, Types.StringType.get()))
        );
        PartitionSpec spec = PartitionSpec.builderFor(schema)
                .hour("event_time")
                .identity("level")
                .build();

        Table table = catalog.createTable(name, schema, spec);
        return table;
    }

    public void addRowsToTable(Table table) throws Exception
    {
        PartitionSpec partitionSpec = table.spec();
        Schema schema = table.schema();
        // TODO : How to set this partition data ?
        PartitionData partitionData = new PartitionData(partitionSpec.partitionType());
        partitionData.set(0, 12);
        partitionData.set(1, "info");

        GenericRecord record = GenericRecord.create(schema);
        GenericRecord record1 = record.copy(Maps.mutable.of(
                "level", "info",
                "event_time", OffsetDateTime.parse("2011-12-03T10:15:30+01:00"),
                "message", "log1",
                "call_stack", Collections.emptyList()));

        GenericRecord record2 = record.copy(Maps.mutable.of(
                "level", "debu",
                "event_time", OffsetDateTime.parse("2011-12-03T10:15:31+01:00"),
                "message", "log2",
                "call_stack", Collections.emptyList()));

        String filepath = table.location() + "/" + "rawfile-" + UUID.randomUUID().toString();
        OutputFile file = table.io().newOutputFile(filepath);
        DataWriter<GenericRecord> dataWriter =
                Parquet.writeData(file)
                        .schema(schema)
                        .createWriterFunc(GenericParquetWriter::buildWriter)
                        .withSpec(partitionSpec)
                        .withPartition(partitionData)
                        .overwrite()
                        .build();

        dataWriter.write(record1);
        dataWriter.write(record2);
        dataWriter.close();

        DataFile dataFile = dataWriter.toDataFile();
        table.newAppend().appendFile(dataFile).commit();
    }

    public void readTable(Table table) throws Exception
    {
        CloseableIterable<Record> records = IcebergGenerics.read(table).build();
        for (Record record : records)
        {
            System.out.println(record.toString());
        }
    }
}
