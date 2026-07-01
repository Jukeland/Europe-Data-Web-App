package servlets;

import database.tables.EditAnnualDataTable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import mainClasses.ConfigManager;

/**
 *
 * @author anton
 */
@WebServlet("/UploadCSV")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1,  // 1 MB
        maxFileSize = 1024 * 1024 * 10,       // 10 MB
        maxRequestSize = 1024 * 1024 * 100    // 100 MB 
)
public class UploadCSV extends HttpServlet {

    /**
     * function to check if the CSV file has the structure we need it to before uploading<br>
     * if it is valid rename the .tmp file with the actual file name<br>
     * if it is not valid delete the .tmp file
     * @param filePart the file part, one of these values: malePart, femalePart, inflationPart or migrationPart
     * @param finalFileName the file name if it passes validation
     * @param expectedHeader the expected header of the CSV file
     * @param uploadDirPath the path to the "uploads" directory
     * @param expectedGender "male" or "female" for population data, an empty string otherwise
     * @return returns null if successful, otherwise returns what went wrong
     */
    private String validateAndProcessCSV(Part filePart, String finalFileName, String expectedHeader, 
            Path uploadDirPath, String expectedGender){
        
        if(filePart == null || filePart.getSize() == 0){
            return "No file selected.";
        }
        
        Path tempFile = uploadDirPath.resolve(finalFileName + ".tmp");
        Path finalFile = uploadDirPath.resolve(finalFileName);

        try{

            // save the file as a temporary .tmp file
            try(InputStream fileContent = filePart.getInputStream()){
                Files.copy(fileContent, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // get the actual header of the CSV file by reading the first line and the first data row by reading the next line
            String actualHeader;
            String firstDataRow;
            try(BufferedReader reader = Files.newBufferedReader(tempFile)){
                actualHeader = reader.readLine();               
                firstDataRow = reader.readLine();      
            }
            
            // if an expected gender has been provided
            if(!expectedGender.equals("")){
                String[] columns = firstDataRow.split(",");
                if(columns.length < 4)
                    return "Invalid format.";
                
                // get the gender from the third column of the csv file (here is actually the fourth because the third column contains a comma inside it)
                String actualGender = columns[3].toLowerCase().replace("\"", "").stripLeading();

                //System.out.println("Expected: " + expectedGender);
                //System.out.println("Actual: " + actualGender);
                //System.out.println("Condition: " + actualGender.equals(expectedGender.toLowerCase()));
                
                // check if the expected data is actually present
                if(!actualGender.equals(expectedGender.toLowerCase()))
                    return "The file uploaded for the " + expectedGender + " population does not appear to contain " + expectedGender + " data.";
                
            }
            
            // if the file is not empty
            if(actualHeader != null){
                String cleanActual = actualHeader.replace("\uFEFF", "").replace("\"", "").toLowerCase().trim();
                String cleanExpected = expectedHeader.replace("\"", "").toLowerCase().trim();

                // compare the actual header with the expected header 
                // and if they match replace the .tmp file with the actual file
                if(cleanActual.equals(cleanExpected)){
                    Files.move(tempFile, finalFile, StandardCopyOption.REPLACE_EXISTING);
                    return null; 
                }
            }

            //System.out.println("\n\n=== validateAndProcessCSV says: I reached the end ===\n");
            return "Invalid format.";

        }catch(IOException e){
            return "Server encountered an error while processing the file.";
        }finally{
            // always delete .tmp file if it still exists
            try{
                Files.deleteIfExists(tempFile);
            }catch(IOException e){
                System.err.println("Could not clean up temp file: " + tempFile);
            }
        }
        
    }

    /**
     * validates and uploads CSV files to the server
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String uploadDirectory = ConfigManager.getUploadDir();
        System.out.println("Upload directory set to: " + uploadDirectory);
        if(uploadDirectory == null || uploadDirectory.trim().isEmpty()){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Server misconfigured. Upload directory missing.\"}");
            out.flush();
            return; 
        }

        EditAnnualDataTable eadt = new EditAnnualDataTable();
        
        try{
            
            Part malePart = request.getPart("male_population"); 
            Part femalePart = request.getPart("female_population");
            Part inflationPart = request.getPart("inflation");
            Part migrationPart = request.getPart("migration");
            
            Path uploadDirPath = Paths.get(uploadDirectory);
                
            String maleResult = "";
            String femaleResult = "";
            
            boolean hasMale = malePart != null && malePart.getSize() > 0;
            boolean hasFemale = femalePart != null && femalePart.getSize() > 0;
            
            // population data checker
            if(hasMale && !hasFemale){
                maleResult = "Upload blocked: The Female Population CSV must also be provided.";
            }else if(!hasMale && hasFemale){
                femaleResult = "Upload blocked: The Male Population CSV must also be provided.";
            }else if(hasMale && hasFemale){ // both files have been provided
      
                String populationExpectedHeader = ConfigManager.getProperty("csv.headers.population");
                maleResult = validateAndProcessCSV(malePart, "male_population.csv", populationExpectedHeader, uploadDirPath, "male");
                femaleResult = validateAndProcessCSV(femalePart, "female_population.csv", populationExpectedHeader, uploadDirPath, "female");
                System.out.println("VGHKA");
                if(maleResult == null && femaleResult != null){ // male is valid, female is invalid
                    Files.deleteIfExists(uploadDirPath.resolve("male_population.csv"));
                    maleResult = "Upload rolled back: The Female Population file failed validation.";
                }else if(maleResult != null && femaleResult == null){ // male is invlaid, female is valid
                    Files.deleteIfExists(uploadDirPath.resolve("female_population.csv"));
                    femaleResult = "Upload rolled back: The Male Population file failed validation.";
                }else if(maleResult != null && femaleResult != null){ // both are invalid               
                    Files.deleteIfExists(uploadDirPath.resolve("male_population.csv"));
                    Files.deleteIfExists(uploadDirPath.resolve("female_population.csv"));
                    maleResult = "Invalid format.";
                    femaleResult = "Invalid format.";
                }else{ // both files are valid => update the database with the new population files
                    //System.out.println("male_population.csv path: " + uploadDirPath.resolve("male_population.csv").toString());
                    eadt.addPopulationToDatabase(uploadDirPath.resolve("male_population.csv").toString(), uploadDirPath.resolve("female_population.csv").toString());
                }
                
            }
            
            String inflationExpectedHeader = ConfigManager.getProperty("csv.headers.inflation");
            String migrationExpectedHeader = ConfigManager.getProperty("csv.headers.migration");
            String inflationResult = validateAndProcessCSV(inflationPart, "inflation.csv", inflationExpectedHeader, uploadDirPath, "");
            String migrationResult = validateAndProcessCSV(migrationPart, "migration.csv", migrationExpectedHeader, uploadDirPath, "");
            
            if(inflationResult == null){ // inflation file is valid
                try{
                    Path inlfationPath = uploadDirPath.resolve("inflation.csv").toAbsolutePath();
                    
                    // wait until the inflation file is actually done writing - 2 seconds max
                    int attempts = 0;
                    while(!Files.exists(inlfationPath) && attempts < 40){
                        Thread.sleep(100); // Pause for 100 milliseconds
                        attempts++;
                    }
                    
                    // update database with the new inflation file
                    if(Files.exists(inlfationPath))
                        eadt.addInflationToDatabase(inlfationPath.toString());
                    else
                        throw new Exception("File system timeout: File was not saved in time.");          
                    
                }catch(Exception e){
                    e.getMessage();
                    inflationResult = "Database Error: Could not save inflation data.";
                    try{ 
                        Files.deleteIfExists(uploadDirPath.resolve("inflation.csv")); 
                    }catch(IOException ex){
                        ex.getMessage();
                    }
                }
            }
            
            
            if(migrationResult == null){ // migration file is valid
                try{
                    Path migrationPath = uploadDirPath.resolve("migration.csv").toAbsolutePath();
                    
                    // wait until the migration file is actually done writing - 2 seconds max
                    int attempts = 0;
                    while(!Files.exists(migrationPath) && attempts < 20){
                        Thread.sleep(100); // Pause for 100 milliseconds
                        attempts++;
                    }
                    
                    // update the database with the new migration file
                    if(Files.exists(migrationPath))
                        eadt.addMigrationToDatabase(migrationPath.toString());
                    else
                        throw new Exception("File system timeout: File was not saved in time.");
                    
                }catch(Exception e){
                    e.getMessage();
                    migrationResult = "Database Error: Could not save migration data.";
                    try{ 
                        Files.deleteIfExists(uploadDirPath.resolve("migration.csv")); 
                    }catch(IOException ex){
                        ex.getMessage();
                    }
                }
            }
            
            String jsonResponse = String.format(
                "{\"male_population\": \"%s\", \"female_population\": \"%s\", \"inflation\": \"%s\", \"migration\": \"%s\"}",
                escapeJson(maleResult),
                escapeJson(femaleResult),
                escapeJson(inflationResult),
                escapeJson(migrationResult)
            );
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(jsonResponse);
            out.flush();
            
        }catch(IOException | ServletException e){
            e.getMessage();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"An unexpected server error occurred.\"}");
            out.flush();
        }catch(SQLException | ClassNotFoundException ex){
            ex.getMessage();
            Logger.getLogger(UploadCSV.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * escapes all characters that might interfere with the building of a json string
     * @param text the raw json string
     * @return the escaped json string
     */
    private String escapeJson(String text){
        
        if (text == null) return "Success";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "")
                   .replace("\r", "");
        
    } 
    
}

