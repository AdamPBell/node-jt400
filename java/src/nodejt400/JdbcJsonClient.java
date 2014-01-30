package nodejt400;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JdbcJsonClient
{
	private final ConnectionProvider pool;

	public JdbcJsonClient(ConnectionProvider pool)
	{
		this.pool = pool;
	}

	public String query(String sql, String paramsJson)
			throws Exception
	{
		Connection c = pool.getConnection();
		PreparedStatement st = null;
		JSONArray array = new JSONArray();
		try
		{
			st = c.prepareStatement(sql);
			setParams(paramsJson, st);
			ResultSet rs = st.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			while (rs.next())
			{
				JSONObject json = new JSONObject();
				int columnCount = metaData.getColumnCount();
				for (int i = 1; i <= columnCount; i++)
				{
					json.put(metaData.getColumnName(i), trim(rs.getString(i)));
				}
				array.add(json);
			}

		}
		catch (Exception e)
		{
			System.err.println(sql + " params: " + paramsJson);
			throw e;
		}
		finally
		{
			if (st != null)
				st.close();
			c.close();
		}
		return array.toJSONString();
	}

	public String executeQuery(String sql, String paramsJson)
			throws Exception
	{
		Connection c = pool.getConnection();
		PreparedStatement st = null;
		JSONObject result = new JSONObject();
		try
		{
			JSONObject metadataJson = new JSONObject();
			JSONArray columns = new JSONArray();
			JSONArray data = new JSONArray();
			result.put("metadata", metadataJson);
			result.put("data", data);
			metadataJson.put("columns", columns);
			st = c.prepareStatement(sql);
			setParams(paramsJson, st);
			ResultSet rs = st.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++)
			{
				JSONObject column = new JSONObject();
				columns.add(column);
				column.put("name", metaData.getColumnName(i));
				column.put("typeName", metaData.getColumnTypeName(i));
				column.put("precision", metaData.getPrecision(i));
				column.put("scale", metaData.getScale(i));
			}
			while (rs.next())
			{
				JSONArray row = new JSONArray();
				for (int i = 1; i <= columnCount; i++)
				{
					row.add(trim(rs.getString(i)));
				}
				data.add(row);
			}

		}
		catch (Exception e)
		{
			System.err.println(sql + " params: " + paramsJson);
			throw e;
		}
		finally
		{
			if (st != null)
				st.close();
			c.close();
		}
		return result.toJSONString();
	}

	private String trim(String value)
	{
		return value == null ? null : value.trim();
	}

	public int update(String sql, String paramsJson)
			throws Exception
	{
		Connection c = pool.getConnection();
		PreparedStatement st = null;
		int result = 0;
		try
		{
			st = c.prepareStatement(sql);
			setParams(paramsJson, st);
			result = st.executeUpdate();
		}
		catch (Exception e)
		{
			System.err.println(sql + " params: " + paramsJson);
			throw e;
		}
		finally
		{
			if (st != null)
				st.close();
			c.close();
		}
		return result;
	}

	public double insertAndGetId(String sql, String paramsJson)
			throws Exception
	{
		Connection c = pool.getConnection();
		PreparedStatement st = null;
		double result = 0;
		try
		{
			st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			setParams(paramsJson, st);
			st.executeUpdate();
			ResultSet keys = st.getGeneratedKeys();
			if (keys.next())
			{
				result = keys.getDouble(1);
			}
		}
		catch (Exception e)
		{
			System.err.println(sql + " params: " + paramsJson);
			throw e;
		}
		finally
		{
			if (st != null)
				st.close();
			c.close();
		}
		return result;
	}

	private void setParams(String paramsJson, PreparedStatement st) throws SQLException
	{
		Object[] params = parseParams(paramsJson);
		for (int i = 0; i < params.length; i++)
		{
			Object value = params[i];
			try
			{
				if (value instanceof Long)
				{
					st.setLong(i + 1, (Long) value);
				}
				else if (value instanceof Double)
				{
					st.setDouble(i + 1, (Double) value);
				}
				else if (value == null)
				{
					st.setNull(i + 1, Types.VARCHAR);
				}
				else
				{
					st.setString(i + 1, value.toString());
				}
			}
			catch (SQLException e)
			{
				System.err.println("Value: " + value + ", index: " + i);
				throw e;
			}
		}
	}

	private Object[] parseParams(String paramsJson)
	{
		JSONArray jsonArray = (JSONArray) JSONValue.parse(paramsJson);
		int n = jsonArray.size();
		Object[] params = new Object[n];
		for (int i = 0; i < n; i++)
		{
			params[i] = jsonArray.get(i);
		}
		return params;
	}
}