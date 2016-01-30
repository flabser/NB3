package kz.flabs.webrule.scheduler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import kz.flabs.appenv.AppEnv;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ScheduleSettings{
	public ScheduleType schedulerType = ScheduleType.UNDEFINED;
	public Date lastUpdate = new Date();
	public RunMode isOn = RunMode.ON;
	public ArrayList<Date> startTimes = new ArrayList<Date>();
	public ArrayList<DaysOfWeek> daysOfWeek = new ArrayList<DaysOfWeek>();
	public int minInterval;
	
	private Calendar nextStart;

	public ScheduleSettings(String currentXmlFile, Node node){		
		try{

			if (XMLUtil.getTextContent(node,"@mode", false).equalsIgnoreCase("off")){                    
				isOn = RunMode.OFF;						
			}else{
//				setDaysOfWeek(node, "mininterval/@daysofweek");
				setDaysOfWeek(currentXmlFile, node, "days");
			
				String interval = XMLUtil.getTextContent(node,"mininterval", false);
				try{
					minInterval = Integer.parseInt(interval);
					schedulerType = ScheduleType.PERIODICAL;
				}catch(NumberFormatException e){
					NodeList fields =  XMLUtil.getNodeList(node,"starttime"); 
					
					
					int len = fields.getLength();
					if (len > 0){
						for(int i = 0; i < len; i++){							
							startTimes.add(Util.timeFormat.parse(XMLUtil.getTextContent(fields.item(i),".", false)));
						}
						String nextStartAsText = XMLUtil.getTextContent(node,"nextstart", true, "", false);
						nextStart = new GregorianCalendar();		
						try{					
							nextStart.setTime(Util.dateTimeFormat.parse(nextStartAsText));	
						}catch(ParseException pe){
							Calendar currentTime = new GregorianCalendar();		
							currentTime.setTime(new Date());
							Calendar scheduleTime = new GregorianCalendar();		
							scheduleTime.setTime(startTimes.get(0));
							nextStart.set(currentTime.get(Calendar.YEAR),currentTime.get(Calendar.MONTH),currentTime.get(Calendar.DAY_OF_MONTH), 
									scheduleTime.get(Calendar.HOUR_OF_DAY),scheduleTime.get(Calendar.MINUTE));	
						//	Server.logger.verboseLogEntry("Current: " + Util.dateTimeFormat.format(currentTime.getTime()));
						//	Server.logger.verboseLogEntry("NextStart " + Util.dateTimeFormat.format(nextStart.getTime()));							
							if (currentTime.compareTo(nextStart) >= 0){
								nextStart.set(nextStart.get(Calendar.YEAR),nextStart.get(Calendar.MONTH), nextStart.get(Calendar.DAY_OF_MONTH) + 1, 
										nextStart.get(Calendar.HOUR_OF_DAY),nextStart.get(Calendar.MINUTE));	
							}
						//	Server.logger.verboseLogEntry("NextStart " + Util.dateTimeFormat.format(nextStart.getTime()));		
						}				
						schedulerType = ScheduleType.INTIME;
					}else{
						isOn = RunMode.OFF;	
					}	
				}
			}
		}catch(Exception e) {     
			AppEnv.logger.errorLogEntry(e);		
			isOn = RunMode.OFF;	
		}
	}

	public ScheduleSettings(int mi){				             
		isOn = RunMode.ON;		
		daysOfWeek.add(DaysOfWeek.MONDAY);
		daysOfWeek.add(DaysOfWeek.TUESDAY);
		daysOfWeek.add(DaysOfWeek.WEDNESDAY);
		daysOfWeek.add(DaysOfWeek.THURSDAY);
		daysOfWeek.add(DaysOfWeek.FRIDAY);
		minInterval = mi;
		schedulerType = ScheduleType.PERIODICAL;
	}

	public void setNextStart(Calendar ns){
		nextStart = ns;
	}

	public Calendar getStartTime(){
		if (schedulerType == ScheduleType.PERIODICAL){

			if (nextStart == null){
				Calendar startTime = new GregorianCalendar();		
				startTime.setTime(new Date());
				startTime.set(startTime.get(Calendar.YEAR),startTime.get(Calendar.MONTH),startTime.get(Calendar.DAY_OF_MONTH),
						startTime.get(Calendar.HOUR_OF_DAY),startTime.get(Calendar.MINUTE) + 3);
				nextStart = startTime;
			}
			return nextStart;

		}else if(schedulerType == ScheduleType.INTIME){
			return nextStart;

		}
		return null;
	}

	public String toString(){
		return " scheduler=" + schedulerType;
	}


	private void setDaysOfWeek(String currentXmlFile, Node node, String xPath){
		String daysAsText = XMLUtil.getTextContent(node,xPath, true, "WORKWEEK", false);
		StringTokenizer st = new StringTokenizer(daysAsText,",");
		try{
    		while(st.hasMoreElements()){
    			String token = st.nextToken().trim();
    			if (token.equalsIgnoreCase("WORKWEEK") || token.equalsIgnoreCase("ALL_WEEK")){
    				daysOfWeek.add(DaysOfWeek.valueOf(token));
    				break;
    			}else{
    				daysOfWeek.add(DaysOfWeek.valueOf(token));
    			}
    		}
		}catch(Exception e){
		    e.printStackTrace();
		    System.err.println("current xml file : " + currentXmlFile);
		    daysOfWeek.add(DaysOfWeek.WORKWEEK);
		}
	}

}
