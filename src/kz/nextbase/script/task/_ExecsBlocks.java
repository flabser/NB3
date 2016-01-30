package kz.nextbase.script.task;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.IStructure;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.task.ExecsBlock;
import kz.flabs.runtimeobj.document.task.Executor;
import kz.flabs.util.Util;
import kz.nextbase.script.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class _ExecsBlocks implements _IXMLContent  {
	private ExecsBlock execs;
	private _Session ses;

	_ExecsBlocks( _Session ses){
		execs = new ExecsBlock();
		this.ses = ses;
	}


	public _ExecsBlocks(_Session ses, ExecsBlock execs) {
		this.execs = execs;
		this.ses = ses;
	}

	boolean hasExecutor(String userID){
		for(Executor e: execs.getExecutors()){
			if (e.getID().equals(userID)) return true;
		}
		return false;

	}

	int getExecutorsCount(){
		return execs.getExecutorsCount();

	}

	int getResetedExecutorsCount(){
		return execs.getResetedExecutorsCount();

	}

    public void addExtExecutor(String complexString) {

    }

	public void addExecutor(String complexString){
        Executor.ExecutorType type = Executor.ExecutorType.INTERNAL;
		StringTokenizer t = new StringTokenizer(complexString,"`");
		while(t.hasMoreTokens()){
			switch(t.nextToken()) {
                case "1":
                    type = Executor.ExecutorType.INTERNAL;
                    break;
                case "2":
                    type = Executor.ExecutorType.EXTERNAL;
                    break;
            }
            Executor taskExecutor = new Executor(t.nextToken(), type);
            //taskExecutor.setID(t.nextToken());
			String o = t.nextToken().toString();
			try{
				taskExecutor.setResponsible(Integer.valueOf(o));
			}catch(NumberFormatException e){
				taskExecutor.setResponsible(0);
			}
			try{
				o = t.nextToken().toString();
				Date resetDate = Util.convertStringToDateTimeSilently(o);
				if (resetDate != null){
					taskExecutor.setResetDate(resetDate);
					taskExecutor.resetAuthorID = t.nextToken();
					taskExecutor.isReset = true;
				}else{		
					taskExecutor.isReset = false;
				}
			}catch (java.util.NoSuchElementException nse){
				taskExecutor.isReset = false;
			}
			execs.addExecutor(taskExecutor);
		}		
	}


	public ArrayList<_Executor> getExecutors(){
		ArrayList<_Executor> execsList = new ArrayList<_Executor>();
		for(kz.flabs.runtimeobj.document.task.Executor e:execs.getExecutors()){
			execsList.add(new _Executor(ses, e));
		}
		return execsList;

	}

	public String getExecutorsAsText(){
		String result = "";
		for(Executor e:execs.getExecutors()){
			result += e.getID() + ",";
		}
		return result;

	}

	@Override
	public String toXML() throws _Exception {
		StringBuffer xmlContent = new StringBuffer(10000);
        IStructure struct = ses.getCurrentDatabase().getBaseObject().getStructure();
		if (!execs.getExecutors().isEmpty()) {
			xmlContent.append("<executors>");
			ArrayList<Executor> execsList = execs.getExecutors();
			for (int i = 0; i < execsList.size(); i++) {
				Executor e = execsList.get(i);
				xmlContent.append("<entry><num>" + e.num + "</num><userid>" + e.getID() + "</userid>");
                xmlContent.append("<type>" + e.type + "</type>");
                String fullName = "";
                String shortName = "";
                String form = "";
                String corr_viewtext = "";
                switch (e.type) {
                    case INTERNAL:
                        Employer emp = struct.getAppUser(e.getID());
                        if(emp != null){
                            fullName = emp.getFullName();
                            shortName = emp.getShortName();
                            form = emp.form;
                        }
                        break;
                    case EXTERNAL:
                        _Glossary glos = ses.getCurrentDatabase().getGlossaryByID(e.getID());
                        if (glos != null) {

                            if (glos.getField("name") != null) {
                                fullName = glos.getName();
                            }

                            if (glos.getField("shortname") != null) {
                                shortName = glos.getShortName();
                            }

                            form = glos.getDocumentForm();
                            try {
                                _Glossary parentDoc = (_Glossary) glos.getParentDocument();
                                if ("corr".equalsIgnoreCase(parentDoc.getDocumentForm())) {
                                    corr_viewtext = parentDoc.getViewText();
                                }
                            } catch (Exception ex) {
                                AppEnv.logger.errorLogEntry(ex);
                            }
                        }
                        break;
                }
                xmlContent.append("<form>" + form + "</form>");
                xmlContent.append("<corr_viewtext>" + corr_viewtext + "</corr_viewtext>");
                xmlContent.append("<shortname attrval=\"" + e.getID() + "\">" + (shortName != null ? shortName : "") + "</shortname>");
                xmlContent.append("<fullname attrval=\""  + e.getID() + "\">" + (fullName  != null ? fullName  : "") + "</fullname>");
				xmlContent.append("<isreset>" + e.isReset + "</isreset>" +
						"<resetauthorid>" + e.resetAuthorID + "</resetauthorid>");
				Employer emp1 = struct.getAppUser(e.resetAuthorID);
				if(emp1 != null){						
					xmlContent.append("<resetauthorname attrval=\"" + emp1.getUserID() + "\">" + emp1.getShortName() + "</resetauthorname>");
				}else{
					xmlContent.append("<resetauthorname attrval=\"" + e.resetAuthorID + "\"></resetauthorname>");
				}
				xmlContent.append("<resetdate>" + _Helper.getDateAsStringSilently(e.getResetDate()) + "</resetdate>");
				xmlContent.append("<responsible>" + e.responsible + "</responsible>");
				xmlContent.append("<comment>" + e.comment + "</comment></entry>");
			}
			xmlContent.append("</executors>");
		}

		return xmlContent.toString();
	}


	public ExecsBlock getBaseObject() {
		return execs;
	}




}
