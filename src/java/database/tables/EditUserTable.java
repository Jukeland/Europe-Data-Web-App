package database.tables;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 *
 * @author anton
 */
public class EditUserTable {
    
    /**
     * main method to test the methods below
     * @param args
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException{
        
        EditUserTable eut = new EditUserTable();
        //eut.createUserTable();
        eut.initTable();
        
    }
    
    /**
     * creates the database table for the users
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public void createUserTable() throws SQLException, ClassNotFoundException{
        
        Connection con = DBConnection.getConnection();
        Statement stmt = con.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS user "
                + "(user_id INTEGER NOT NULL AUTO_INCREMENT, "
                + " username VARCHAR(30) UNIQUE NOT NULL, "
                + " password VARCHAR(32) NOT NULL, "
                + " PRIMARY KEY (user_id))";
        stmt.execute(sql);
        stmt.close();
        con.close();
        
    }
    
    /**
     * initializes the database table for the users, add the initial admin
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public void initTable() throws SQLException, ClassNotFoundException{
        
        Connection con = DBConnection.getConnection();
        Statement stmt = con.createStatement();
        String insertQuery = "INSERT INTO user (username, password) "
                + "VALUES ('admin', 'admin')";
        
        stmt.executeUpdate(insertQuery);
        stmt.close();
        con.close();
        
    }
    
    /**
     * checks if a user exists in the database by providing the username and password
     * @param username the username of the user you want to check
     * @param password the password of the user you want to check
     * @return true if the user exists, false if the user does NOT exist
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public boolean userExists(String username, String password) 
            throws SQLException, ClassNotFoundException{
        
        Connection con = DBConnection.getConnection();
        PreparedStatement pstmt;
        ResultSet rs;
        
        try{
            
            String query = "SELECT EXISTS(SELECT * FROM user "
                    + "WHERE username = ? AND password = ?)";
            
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();
            
            boolean result = false;
            if(rs.next()){
                result = rs.getInt(1) == 1;
            }
            pstmt.close();
            con.close();
            return result;
            
        }catch(SQLException e){
            System.err.println("Got an exception");
            System.err.println(e.getMessage());
        }
        
        return false;
        
    }
    
}

