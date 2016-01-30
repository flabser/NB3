package kz.pchelka.backup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import kz.flabs.runtimeobj.RuntimeObjUtil;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

public class BackupList {

	public File dir;
	private ArrayList<File> fl = new ArrayList<File>();

	public BackupList(){
		dir = new File("." + File.separator + "backup");	
		if(dir.isDirectory()){
			File[] list = dir.listFiles();
			 Arrays.sort(list, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
			 //Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
			 
			for(int i = list.length; --i>=0;){
				fl.add(list[i]);			
			}
		}		
	}

	public ArrayList<File> getFileList(int page, int pageSize) {
		ArrayList<File> pageOffl = new ArrayList<File>();
		int startEntry = RuntimeObjUtil.calcStartEntry(page, pageSize);
		for(int i = startEntry; i < getCount(); i++){
			pageOffl.add(fl.get(i));
		}
		return pageOffl;
	}

	public int getCount() {
		return fl.size();
	}
	

}
