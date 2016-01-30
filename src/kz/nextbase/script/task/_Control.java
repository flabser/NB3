package kz.nextbase.script.task;

import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.task.Control;
import kz.flabs.runtimeobj.document.task.Control.Shift;
import kz.flabs.util.Util;
import kz.nextbase.script._Database;
import kz.nextbase.script._Document;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._Session;
import kz.nextbase.script.constants._AllControlType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class _Control implements _IXMLContent  {
	private Control control;
	private _Session ses;

	public _Control( _Session ses){
		control = new Control();
		this.ses = ses;
	}

	_Control(_Session ses, Date c, boolean sixWorkdays, Date pcd){
		this.ses = ses;
		Calendar currentDate = Calendar.getInstance();	
		Calendar controlDate = Calendar.getInstance();	
		currentDate.setTime(c);
		controlDate.setTime(pcd);
		control = new Control(currentDate, sixWorkdays, controlDate);
	}	

	_Control(_Session ses, Date c, boolean sixWorkdays, double priority, double complication){
		this.ses = ses;
		Calendar currentDate = Calendar.getInstance();	
		currentDate.setTime(c);
		control = new Control(currentDate, sixWorkdays, priority, complication);
	}

    _Control(_Session ses, Date c, boolean sixWorkdays, double priority, double complication, double coefficient){
        this.ses = ses;
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(c);
        control = new Control(currentDate, sixWorkdays, priority, complication, coefficient);
    }

    _Control(_Session ses, Date c, boolean sixWorkdays, int days){
		this.ses = ses;
		Calendar currentDate = Calendar.getInstance();	
		currentDate.setTime(c);
		control = new Control(currentDate, sixWorkdays, days);
	}	


	public _Control(_Session ses, Control control) {
		this.control = control;
		this.ses = ses;
	}

	public void setBaseExecDate(Date d){
		control.setPrimaryCtrlDate(d);
	}

	public void setWeight(double priority, double complication){
		control.setWeight(priority, complication);
	}

	public void addProlongation(int days, String reason){
		control.addProlongation(days, reason, ses.getCurrentUserID());
	}

    public void addMarkOfExpiration(int days) {
        control.addMarkOfExpiration(days);
    }

	public Date getPrimaryCtrlDate(){
		return control.getPrimaryCtrlDate().getTime();
	} 

	public Date getStartDate() {
		Calendar startDate = control.getStartDate();
		if (startDate != null) {
			return startDate.getTime();
		} else {
			return null;
		}
	}

    public Date getResettDate() {
        Calendar resetDate = control.getResetDate();
        if (resetDate != null) {
            return resetDate.getTime();
        } else {
            return null;
        }
    }

	public void setStartDate(Date startDate) {
		control.setStartDate(startDate);
	}
	public void setResetDate(Date resetDate) {
		control.setResetDate(resetDate);
	}

	public Date getCtrlDate(){
		return control.getCtrlDate().getTime();
	} 

	public int getDiffBetweenDays(Date c){
		Calendar currentDate = Calendar.getInstance();	
		currentDate.setTime(c);
		return control.getDiffBetweenDays(currentDate);
	}

	public int getDiffBetweenDays(){
		return control.getDiffBetweenDays(Calendar.getInstance());
	}

	public void setAllControl(_AllControlType ac){
		switch (ac) {
			case RESET:
				control.setAllControl(0);
				break;
			case ACTIVE:
				control.setAllControl(1);
				break;
			case READY_TO_RESET:
				control.setAllControl(2);
				break;
			case AWAITING:
				control.setAllControl(3);
				break;
			default:
				control.setAllControl(1);
				break;
		}
	}

	public int getAllControl(){
		return control.getAllControl();
	}
	
	@Deprecated
	public int getAllContr(){
		return control.getAllControl();
	}

	public void setCycle(int cycle) {
		control.setCycle(cycle);
	}

	public void setOld(int isOld) {
		control.setOld(isOld);	
	}


	public String toXML() throws _Exception{
		StringBuffer xmlContent = new StringBuffer(10000);

		xmlContent.append("<primaryctrldate>" + Util.dateTimeFormat.format(control.getPrimaryCtrlDate().getTime()) + "</primaryctrldate>");
		xmlContent.append("<ctrldate>" + Util.dateTimeFormat.format(getCtrlDate().getTime()) + "</ctrldate>");
		xmlContent.append("<startdate>" + (getStartDate() != null ? Util.dateTimeFormat.format(getStartDate()) : "") + "</startdate>");
		xmlContent.append("<cyclecontrol>" + control.getCycle() + "</cyclecontrol>");
		xmlContent.append("<allcontrol>" + getAllContr() + "</allcontrol>");
		xmlContent.append("<isold>" + control.getOld() + "</isold>");
		xmlContent.append("<priority>" + control.getPriority() + "</priority>");
		xmlContent.append("<complication>" + control.getComplication() + "</complication>");
		xmlContent.append("<diff>" + control.getDiffBetweenDays((control.getAllControl() == 0 && control.getResetDate() != null) ? control.getResetDate() : Calendar.getInstance()) + "</diff>");

		if (!control.getShifts().isEmpty()) {
			xmlContent.append("<shift>");
			ArrayList<Shift> shiftList = control.getShifts();
			for (int i = 0; i < shiftList.size(); i++) {
				Shift s = shiftList.get(i);

				xmlContent.append("<entry><days>+" + s.days + "</days>");
				_Database currentDb = ses.getCurrentDatabase();
				try{
					_Document doc = currentDb.getDocumentByID(s.reason);
					xmlContent.append("<reason>" + doc.getValueString("name") + "</reason>");
				}catch(Exception e){
					xmlContent.append("<reason>" + s.reason + "</reason>");
				}
				
				try{
					Employer emp = currentDb.getBaseObject().getStructure().getAppUser(s.author);
					xmlContent.append("<author attrval=\"" + s.author + "\">" + emp.getFullName() + "</author>");
				}catch(Exception e){
					xmlContent.append("<author>" + s.author + "</author>");
				}
				
				xmlContent.append("<date>"	+ Util.convertDataTimeToString(s.date) + "</date></entry>");
			}
			xmlContent.append("</shift>");
		}

		return xmlContent.toString();
	}

	public Control getBaseObject() {
		return control;
	}

	public String toString(){
		return control.toString();
	}



}
