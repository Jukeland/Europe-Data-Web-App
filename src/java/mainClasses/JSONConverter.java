package mainClasses;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author anton
 */
public class JSONConverter {
    
    /**
     * gets the input from the front-end as a json string
     * @param reader
     * @return
     * @throws IOException 
     */
    public String getJSONFromAjax(BufferedReader reader) throws IOException{
        
        StringBuilder buffer = new StringBuilder();
        String line;

        while((line = reader.readLine()) != null){
            buffer.append(line);
        }

        String data = buffer.toString();

        return data;
        
    }

}

