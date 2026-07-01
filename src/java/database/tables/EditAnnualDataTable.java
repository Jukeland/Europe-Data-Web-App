package database.tables;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import database.DBConnection;
import mainClasses.ConfigManager;
import mainClasses.AnnualData;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author anton
 */
public class EditAnnualDataTable{
    
    /**
     * main method to test the methods below
     * @param args
     * @throws IOException
     * @throws CsvException
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) 
            throws IOException, CsvException, SQLException, ClassNotFoundException {

        EditAnnualDataTable eadt = new EditAnnualDataTable();
        
        String male_path = ConfigManager.getResourcesPath() + "male_population.csv";
        String female_path = ConfigManager.getResourcesPath() + "female_population.csv";
        String inflation_path = ConfigManager.getResourcesPath() + "inflation.csv";
        String migration_path = ConfigManager.getResourcesPath() + "migration.csv";
        
        eadt.createAnnualDataTable();
        
        eadt.addPopulationToDatabase(male_path, female_path);
        eadt.addInflationToDatabase(inflation_path);
        eadt.addMigrationToDatabase(migration_path);
                
    }
    
    /**
     * creates the database table for the countries' annual data
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void createAnnualDataTable() throws SQLException, ClassNotFoundException{
        
        Connection con = DBConnection.getConnection();
        Statement stmt = con.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS annual_data "
                + "(country_id INTEGER NOT NULL, "
                + " year INTEGER NOT NULL, "
                + " male_population INTEGER, "
                + " female_population INTEGER, "
                + " inflation FLOAT, "
                + " migration INTEGER, "
                + " FOREIGN KEY (country_id) REFERENCES countries(country_id), "
                + " PRIMARY KEY (country_id, year))";
        stmt.execute(sql);
        stmt.close();
        con.close();
        
    }
    
    /**
     * retrieves all available years for the population data from the database
     * @return an ArrayList of all available years
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public ArrayList<Integer> getAvailableYears() throws SQLException, ClassNotFoundException{
        
        Connection con = DBConnection.getConnection();
        Statement stmt = con.createStatement();
        ArrayList<Integer> availableYears = new ArrayList<>();
        ResultSet rs;
        
        try{
            
            rs = stmt.executeQuery("SELECT DISTINCT year FROM annual_data WHERE male_population IS NOT NULL ORDER BY year DESC");
            while(rs.next()){
                availableYears.add(rs.getInt("year"));
            }
            con.close();
            stmt.close();
            
            return availableYears;
            
        }catch(SQLException e){
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        
        return null;
        
    }
    
    /**
     * retrieves the data for all countries for a given year
     * @param year
     * @return a list of AnnualData objects
     * @throws SQLException 
     * @throws ClassNotFoundException 
     */
    public ArrayList<AnnualData> getAnnualDataByYear(int year) 
            throws SQLException, ClassNotFoundException{
        
        Connection con = DBConnection.getConnection();
        PreparedStatement pstmt;
        ArrayList<AnnualData> dataList = new ArrayList<>();
        ResultSet rs;
        
        try{
            
            String query = "SELECT c.country_name, a.* "
                    + "FROM annual_data a JOIN countries c ON a.country_id = c.country_id "
                    + "WHERE a.year = ? "
                    + "ORDER BY c.country_name ASC";

            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, year);
            rs = pstmt.executeQuery();

            while(rs.next()){
                String json = DBConnection.getResultsToJSON(rs);
                Gson gson = new Gson();
                AnnualData data = gson.fromJson(json, AnnualData.class);
                dataList.add(data);
            }
            
            return dataList;
            
        }catch(SQLException e){
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        
        return null;
        
    }
    
    /**
     * retrieves all data for a given country
     * @param country_name the desired country's name
     * @return a list of AnnualData objects
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public ArrayList<AnnualData> getAnnualDataByCountry(String country_name) 
            throws SQLException, ClassNotFoundException{
        
        Connection con = DBConnection.getConnection();
        PreparedStatement pstmt;
        ArrayList<AnnualData> dataList = new ArrayList<>();
        ResultSet rs;
        
        try{
            
            String query = "SELECT c.country_name, a.* "
                    + "FROM annual_data a JOIN countries c ON a.country_id = c.country_id "
                    + "WHERE c.country_name = ? "
                    + "ORDER BY a.year ASC";
            
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, country_name);
            rs = pstmt.executeQuery();
            
            while(rs.next()){
                String json = DBConnection.getResultsToJSON(rs);
                Gson gson = new Gson();
                AnnualData data = gson.fromJson(json, AnnualData.class);
                dataList.add(data);
            }
            
            con.close();
            pstmt.close();
            
            return dataList;
            
        }catch(JsonSyntaxException | SQLException e){
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        
        return null;
        
    }
    
    /**
     * parses the population CSV files and adds their data into the database
     * @param male_population_csv the path of the male_population CSV file
     * @param female_population_csv the path of the female_population CSV file
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException 
     */
    public void addPopulationToDatabase(String male_population_csv, String female_population_csv) 
            throws SQLException, ClassNotFoundException, IOException{
        
        Connection con = DBConnection.getConnection();
        String insertQuery = "INSERT INTO annual_data (country_id, year, male_population, female_population) "
                + "SELECT country_id, ?, ?, ? FROM countries WHERE country_name = ? "
                + "ON DUPLICATE KEY UPDATE "
                + "male_population = VALUES(male_population), female_population = VALUES(female_population)";
        PreparedStatement pstmt = con.prepareStatement(insertQuery);
        
        List<String> european_countries = getEuropeanCountries();
        
        try{
            
            // read the whole male population csv into male_population
            FileReader male = new FileReader(male_population_csv);
            CSVReader csv_male = new CSVReaderBuilder(male)
                                    .withSkipLines(1)
                                    .build();
            List<String[]> male_population = csv_male.readAll();
            
            // read the whole female population csv into female_population
            FileReader female = new FileReader(female_population_csv);
            CSVReader csv_female = new CSVReaderBuilder(female)
                                    .withSkipLines(1)
                                    .build();
            List<String[]> female_population = csv_female.readAll();
            
            // create a map of "country_name":"whole_line" from the female population data
            Map<String, String[]> femaleDataMap = new HashMap<>();
            for(String[] fp : female_population){
                if(fp.length > 0)
                    femaleDataMap.put(fp[0], fp); 
            }
            
            // for each line in the male population data
            for(String[] mp : male_population){
                
                // get country name
                String country_name = mp[0];
                
                // if it's a country of interest (European country)
                if(european_countries.contains(country_name)){
                    
                    // get the corresponding line from the female population data
                    String[] fp = femaleDataMap.get(country_name); 
                    
                    if(fp != null){
                        
                        // start from the year 1960
                        int year = 1960;
                        
                        // find the total columns, min is used just in case their length is different, to prevent crashing
                        int totalColumns = Math.min(mp.length, fp.length);
                        
                        // skip the first 4 columns and go until 1 column before the end of the row
                        // that's because the world bank csv files end on a trailing comma 
                        // maybe that changes in the future so this logic might not be future-proof
                        for(int i = 4; i <= totalColumns - 1; i++){
                            
                            String maleData = mp[i].trim();
                            String femaleData = fp[i].trim();
                            
                            if(!maleData.isEmpty() && !femaleData.isEmpty()){
                                
                                // get the population value from each data
                                int malePop = Integer.parseInt(maleData);
                                int femalePop = Integer.parseInt(femaleData);
                                
                                // set the prepared statement with the data we got above
                                pstmt.setInt(1, year);
                                pstmt.setInt(2, malePop);
                                pstmt.setInt(3, femalePop);
                                pstmt.setString(4, getDifferentName(country_name));
                                
                                // execute the query
                                pstmt.executeUpdate();
                                
                            }
                            
                            // increase the year by one
                            year ++;
                            
                        }
                        
                    }
                    
                }
                
            }
            
            System.out.println("# The population data was successfully added to the database.");
            
            pstmt.close();
            csv_male.close(); male.close();
            csv_female.close(); female.close();
            con.close();
            
        }catch (CsvException | IOException e){
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        
    }

    /**
     * parses the inflation CSV file and adds its data into the database
     * @param inflation_csv the path of the inflation CSV file
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException 
     */
    public void addInflationToDatabase(String inflation_csv) 
            throws SQLException, ClassNotFoundException, IOException {
    
        Connection con = DBConnection.getConnection();
        String insertQuery = "INSERT INTO annual_data (country_id, year, inflation) "
                + "SELECT country_id, ?, ? FROM countries WHERE country_code = ?"
                + "ON DUPLICATE KEY UPDATE "
                + "inflation = VALUES(inflation)";
        PreparedStatement pstmt = con.prepareStatement(insertQuery);
        
        try{
            
            List<List<String>> records = new ArrayList<>();
            
            CSVReader csvReader = new CSVReader(new FileReader(inflation_csv));
            String[] values = null;
            
            // read the whole csv into records
            while((values = csvReader.readNext()) != null)
                records.add(Arrays.asList(values));
            
            // for all columns (aka for each country)
            for(int i = 2; i < 23; i++){
                
                // get the country code from the column names 
                String country_code = records.get(0).get(i).split("\\.")[2];
                
                // for each June of each year (median value)
                for(int current_line = 7; current_line < 344; current_line+=12){
                    
                    // if June's inflation value is not present ==> go to the next year
                    if(records.get(current_line).get(i).isEmpty())
                        continue;
                    
                    // get the year and inflation value from the respected places
                    String year = records.get(current_line).get(0).split("\\-")[0];
                    float inflation = Float.parseFloat(records.get(current_line).get(i));
                    
                    // insert record into the database
                    pstmt.setInt(1, Integer.parseInt(year));
                    pstmt.setFloat(2, inflation);
                    pstmt.setString(3, country_code);
                    
                    pstmt.executeUpdate();
                    
                }

            }
            
            System.out.println("# The inflation data was successfully added to the database.");
            
            pstmt.close();
            csvReader.close();
            con.close();
            
        }catch (CsvException | IOException e){
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        
    }

    /**
      * parses the migration CSV file and adds its data into the database
      * @param migration_csv the path of the migration CSV file
      * @throws SQLException
      * @throws ClassNotFoundException
      * @throws IOException 
      */
    public void addMigrationToDatabase(String migration_csv) 
            throws SQLException, ClassNotFoundException, IOException {
    
        Connection con = DBConnection.getConnection();
        String insertQuery = "INSERT INTO annual_data (country_id, year, migration) "
                + "SELECT country_id, ?, ? FROM countries WHERE country_name = ?"
                + "ON DUPLICATE KEY UPDATE "
                + "migration = VALUES(migration)";
        PreparedStatement pstmt = con.prepareStatement(insertQuery);
        
        try{
            
            FileReader mig = new FileReader(migration_csv);
            CSVReader reader = new CSVReaderBuilder(mig)
                                  .withSkipLines(1)
                                  .build();
            String[] current;
            
            // for each line in the csv file
            while((current = reader.readNext()) != null){
                
                // read the values that we need and insert them into the database
                pstmt.setInt(1, Integer.parseInt(current[9]));
                pstmt.setInt(2, Integer.parseInt(current[10]));
                pstmt.setString(3, current[8]);
                
                pstmt.executeUpdate();
                
            }
            
            System.out.println("# The migration data was successfully added to the database.");
            
            pstmt.close();
            reader.close();
            con.close();
            
        } catch (CsvException | IOException e) {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        
    }
    
    /**
     * gets a list of all European countries
     * @return a List of all European countries
     * @throws IOException 
     */
    public List<String> getEuropeanCountries() throws IOException{
        
        Path filePath = Paths.get(ConfigManager.getResourcesPath() + "european_countries.txt");
        
        //System.out.println("/n/n=== EADT.getEuropeanCountries says: path = " + filePath.toString());
        List<String> countries = Files.readAllLines(filePath);
        
        Collections.replaceAll(countries, "Turkey", "Turkiye");
        Collections.replaceAll(countries, "Russia", "Russian Federation");
        Collections.replaceAll(countries, "Slovakia", "Slovak Republic");
        
        /*
        for(String country : countries){
            System.out.println(country);
        }
        */
        return countries;
        
    }
    
    /**
     * changes specific countries' names
     * @param country_name
     * @return 
     */
    public String getDifferentName(String country_name){
        
        if(country_name.equals("Turkiye"))
            return "Turkey";
        if(country_name.equals("Russian Federation"))
            return "Russia";
        if(country_name.equals("Slovak Republic"))
            return "Slovakia";
        
        return country_name;
    }
    
}

