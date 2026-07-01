package servlets;

import database.InitDatabase;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import mainClasses.ConfigManager;

/**
 *
 * @author anton
 */
@WebListener
public class InitDB implements ServletContextListener{

    /**
     * runs once when the server starts and initializes the database
     * @param sce 
     */
    @Override
    public void contextInitialized(ServletContextEvent sce){

        String resourcesPath = ConfigManager.getResourcesPath();
        String malePath = resourcesPath + "male_population.csv";
        String femalePath = resourcesPath + "female_population.csv";
        String funFactsPath = resourcesPath + "fun_facts.csv";
        String inflationPath = resourcesPath + "inflation.csv";
        String migrationPath = resourcesPath + "migration.csv";
        System.out.println(resourcesPath + "\n" + malePath);
        InitDatabase init = new InitDatabase();
        
        try{
            //if(!init.hasData())
                init.initDatabase(malePath, femalePath, funFactsPath, inflationPath, migrationPath);
        }catch(SQLException | ClassNotFoundException | IOException ex){
            Logger.getLogger(InitDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * runs when the server shuts down, cleans up
     * @param sce 
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}

