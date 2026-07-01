package servlets;

import database.tables.EditAnnualDataTable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mainClasses.AnnualData;
import org.json.JSONObject;

/**
 *
 * @author anton
 */
@WebServlet(name = "GetCompareData", urlPatterns = {"/GetCompareData"})
public class GetCompareData extends HttpServlet {

    /**
     * queries the database for all data from all countries for a specific year
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

        // get the year from the client's request
        int year = Integer.parseInt(request.getParameter("year"));
        
        try{

            EditAnnualDataTable eadt = new EditAnnualDataTable();
            
            // query the database to get all available data for the specified year
            ArrayList<AnnualData> allYearData = eadt.getAnnualDataByYear(year);
            
            Map<String, Integer> population = new LinkedHashMap<>();
            Map<String, Double> inflation = new LinkedHashMap<>();
            Map<String, Double> migration = new LinkedHashMap<>();

            // iterate over the combined data once and populate the three maps manually, checking for nulls along the way
            for(AnnualData data : allYearData){
                String country = data.getCountryName();
                
                if(data.getMalePopulation() != null && data.getFemalePopulation() != null)
                    population.put(country, data.getMalePopulation() + data.getFemalePopulation());
                
                if(data.getInflation() != null)
                    inflation.put(country, (double) data.getInflation());
                
                if(data.getMigration() != null)
                    migration.put(country, (double) data.getMigration());
                
            }
            
            // put the data into json objects
            JSONObject populationJson = new JSONObject(population);
            JSONObject inflationJson = new JSONObject(inflation);
            JSONObject migrationJson = new JSONObject(migration);
            
            // put all data into one master json object
            JSONObject masterJson = new JSONObject();
            masterJson.put("population", populationJson);
            masterJson.put("inflation", inflationJson);
            masterJson.put("migration", migrationJson);
            
            //System.out.println(masterJson.toString());
            
            // return master json object to the client
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(masterJson.toString());

        }catch (NumberFormatException e){
            e.getMessage();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Server failed to load data.\"}");
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(GetCompareData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}

