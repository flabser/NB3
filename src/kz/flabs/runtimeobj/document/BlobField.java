package kz.flabs.runtimeobj.document;

import kz.flabs.dataengine.Const;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class BlobField implements Const, Serializable, Cloneable {
	public long size;
	public String name;
	
	private static final long serialVersionUID = 1L;	
	private HashMap<String, BlobFile> filesMap = new HashMap<>();
		
	public BlobField(String n){
		name = n;			
	}
	
	public BlobField(String n, ArrayList<BlobFile> p){
		name = n;
		for (BlobFile file: p) {
			filesMap.put(file.originalName, file);
		}
	}
	
	public BlobField(String n, HashMap<String, BlobFile> p) {
		name = n;
		filesMap = p;
		
	}
	
	public int getFilesCount() {
		return filesMap.size();
	}
	
	public Set<String> getFileNames() {
		return filesMap.keySet();
	}
	
	public Set<String> getAsXMLFragment() {
		return filesMap.keySet();
	}
	
	public Set<String> getEncodeFileNames() {
		HashSet<String> set = new HashSet<String>();
		for(String fileName:filesMap.keySet()){
			try {
				String fileNameToHeader = URLEncoder.encode(fileName, "UTF-8");
				set.add(fileNameToHeader);
			} catch (UnsupportedEncodingException e) {		
				e.printStackTrace();
			}
		}
		return set;
	}
	
	public BlobFile findFile(BlobFile file) {
		if (filesMap.containsKey(file.originalName) && filesMap.get(file.originalName).checkHash.equals(file.checkHash))
			return filesMap.get(file.originalName);
		else
			if (filesMap.values().contains(file))
				return getFileByHash(file.checkHash);
		return null;
	}
	
	public boolean hasFileName(String fileName) {
		return filesMap.containsKey(fileName);
	}

	public boolean addFile(BlobFile newFile){
		if (filesMap.containsKey(newFile.originalName)) {
			BlobFile existingFile = filesMap.get(newFile.originalName);
			if (!existingFile.checkHash.equals(newFile.checkHash)) {
				newFile.originalName = createNewFileName(newFile.originalName);
				filesMap.put(newFile.originalName, newFile);
				return true;
			} else
				return false;
		}
		if (filesMap.values().contains(newFile)) return false;
		filesMap.put(newFile.originalName, newFile);
		return true;
	}

	public int addFiles(ArrayList<BlobFile> p) {
		int i = 0;
		for (BlobFile file: p) {
			if (addFile(file)) {
				i++;
			}
		}
		return i;
	}
	
	public int addFiles(HashMap<String, BlobFile> p) {
		int i = 0;
		for (BlobFile file: p.values()) {
			if (addFile(file)) {
				i++;
			}
		}
		return i;
	}
	
	public Collection<BlobFile> getFiles() {		
		return filesMap.values();
	}
	

	public BlobFile getFile(String fileName) {
		return filesMap.get(fileName);
	}
	
	public BlobFile getFile(BlobFile file) {
		return filesMap.get(file.originalName);
	}
	
	private BlobFile getFileByHash(String fileHash) {
		for (BlobFile file: filesMap.values()) {
			if (file.checkHash.equals(fileHash))
				return file;
		}
		return null;
	}
	
	public boolean removeFile(String fileName) {
		BlobFile bf = filesMap.remove(fileName);
		return (bf != null);
	}
	
	public boolean removeFile(BlobFile file) {
		if (filesMap.containsKey(file.originalName) && filesMap.get(file.originalName).checkHash.equals(file.checkHash))
			return (filesMap.remove(file.originalName) != null);
		else
			if (filesMap.values().contains(file))
				return (filesMap.remove(getFileByHash(file.checkHash).originalName) != null);
		return false;
	}
	
	public String toString(){
		return "[name=" + name + ", path=" + filesMap + ", size=" + size + "]";
	}
	
	
	private String createNewFileName(String currentFileName) {
		int i = 1;
		String newFileName = "";
		do {
			newFileName = getBaseFileName(currentFileName) + "[" + Integer.toString(i) + "]" + "." + getExtension(currentFileName);
			i++;
		} while (filesMap.containsKey(newFileName));
		return newFileName;
	}
	
	private String getBaseFileName(String fullFileName) {
		return fullFileName.substring(0, fullFileName.lastIndexOf('.'));
	}
	
	private String getExtension(String fullFileName) {
		return fullFileName.substring(fullFileName.lastIndexOf('.'), fullFileName.length());
	}

	public Object clone() {
		BlobField field = null;
		try {
			field = (BlobField) super.clone();
			Map<String, BlobFile> files = new HashMap<>();
			for (Map.Entry<String, BlobFile> file : field.filesMap.entrySet()) {
				files.put(file.getKey(), (BlobFile) file.getValue().clone());
			}
			field.filesMap = (HashMap<String, BlobFile>) files;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return field;
	}
	
}