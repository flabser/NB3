package kz.pchelka.messenger.robot;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import kz.flabs.appenv.AppEnv;
import kz.flabs.localization.LocalizatorExceptionType;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class RobotBorner {
	private AppEnv env;
	private Robot robot;
	


	public boolean populate() throws RobotException{
		File docFile = new File(env.globalSetting.rulePath + File.separator + "Resources"+File.separator+"robot.xml");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document robotDoc = db.parse(docFile.toString());
			if (robotDoc == null) {
				throw new RobotException(LocalizatorExceptionType.VOCABULAR_NOT_FOUND);				
			}
			robot = new Robot(robotDoc);		
			return true;
		} catch (ParserConfigurationException e) {		
			e.printStackTrace();
			return false;
		} catch (IOException e) {		
			e.printStackTrace();
			return false;
		} catch (SAXException e) {		
			e.printStackTrace();
			return false;
		}
	}
	
	public Robot getRobot(){
		return robot;
	}
}
