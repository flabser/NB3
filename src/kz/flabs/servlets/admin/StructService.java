package kz.flabs.servlets.admin;

import kz.flabs.dataengine.IStructure;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentException;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.users.User;
import kz.flabs.util.ResponseType;
import kz.flabs.util.XMLResponse;

import java.io.File;

public class StructService extends DatabaseServices {
	IStructure struct = db.getStructure();
	
	StructService(String dbID){
		super(dbID);		
	}
	
	public AdminProviderResult getOrganization(String id) throws DocumentException, ComplexObjectException{
		AdminProviderResult pr = new AdminProviderResult(PublishAsType.HTML, "forms"+File.separator+"document.xsl");		
		int docID = Integer.parseInt(id);
        User user = new User(sysUser);
		pr.setOutput(struct.getOrganization(docID, user).toXML(false));
		return pr;
	}
	
	public AdminProviderResult deleteOrganization(String orgID){
		AdminProviderResult pr = new AdminProviderResult(PublishAsType.XML, null);		
		int docID = Integer.parseInt(orgID);
		int result = struct.deleteOrganization(docID);				
		XMLResponse resp = new XMLResponse(ResponseType.DELETE_ORGANIZATION);
		if (result == -1){
			resp.setResponseStatus(false);
		}else{
			resp.setResponseStatus(true);
		}			
		pr.setOutput(resp.toXML());
		return pr;
	}
	
	public AdminProviderResult getDepartment(String id) throws DocumentException, ComplexObjectException{
		AdminProviderResult pr = new AdminProviderResult(PublishAsType.HTML, "forms"+File.separator+"document.xsl");						
		int docID = Integer.parseInt(id);
        User user = new User(sysUser);
		pr.setOutput(struct.getDepartment(docID, user).toXML(false));
		return pr;
	}

	
}
