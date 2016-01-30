package kz.flabs.runtimeobj.document;

import java.io.Serializable;

public class BlobFile implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 1L;
	
	public String originalName;
	public String path = "";
	public long size;
	public String checkHash;
	public String comment = "";
    public String id;
	public long docid;
    public boolean toDelete;
	private byte[] content;


	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BlobFile)) return false;
		BlobFile thisBlobFile = (BlobFile)o;
		if (thisBlobFile.checkHash.length() > 0 &&
				this.checkHash.length() > 0 && 
				thisBlobFile.checkHash.equals(this.checkHash))
			return true;
		return false;
	}
	
	public byte[] getContent(){
		return content;
	}
	
	public void setContent(byte[] content){
		this.content = content;
	}
	
	public String[] getNameAndComment(){
		String val[] = new String[2];
		val[0] = originalName;
		val[1] = comment;
		return val;
	}
	
	public String getComment(){
		if (comment != null){
			return comment;
		}else{
			return "";
		}
	}

	public String toString(){
		return "path=" + path + ", originalName=" + originalName + ", comment=" + comment;
	}

	@Override
	public Object clone() {
		BlobFile file = null;
		try {
			file = (BlobFile) super.clone();
			file.docid = 0;
			file.id = null;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return file;
	}

}