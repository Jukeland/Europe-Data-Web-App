package database;

import com.google.gson.JsonObject;
import static database.DBConnection.getInitialConnection;
import database.tables.EditAnnualDataTable;
import database.tables.EditFunFactsTable;
import database.tables.EditCountriesTable;
import database.tables.EditUserTable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import mainClasses.ConfigManager;

public class InitDatabase {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        
        
        String resourcesPath = ConfigManager.getResourcesPath();
        String malePath = resourcesPath + "male_population.csv";
        String femalePath = resourcesPath + "female_population.csv";
        String funFactsPath = resourcesPath + "fun_facts.csv";
        String inflationPath = resourcesPath + "inflation.csv";
        String migrationPath = resourcesPath + "migration.csv";
        
        InitDatabase init = new InitDatabase();
        init.dropDatabase();
        init.initDatabase(malePath, femalePath, funFactsPath, inflationPath, migrationPath);

    }

    /*
     * drops the whole database
     */
    public void dropDatabase() throws SQLException, ClassNotFoundException {
        
        Connection conn = getInitialConnection();
        Statement stmt = conn.createStatement();
        String sql = "DROP DATABASE world_population";
        stmt.executeUpdate(sql);
        System.out.println("Database dropped successfully...");
        
    }

    /*
     * creates the database
     */
    public void initDatabase(String male_path, String female_path, String funFacts_path, String inflation_path, 
            String migration_path) throws SQLException, ClassNotFoundException, IOException {
        
        System.out.println("InitDatabase class says: EIMAI EDW");
        Connection conn = getInitialConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE DATABASE IF NOT EXISTS world_population");
        stmt.close();
        conn.close();
        
        initTables(male_path, female_path, funFacts_path, inflation_path, migration_path);
        
    }

    /*
     * creates and populates the tables of the database
     */
    public void initTables(String male_path, String female_path, String funFacts_path, String inflation_path, 
            String migration_path) throws SQLException, ClassNotFoundException, IOException {
        
        EditCountriesTable ect = new EditCountriesTable();
        ect.createCountriesTable();
        ect.addEuropeanCountriesToDatabase();
        
        EditFunFactsTable efft = new EditFunFactsTable();
        efft.createFunFactsTable();
        efft.addFunFactsToDatabase(funFacts_path);
        
        /*
        EditEuropePopulationTable eept = new EditEuropePopulationTable();
        eept.createEuropePopulationTable(); 
        eept.addPopulationToDatabase(male_path, female_path);
                
        EditInflationTable eit = new EditInflationTable();
        eit.createInflationTable();
        eit.addInflationToDatabase(inflation_path);
        
        EditMigrationTable emt = new EditMigrationTable();
        emt.createMigrationTable();
        emt.addMigrationToDatabase(migration_path);
        */
        
        EditAnnualDataTable eadt = new EditAnnualDataTable();
        eadt.createAnnualDataTable();
        eadt.addPopulationToDatabase(male_path, female_path);
        eadt.addInflationToDatabase(inflation_path);
        eadt.addMigrationToDatabase(migration_path);
        
        EditUserTable eut = new EditUserTable();
        eut.createUserTable();
        eut.initTable();
        
    }
    
    /**
     * method to check if the data has already been inserted into the database
     * @return true if the data has already been inserted, false otherwise
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public boolean hasData() throws SQLException, ClassNotFoundException{
        
        Connection con = DBConnection.getConnection();
        Statement stmt = con.createStatement();
        String sql = "SELECT COUNT(*) AS count FROM countries";
        ResultSet rs;
        rs = stmt.executeQuery(sql);
        rs.next();
        JsonObject json = DBConnection.getResultsToJSONObject(rs);
        
        String count = json.get("count").toString();
        return count.equals("\"41\"");
        
    }

}

