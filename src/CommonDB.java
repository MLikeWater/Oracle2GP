import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class CommonDB
{

	private static String myclass = "CommonDB";
	public static boolean debug = false;

	public static void checkSourceObjects(String sourceType, String sourceServer, String sourceInstance, int sourcePort, String sourceDatabase, String sourceSchema, String sourceTable, String sourceUser, String sourcePass, String columnName, String refreshType) throws Exception
	{
		String method = "checkSourceObjects";
		int location = 1000;
		Connection extConn = null;

		try
		{
			location = 2000;
			//schema
			//table
			//append column if applicable

			if (sourceType.equals("sqlserver"))
			{
				location = 3000;
				extConn = connectSQLServer(sourceServer, sourceInstance, sourceUser, sourcePass);

				location = 3100;
				SQLServer.checkSourceSchema(extConn, sourceDatabase, sourceSchema);

				location = 3200;
				SQLServer.checkSourceTable(extConn, sourceDatabase, sourceSchema, sourceTable);

				location = 3300;
				if (refreshType.equals("append"))
				{
					location = 3400;
					SQLServer.checkAppendColumnName(extConn, sourceDatabase, sourceSchema, sourceTable, columnName);
				}
				else if (refreshType.equals("replication"))
				{
					location = 3500;
					SQLServer.checkReplAppendColumnName(extConn, sourceDatabase, sourceSchema, sourceTable, columnName);

					location = 3600;
					SQLServer.checkReplPrimaryKey(extConn, sourceDatabase, sourceSchema, sourceTable);
				}

				location = 3700;
				extConn.close();

			}
			else if (sourceType.equals("oracle"))
			{
				location = 4000;
				extConn = connectOracle(sourceServer, sourceDatabase, sourcePort, sourceUser, sourcePass, 10);

				location = 4100;
				Oracle.checkSourceSchema(extConn, sourceDatabase, sourceSchema);

				location = 4200;
				Oracle.checkSourceTable(extConn, sourceDatabase, sourceSchema, sourceTable);
				
				location = 4300;
				if (refreshType.equals("append"))
				{
					location = 4400;
					Oracle.checkAppendColumnName(extConn, sourceDatabase, sourceSchema, sourceTable, columnName);
				}
				else if (refreshType.equals("replication"))
				{
					location = 4500;
					Oracle.checkReplAppendColumnName(extConn, sourceDatabase, sourceSchema, sourceTable, columnName);

					location = 4600;
					Oracle.checkReplPrimaryKey(extConn, sourceDatabase, sourceSchema, sourceTable);
				}

				location = 4700;
				extConn.close();

			}

		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
		finally 
		{
			if (extConn != null) 
				extConn.close();
		}
	}

	public static String formatSQLForColumnName(Connection conn, String strSQL) throws SQLException
	{
		String method = "formatSQLForColumnName";
		int location = 1000;

		try
		{
			location = 2000;
			Statement stmt = conn.createStatement();

			location = 2100;
			ResultSet rs = stmt.executeQuery(strSQL);

			location = 2200;
			String columnSQL = "";

			location = 2300;
			while (rs.next())
			{

				location = 3000;
				//Create the list of columns for a SQL statement below
				if (columnSQL == "")
				{
					columnSQL = rs.getString(1);
				}
				else
				{
					columnSQL = columnSQL + ", " + rs.getString(1);
				}
			}
		
			location = 3500;	
			return columnSQL;
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static String getGPCreateTableDDL(Connection conn, String targetSchema, String targetTable, boolean targetAppendOnly, boolean targetCompressed, boolean targetRowOrientation, String strSQL) throws SQLException
	{
		String method = "getGPCreateTableDDL";
		int location = 1000;

		try
		{
			location = 2000;
			if (debug)
				Logger.printMsg("Executing SQL: " + strSQL);

			location = 2050;
			//Get Column names
			String columnName = "";
			String dataType = "";

			location = 2100;
			Statement stmt = conn.createStatement();

			location = 2200;
			ResultSet rs = stmt.executeQuery(strSQL);
			String output = "";

			location = 2300;
			while (rs.next())
			{
				columnName = rs.getString(1);
				dataType  = rs.getString(2);

				if (rs.getRow() == 1)
					output = "CREATE TABLE \"" + targetSchema + "\".\"" + targetTable + "\" \n" +
						 "(\"" + columnName + "\" " + dataType + " null";
				else
					output = output + ", \n" +
						"\"" + columnName + "\" " + dataType + " null";
			}
			output = output + ")\n";

			location = 3000;
			//set the Append-Only, Compression, and Orientation

			if (targetAppendOnly)
			{
				output += "WITH (APPENDONLY=TRUE";
				if (ExternalDataD.dbVersion.startsWith("HAWQ"))
				{
					if (targetCompressed)
					{
						if (targetRowOrientation)
							output += ", COMPRESSTYPE=quicklz";
						else
							output += ", COMPRESSTYPE=snappy";
					}
					if (targetRowOrientation)
						output += ", ORIENTATION=row";
					else
						output += ", ORIENTATION=parquet";
				} else if (ExternalDataD.dbVersion.equals("GPDB"))
				{
					if (targetCompressed)
						output += ", COMPRESSTYPE=quicklz";
					if (targetRowOrientation)
						output += ", ORIENTATION=row";
					else
						output += ", ORIENTATION=column";
				}
				output += ")\n";

			}
			return output;
		}

		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}

	}

	private static String getDistributionDDL(Connection conn, String targetSchema, String targetTable, String strSQL) throws SQLException
	{
		String method = "getDistributionDDL";
		int location = 1000;

		try
		{
			location = 2000;
			String distributedBy = "";
			if (ExternalDataD.dbVersion.equals("HAWQ_2"))
			{
				distributedBy = "DISTRIBUTED RANDOMLY; \n";
			}
			else
			{

				if (debug)
					Logger.printMsg("Execting SQL: " + strSQL);

				location = 2100;
				Statement stmt = conn.createStatement();

				location = 2200;
				ResultSet rs = stmt.executeQuery(strSQL);

				location = 2300;
				String columnName = "";

				location = 2400;
				while (rs.next())
				{
					columnName = rs.getString(1);

					if (rs.getRow() == 1)
					{
						distributedBy = "DISTRIBUTED BY (" + columnName;
					}
					else
					{
						distributedBy = distributedBy + ", " + columnName;
					}
				}
				if (!(distributedBy.equals("")))
				{
					distributedBy = distributedBy + "); \n";
				}
				else
				{
					distributedBy = "DISTRIBUTED RANDOMLY; \n";
				}
			}
			location = 3000;
			return distributedBy;
		}

		catch (SQLException ex)
		{
			//throw ex;
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}

	}
	public static String getGPTableDDL(String sourceType, String sourceServer, String sourceInstance, int sourcePort, String sourceDatabase, String sourceSchema, String sourceTable, String sourceUser, String sourcePass, String targetSchema, String targetTable, boolean targetAppendOnly, boolean targetCompressed, boolean targetRowOrientation) throws Exception
	{
		String method = "GetGPTableDDL";
		int location = 1000;
		Connection extConn = null;

		try
		{
			String strSQL = "";
			String output = "";

			if (sourceType.equals("sqlserver"))
			{
				location = 3000;
				extConn = connectSQLServer(sourceServer, sourceInstance, sourceUser, sourcePass);

				location = 3100;
				//Get the SQL needed to generate CREATE TABLE
				strSQL = SQLServer.getSQLForCreateTable(extConn, sourceDatabase, sourceSchema, sourceTable);

				//Get the GP DDL based on source DDL
				location = 3200;
				output = getGPCreateTableDDL(extConn, targetSchema, targetTable, targetAppendOnly, targetCompressed, targetRowOrientation, strSQL);

				location = 3300;
				strSQL = SQLServer.getSQLForDistribution(extConn, sourceDatabase, sourceSchema, sourceTable);

				//Get the table distribution based on Primary Key of source
				location = 3400;
				output = output + getDistributionDDL(extConn, targetSchema, targetTable, strSQL);

				location = 3500;
				extConn.close();
			}
			else if (sourceType.equals("oracle"))
			{
				location = 4000;
				extConn = connectOracle(sourceServer, sourceDatabase, sourcePort, sourceUser, sourcePass, 10);

				location = 4100;
				//Get the SQL needed to generate CREATE TABLE
				strSQL = Oracle.getSQLForCreateTable(extConn, sourceSchema, sourceTable);

				location = 4200;
				//Get the GP DDL based on source DDL
				output = getGPCreateTableDDL(extConn, targetSchema, targetTable, targetAppendOnly, targetCompressed, targetRowOrientation, strSQL);

				location = 4300;
				//Get the SQL needed to generate the table distribution based on Primary Key of source
				strSQL = Oracle.getSQLForDistribution(extConn, sourceSchema, sourceTable);

				location = 4400;
				//Get the table distribution based on Primary Key of source
				output = output + getDistributionDDL(extConn, targetSchema, targetTable, strSQL);

				location = 4500;
				extConn.close();
			}

			//output the DDL for the table to be created in GP
			location = 5000;
			return output;
		}

		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
		finally 
		{
			if (extConn != null) 
				extConn.close();
		}
	}

	public static boolean checkSourceReplObjects(String sourceType, String sourceServer, String sourceInstance, int sourcePort, String sourceDatabase, String sourceSchema, String sourceTable, String sourceUser, String sourcePass, String columnName) throws Exception
	{
		String method = "checkSourceReplObjects";
		int location = 1000;
		Connection extConn = null;

		try
		{
			location = 2000;
			boolean found = false;

			if (sourceType.equals("sqlserver"))
			{
				location = 3000;
				extConn = connectSQLServer(sourceServer, sourceInstance, sourceUser, sourcePass);

				location = 3100;
				found = SQLServer.snapshotSourceTable(extConn, sourceDatabase, sourceSchema, sourceTable);

				location = 3200;
				extConn.close();

			}
			else if (sourceType.equals("oracle"))
			{
				location = 4000;
				extConn = connectOracle(sourceServer, sourceDatabase, sourcePort, sourceUser, sourcePass, 10);

				location = 4100;
				found = Oracle.snapshotSourceTable(extConn, sourceSchema, sourceTable);

				location = 4200;
				extConn.close();

			}

			location = 5000;
			return found;
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
		finally 
		{
			if (extConn != null) 
				extConn.close();
		}
	}

	public static void configureReplication(String sourceType, String sourceServer, String sourceInstance, int sourcePort, String sourceDatabase, String sourceSchema, String sourceTable, String sourceUser, String sourcePass, String columnName) throws Exception
	{
		String method = "configureReplication";
		int location = 1000;

		Connection extConn = null;

		try
		{
			location = 2000;	
			if (sourceType.equals("sqlserver"))
			{
				location = 2100;
				extConn = connectSQLServer(sourceServer, sourceInstance, sourceUser, sourcePass);

				location = 2200;
				SQLServer.configureReplication(extConn, sourceDatabase, sourceSchema, sourceTable, columnName);	

				location = 2300;
				extConn.close();

			}
			else if (sourceType.equals("oracle"))
			{
				location = 3100;
				extConn = connectOracle(sourceServer, sourceDatabase, sourcePort, sourceUser, sourcePass, 10);

				location = 3200;
				Oracle.configureReplication(extConn, sourceDatabase, sourceSchema, sourceTable, columnName);	

				location = 3300;
				extConn.close();

			}

		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
		finally 
		{
			if (extConn != null) 
				extConn.close();
		}
	}

	public static String getReplMaxId(String sourceType, String sourceServer, String sourceInstance, int sourcePort, String sourceDatabase, String sourceSchema, String sourceTable, String sourceUser, String sourcePass, String columnName) throws Exception
	{	
		String method = "getReplMaxId";
		int location = 1000;
		Connection extConn = null;

		try
		{
			location = 2000;
			String replTable = GP.getReplTableName(sourceType, sourceTable);

			String maxId = "-1";

			if (sourceType.equals("sqlserver"))
			{
				location = 3000;
				extConn = connectSQLServer(sourceServer, sourceInstance, sourceUser, sourcePass);

				location = 3100;
				maxId = SQLServer.getMaxId(extConn, sourceDatabase, sourceSchema, replTable, columnName);

				location = 3200;
				extConn.close();

			}
			else if (sourceType.equals("oracle"))
			{
				location = 4000;
				extConn = connectOracle(sourceServer, sourceDatabase, sourcePort, sourceUser, sourcePass, 10);

				location = 4100;
				maxId = Oracle.getMaxId(extConn, sourceSchema, replTable, columnName);

				location = 4200;
				extConn.close();

			}

			location = 5000;
			return maxId;

		}

		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
		finally 
		{
			if (extConn != null) 
				extConn.close();
		}

	}

	public static String getMaxId(String sourceType, String sourceServer, String sourceInstance, int sourcePort, String sourceDatabase, String sourceSchema, String sourceTable, String sourceUser, String sourcePass, String columnName) throws Exception
	{
		String method = "getMaxId";
		int location = 1000;
		Connection extConn = null;

		try
		{
			location = 2000;
			String maxId = "-1";

			if (sourceType.equals("sqlserver"))
			{
				location = 3000;
				extConn = connectSQLServer(sourceServer, sourceInstance, sourceUser, sourcePass);

				location = 3100;
				maxId = SQLServer.getMaxId(extConn, sourceDatabase, sourceSchema, sourceTable, columnName);

				location = 3200;
				extConn.close();

			}
			else if (sourceType.equals("oracle"))
			{
				location = 4000;
				extConn = connectOracle(sourceServer, sourceDatabase, sourcePort, sourceUser, sourcePass, 10);

				location = 4100;
				maxId = Oracle.getMaxId(extConn, sourceSchema, sourceTable, columnName);

				location = 4200;
				extConn.close();

			}

			location = 5000;
			return maxId;

		}

		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
		finally 
		{
			if (extConn != null) 
				extConn.close();
		}

	}

	public static void outputData(Connection conn, String strSQL) throws SQLException
	{
		String method = "outputData";
		int location = 1000;

		try
		{
			location = 2000;
			if (debug)
				Logger.printMsg("Execting SQL: " + strSQL);
            /*
                ResultSet.TYPE_FORWARD_ONLY: 结果集的游标只能向下滚动    
                ResultSet.CONCUR_READ_ONLY 不能用结果集更新数据库中的表     
            */
			Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery(strSQL);
            // 得到结果集(rs)的结构信息，比如字段数、字段名等          
			ResultSetMetaData rsmd = rs.getMetaData();
            // 得列数      
			int numberOfColumns = rsmd.getColumnCount();

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			String output;
			String columnValue = "";

			location = 2500;
			while (rs.next())
			{
				output="";
				// Get the column names; column indices start from 1
				for (int i=1; i<numberOfColumns+1; i++)
				{

					//Extra try/catch block because of an Oracle problem at Virgin Airlines.  
					//Getting an "ArrayIndexOutOfBoundsException" error which might be caused by the Oracle JDBC driver
					try
					{
                        columnValue = rs.getString(i);
					}
					catch (Exception e)
					{
						columnValue = (String) rs.getObject(i);
					}

					if (columnValue != null)
					{
						if (debug)
							Logger.printMsg(rsmd.getColumnName(i) + ":" + rsmd.getColumnTypeName(i) + ":" + i + ":" + columnValue);

						//Oracle has the DATE data type (SQL Server has date and datetime)
						//The range for Oracle DATE is January 1, 4712 BCE through December 31, 4712 CE 
						if (rsmd.getColumnTypeName(i) == "DATE" || rsmd.getColumnTypeName(i) == "TIMESTAMP" ) 
						{
							columnValue = df.format(rs.getTimestamp(i));
						}

						//Filter out \ and | from the columnValue for not null records.  the rest will default to "null"
						columnValue = columnValue.replace("\\", "\\\\");
						columnValue = columnValue.replace("|", "\\|");
						columnValue = columnValue.replace("\r", " ");
						columnValue = columnValue.replace("\n", " ");
						columnValue = columnValue.replace("\0", "");
					}

					if (i == 1)
						output = columnValue;
					else
						output = output + "|" + columnValue;
				}

				System.out.println(output);
			}
		}

		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}

	}

	public static Connection connectSQLServer(String sourceServer, String sourceInstance, String sourceUser, String sourcePass) throws Exception
	{
		String method = "connectSQLServer";
		int location = 1000;

		try
		{
			if (debug)
				Logger.printMsg("try");
			location = 2000;
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			if (debug)
				Logger.printMsg("loaded classforname");

			String connectionUrl = null;

			if (sourceInstance != null)
			{
				location = 3000;
				if (sourceInstance.compareTo("") != 0)
				{
					location = 3100;
					connectionUrl = "jdbc:sqlserver://" + sourceServer + ";instanceName=" + sourceInstance + ";CODEPAGE=65001;responseBuffering=adaptive;selectMethod=cursor;";
				}
				else
				{
					location = 4100;
					connectionUrl = "jdbc:sqlserver://" + sourceServer + ";CODEPAGE=65001;responseBuffering=adaptive;selectMethod=cursor;";
				}
			}
			else
			{
				location = 5100;
				connectionUrl = "jdbc:sqlserver://" + sourceServer + ";CODEPAGE=65001;responseBuffering=adaptive;selectMethod=cursor;";
			}

			if (debug)
				Logger.printMsg("connectionUrl: " + connectionUrl);
			
			location = 6000;
			if (debug)
				Logger.printMsg("attempting to connect");

			Connection extConn = DriverManager.getConnection(connectionUrl, sourceUser, sourcePass);

			location = 6100;
			return extConn;
		}
		catch(ClassNotFoundException e)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":ClassNotFound! " + e.getMessage() + ")");
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}


	public static Connection connectOracle(String sourceServer, String sourceDatabase, int sourcePort, String sourceUser, String sourcePass, int fetchSize) throws Exception
	{
		String method = "connectOracle";
		int location = 1000;

		try
		{
			location = 2000;
			Class.forName("oracle.jdbc.driver.OracleDriver");

			location = 2100;
			System.setProperty("java.security.egd", "file:///dev/urandom");

			location = 2200;
			Properties props = new Properties(); 

			location = 2210;
			props.put("user", sourceUser); 

			location = 2220;
			props.put("password", sourcePass);

			location = 2230;
			String fetchSizeString = Integer.toString(fetchSize);

			location = 2235;
			props.put("defaultRowPrefetch", fetchSizeString);

			location = 2300;
			String connectionUrl = "jdbc:oracle:thin:@" + sourceServer + ":" + sourcePort + "/" + sourceDatabase;

			location = 2400;
			Connection extConn = DriverManager.getConnection(connectionUrl, props);

			return extConn;
		}
		catch(ClassNotFoundException e)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":ClassNotFound! " + e.getMessage() + ")");
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static Connection connectGP(String configFile) throws SQLException
	{
		String method = "connectGP";
		int location = 1000;
		try
		{
			Connection conn = connectGP(configFile, null, null);
			return conn;
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}
	
	public static Connection connectGP(String configFile, String gpUserName, String gpPassword) throws SQLException
	{
		String method = "connectGP";
		int location = 1000;

		try
		{
			try
			{
				location = 2000;
				Class.forName("org.postgresql.Driver");
			}
			catch (Exception e)
			{
				throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + e.getMessage() + ")");
			}

			location = 2100;
			OSProperties.getPropValues(configFile);

			location = 2200;
			String connectionUrl = "jdbc:postgresql://" + OSProperties.gpServer + ":" + OSProperties.gpPort + "/" + OSProperties.gpDatabase;

			location = 2300;
			if (gpUserName == null)
			{
				gpUserName = OSProperties.gpUserName;
				gpPassword = OSProperties.gpPassword;
			}

			Connection conn = DriverManager.getConnection(connectionUrl, gpUserName, gpPassword);

			location = 2400;
			return conn;
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}

		catch (IOException iex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + iex.getMessage() + ")");
		}

	}
}
