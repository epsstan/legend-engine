// Copyright 2020 Goldman Sachs
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

package org.finos.legend.engine.plan.execution.stores.relational.connection.tests.api.dynamicTestConnections;

import org.finos.legend.engine.plan.execution.stores.relational.AlloyH2Server;
import org.finos.legend.engine.plan.execution.stores.relational.connection.tests.api.DynamicTestConnection;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.relational.connection.DatabaseType;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.relational.connection.RelationalDatabaseConnection;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.relational.connection.authentication.DefaultH2AuthenticationStrategy;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.relational.connection.specification.StaticDatasourceSpecification;
import org.finos.legend.engine.shared.core.port.DynamicPortGenerator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class H2AlloyServer implements DynamicTestConnection {
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.H2;
    }

    private org.h2.tools.Server h2Server;

    @Override
    public void setup() {
        long start = System.currentTimeMillis();
        System.out.println("Starting setup of dynamic connection for database: H2 ");

        int relationalDBPort = DynamicPortGenerator.generatePort();
        relationalDBPort = 9099;
        try {
            h2Server = AlloyH2Server.startServer(relationalDBPort);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();

        try {
            setupTestData(relationalDBPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Completed setup of dynamic connection for database: H2 on port:" + relationalDBPort + " , time taken(ms):" + (end - start));
    }

    public void setupTestData(int relationalDBPort) throws Exception {
        Class.forName("org.h2.Driver");
        String url = "jdbc:h2:tcp://127.0.0.1:" + relationalDBPort +"/mem:testDB";
        Connection conn = DriverManager.getConnection(url, "sa", "");
        setupData(conn);
    }

    public void executeInDb(String sql, Connection connection) throws Exception {
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }

    public void setupData(Connection connection) throws Exception
    {
        executeInDb("Drop table if exists PersonNameParameter;", connection);
        executeInDb("Create Table PersonNameParameter(id INT, lastNameFirst VARCHAR(200), title VARCHAR(200));", connection);
        executeInDb("insert into PersonNameParameter (id, lastNameFirst, title) values (1, true, 'eee');", connection);

        //meta::relational::tests::createPersonTableAndFillDb (connection);

        /*executeInDb("Drop table if exists tableWithQuotedColumns;", connection);
        executeInDb("Create Table tableWithQuotedColumns(ID INT, \"FIRST NAME\" VARCHAR(200), \"LAST NAME\" VARCHAR(200), \"1columnStartsWithNumber\" VARCHAR(200));", connection);
        executeInDb("insert into tableWithQuotedColumns (ID, \"FIRST NAME\", \"LAST NAME\", \"1columnStartsWithNumber\") values (1, \"Peter\", \"Smith\", \"value1\");", connection);
        executeInDb("insert into tableWithQuotedColumns (ID, \"FIRST NAME\", \"LAST NAME\", \"1columnStartsWithNumber\") values (2, \"John\", \"Johnson\", \"value2\");", connection);
        executeInDb("insert into tableWithQuotedColumns (ID, \"FIRST NAME\", \"LAST NAME\", \"1columnStartsWithNumber\") values (3, \"John\", \"Hill\", \"value3\");", connection);
        executeInDb("insert into tableWithQuotedColumns (ID, \"FIRST NAME\", \"LAST NAME\", \"1columnStartsWithNumber\") values (4, \"Anthony\", \"Allen\", \"value4\");", connection);

        executeInDb("Drop table if exists PersonTableExtension;", connection);
        executeInDb("Create Table PersonTableExtension(id INT, firstName VARCHAR(200), lastName VARCHAR(200), age INT, addressId INT, firmId INT, managerId INT, birthDate DATE);", connection);
        executeInDb("insert into PersonTableExtension (id, firstName, lastName, age, addressId, firmId, managerId, birthDate) values (1, \"Peter\", \"Smith\",23, 1,1,2,\"2013-12-01\");", connection);
        executeInDb("insert into PersonTableExtension (id, firstName, lastName, age, addressId, firmId, managerId, birthDate) values (2, \"John\", \"Johnson\",22, 2,1,4,\"2013-12-02\");", connection);
        executeInDb("insert into PersonTableExtension (id, firstName, lastName, age, addressId, firmId, managerId, birthDate) values (3, \"John\", \"Hill\",12, 3,1,2,\"2013-12-03\");", connection);
        executeInDb("insert into PersonTableExtension (id, firstName, lastName, age, addressId, firmId, managerId, birthDate) values (4, \"Anthony\", \"Allen\",22, 4,1,null,\"2013-12-04\");", connection);
        executeInDb("insert into PersonTableExtension (id, firstName, lastName, age, addressId, firmId, managerId, birthDate) values (5, \"Fabrice\", \"Roberts\",34, 5,2,null,\"2013-12-01\");", connection);
        executeInDb("insert into PersonTableExtension (id, firstName, lastName, age, addressId, firmId, managerId, birthDate) values (6, \"Oliver\", \"Hill\",32, 6,2,null,\"2013-12-02\");", connection);
        executeInDb("insert into PersonTableExtension (id, firstName, lastName, age, addressId, firmId, managerId, birthDate) values (7, \"David\", \"Harris\",35, 7,3,null,\"2013-12-01\");", connection);

        //meta::relational::functions::toDDL::dropAndCreateTableInDb(db, "differentPersonTable", connection);

        executeInDb("Drop table if exists InteractionTable;", connection);
        executeInDb("Create Table InteractionTable(id VARCHAR(200), sourceId INT, targetId INT, time INT, active VARCHAR(1));", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (1, 1, 2, 4, \"Y\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (2, 1, 2, 6, \"N\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (2, 1, 3, 12, \"N\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (2, 1, 4, 14, \"Y\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (3, 4, 5, 3, \"N\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (3, 4, 6, 23, \"Y\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (4, 3, 6, 11, \"N\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (5, 3, 7, 33, \"Y\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (6, 4, 1, 44, \"Y\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (6, 4, 3, 55, \"N\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (6, 5, 4, 22, \"Y\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (6, 5, 6, 33, \"Y\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (7, 4, 1, 14, \"N\");", connection);
        executeInDb("insert into InteractionTable (id, sourceId, targetId, time, active) values (7, 4, 2, 11, \"Y\");", connection);

        //meta::relational::tests::createFirmTableAndFillDb (connection);

        executeInDb("Drop table if exists firmExtensionTable;", connection);
        executeInDb("create Table firmExtensionTable (firmId INT, legalName VARCHAR(200), establishedDate DATE)", connection);
        executeInDb("insert into firmExtensionTable(firmId, legalName, establishedDate) values(1,\"FirmA\",\"2013-12-01\")", connection);
        executeInDb("insert into firmExtensionTable(firmId, legalName, establishedDate) values(2,\"FirmB\",\"2013-12-01\")", connection);
        executeInDb("insert into firmExtensionTable(firmId, legalName, establishedDate) values(3,\"FirmC\",\"2013-12-02\")", connection);

        //meta::relational::functions::toDDL::dropAndCreateTableInDb(db, "otherFirmTable", connection);
        //meta::relational::functions::toDDL::dropAndCreateTableInDb(db, "addressTable", connection);

        executeInDb("insert into addressTable (id, type, name, street, comments) values (1,1,\"Hoboken\", null, \"A comment with a % in the middle\");", connection);
        executeInDb("insert into addressTable (id, type, name, street, comments) values (2,1,\"New York\", null, \"A comment with a _ in the middle\");", connection);
        executeInDb("insert into addressTable (id, type, name, street) values (3,1,\"New York\", null);", connection);
        executeInDb("insert into addressTable (id, type, name, street) values (4,1,\"New York\", null);", connection);
        executeInDb("insert into addressTable (id, type, name, street) values (5,1,\"San Fransisco\", null);", connection);
        executeInDb("insert into addressTable (id, type, name, street) values (6,1,\"Hong Kong\", null);", connection);
        executeInDb("insert into addressTable (id, type, name, street) values (7,1,\"New York\", null);", connection);
        executeInDb("insert into addressTable (id, type, name, street) values (8,1,\"New York\", \"West Street\");", connection);
        executeInDb("insert into addressTable (id, type, name, street) values (9,1,\"Cupertino\", \"Infinite Loop\");", connection);
        executeInDb("insert into addressTable (id, type, name, street) values (10,1,\"Tokyo\", null);", connection);
        executeInDb("insert into addressTable (id, type, name, street) values (11,1,\"Mountain View\", null);", connection);

        executeInDb("Drop table if exists LocationTable;", connection);
        executeInDb("Create Table LocationTable(id INT, personId INT, place VARCHAR(200),date DATE);", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (1, 1,\"New York\",\"2014-12-01\");", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (2, 1,\"Hoboken\",\"2014-12-01\");", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (3, 2,\"New York\",\"2014-12-01\");", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (4, 2,\"Hampton\",\"2014-12-01\");", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (5, 3,\"New York\",\"2014-12-01\");", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (6, 3,\"Jersey City\",\"2014-12-01\");", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (7, 4,\"New York\",\"2014-12-01\");", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (8, 4,\"Jersey City\",\"2014-12-01\");", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (9, 5,\"San Fransisco\",\"2014-12-01\");", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (10, 5,\"Paris\",\"2014-12-01\");", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (11, 6,\"Hong Kong\",\"2014-12-01\");", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (12, 6,\"London\",\"2014-12-01\");", connection);
        executeInDb("insert into LocationTable (id, personId, place, date) values (13, 7,\"New York\",\"2014-12-01\");", connection);


        executeInDb("Drop table if exists placeOfInterestTable;", connection);
        executeInDb("Create Table placeOfInterestTable(id INT, locationId INT, name VARCHAR(200));", connection);
        executeInDb("insert into  placeOfInterestTable (id, locationId, name) values (1, 1,\"Statue of Liberty\");", connection);
        executeInDb("insert into  placeOfInterestTable (id, locationId, name) values (2, 1,\"Columbus Park\");", connection);
        executeInDb("insert into  placeOfInterestTable (id, locationId, name) values (3, 2,\"Broadway\");", connection);
        executeInDb("insert into  placeOfInterestTable (id, locationId, name) values (4, 2,\"Hoboken City Hall\");", connection);
        executeInDb("insert into  placeOfInterestTable (id, locationId, name) values (5, 3,\"Empire State Building\");", connection);

        //meta::relational::tests::createProductSchemaTablesAndFillDb (connection);

        executeInDb("Drop table if exists accountTable;", connection);
        executeInDb("Create Table accountTable(ID INT, name VARCHAR(200), createDate DATE);", connection);
        executeInDb("insert into accountTable (ID, name, createDate) values (1, \"Account 1\", \"2013-12-01\");", connection);
        executeInDb("insert into accountTable (ID, name, createDate) values (2, \"Account 2\", \"2013-12-02\");", connection);

        executeInDb("Drop table if exists orderTable;", connection);
        executeInDb("Create Table orderTable(id INT, prodid INT, accountId INT, quantity FLOAT, orderDate DATE, settlementDateTime TIMESTAMP);", connection);
        executeInDb("insert into orderTable (id, prodid, accountId, quantity, orderDate, settlementDateTime) values (1, 1, 1, 25, \"2014-12-01\", \"2014-12-02 21:00:00\");", connection);
        executeInDb("insert into orderTable (id, prodid, accountId, quantity, orderDate, settlementDateTime) values (2, 1, 2, 320, \"2014-12-01\", \"2014-12-02 21:00:00\");", connection);
        executeInDb("insert into orderTable (id, prodid, accountId, quantity, orderDate, settlementDateTime) values (3, 2, 1, 11, \"2014-12-01\", \"2014-12-02 21:00:00\");", connection);
        executeInDb("insert into orderTable (id, prodid, accountId, quantity, orderDate, settlementDateTime) values (4, 1, 2, 300, \"2014-12-02\", \"2014-12-03 21:00:00\");", connection);

        //meta::relational::tests::createTradeTableAndFillDb (connection);

        executeInDb("Drop table if exists orderPnlTable;", connection);
        executeInDb("Create Table orderPnlTable(ORDER_ID INT, pnl FLOAT);", connection);
        executeInDb("insert into orderPnlTable (ORDER_ID, pnl) values (1, 100);", connection);
        executeInDb("insert into orderPnlTable (ORDER_ID, pnl) values (2, 200);", connection);
        executeInDb("insert into orderPnlTable (ORDER_ID, pnl) values (3, 0);", connection);
        executeInDb("insert into orderPnlTable (ORDER_ID, pnl) values (4, 150);", connection);

        executeInDb("Drop table if exists salesPersonTable;", connection);
        executeInDb("Create Table salesPersonTable(PERSON_ID INT, ACCOUNT_ID INT, NAME VARCHAR(200));", connection);
        executeInDb("insert into salesPersonTable (person_id, account_id, name) values (1, 1, \"Peter Smith\");", connection);
        executeInDb("insert into salesPersonTable (person_id, account_id, name) values (2, 2, \"John Johnson\");", connection);

        executeInDb("Drop table if exists tradeEventTable;", connection);
        executeInDb("Create Table tradeEventTable(event_id INT, trade_id INT, eventType VARCHAR(10), eventDate DATE, person_id INT);", connection);
        executeInDb("insert into tradeEventTable (event_id, trade_id, eventType, eventDate, person_id) values (1, 1, \"New\", \"2014-12-01\", 1);", connection);
        executeInDb("insert into tradeEventTable (event_id, trade_id, eventType, eventDate, person_id) values (2, 1, \"Correct\", \"2014-12-02\", 2);", connection);
        executeInDb("insert into tradeEventTable (event_id, trade_id, eventType, eventDate, person_id) values (3, 1, \"Settle\", \"2014-12-03\", 3);", connection);
        executeInDb("insert into tradeEventTable (event_id, trade_id, eventType, eventDate, person_id) values (4, 6, \"New\", \"2014-12-03\", 4);", connection);
        executeInDb("insert into tradeEventTable (event_id, trade_id, eventType, eventDate, person_id) values (5, 6, \"Cancel\", \"2014-12-04\", 5);", connection);

        //Data for case sensitive tables*/
        executeInDb("Drop schema if exists schemaA cascade;", connection);
        executeInDb("create schema schemaA;", connection);
        executeInDb("Drop table if exists schemaA.firmSet;", connection);
        executeInDb("Create Table schemaA.firmSet(id INT, name VARCHAR(200));", connection);
        executeInDb("insert into schemaA.firmSet (id, name) values (1, 'Firm X');", connection);
        executeInDb("insert into schemaA.firmSet (id, name) values (2, 'Firm A');", connection);

        executeInDb("Drop table if exists schemaA.personset;", connection);
        executeInDb("Create Table schemaA.personset(id INT, lastName VARCHAR(200), FirmID INT, firstName VARCHAR(200));", connection);
        executeInDb("insert into schemaA.personset(id, lastname, FirmID, firstName) values (3, 'Williams', 1, 'Mohammed');", connection);

        executeInDb("Drop schema if exists schemaB cascade;", connection);
        executeInDb("create schema schemaB;", connection);
        executeInDb("Drop table if exists schemaB.PERSONSET;", connection);
        executeInDb("Create Table schemaB.PERSONSET (ID INT,  age INT);", connection);
        executeInDb("insert into schemaB.PERSONSET (ID, age) values (1, 17);", connection);
        executeInDb("insert into schemaB.PERSONSET (ID, age) values (2,  20);", connection);
        executeInDb("insert into schemaB.PERSONSET (ID, age) values (3,  23);", connection);

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select \"root\".ID as \"pk_0\" from SCHEMAA.firmSet as \"root\"");
        System.out.println(resultSet.next());

        /*executeInDb("Drop table if exists otherNamesTable;", connection);
        executeInDb("Create Table otherNamesTable (PERSON_ID INT, OTHER_NAME VARCHAR(200));", connection);
        executeInDb("insert into otherNamesTable (PERSON_ID, OTHER_NAME) values (1, \"abc\");", connection);
        executeInDb("insert into otherNamesTable (PERSON_ID, OTHER_NAME) values (1, \"def\");", connection);
        executeInDb("insert into otherNamesTable (PERSON_ID, OTHER_NAME) values (1, \"ghi\");", connection);
        executeInDb("insert into otherNamesTable (PERSON_ID, OTHER_NAME) values (2, \"jkl\");", connection);
        executeInDb("insert into otherNamesTable (PERSON_ID, OTHER_NAME) values (2, \"mno\");", connection);
   */
    }

    @Override
    public RelationalDatabaseConnection getConnection() {
        RelationalDatabaseConnection h2Connection = new RelationalDatabaseConnection();
        StaticDatasourceSpecification ds = new StaticDatasourceSpecification();
        ds.databaseName = "temp";
        ds.host = "127.0.0.1";
        ds.port = h2Server.getPort();
        h2Connection.datasourceSpecification = ds;
        h2Connection.databaseType = DatabaseType.H2;
        h2Connection.authenticationStrategy = new DefaultH2AuthenticationStrategy();
        h2Connection.type = h2Connection.databaseType;        // for compatibility with legacy DatabaseConnection
        h2Connection.element = "";                            // placeholder , will be set by pure tests
        return h2Connection;
    }

    @Override
    public void cleanup() {
        h2Server.shutdown();
    }

}
