package database.tables;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import mainClasses.FunFact;
import database.DBConnection;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;

/**
 *
 * @author anton
 */
public class EditFunFactsTable {
    
    /**
     * main method to test the methods below
     * @param args
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        
        EditFunFactsTable efft = new EditFunFactsTable();
        efft.addFunFactsToDatabase(System.getProperty("user.dir") + "\\resources\\fun_facts.csv");
        //FunFact ff = efft.getRandomFunFact("Greece");
        //System.out.println(ff.getFunFact());

    }
    
    /**
     * creates the database table for the fun facts
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public void createFunFactsTable() throws SQLException, ClassNotFoundException {
        
        Connection con = DBConnection.getConnection();
        Statement stmt = con.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS fun_facts "
                + "(country_id INTEGER NOT NULL, "
                + " fun_fact_no INTEGER NOT NULL, "
                + " fun_fact VARCHAR(200) NOT NULL, "
                + " FOREIGN KEY (country_id) REFERENCES countries(country_id), "
                + " PRIMARY KEY (country_id, fun_fact_no))";
        stmt.execute(sql);
        stmt.close();
        con.close();
        
    }
    
    /**
     * generates a pseudo-random number between 1 and 15
     * @return 
     */
    public int pseudoRNG(){
        
        int min = 1;
        int max = 15;
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
        
    }
    
     /**
      * retrieves a random fun fact of a country from the database
      * @param country_name the name of the country you want to get the fun fact for
      * @return a FunFact class object with the all of its data
      * @throws SQLException
      * @throws ClassNotFoundException 
      */
    public FunFact getRandomFunFact(String country_name)
            throws SQLException, ClassNotFoundException {
        
        Connection con = DBConnection.getConnection();
        PreparedStatement pstmt;
        ResultSet rs;
        int random = pseudoRNG();
        //System.out.println("random fun fact: " + random);
        
        try{
            
            String query = "SELECT c.country_name, f.fun_fact_no, f.fun_fact "
                    + "FROM fun_facts f JOIN countries c ON f.country_id = c.country_id "
                    + "WHERE country_name = ? AND fun_fact_no = ?";
            
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, country_name);
            pstmt.setInt(2, random);
            rs = pstmt.executeQuery();
            rs.next();
            String json = DBConnection.getResultsToJSON(rs);
            Gson gson = new Gson();
            FunFact ff = gson.fromJson(json, FunFact.class);
            
            return ff;
            
        }catch(JsonSyntaxException | SQLException e){
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        
        return null;
        
    }
    
    /**
     * adds a whole CSV into the fun_facts table of the database
     * @param file the path of the CSV file
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public void addFunFactsToDatabase(String file) throws SQLException, ClassNotFoundException {
    
        Connection con = DBConnection.getConnection();
        String insertQuery = "INSERT INTO fun_facts (country_id, fun_fact_no, fun_fact) "
                + "SELECT country_id, ?, ? FROM countries WHERE country_name = ?";
        PreparedStatement pstmt = con.prepareStatement(insertQuery);
        
        try{
            
            InputStreamReader filereader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                                  .withSkipLines(1)
                                  .build();
            List<String[]> allData = csvReader.readAll();
           
            for(String[] ff : allData){
                
                pstmt.setInt(1, Integer.parseInt(ff[1].trim()));
                pstmt.setString(2, ff[2]);
                pstmt.setString(3, ff[0]);
                pstmt.executeUpdate();

            }
            
            System.out.println("# The fun facts were successfully added in the database.");
            
            pstmt.close();
            csvReader.close();
            filereader.close();
            
        } catch (CsvException | IOException e) {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        
    }
    
}

