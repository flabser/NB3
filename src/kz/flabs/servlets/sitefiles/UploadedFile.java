package kz.flabs.servlets.sitefiles;

import kz.flabs.util.Util;
import kz.lof.env.Environment;

import java.io.File;

public class UploadedFile{
	public String originalName;
	public long size;
	public String fullName;
	public String fileType;
	public String commentField;
    public String localPath;
    public String id;
	
	public UploadedFile(String originalName, long size, String tmpName, String fileType){
		this.originalName = originalName;
		this.size = size;
		this.fullName = tmpName;
		this.fileType = fileType;
		this.commentField = Util.getHexHash(fullName);
	}

    public UploadedFile(String originalName, String hexHash, long size, String fileType, String id){
        this.originalName = originalName;
        this.size = size;
        this.fullName = originalName;
        this.fileType = fileType;
        this.commentField = hexHash;
        this.id = id;
    }

	public void save(String formSesID){
		int folderNum = 1;
		File dir = new File(Environment.tmpDir + File.separator + formSesID + File.separator + Integer.toString(folderNum));
		while (dir.exists()){
			folderNum ++ ;
			dir = new File(Environment.tmpDir + File.separator + formSesID + File.separator + Integer.toString(folderNum));
		}
		
	}
	
	public String toString(){
		return "originalName=" + originalName + ", fullName=" + fullName + ", size=" + size + ", comment field=" + commentField;
	}
}