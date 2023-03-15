
```
function sqlAST():Any[*]
{
  meta::relational::tests::postProcessor::setUp();
  meta::relational::tests::postProcessor::testSimple();

  let pTable = meta::relational::tests::db->schema('default')->toOne()->table('personTable')->toOne();
  let aTable = meta::relational::tests::db->schema('default')->toOne()->table('addressTable')->toOne();

  let pTableAlias = 'P';
  let aTableAlias = 'A';
  let joinAlias = 'J';

  let join1 = ^Join(
      name='join1', 
      target=^TableAlias(name=$pTableAlias, relationalElement=$pTable), 
      aliases=[pair(^TableAlias(name=$pTableAlias, relationalElement=$pTable), ^TableAlias(name=$aTableAlias, relationalElement=$aTable))], 
      operation=^DynaFunction(name = 'equal',parameters = [^Literal(value = 1), ^Literal(value = 1)])
  );

  let join2 = ^Join(
      name='join1', 
      target=^TableAlias(name=$pTableAlias, relationalElement=$pTable), 
      aliases=[pair(^TableAlias(name=$pTableAlias, relationalElement=$pTable), ^TableAlias(name=$aTableAlias, relationalElement=$aTable))], 
      operation=^DynaFunction(name = 'equal',parameters = [
        ^meta::relational::metamodel::TableAliasColumn(column=^Column(name='ADDRESSID', type=^meta::relational::metamodel::datatype::Integer()),alias=^TableAlias(name=$pTableAlias, relationalElement=$pTable)),
        ^meta::relational::metamodel::TableAliasColumn(column=^Column(name='ID', type=^meta::relational::metamodel::datatype::Integer()),alias=^TableAlias(name=$aTableAlias, relationalElement=$aTable))
       ]
      )  
  );  

  let joinTreeNode = ^meta::relational::metamodel::join::JoinTreeNode(database=^Database(), joinName='join1', alias=^TableAlias(name=$joinAlias, relationalElement=$aTable), join=$join2);
  let sql = ^meta::relational::metamodel::relation::SelectSQLQuery(columns = [^ColumnName(name='a'), ^ColumnName(name='b')], data=^meta::relational::metamodel::join::RootJoinTreeNode(alias=^TableAlias(name=$pTableAlias, relationalElement=$pTable), childrenData=$joinTreeNode));
  let sqlString = meta::relational::functions::sqlQueryToString::sqlQueryToString($sql, DatabaseType.H2, []);

  println($sqlString);
  $sql->traverse();
}

```
