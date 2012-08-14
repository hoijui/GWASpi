package org.gwaspi.global;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.gwaspi.database.DbManager;
import org.gwaspi.database.DbManagerImpl;
import org.gwaspi.framework.jdbc.connection.ConnectionProvider;
import org.gwaspi.framework.jdbc.connection.JdbcConnectionProvider;
import org.gwaspi.framework.jdbc.connection.JavaDBConnectionProvider;

public class ServiceLocator {
	private static Map<String,DbManager> dbManagers;
	private static Map<String,ConnectionProvider> connectionProviders;

        
	public static DbManager getDbManager(String dbName) throws IOException {
		if (dbManagers==null) {
                    dbManagers=new HashMap<String, DbManager>();
		}
		DbManager dbManager = dbManagers.get(dbName);
		if (dbManager==null) {
                    dbManager=new DbManagerImpl(getConnectionProvider(dbName));
                    dbManagers.put(dbName, dbManager);
		}
		return dbManager;
	}
        

	public static ConnectionProvider getConnectionProvider(String dbName) throws IOException {
            if (connectionProviders==null) {
                connectionProviders=new HashMap<String, ConnectionProvider>();
            }
                ConnectionProvider connectionProvider = connectionProviders.get(dbName);
                if (connectionProvider==null) {
                JdbcConnectionProvider cp = new JavaDBConnectionProvider(dbName);
                final Properties props = new Properties();
                props.put("user", "toppolino");
                props.put("password", "f14883rg4s73d");
                cp.setProperties(props);
                connectionProvider=cp;
                connectionProviders.put(dbName, cp);
            }
            return connectionProvider;
	}
}
