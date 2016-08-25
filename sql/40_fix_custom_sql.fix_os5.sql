DROP VIEW os.custom_sql;

ALTER TABLE os.ao_custom_sql RENAME TO ao_custom_sql_backup;

CREATE TABLE os.ao_custom_sql
( id serial NOT NULL,
  table_name text NOT NULL,
  columns text[] NOT NULL,
  column_datatypes text[] NOT NULL,
  sql_text text NOT NULL,
  source_type text,
  source_server_name text NOT NULL,
  source_instance_name text,  --optional by SQL Server
  source_port int, --used by Oracle
  source_database_name text,  --used by Oracle
  source_user_name text NOT NULL,
  source_pass text NOT NULL,
  gpfdist_port int NOT NULL,
  deleted boolean NOT NULL DEFAULT FALSE,
  insert_id serial NOT NULL)
 WITH (appendonly=true)
:DISTRIBUTED_BY;
 --DISTRIBUTED BY (id);

CREATE VIEW os.custom_sql AS
SELECT id, table_name, columns, column_datatypes, sql_text, source_type, source_server_name, source_instance_name, source_port,
       source_database_name, source_user_name, source_pass, gpfdist_port
FROM    (
        SELECT  id, table_name, columns, column_datatypes, sql_text, source_type, source_server_name, source_instance_name, source_port,
                source_database_name, source_user_name, source_pass, gpfdist_port,
                row_number() OVER (PARTITION BY id ORDER BY insert_id DESC) AS rownum, deleted
        FROM os.ao_custom_sql
        ) AS sub
WHERE rownum = 1 AND NOT deleted;

ALTER TABLE os.ao_custom_sql
  ADD CONSTRAINT custom_check
  CHECK ( (source_type = 'oracle'::text AND source_database_name IS NOT NULL AND source_instance_name IS NULL) OR
          (source_type = 'sqlserver' AND source_database_name IS NULL) );

ALTER TABLE os.ao_custom_sql ADD CONSTRAINT custom_check_port
  CHECK ((source_port > 0 AND source_type = 'oracle'::text) OR (source_type = 'sqlserver'::text AND source_port IS NULL));
  
INSERT INTO os.ao_custom_sql
(table_name, columns, column_datatypes, sql_text, source_type, 
            source_server_name, source_instance_name, source_port, source_database_name, 
            source_user_name, source_pass, gpfdist_port)
SELECT table_name, columns, column_datatypes, sql_text, source_type, 
            source_server_name, source_instance_name, source_port, source_database_name, 
            source_user_name, source_pass, (:osport_custom_lower - 1 + row_number() over ())::int as gpfdist_port
FROM os.ao_custom_sql_backup
WHERE DELETED = FALSE;

DROP TABLE os.ao_custom_sql_backup;
