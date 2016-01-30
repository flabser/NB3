package kz.flabs.webrule.rulefile;

import java.io.*;
import java.util.*;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.util.XMLUtil;
import kz.pchelka.log.JavaConsoleLogger;

public class RuleFile implements Const {
	private String filePath;
	private ArrayList<IRuleFileElement> tags =  new ArrayList<IRuleFileElement>();

	public RuleFile(String filePath){
		this.filePath = filePath;
	}

	public RuleTag addTag(String tagName){
		RuleTag tag = new RuleTag(tagName, "");
		tags.add(tag);
		return tag;
	}

	public RuleTag addTag(String tagName, String tagValue){
		RuleTag tag = new RuleTag(tagName, tagValue);
		tags.add(tag);
		return tag;
	}
	
	public RuleTag addTag(RuleTag parentTag, String tagName, String tagValue){
		RuleTag tag = new RuleTag(tagName, tagValue);		
		if (parentTag != null) parentTag.tags.add(tag);
		return tag;
	}
	
	public void addComment(String comment){
		tags.add(new Comment(comment));
	}
	
	public boolean save(){
		StringBuffer output = new StringBuffer(10000);	
		try{
			//filePath = "c:\\tmp\\tets.xml";
			FileOutputStream os = new FileOutputStream(filePath);
			output.insert(0, XMLUtil.xmlHeader + toString());			
			byte[] buf = output.toString().getBytes("UTF8");	
			if (os != null){
				for (int i=0; i < buf.length; i ++) {
					os.write(buf[i]);
				}			
				os.close();
				os = null;
			}
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String toString(){
		StringBuffer result = new StringBuffer(100);
		for(IRuleFileElement tag :tags){
			result.append(tag);
		}		
		return result.toString();
	}

	public static void main(String[] args){
		try {
			AppEnv.logger = new JavaConsoleLogger();
			RuleFile rf = new RuleFile("c:" + File.separator + "tmp" + File.separator + "reminder_task.xml");
			RuleTag parent = rf.addTag("rule","");
			parent.addComment("comment");
			RuleTag tag = rf.addTag(parent,"description","this is description text");
			//tag.setAttr("source", "static");
			System.out.println(rf);
			rf.save();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
