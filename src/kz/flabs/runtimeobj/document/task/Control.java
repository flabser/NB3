package kz.flabs.runtimeobj.document.task;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.h2.holiday.HolidayCollection;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.runtimeobj.document.AbstractComplexObject;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.util.adapters.CalendarAdapter;
import kz.pchelka.env.Environment;
import org.apache.commons.lang3.time.DateUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@XmlRootElement(name = "class")
@XmlAccessorType(XmlAccessType.FIELD)
public class Control extends AbstractComplexObject implements Serializable {

	private static final int v = 60 * 60 * 1000;
	private static final long serialVersionUID = 1L;
	private static final float PRIORITY_FACTOR = 2f;
    private static final double COEFFICIENT = 45.0 / 19.0;

    @XmlElement(name = "cyclecontrol")
    private int cycle = 1;

    @XmlElement(name = "allcontrol")
	private int allControl = 1;

    @XmlElement(name = "isold")
    private int old = 0;

    @XmlTransient
	@Deprecated
    private Task document;

    @XmlTransient
	private static Calendar[] holidays = Environment.systemBase.getHolidayCol(
			Calendar.getInstance().get(Calendar.YEAR), 0, 100).getHolydays();

    @XmlElement(name = "primaryctrldate")
    @XmlJavaTypeAdapter(CalendarAdapter.class)
    private Calendar primaryCtrlDate;

    @XmlTransient
	private boolean sixWorkdays = false;

    @XmlElement(name = "priority")
	private double priority;

    @XmlElement(name = "complication")
    private double complication;

    @XmlElement(name = "shift")
	private ArrayList<Shift> shifts = new ArrayList<>();

    @XmlElement(name = "expiration")
    private  ArrayList<Expiration> expirations = new ArrayList<>();

    @XmlElement(name = "startdate")
    @XmlJavaTypeAdapter(CalendarAdapter.class)
	private Calendar startDate;

    @XmlElement(name = "resetdate")
    @XmlJavaTypeAdapter(CalendarAdapter.class)
    private Calendar resetDate;

    @XmlAttribute
    private String className = Control.class.getName();

	public Control() {
		primaryCtrlDate = Calendar.getInstance();
		primaryCtrlDate.add(Calendar.MONTH, 1);
	}

    public Control(Calendar cd, boolean sw, double priority, double complication, double coefficient) {
        startDate = cd;
		sixWorkdays = sw;
        int days = getDayWeight(priority, complication, coefficient);
        primaryCtrlDate = addExtraDays(cd, days, sw, new HolidayCollection(cd.get(Calendar.YEAR)));
		if (!DateUtils.isSameDay(cd, Calendar.getInstance())) {
			this.setAllControl(3);
		}
    }

    public Control(Calendar cd, boolean sw, double priority, double complication) {
		startDate = cd;
		sixWorkdays = sw;
		int days = getDayWeight(priority, complication);
		primaryCtrlDate = addExtraDays(cd, days, sw, new HolidayCollection(cd.get(Calendar.YEAR)));
		if (!DateUtils.isSameDay(cd, Calendar.getInstance())) {
			this.setAllControl(3);
		}
	} 
	
	public Control(Calendar cd, boolean sw, Calendar pcd) {
		startDate = cd;
		sixWorkdays = sw;
		primaryCtrlDate = pcd;
		if (!DateUtils.isSameDay(cd, Calendar.getInstance())) {
			this.setAllControl(3);
		}
	}

	public Control(Calendar cd, boolean sw, int days) {
		startDate = cd;
		sixWorkdays = sw;
		primaryCtrlDate = addExtraDays(cd, days, sw, new HolidayCollection(cd.get(Calendar.YEAR)));
		if (!DateUtils.isSameDay(cd, Calendar.getInstance())) {
			this.setAllControl(3);
		}
	}

	public Calendar getStartDate() {
		return startDate;
	}
	public Calendar getResetDate() {
		return resetDate;
	}

	public void setStartDate(Date startDate) {
		if (this.startDate == null) {
			this.startDate = Calendar.getInstance();
		}
		this.startDate.setTime(startDate);
	}

	public void setResetDate(Date resetDate) {
		if (this.resetDate == null) {
			this.resetDate = Calendar.getInstance();
		}
		this.resetDate.setTime(resetDate);
	}

	@Deprecated
	public void setDocument(Document document) {
		this.document = (Task) document;
	}


	@Deprecated
	public Date getExecDate() {
		return primaryCtrlDate.getTime();
	}

	public void setPrimaryCtrlDate(Date execDate) {
		primaryCtrlDate.setTime(execDate);
	}

	public int getCycle() {
		return cycle;
	}

	public void setCycle(int cycle) {
		this.cycle = cycle;
	}

	public int getAllControl() {
		return allControl;
	}

	public void setAllControl(int allControl) {
		this.allControl = allControl;
	}

	public int getOld() {
		return old;
	}

	public void setOld(int isOld) {
		this.old = isOld;
	}

	@Deprecated
	public int getDayDiff() {
		if (document != null) {
			return document.getAppEnv().getDataBase().getTasks()
					.recalculate(document.getDocID());
		} else {
			return 30;
		}
	}

	@Deprecated
	public Calendar addExtraDays(Calendar initDate, double dayCount) {
	    Calendar startDate = Calendar.getInstance(),
        tempDate = Calendar.getInstance(),
        fromTime = Calendar.getInstance();
	    
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy.HH.mm.ss");
        try {
            tempDate.setTime(format.parse("01.01.2000.18.00.00"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        tempDate.set(fromTime.get(Calendar.YEAR), fromTime.get(Calendar.MONTH), 
                fromTime.get(Calendar.DAY_OF_MONTH));
        
        switch(fromTime.get(Calendar.DAY_OF_WEEK)){
        case Calendar.SATURDAY: startDate.setTimeInMillis(tempDate.getTimeInMillis()-24 * v); break;
        case Calendar.SUNDAY:   startDate.setTimeInMillis(tempDate.getTimeInMillis()-48 * v); break;
        default:
            startDate.setTimeInMillis(tempDate.getTimeInMillis()); break;
        }
        
        
        initDate.setTimeInMillis(startDate.getTimeInMillis());
        for(int i = 0; i < (int)dayCount; i++){
            if(initDate.get(Calendar.DAY_OF_WEEK)!=Calendar.FRIDAY){
                initDate.setTimeInMillis(initDate.getTimeInMillis()+24 * v);
            }else{
                initDate.setTimeInMillis(initDate.getTimeInMillis()+72 * v);
            }
        }
        
        return initDate;
	}

	public Calendar addExtraDays(Calendar baseDate, int days, boolean sixWorkdays, HolidayCollection holidays){
		Calendar ctrlDate;
		ctrlDate = (Calendar) baseDate.clone();
		if(days==0)return ctrlDate;
		int sign = days/Math.abs(days);

		while(days != 0){
			for(int i = 0; i < holidays.getHolydays().length; i++){
				if(ctrlDate.get(Calendar.MONTH)==holidays.getHolydays()[i].get(Calendar.MONTH)&&
						ctrlDate.get(Calendar.DAY_OF_MONTH)==holidays.getHolydays()[i].get(Calendar.DAY_OF_MONTH)){
					days += sign;
					break;
				}
			}
			if(ctrlDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || 
					(!sixWorkdays && ctrlDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)){
				days += sign;
			}
			ctrlDate.setTimeInMillis(ctrlDate.getTimeInMillis() + sign * 24 * v);
			days -= sign;
		}

		boolean isHoliday = false;
		for(int i = 0; i < holidays.getHolydays().length; i++){
			if(ctrlDate.get(Calendar.MONTH)==holidays.getHolydays()[i].get(Calendar.MONTH)&&
					ctrlDate.get(Calendar.DAY_OF_MONTH)==holidays.getHolydays()[i].get(Calendar.DAY_OF_MONTH)){
				isHoliday = true;
				break;
			}
		}
		if(isHoliday || ctrlDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || 
				(!sixWorkdays && ctrlDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)){
			if (sign != 0) ctrlDate.setTimeInMillis(addExtraDays(ctrlDate, sign, sixWorkdays, holidays).getTimeInMillis() - sign * 24 * v);
		}

		return ctrlDate;
	}

	@Deprecated
	public Calendar setWeight(double p, double c) {
		priority = p;
		complication = c;
		double dayCount = (int)(priority + complication) / PRIORITY_FACTOR; 
		return addExtraDays(primaryCtrlDate, dayCount);
	}

	public Calendar getPrimaryCtrlDate() {
		return  primaryCtrlDate;
	}

	public Calendar getCtrlDate() {
		Calendar cd = (Calendar) primaryCtrlDate.clone();
		int days = 0;
		for(Shift p: getShifts()){
			days += p.days;
		}
		return  addExtraDays(cd, days, sixWorkdays, new HolidayCollection(primaryCtrlDate.get(Calendar.YEAR)));
	}

    public void resetTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));
    }

	public int getDiffBetweenDays(Calendar currentDate) {
		Calendar cd = getCtrlDate();
		
		Calendar currDate, ctrlDate;
        int sign = 1;
        if(currentDate.getTimeInMillis() > cd.getTimeInMillis()){
            currDate = (Calendar) cd.clone();
            ctrlDate = (Calendar) currentDate.clone();
            sign = -1;
        }else{
            currDate = (Calendar) currentDate.clone();
            ctrlDate = (Calendar) cd.clone();
        }

        resetTime(currDate);
        resetTime(ctrlDate);

		int dayOfWeek, workDayCount, allDayCount, weekCount;
		allDayCount = (int)((ctrlDate.getTimeInMillis() - currDate
				.getTimeInMillis()) / (v * 24));
		weekCount = (int)(allDayCount / 7);
		workDayCount = (sixWorkdays ? weekCount * 6 : weekCount * 5);
		dayOfWeek = currDate.get(Calendar.DAY_OF_WEEK);

		for (int i = 0; i < allDayCount - weekCount * 7; i++) {
			if ((sixWorkdays ? dayOfWeek != 1
					: (dayOfWeek != 1 && dayOfWeek != 7))) {
				workDayCount++;
			}
			if (dayOfWeek != 7) {
				dayOfWeek++;
			} else {
				dayOfWeek = 1;
			}
		}

		Calendar[] tHolidays = (Calendar[]) holidays.clone();

		for (int i = currDate.get(Calendar.YEAR); i <= ctrlDate.get(Calendar.YEAR); i++) {
			for (int j = 0; j < tHolidays.length; j++) {
				tHolidays[j].set(Calendar.YEAR, i + 1900);
				if (currDate.getTime().compareTo(tHolidays[j].getTime())
						* tHolidays[j].getTime().compareTo(ctrlDate.getTime()) > 0) {
					workDayCount--;
				}
			}
		}
		
		return workDayCount * sign;
	}


	public void addProlongation(int days, String reason, String author) {
		Shift prol = new Shift();
		prol.days = days;
		prol.reason = reason;
		prol.author = author;
		prol.date = new Date();
		shifts.add(prol);
	}

    public void addMarkOfExpiration(int days) {
        Expiration expiration = new Expiration();
        expiration.recalcdate = Calendar.getInstance();
        expiration.countofdays = days;
        expirations.add(expiration);
    }

	public ArrayList<Shift> getShifts() {
		return shifts;
	}

	@Override
	public String getContent() {
		StringBuffer xmlContent = new StringBuffer(10000);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		//xmlContent.append("<primaryctrldate>" + Util.dateTimeFormat.format(primaryCtrlDate.getTime()) + "</primaryctrldate>");
        xmlContent.append("<primaryctrldate>" + sdf.format(primaryCtrlDate.getTime()) + "</primaryctrldate>");
		xmlContent.append("<cyclecontrol>" + getCycle() + "</cyclecontrol>");
		xmlContent.append("<allcontrol>" + getAllControl() + "</allcontrol>");
		xmlContent.append("<isold>" + getOld() + "</isold>");
		xmlContent.append("<priority>" + getPriority() + "</priority>");
		xmlContent.append("<complication>" + getComplication() + "</complication>");
		//xmlContent.append("<startdate>" + (startDate != null ? Util.dateTimeFormat.format(getStartDate().getTime()) : "") + "</startdate>");
        xmlContent.append("<startdate>" + (startDate != null ? sdf.format(getStartDate().getTime()) : "") + "</startdate>");

		if ((getShifts() != null) && (!getShifts().isEmpty())) {
			xmlContent.append("<shift>");
			for (int i = 0; i < getShifts().size(); i++) {
				Shift shift = getShifts().get(i);
				xmlContent.append("<entry><days>" + shift.days
						+ "</days><reason>"
						+ shift.reason
						+ "</reason><author>"
						+ shift.author
						+ "</author><date>"
						+ Util.convertDataTimeToString(shift.date)
						+ "</date></entry>");
			}
			xmlContent.append("</shift>");
		}
		return xmlContent.toString();

	}

	@Override
	public void init(IDatabase env, String initString) throws ComplexObjectException {

		try {
			initString = "<root>" + initString + "</root>";

			DocumentBuilderFactory DocFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder xmlDocBuilder = DocFactory.newDocumentBuilder();
			ByteArrayInputStream stream = new ByteArrayInputStream(
					initString.getBytes(Charset.forName("UTF-8")));
			org.w3c.dom.Document xmlDoc = xmlDocBuilder.parse(stream);

			if (!XMLUtil.getTextContent(xmlDoc, "/root/primaryctrldate").equals("")
					&& XMLUtil.getTextContent(xmlDoc, "/root/primaryctrldate") != null) {
				SimpleDateFormat format = new SimpleDateFormat(
						"dd.MM.yyyy hh:mm:ss");
				Date date = format.parse(XMLUtil.getTextContent(xmlDoc,
						"/root/primaryctrldate"));
				primaryCtrlDate.setTime(date);
			}
			if (!XMLUtil.getTextContent(xmlDoc, "/root/startdate").equals("")
					&& XMLUtil.getTextContent(xmlDoc, "/root/startdate") != null) {
				SimpleDateFormat format = new SimpleDateFormat(
						"dd.MM.yyyy hh:mm:ss");
				Date date = format.parse(XMLUtil.getTextContent(xmlDoc,
						"/root/startdate"));
				if (startDate == null) {
					startDate = Calendar.getInstance();
				}
				startDate.setTime(date);
			}
			if (!XMLUtil.getTextContent(xmlDoc, "/root/cyclecontrol").equals("")
					&& XMLUtil.getTextContent(xmlDoc, "/root/cyclecontrol") != null) {
				cycle = Integer.parseInt(XMLUtil.getTextContent(xmlDoc,
						"/root/cyclecontrol"));
			}
			if (!XMLUtil.getTextContent(xmlDoc, "/root/allcontrol").equals("")
					&& XMLUtil.getTextContent(xmlDoc, "/root/allcontrol") != null) {
				allControl = Integer.parseInt(XMLUtil.getTextContent(xmlDoc,
						"/root/allcontrol"));
			}
			if (!XMLUtil.getTextContent(xmlDoc, "/root/isold").equals("")
					&& XMLUtil.getTextContent(xmlDoc, "/root/isold") != null) {
				old = Integer.parseInt(XMLUtil
						.getTextContent(xmlDoc, "/root/isold"));
			}

			if (!XMLUtil.getTextContent(xmlDoc, "/root/priority").equals("")
					&& XMLUtil.getTextContent(xmlDoc, "/root/priority") != null) {
				priority = Double.parseDouble(XMLUtil.getTextContent(xmlDoc,
						"/root/priority"));
			}

			if (!XMLUtil.getTextContent(xmlDoc, "/root/complication").equals("")
					&& XMLUtil.getTextContent(xmlDoc, "/root/complication") != null) {
				complication = Double.parseDouble(XMLUtil.getTextContent(xmlDoc,
						"/root/complication"));
			}

			org.w3c.dom.Element root = xmlDoc.getDocumentElement();
			NodeList nodes = root.getElementsByTagName("entry");

			for (int i = 0; i < nodes.getLength(); i++) {
				Node entryNode = nodes.item(i);

				Shift prol = new Shift();
				if (!XMLUtil.getTextContent(entryNode, "days").equals("")
						&& XMLUtil.getTextContent(entryNode, "days") != null) {
					prol.days = Integer.parseInt(XMLUtil.getTextContent(entryNode,
							"days"));
				}
				if (!XMLUtil.getTextContent(entryNode, "reason").equals("")
						&& XMLUtil.getTextContent(entryNode, "reason") != null) {
					prol.reason = XMLUtil.getTextContent(entryNode, "reason");
				}
				prol.author = XMLUtil.getTextContent(entryNode, "author");
				prol.date = Util.convertStringToDateTimeSilently(XMLUtil.getTextContent(entryNode, "date"));

				shifts.add(prol);

			}
		} catch (ParserConfigurationException e) {
			throw new ComplexObjectException(e, getClass().getName());
		} catch (SAXException e) {
			throw new ComplexObjectException(e, getClass().getName());
		} catch (IOException e) {
			throw new ComplexObjectException(e, getClass().getName());
		} catch (ParseException e) {
			throw new ComplexObjectException(e, getClass().getName());
		}
	}


	public String toString(){
		return "primaryctrldate=" +  Util.dateTimeFormat.format(primaryCtrlDate.getTime()) + ", ctrldate=" +  Util.dateTimeFormat.format(getCtrlDate().getTime()) + 
				"cyclecontrol=" + getCycle() + ", isold=" + getOld() + ", diff=" + getDiffBetweenDays(Calendar.getInstance()) + ", prolongation=(" + getShifts() +
				"), startdate = " +  (startDate != null ? Util.dateTimeFormat.format(getStartDate().getTime()) : "null");
	}

	private int getDayWeight(double p, double c) {
		priority = p;
		complication = c;
//		double dayCount = (priority + complication) / PRIORITY_FACTOR; // по приоритету

		return (int) Math.round(COEFFICIENT * (p - 1));
	}

    /**
     * в методе не учитывается значение complication
     */
    private int getDayWeight(double p, double c, double coefficient) {
        priority = p;
        complication = c;
        return  (int) Math.round(priority * coefficient * 10 + 1) ;
    }

	public double getComplication() {
		return complication;
	}

	public double getPriority() {
		return priority;
	}

    @XmlRootElement(name = "expiration")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Expiration extends AbstractComplexObject {
        @XmlElement
        @XmlJavaTypeAdapter(CalendarAdapter.class)
        public Calendar recalcdate;

        @XmlElement
        public int countofdays;

        @Override
        public void init(IDatabase db, String initString) throws ComplexObjectException {

        }

        @Override
        public String getContent() {
            return null;
        }
    }

    @XmlRootElement(name = "shift")
    @XmlAccessorType(XmlAccessType.FIELD)
	public static class Shift extends AbstractComplexObject {
        @XmlElement
		public int days;
        @XmlElement
		public String reason;
        @XmlElement
		public String author;
        @XmlElement
		public Date date;

		public String toString(){
			return "days=" + days + ", reason=" +  reason + ", author=" + author;									
		}

        @Override
        public void init(IDatabase db, String initString) throws ComplexObjectException {

        }

        @Override
        public String getContent() {
            return null;
        }
    }

	@Deprecated
	public String getTypeID() {
		return "";
	}




}
