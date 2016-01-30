package kz.nextbase.script.struct;


import kz.flabs.runtimeobj.document.structure.UserRole;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IXMLContent;

public class _UserRole implements _IXMLContent {
	private UserRole role;
	
	public _UserRole(UserRole r) {
		role = r;
	}

	public String toString(){
		return role.getName();
	}

	@Override
	public String toXML() throws _Exception {	
		return "<name>" + role.getName() + "</name><appid>" + role.getApplication() + "</appid><description>" + role.getDescription() + "</description>" ;
	}
	

}
