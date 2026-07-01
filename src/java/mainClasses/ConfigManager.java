package mainClasses;

import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author anton
 */
public class ConfigManager{
    
    private static final Properties properties = new Properties();

    // this static block runs exactly once when the application starts up
    static{
        
        // look for the file in the classpath (WEB-INF/classes)
        try(InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")){
            
            if(input == null)
                System.err.println("CRITICAL ERROR: Unable to find config.properties in the classpath!");
            else{
                properties.load(input);
                System.out.println("Configuration loaded successfully.");
            }
            
        }catch(Exception e){
            System.err.println("Failed to load configuration file.");
            e.getMessage();
        }
        
    }

    // method to grab any property by its key
    public static String getProperty(String key){
        return properties.getProperty(key);
    }
    
    // method specifically for the resources path
    public static String getResourcesPath(){
        return properties.getProperty("resources.path");
    }
    
    // method specifically for the upload path
    public static String getUploadDir(){
        return properties.getProperty("csv.upload.path");
    }
    
}

