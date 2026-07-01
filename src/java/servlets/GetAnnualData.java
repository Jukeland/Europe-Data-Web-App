package servlets;

import database.tables.EditAnnualDataTable;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mainClasses.AnnualData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author anton
 */
@WebServlet("/GetAnnualData")
public class GetAnnualData extends HttpServlet {

    // helper class to get the total population and the gender percentages 
    public class Data{
        
        private int total_population;
        private float male_percentage, female_percentage;
        
        void setTotalPopulation(int total_population){
            this.total_population = total_population;
        }
        
        int getTotalPopulation(){
            return this.total_population;
        }
        
        void setMalePercentage(float male_percentage){
            this.male_percentage = male_percentage;
        }
        
        float getMalePercentage(){
            return this.male_percentage;
        }
        
        void setFemalePercentage(float female_percentage){
            this.female_percentage = female_percentage;
        }
        
        float getFemalePercentage(){
            return this.female_percentage;
        }
        
    }

    /**
     * helper function to calculate the total population and the gender percentages 
     * @param male_population the male population value
     * @param female_population the female population value
     * @return a Data class object with the total population and the gender percentages
     */
    public Data getPercentages(int male_population, int female_population){
        
        Data data = new Data();
        
        // set the total population as the addition of the male and female population
        data.setTotalPopulation(male_population + female_population);
        
        // calculate and set the male percentage as male_population / total_population
        // round to 2 decimal points
        BigDecimal male_percentage = BigDecimal.valueOf(((float) male_population / data.getTotalPopulation()) * 100);
        male_percentage = male_percentage.setScale(2, RoundingMode.HALF_UP);
        data.setMalePercentage(male_percentage.floatValue());
        
        // calculate and set the female percentage as female_population / total_population
        // round to 2 decimal points
        BigDecimal female_percentage = BigDecimal.valueOf(((float) female_population / data.getTotalPopulation()) * 100);
        female_percentage = female_percentage.setScale(2, RoundingMode.HALF_UP);
        data.setFemalePercentage(female_percentage.floatValue());
        
        return data;
        
    }

    /**
     * queries the database for all annual data of a given country
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException{
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try{
            
            EditAnnualDataTable eadt = new EditAnnualDataTable();
            String country_name = request.getParameter("country");
            
            ArrayList<AnnualData> annualDataList = eadt.getAnnualDataByCountry(country_name);
            JSONArray jsonArray = new JSONArray();
            
            // for each AnnualData object returned from the database
            for(AnnualData data : annualDataList){
                
                // define a new json object and put the year into it
                JSONObject yearObj = new JSONObject();
                yearObj.put("year", data.getYear());
                
                // put the population data into the json object, checking for nulls
                if(data.getMalePopulation() != null && data.getFemalePopulation() != null){
                    Data popData = getPercentages(data.getMalePopulation(), data.getFemalePopulation());
                    yearObj.put("total_population", popData.getTotalPopulation());
                    yearObj.put("male_percentage", popData.getMalePercentage());
                    yearObj.put("female_percentage", popData.getFemalePercentage());
                }else{
                    yearObj.put("total_population", JSONObject.NULL);
                    yearObj.put("male_percentage", JSONObject.NULL);
                    yearObj.put("female_percentage", JSONObject.NULL);
                }
                
                // put the inflation data into the json object, checking for nulls
                if(data.getInflation() != null)
                    yearObj.put("inflation", data.getInflation());
                else
                    yearObj.put("inflation", JSONObject.NULL);
                
                // put the migration data into the json object, checking for nulls
                if(data.getMigration() != null)
                    yearObj.put("migration", data.getMigration());
                else
                    yearObj.put("migration", JSONObject.NULL);
                            
                // put the whole object built earlier to the jsonArray
                jsonArray.put(yearObj);
                
            }
            
            // return the jsonArray to the front-end
            out.print(jsonArray.toString());
            
        }catch(ClassNotFoundException | SQLException | JSONException ex){
            System.err.println("GetAnnualData Servlet says: Got an exception!");
            System.err.println(ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
        }
        
    }
    
}

