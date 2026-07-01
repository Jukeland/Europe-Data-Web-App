package servlets;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author anton
 */
@WebServlet(name = "LoggedInChecker", urlPatterns = {"/LoggedInChecker"})
public class LoggedInChecker extends HttpServlet {

    /**
     * checks if an HTTP session is currently in use 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException{

        // get the current session, don't create a new one if it doesn't exist
        HttpSession session = request.getSession(false);
        
        // if user is admin send him to the admin page, if not send them to the home page
        if(session != null && session.getAttribute("isAdmin") != null && session.getAttribute("isAdmin").equals("true")){
            
            // do NOT cache the admin page, prevents a false view of the page
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            
            request.getRequestDispatcher("/WEB-INF/admin_page.html").forward(request, response);
    }else
            response.sendRedirect("index.html"); 
        
        
    }

}

