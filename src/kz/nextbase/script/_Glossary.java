package kz.nextbase.script;

import kz.flabs.dataengine.Const;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.users.User;

public class _Glossary extends _Document{
	public Glossary glos;
		
	public _Glossary(Glossary glos, _Session ses) {
		super(glos, ses);
		this.glos = glos;
		currentUserID = Const.sysUser;
	}

	public _Glossary(Glossary glos, String userID){
		super(glos, userID);
		this.glos = glos;
		currentUserID = userID;
	}
	
	
	@Deprecated
	public String getGlossaryForm(){
		return glos.getForm();
	}
    
	public String getCode() throws _Exception{
		try {
			return glos.getValueAsString("code")[0];
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.getCode()");
		}
	}
	
	public String getName() throws _Exception{
		try {
			return glos.getValueAsString("name")[0];
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.getName()");
		}
	}
	public String getRank() throws _Exception{
		try {
			return glos.getValueAsString("rank")[0];
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.getRank()");
		}
	}
	public String getNomenclature() throws _Exception{
		try {
			return glos.getValueAsString("ndelo")[0];
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.getNomenclature()");
		}
	}
	
	public String getDirection() throws _Exception{
		try {
			return glos.getValueAsString("direction")[0];
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.getDirection()");
		}
	}
	
	public String getCountry() throws _Exception{
		try {
			return glos.getValueAsString("country")[0];
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.getCountry()");
		}
	}
	
	public String getRang() throws _Exception{
		try {
			return glos.getValueAsString("rang")[0];
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.getRang()");
		}
	}
	public String getTitle() throws _Exception{
		try {
			return glos.getValueAsString("title")[0];
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.getTitle()");
		}
	}
	
	public String getShortName() throws _Exception{
		try {
			return glos.getValueAsString("shortname")[0];
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.getShortName()");
		}
	}
	public String getCategory() throws _Exception{
		try {
			return glos.getValueAsString("corrcat")[0];
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.getCategory()");
		}
	}
	public String getRankText() throws _Exception{
		try {
			return glos.getValueAsString("ranktext")[0];
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.getRankText()");
		}
	}
	public void setName(String value) {
		glos.setName(value);
	}
	
	public void setCode(String code) throws DocumentException{
		glos.setCode(code);
	}	
	public void setDirection(String value) {
		glos.setName(value);
	}
	public void setCountry(String value) {
		glos.setCountry(value);
	}
	public void setCategory(String value) {
		glos.setCategory(value);
	}
		
	public void setShortName(String shortname) throws DocumentException{
		glos.setCode(shortname);
	}	
	public void setRank(String rank) throws DocumentException{
		glos.setRank(rank);
	}	
	public void setNomenclature(String ndelo) throws DocumentException{
		glos.setNomenclature(ndelo);
	}
	
	public void setTitle(String title) throws DocumentException{
		glos.setTitle(title);
	}
	
	public void setRang(String rang) throws DocumentException{
		glos.setRang(rang);
	}
	
	public void setRankText(String ranktext) throws DocumentException{
		glos.setRankText(ranktext);
	}
	public String getDdbID(){
		return glos.getDdbID();
	}	
		
	public boolean save(){
        try {   
            User user = new User(currentUserID);
            int result = glos.save(user);
            if (result > -1){
                return true;
            }else{
                return false;   
            }           
        } catch (DocumentAccessException e) {
            ScriptProcessor.logger.errorLogEntry(e);
        } catch (DocumentException e) {
            ScriptProcessor.logger.errorLogEntry(e);
        }   
        return false;
    }
	
	public boolean save(String absoluteUserID){
        try {   
            User user = new User(absoluteUserID);
            int result = glos.save(user);
            if (result > -1){
                return true;
            }else{
                return false;   
            }           
        } catch (DocumentAccessException e) {
            ScriptProcessor.logger.errorLogEntry(e);
        } catch (DocumentException e) {
            ScriptProcessor.logger.errorLogEntry(e);
        }   
        return false;
    }
}
