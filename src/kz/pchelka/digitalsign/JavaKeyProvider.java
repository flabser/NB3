package kz.pchelka.digitalsign;


import java.io.*;
import java.security.*;

public class JavaKeyProvider implements AbstractProvider {
	Log4jLoggerApplet log = new Log4jLoggerApplet("");
	@Override
	public String sign(String object) {
		return null;
	}

	@Override
	public boolean verify(String object, String text) {
		return false;
	}

	@Override
	public boolean validate() {
		return false;
	}
	@SuppressWarnings("null")
	public static KeyPair serializeKeyPair() {
		JavaKeyProvider jkp = new JavaKeyProvider();
		try {
			String filePath = "";
			String path = System.getProperty( "user.home" ) + File.separator;
			File iniFile = new File(path + "nb.ini");
			String line;
			if (iniFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(iniFile));
				if ((line = br.readLine()) != null) {
					filePath  = line;
					jkp.log.normalLogEntry("keypair file exist on path: " + filePath);
				} else {
					filePath  = path;
					BufferedWriter bw = new BufferedWriter(new FileWriter(iniFile, true));
			        bw.write(filePath);
			        bw.close();
			        jkp.log.normalLogEntry("keypair file created on path: " + filePath);
				}
				br.close();
			} else {
				filePath  = path;
				BufferedWriter bw = new BufferedWriter(new FileWriter(iniFile, true));
		        bw.write(filePath);
		        bw.close();
		        jkp.log.normalLogEntry("keypair file created on path: " + filePath);
			}
			File fileKeyPair = new File(filePath + "keyPair.data");
			try {
				 if(fileKeyPair.exists()){
					 ObjectInputStream objPub = new ObjectInputStream(new FileInputStream(fileKeyPair));
					 KeyPair keyPair = (KeyPair)objPub.readObject();
					 objPub.close();
					 return keyPair;
				 } else {
					 try {
						 KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
						 jkp.log.normalLogEntry("created keypair");
						 KeyPair keyPair = kg.genKeyPair();
						 ObjectOutputStream objPub = new ObjectOutputStream(new FileOutputStream(fileKeyPair));
						 objPub.writeObject(keyPair);
						 objPub.flush();
						 objPub.close();
						 return keyPair;
					 } catch (NoSuchAlgorithmException e) {
						 jkp.log.errorLogEntry(e);
						 e.printStackTrace();
					 }
				 }
			 } catch(IOException e){
				 jkp.log.errorLogEntry(e);
				 e.printStackTrace();
			 }
		} catch (Exception e) {
			jkp.log.errorLogEntry(e);
			System.exit(0);
		}
		return null;
	}
}
