package mainClasses;

/**
 *
 * @author anton
 */
public class AnnualData {
    
    private String country_name;
    private Integer country_id, year, male_population, female_population, migration;
    private Float inflation;
    
    public void AnnualData(String country_name, Integer country_id, Integer year, Integer male_population, 
            Integer female_population, Float inflation, Integer migration){
        
        this.country_name = country_name;
        this.country_id = country_id;
        this.year = year;
        this.male_population = male_population;
        this.female_population = female_population;
        this.inflation = inflation;
        this.migration = migration;
        
    }
    
    public void setCountryName(String country_name){
        this.country_name = country_name;
    }
    
    public String getCountryName(){
        return this.country_name;
    }
    
    public void setCountryId(Integer country_id){
        this.country_id = country_id;
    }
    
    public Integer getCountryId(){
        return this.country_id;
    }
    
    public void setYear(Integer year){
        this.year = year;
    }
    
    public Integer getYear(){
        return this.year;
    }
    
    public void setMalePopulation(Integer male_population){
        this.male_population = male_population;
    }
    
    public Integer getMalePopulation(){
        return this.male_population;
    }
    
    public void setFemalePopulation(Integer female_population){
        this.female_population = female_population;
    }
    
    public Integer getFemalePopulation(){
        return this.female_population;
    }
    
    public void setInflation(Float inflation){
        this.inflation = inflation;
    }
    
    public Float getInflation(){
        return this.inflation;
    }
    
    public void setMigration(Integer migration){
        this.migration = migration;
    }
    
    public Integer getMigration(){
        return this.migration;
    }
    
}

