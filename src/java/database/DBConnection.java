package database;

import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import mainClasses.ConfigManager;

public class DBConnection{
    
    private static final String url = ConfigManager.getProperty("database.url");
    private static final String databaseName = ConfigManager.getProperty("database.name");
    private static final int port = Integer.parseInt(ConfigManager.getProperty("database.port"));
    private static final String username = ConfigManager.getProperty("database.username");
    private static final String password = ConfigManager.getProperty("database.password");

    /*
     * connects to the database through JDBC with the configuration from the config.properties file
     * returns the connection to the database
     */
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        
        Class.forName("com.mysql.jdbc.Driver");;
        return DriverManager.getConnection(url + ":" + port + "/" + databaseName, username, password);
        
    }
    
    /*
     * gets the initial connection to create the database
     * returns the initial connection
     */
    public static Connection getInitialConnection() throws SQLException, ClassNotFoundException {
        
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(url + ":" + port, username, password);
        
    }
    
    /*
     * prints the result set returned from a query to the database
     */
    public static void printResults(ResultSet rs) throws SQLException {
          
        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();
        
        for(int i = 1; i <= columnCount; i++){
            
            String name = metadata.getColumnName(i);
            String value = rs.getString(i);
            System.out.println(name + " " + value);
            
        }
        
    }
    
    /*
     * converts the result set returned from the database to a json string
     */
    public static String getResultsToJSON(ResultSet rs) throws SQLException {
         
        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();
        JsonObject object = new JsonObject();
     
        for(int i = 1; i <= columnCount; i++){
            
            String name = metadata.getColumnName(i);
            String value = rs.getString(i);
            object.addProperty(name,value);
            
        }
        
        return object.toString();
        
    }
     
    /*
     * converts the result set returned from the database to a json object
     */
    public static JsonObject getResultsToJSONObject(ResultSet rs) throws SQLException {
         
        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();
        JsonObject object = new JsonObject();
        
        for(int i = 1; i <= columnCount; i++){
            
            String name = metadata.getColumnName(i);
            String value = rs.getString(i);
            object.addProperty(name,value);
            
        }
        
        return object;
        
    }
     
}

