package kz.flabs.dataengine.h2.holiday;

import java.util.Calendar;
import java.util.Date;

public class Holiday {
    private int id;
    private String country;
    private String title;
    private int repeat;
    private Calendar startDate = Calendar.getInstance();
    private int continuing;
    private Calendar endDate = Calendar.getInstance();
    private int ifFallSon;
    private String comment;
    
    
    public Holiday(int id, String country, String title, int repeat, Date startDate, 
                   int continuing, Date endDate, int ifFallSon, String comment){
        this.setCountry(country);
        this.setTitle(title);
        this.setRepeat(repeat);
        this.setStartDate(startDate);
        this.setContinuing(continuing);
        this.setEndDate(endDate);
        this.setIfFallSon(ifFallSon);
        this.setComment(comment);
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    public String getCountry(){
        return country;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
    public void setRepeat(int repeat){
        this.repeat = repeat;
    }
    public int getRepeat(){
        return repeat;
    }
    public void setStartDate(Date startDate){
        this.startDate.setTime(startDate);
    }
    public Calendar getStartDate(){
        return startDate;
    }
    public void setContinuing(int continuing){
        this.continuing = continuing;
    }
    public int getContinuing(){
        return continuing;
    }
    public void setEndDate(Date endDate){
        this.endDate.setTime(endDate);
    }
    public Calendar getEndDate(){
        return endDate;
    }
    public void setIfFallSon(int ifFallSon){
        this.ifFallSon = ifFallSon;
    }
    public int getIfFallSon(){
        return ifFallSon;
    }
    public void setComment(String comment){
        this.comment = comment;
    }
    public String getComment(){
        return comment;
    }
    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return id;
    }
}
