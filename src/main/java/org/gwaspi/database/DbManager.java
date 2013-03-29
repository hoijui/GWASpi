package org.gwaspi.database;

import java.util.List;
import java.util.Map;
import org.gwaspi.framework.jdbc.connection.ConnectionProvider;

public interface DbManager {

	ConnectionProvider getConnectionProvider();

	void setConnectionProvider(ConnectionProvider connectionProvider);

	List<Map<String, Object>> selectMutipleClauses(String schema, String table,
			String[] fields, String[] clauseFields, Object[] clauseValues);

	int executeStatement(String statement);

	List<Map<String, Object>> executeSelectStatement(String selectStatement);

	boolean insertValuesInTable(String schema, String table, String[] fields, Object[] values);

	boolean updateTable(
			String schema,
			String table,
			String[] fields,
			Object[] values,
			String[] clauseFields,
			Object[] clauseValues);

	boolean dropTable(String schema, String table);

	boolean createTable(String schema, String table, String[] fieldStatements);

	boolean createSchema(String schema);

	void shutdownConnection();
}
