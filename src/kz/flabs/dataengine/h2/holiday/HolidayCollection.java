package kz.flabs.dataengine.h2.holiday;

import java.util.ArrayList;
import java.util.Calendar;

public class HolidayCollection {
	int year;
	public ArrayList<Holiday> holidays = new  ArrayList<Holiday>();
	 
	public HolidayCollection(int year){
		this.year = year;
	}
	
	void addHoloday(Holiday holiday){
	    this.holidays.add(holiday);
	}
	
	public Calendar[] getHolydays(){
		Calendar[] c = new Calendar[0];
		return c;
		
	}

    public void setHolidays(ArrayList<Holiday> holidays){
        this.holidays = holidays;
    }
}
