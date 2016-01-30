package kz.nextbase.script;

import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.BlobFile;

import java.util.Collection;

import static org.apache.commons.lang3.StringEscapeUtils.escapeXml10;
import static org.apache.commons.lang3.StringEscapeUtils.escapeXml11;

public class _BlobField extends _Field {		
	private BlobField blobField;

    public _BlobField(_Document doc, String name) {
		super(doc, name);	
		blobField = doc.getBaseObject().blobFieldsMap.get(name);	
	}

	public _BlobField(BlobField blobField) {
		this.blobField = blobField;
	}

    public _BlobField (_Document doc, String name, BlobField blobField) {
		super(doc, name);	
		this.blobField = blobField;
	}


	@Override
	public String toXML() throws _Exception {
		StringBuffer xmlContent = new StringBuffer(10000);
		Collection<BlobFile> files = blobField.getFiles();					
		for (BlobFile file: files){
			xmlContent.append("<entry filename=\"" + escapeXml11(file.originalName.replaceAll("\\%", "%25")).replaceAll("\\+", "%2b") + "\" hash=\"" + file.checkHash + "\" id=\"" + file.id + "\"><comment>" + escapeXml10(file.comment) + "</comment></entry>");
		}
		return xmlContent.toString();
	}
}