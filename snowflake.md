
# UDTF 
```
CREATE OR REPLACE FUNCTION lookup_state(STATE VARCHAR)
    RETURNS TABLE (STATE_FULL VARCHAR)
    LANGUAGE JAVASCRIPT
    AS $$
      {
        processRow: function f(row, rowWriter, context)  {
            if (row.STATE == "NJ" ) {
                rowWriter.writeRow( {STATE_FULL: "New Jersey"});
            }
            else if (row.STATE == "NY" ) {       
                rowWriter.writeRow( {STATE_FULL: "New York"});
            }
            else {
                rowWriter.writeRow( {STATE_FULL: "Unknown"});
            }         
        }
      }
      $$;
```

# Tables 
```
select * from table(lookup_state('NJ'));

drop table if exists PERSON;
create table PERSON(id integer, name varchar(100), state varchar(2));
insert into PERSON values(1, 'alice', 'NJ');
insert into PERSON values(2, 'bob', 'NY');
insert into PERSON values(3, 'charlie', 'CA');

```

# Usage 

```
select P.id, P.name, FUNC.STATE_FULL 
from PERSON as P, table(lookup_state(P.state)) as FUNC
```
