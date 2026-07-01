package database.tables;

import com.google.gson.Gson;
import database.DBConnection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import mainClasses.ConfigManager;
import mainClasses.Country;

/**
 *
 * @author anton
 */
public class EditCountriesTable {
    
    /**
     * main method to test the methods below
     * @param args
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException 
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        
        EditCountriesTable ect = new EditCountriesTable();
        ect.createCountriesTable();
        ect.addEuropeanCountriesToDatabase();

    }
    
    /**
     * creates the database table for the countries
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public void createCountriesTable() throws SQLException, ClassNotFoundException {
        
        Connection con = DBConnection.getConnection();
        Statement stmt = con.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS countries "
                + "(country_id INTEGER NOT NULL AUTO_INCREMENT, "
                + " country_name VARCHAR(25) UNIQUE NOT NULL, "
                + " country_code VARCHAR(2) NOT NULL, "
                + " continent VARCHAR(15) NOT NULL, "
                + " PRIMARY KEY (country_id))";
        stmt.execute(sql);
        stmt.close();
        con.close();
        
    }
    
    /**
     * gets a list of all European countries from a text file
     * @return a list of all country names
     * @throws IOException 
     */
    public List<String> getEuropeanCountries() throws IOException{
        
        Path filePath = Paths.get(ConfigManager.getResourcesPath() + "european_countries.txt");
        System.out.println("countries.txt path: " + filePath);
        List<String> countries = Files.readAllLines(filePath);
        
        /*
        for (String country : countries) {
            System.out.println(country);
        }
        */
        
        return countries;
        
    }
    
    /**
     * gets a list of all European countries' codes
     * @return a list of all country codes
     * @throws IOException 
     */
    public List<String> getEuropeanCodes() throws IOException{
        
        Path filePath = Paths.get(ConfigManager.getResourcesPath() + "country_codes.txt");
        System.out.println("/n/n=== ECT.getCountryCodes says: path = " + filePath.toString());
        List<String> codes = Files.readAllLines(filePath);
        
        /*
        for (String code : codes) {
            System.out.println(code);
        }
        */
        
        return codes;
        
    }
    
    /**
     * adds the European countries into the database
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public void addEuropeanCountriesToDatabase() 
            throws SQLException, IOException, ClassNotFoundException{
        
        Connection con = DBConnection.getConnection();
        String insertQuery = "INSERT INTO countries (country_name, country_code, continent) VALUES (?, ?, 'europe')";
        PreparedStatement pstmt = con.prepareStatement(insertQuery);
        
        // get the countries names and codes
        EditCountriesTable ect = new EditCountriesTable();
        List<String> countries = ect.getEuropeanCountries();
        List<String> codes = ect.getEuropeanCodes();
        
        try{
            /*
            for(String country: countries){
                pstmt.setString(1, country);
                pstmt.executeUpdate();
            }
            */
            
            for(int i = 0; i < 41; i++){
                pstmt.setString(1, countries.get(i));
                pstmt.setString(2, codes.get(i));
                pstmt.executeUpdate();
            }
            
            System.out.println("# The countries were successfully added in the database.");
            
            pstmt.close();
            
        }catch (SQLException e){
            System.err.println("Got an exception");
            System.err.println(e.getMessage());
        }
        
    }
    
    /**
     * retrieves a country from the database by providing its name
     * @param country_name the country's name you want to get
     * @return a Country class object with all of the country's data
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public Country getCountry(String country_name) throws SQLException, ClassNotFoundException{
        
        Connection con = DBConnection.getConnection();
        PreparedStatement pstmt;
        String query = "SELECT * FROM countries WHERE country_name = ?";       
        ResultSet rs;
        
        pstmt = con.prepareStatement(query);
        pstmt.setString(1, country_name);
        rs = pstmt.executeQuery();
        rs.next();
        String json = DBConnection.getResultsToJSON(rs);
        Gson gson = new Gson();
        Country c = gson.fromJson(json, Country.class);
        //System.out.println("country_id = " + c.getCountryId());
        
        return c;
        
    }
    
    /**
     * retrieves a country from the database by providing its id
     * @param country_id the country's id you want to get
     * @return a Country class object with all of the country's data
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public Country getCountry(int country_id) throws SQLException, ClassNotFoundException{
        
        Connection con = DBConnection.getConnection();
        PreparedStatement pstmt;
        String query = "SELECT * FROM countries WHERE country_id = ?";
        ResultSet rs;
        
        pstmt = con.prepareStatement(query);
        pstmt.setInt(1, country_id);
        rs = pstmt.executeQuery();
        rs.next();
        String json = DBConnection.getResultsToJSON(rs);
        Gson gson = new Gson();
        Country c = gson.fromJson(json, Country.class);
        //System.out.println("country_name = " + c.getCountryName());
        
        return c;
        
    }
    
}

