package servlets;

import database.tables.EditFunFactsTable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mainClasses.FunFact;
import org.json.JSONException;

/**
 *
 * @author anton
 */
@WebServlet("/GetFunFact")
public class GetFunFact extends HttpServlet {

    /**
     * queries the database for a random fun fact about a given country
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try{

            EditFunFactsTable efft = new EditFunFactsTable();

            // get the country name from the client's request
            String country_name = request.getParameter("country");
            
            // query the database for a random fun fact of the given country
            FunFact ff = efft.getRandomFunFact(country_name);
            
            //System.out.println("\n=== getFunFact Servlet says: fun fact = " + ff.getFunFact() + " ===\n");
            
            response.setContentType("text/plain; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            
            // return the fun fact to the client
            out.print(ff.getFunFact());
            
        }catch(IOException | ClassNotFoundException | SQLException | JSONException ex){
            System.err.println("GetFunFacts Servlet says: Got an exception!");
            System.err.println(ex.getMessage());
        }
        
    }

}

