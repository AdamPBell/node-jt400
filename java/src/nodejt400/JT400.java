package nodejt400;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.ibm.as400.access.AS400JDBCConnectionPool;
import com.ibm.as400.access.AS400JDBCConnectionPoolDataSource;
import com.ibm.as400.access.AS400JDBCDriver;

public class JT400
{
	private final ConnectionProvider connectionProvider;

	private final JdbcJsonClient client;

	public JT400(ConnectionProvider connectionProvider)
	{
		this.connectionProvider = connectionProvider;
		this.client = new JdbcJsonClient(connectionProvider);
	}
	public static final JT400 createConnection(String jsonConf)
	throws Exception
	{
		JSONObject conf = (JSONObject) JSONValue.parse(jsonConf);
		return new JT400(new SimpleConnection(conf));
	}

	public static final JT400 createPool(String jsonConf)
	{
		JSONObject conf = (JSONObject) JSONValue.parse(jsonConf);
		return new JT400(new Pool(conf));
	}

	public String query(String sql, String paramsJson)
			throws Exception
	{
		return client.query(sql, paramsJson);
	}

	public int[] batchUpdate(String sql, String paramsListJson)
			throws Exception {
		return client.batchUpdate(sql, paramsListJson);
	}

	public StatementWrap execute(String sql, String paramsJson)
		throws Exception
	{
			return client.execute(sql, paramsJson);
	}

	public TablesReadStream getTablesAsStream(String catalog, String schema, String table) throws Exception
	{
		return client.getTablesAsStream(catalog, schema, table);
	}

	public String getColumns(String catalog, String schema, String tableNamePattern, String columnNamePattern)
	throws Exception
	{
		return client.getColumns(catalog, schema, tableNamePattern, columnNamePattern);
	}

	public String getPrimaryKeys(String catalog, String schema, String table)
	throws Exception
	{
		return client.getPrimaryKeys(catalog, schema, table);
	}

	public int update(String sql, String paramsJson)
			throws Exception
	{
		return client.update(sql, paramsJson);
	}

	public double insertAndGetId(String sql, String paramsJson)
			throws Exception
	{
		return client.insertAndGetId(sql, paramsJson);
	}

	public Transaction createTransaction() throws Exception
	{
		return new Transaction(connectionProvider.getConnection());
	}

	public Pgm pgm(String programName, String paramsSchemaJsonStr)
	{
		return new Pgm(connectionProvider, programName, paramsSchemaJsonStr);
	}

	public KeyedDataQ createKeyedDataQ(String name)throws Exception
	{
		return new KeyedDataQ(connectionProvider, name);
	}

	public void close()
	{
		connectionProvider.close();
	}

}

class SimpleConnection implements ConnectionProvider
{
	private final Connection connection;

	public SimpleConnection(JSONObject jsonConf)
	throws Exception
	{
		Connection conn = null;
    Properties connectionProps = new Properties();
    connectionProps.putAll(jsonConf);

		DriverManager.registerDriver(new AS400JDBCDriver());
    connection = DriverManager.getConnection("jdbc:as400://" + jsonConf.get("host"), connectionProps);
	}

	@Override
	public Connection getConnection() throws Exception
	{
		return connection;
	}

	@Override
	public void returnConnection(Connection c) throws Exception
	{
	}

	@Override
	public void close()
	{
		try
		{
			connection.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}

class Pool implements ConnectionProvider
{
	private final AS400JDBCConnectionPool sqlPool;

	private final JdbcJsonClient client;

	public Pool(JSONObject jsonConf)
	{
		Props conf = new Props(jsonConf);
		AS400JDBCConnectionPoolDataSource ds = new AS400JDBCConnectionPoolDataSource();
		ds.setServerName(conf.get("host"));
		ds.setUser(conf.get("user"));
		ds.setPassword(conf.get("password"));
		String naming = conf.get("naming", "system");
		ds.setNaming(naming);
		ds.setDateFormat(conf.get("dateFormat", "iso"));
		ds.setTransactionIsolation(conf.get("transactionIsolation", "repeatable read"));
		ds.setMetaDataSource(0);
		String value = conf.get("sort");
		if (value != null)
		{
			ds.setSort(value);
		}
		value = conf.get("sortTable");
		if (value != null)
		{
			ds.setSortTable(value);
		}
		value = conf.get("sortLanguage");
		if (value != null)
		{
			ds.setSortLanguage(value);
		}
		value = conf.get("libraries");
		if (value != null)
		{
			ds.setLibraries(value);
		}
		this.sqlPool = new AS400JDBCConnectionPool(ds);
		this.client = new JdbcJsonClient(this);
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				System.out.println("close connectionpool.");
				sqlPool.close();
			}
		});
	}

	@Override
	public Connection getConnection() throws Exception
	{
		return sqlPool.getConnection();
	}

	@Override
	public void returnConnection(Connection c) throws Exception
	{
		c.close();
	}

	@Override
	public void close()
	{
		sqlPool.close();
	}
}
