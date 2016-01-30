package kz.nextbase.script.struct;

import java.util.ArrayList;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.structure.EmployerCollection;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._Session;

public class _EmployerCollection  implements _IXMLContent {
	private EmployerCollection emps;
	private _Session ses;
	
	public _EmployerCollection(IDatabase db, _Session ses, String[] e){
		emps = new EmployerCollection(db);
		for (String userID:e){
			emps.addEmployer(userID);
		}
		this.ses = ses;
	}
	
	public _EmployerCollection(_Session ses, EmployerCollection emps) {
		this.emps = emps;
		this.ses = ses;
	}

	public ArrayList<_Employer> getEmployers(){
		ArrayList<_Employer> e = new ArrayList<_Employer>();
		for(Employer emp: emps.getEmployers()){
			e.add(new _Employer(emp));	
		}
		return e;
	}
	
	@Override
	public String toXML() throws _Exception {
		StringBuffer xmlContent = new StringBuffer(10000);
		for(Employer emp: emps.getEmployers()){
			xmlContent.append(new _Employer(emp, ses).toXML());	
		}
		return xmlContent.toString();
	}

	public EmployerCollection getBaseObject() {
		return emps;
	}
	
	public String toString(){
		return getEmployersAsText();
	}
	
	public String getEmployersAsText(){
		StringBuffer xmlContent = new StringBuffer(10000);
		for(Employer emp: emps.getEmployers()){
			xmlContent.append(emp.getUserID() + ",");	
		}
		return xmlContent.toString();
	}

}
