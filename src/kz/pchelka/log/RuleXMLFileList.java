package kz.pchelka.log;

import java.io.File;
import java.util.ArrayList;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;


public class RuleXMLFileList implements Const  {
	public static File xmlDirQuery; 
	public static File xmlDirDocument;
	public static File xmlDirStatic;
	public static File xmlDirView;
	public static File xmlHandlerView;

	public RuleXMLFileList(String app){
		xmlDirQuery = new File("Rule" + File.separator + app + File.separator +"Query");
		xmlDirDocument = new File("Rule"+ File.separator + app + File.separator+ "Form");
		xmlDirStatic = new File("Rule" + File.separator + app + File.separator+ "Static");
		xmlDirView = new File("Rule" + File.separator + app + File.separator + "View");
		xmlHandlerView = new File("Rule" + File.separator + app + File.separator + "Handler");	
	}
	

	public ArrayList<File>  getXmlQFileList(){
		ArrayList<File> fl = new ArrayList<File>();
		if(xmlDirQuery.isDirectory()){
			File[] listQ = xmlDirQuery.listFiles();
			for(int i = listQ.length; --i>=0;){
				String name = listQ[i].getName().toLowerCase();
				if (name.endsWith(".xml")){
					fl.add(listQ[i]);
				}
			}
		}
		return fl;
	}

	public ArrayList<File>  getXmlDFileList(){
		ArrayList<File> fl = new ArrayList<File>();
		if(xmlDirDocument.isDirectory()){
			File[] listQ = xmlDirDocument.listFiles();
			for(int i = listQ.length; --i>=0;){
				String name = listQ[i].getName().toLowerCase();
				if (name.endsWith(".xml")){
					fl.add(listQ[i]);
				}
			}
		}
		return fl;
	}

	public ArrayList<File>  getXmlSFileList(){
		ArrayList<File> fl = new ArrayList<File>();
		if(xmlDirStatic.isDirectory()){
			File[] listQ = xmlDirStatic.listFiles();
			for(int i = listQ.length; --i>=0;){
				String name = listQ[i].getName().toLowerCase();
				if (name.endsWith(".xml")){
					fl.add(listQ[i]);
				}
			}
		}
		return fl;
	}

	public ArrayList<File>  getXmlVFileList(){
		ArrayList<File> fl = new ArrayList<File>();
		if(xmlDirView.isDirectory()){
			File[] listQ = xmlDirView.listFiles();
			for(int i = listQ.length; --i>=0;){
				String name = listQ[i].getName().toLowerCase();
				if (name.endsWith(".xml")){
					fl.add(listQ[i]);
				}
			}
		}
		return fl;
	}
}
