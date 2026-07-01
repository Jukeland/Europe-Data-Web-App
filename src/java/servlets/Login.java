package servlets;

import database.tables.EditUserTable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mainClasses.JSONConverter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author anton
 */
@WebServlet(name = "Login", urlPatterns = {"/Login"})
public class Login extends HttpServlet {

    /**
     * queries the database for the existence of "username" and "password" pair
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    
        // get the client's request 
        JSONConverter jc = new JSONConverter();
        JSONObject jsonObj = new JSONObject(jc.getJSONFromAjax(request.getReader()));
        
        EditUserTable eut = new EditUserTable();
        
        try{
            
            // query the database for whether the username and password provided are correct or not
            if(!eut.userExists(jsonObj.getString("username"), jsonObj.getString("password"))){
                response.sendError(401, "Invalid username or password");
                System.out.println("Invalid username or password");
                return;
            }
            //System.out.println("username: " + jsonObj.getString("username") + ", password: " + jsonObj.getString("password"));
            
            // successful login, get a new session
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedIn", jsonObj.getString("username"));
            session.setAttribute("isAdmin", "true");
            response.setStatus(200);
            response.setContentType("application/json");
            JSONObject jo = new JSONObject();
            jo.put("message", "Successful login");
            jo.put("status", "ok");
            PrintWriter out = response.getWriter();
            out.println(jo);
            
        }catch(IOException | ClassNotFoundException | SQLException | JSONException e){
            
        }
        
    }

}

