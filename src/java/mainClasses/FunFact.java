package mainClasses;

/**
 *
 * @author anton
 */
public class FunFact {
    
    private int country_id, fun_fact_no;
    private String fun_fact;
    
    public void FunFact(int country_id, int fun_fact_no, String fun_fact){
        this.country_id = country_id;
        this.fun_fact = fun_fact;
        this.fun_fact_no = fun_fact_no;
    }
    
    public void setCountryId(int country_id){
        this.country_id = country_id;
    }
    
    public int getCountryId(){
        return this.country_id;
    }
    
    public void setFunFact(String fun_fact){
        this.fun_fact = fun_fact;
    }
    
    public String getFunFact(){
        return this.fun_fact;
    }
    
    public void setFunFactNo(int fun_fact_no){
        this.fun_fact_no = fun_fact_no;
    }
    
    public int getFunFactNo(){
        return this.fun_fact_no;
    }

}

