package kz.pchelka.util;

import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import kz.pchelka.server.*;

public class RecognizedXMLFile {
	public int baselines[];
	public String texts[];
	
	
	public RecognizedXMLFile(String fileName){
		try{
			File f = new File(fileName);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			dbf.setValidating(true);
			Document xmlDoc = db.parse(f);
			xmlDoc.getDocumentElement().normalize();						
			NodeList nl = xmlDoc.getElementsByTagName("line");
			int lineLength = nl.getLength();
			baselines = new int[lineLength];
			texts = new String[lineLength];

			for (int i = 0; i < lineLength; i++){
				Node n = nl.item(i);

				NamedNodeMap nnm = n.getAttributes();
				for (int z = 0; z < nnm.getLength(); z++){
					Node attrNode = nnm.item(z);
					if (attrNode.getNodeName().equals("baseline")){
						baselines[i] = Integer.parseInt(attrNode.getNodeValue());
						texts[i] = "";
						NodeList cn = n.getChildNodes();
						for (int z1 = 0; z1 < cn.getLength(); z1++){
							Node fn = cn.item(z1);							
							texts[i] = texts[i] +  fn.getChildNodes().item(0).getNodeValue();
						}											
						System.out.println("-"+baselines[i]+" : "+texts[i]);
					}					
				}
			}
		}catch(IOException ie){
			Server.logger.errorLogEntry("RecognizedXMLFile",ie); 
		}catch(SAXException se){
			Server.logger.errorLogEntry("RecognizedXMLFile",se); 
		}catch(ParserConfigurationException pce){
			Server.logger.errorLogEntry("RecognizedXMLFile",pce); 
		}		
	}

	public String getLineText(int start, int end){
		String rVal = "";
		for (int i = 0; i < baselines.length; i++){
			if (baselines[i] < end && baselines[i] > start){
				rVal += texts[i].replaceAll("\"", "");
			}
		}
		return rVal;
	}

	public String getParsedText(){
		String rVal = "";
		for (int i = 0; i < baselines.length; i++){			
			rVal += baselines[i]+": "+texts[i].replaceAll("\"", "")+"\n";			
		}
		return rVal;
	}

	
	public static void main(String[] args) {
		System.out.println("RecognizedXMLFile test");
		RecognizedXMLFile r = new RecognizedXMLFile("2.xml");
		System.out.println("text="+r.getLineText(100, 300));
	}
}
