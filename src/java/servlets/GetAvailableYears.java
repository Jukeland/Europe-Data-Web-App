package servlets;

import database.tables.EditAnnualDataTable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;

/**
 *
 * @author anton
 */
@WebServlet(name = "GetAvailableYears", urlPatterns = {"/GetAvailableYears"})
public class GetAvailableYears extends HttpServlet {

    /**
     * queries the database for all available years
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try{

            // query the database for the available years
            EditAnnualDataTable eadt = new EditAnnualDataTable();
            ArrayList<Integer> years = eadt.getAvailableYears();
            
            // put the available years into a JsonArray
            JSONArray jsonArray = new JSONArray(years);
            System.out.println(jsonArray.toString());
            
            // return the jsonArray to the client
            out.print(jsonArray.toString());
            
        }catch(ClassNotFoundException | SQLException e){
            e.getMessage();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
        }
        
    }

}

