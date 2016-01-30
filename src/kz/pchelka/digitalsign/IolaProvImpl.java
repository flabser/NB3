package kz.pchelka.digitalsign;

import kz.iola.jce.provider.IolaProvider;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;

public class IolaProvImpl {
	DigitalProperties digiProp = new DigitalProperties();
	Log4jLoggerApplet log = new Log4jLoggerApplet("");
	String password = "";
	String certAlias = "";
	public void loadKaztoken() {
		try {
			Security.addProvider(new IolaProvider());
			getIniFile();
			String name = getKaztokenNames();
			KeyStore store = KeyStore.getInstance("KAZTOKEN", IolaProvider.PROVIDER_NAME);
			store.load(new ByteArrayInputStream(name.getBytes(Charset.forName("UTF-8"))), password.toCharArray());
			PrivateKey privKey = (PrivateKey) store.getKey(certAlias,password.toCharArray());
			Certificate cert = store.getCertificate(certAlias);
			PublicKey pubKey = cert.getPublicKey();
			/*
			Enumeration<String> en = store.aliases();
			while(en.hasMoreElements()){
				String param = (String)en.nextElement();
				log.normalLogEntry("alias on Kaztoken: " + param);
			}
			*/
			digiProp.setProvider(IolaProvider.PROVIDER_NAME);
			//digiProp.setPrivateKey(privKey);
			//digiProp.setPublicKey(pubKey);
			
		} catch (NoSuchAlgorithmException e) {
			log.errorLogEntry("Error on loadKaztoken()" + e);
			e.printStackTrace();
		} catch (CertificateException e) {
			log.errorLogEntry("Error on loadKaztoken()" + e);
			e.printStackTrace();
		} catch (IOException e) {
			log.errorLogEntry("Error on loadKaztoken()" + e);
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			log.errorLogEntry("Error on loadKaztoken()" + e);
			e.printStackTrace();
		} catch (KeyStoreException e) {
			log.errorLogEntry("Error on loadKaztoken()" + e);
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			log.errorLogEntry(e);
			e.printStackTrace();
		} 
	}
	
	public String getKaztokenNames() {
		
		String terminalName = null;
		TerminalFactory factory = TerminalFactory.getDefault();
	    List<CardTerminal> listCard;
		try {
			listCard = factory.terminals().list();
			for (CardTerminal terminal : listCard) {
		    	if ((terminal.isCardPresent() == true)&& (terminal.getName() != null)) {
		        	terminalName = terminal.getName();
		        } 
		    }
		} catch (CardException e) {
			log.errorLogEntry("Card is not present or name is null" + e);
			e.printStackTrace();
		}
		log.normalLogEntry("Terminal name: " + terminalName);
		return terminalName;
	}
	
	public void getIniFile() {
		String path = System.getProperty( "user.home" ) + File.separator;
		File iniFile = new File(path + "nb.ini");
		String line;
		if (iniFile.exists()) {
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(iniFile));
				while ((line = br.readLine()) != null) {
					if (line.startsWith("password")) {
						String[] splits = line.split(":");
						password = splits[1].trim();
					} else if  (line.startsWith("alias")) {
						String[] splits = line.split(":");
						certAlias = splits[1].trim();
					}
				} 
				br.close();
			} catch (FileNotFoundException e) {
				log.errorLogEntry("getIniFile(): " + e);
				e.printStackTrace();
			} catch (IOException e) {
				log.errorLogEntry("getIniFile(): " + e);
				e.printStackTrace();
			}
		}
	}
	public static void main(String args[]) {
		IolaProvImpl iola = new IolaProvImpl();
		iola.loadKaztoken();
	}
}
