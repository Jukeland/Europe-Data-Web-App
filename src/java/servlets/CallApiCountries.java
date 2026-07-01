package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author anton
 */
@WebServlet("/CallApiCountries")
public class CallApiCountries extends HttpServlet {

    /**
     * calls an external API 
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

        HttpURLConnection connection = null;
        
        try{
            
            /*
            URL url = new URL("https://api.restcountries.com/countries/v5/region/europe?limit=55");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + ConfigManager.getProperty("api.restcountries.key"));
            */
            
             // connect to the external API
            URL url = new URL("https://www.apicountries.com/region/europe");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            // if the response code is ok
            if(connection.getResponseCode() == 200){
                
                // read the data coming from apicountries
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder jsonResponse = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null){
                    jsonResponse.append(line);
                }
                reader.close();

                // return the data to the front-end
                out.print(jsonResponse.toString());
                
            }else{
                // if the external API fails, pass a safe error to the frontend
                response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
                out.print("{\"error\": \"External API returned status " + connection.getResponseCode() + "\"}");
            }

        }catch(IOException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Server encountered an error reaching the external API.\"}");
            System.err.println("GetCountries Proxy Error: " + e.getMessage());
        }finally{
            if(connection != null)
                connection.disconnect();
        }
        
    }
    
}

