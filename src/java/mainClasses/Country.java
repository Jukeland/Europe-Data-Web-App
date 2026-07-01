package mainClasses;

/**
 *
 * @author anton
 */
public class Country {
    
    private int country_id;
    private String country_name, country_code;
    
    public void Country(int country_id, String country_name, String country_code){
        this.country_id = country_id;
        this.country_name = country_name;
        this.country_code = country_code;
    }
    
    public void setCountryId(int country_id){
        this.country_id = country_id;
    }
    
    public int getCountryId(){
        return this.country_id;
    }
    
    public void setCountryName(String country_name){
        this.country_name = country_name;
    }
    
    public String getCountryName(){
        return this.country_name;
    }
    
    public void setCountryCode(String country_code){
        this.country_code = country_code;
    }
    
    public String getCountryCode(){
        return this.country_code;
    }
    
}

