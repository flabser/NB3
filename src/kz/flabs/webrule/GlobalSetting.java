package kz.flabs.webrule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import kz.flabs.dataengine.DatabaseType;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import kz.lof.appenv.AppEnv;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class GlobalSetting {
	public String description;
	public String id = "";
	// TODO it need to check whether it necessary
	public String databaseName;
	public boolean databaseEnable;
	public boolean isWorkspace;
	public String driver;
	public String dbURL;
	public String rulePath;
	public String xsltAppsPath;
	@Deprecated
	public String databaseHost;
	@Deprecated
	public String entryPoint;
	public String defaultRedirectURL;
	@Deprecated
	public String orgName;
	@Deprecated
	public String logo;
	@Deprecated
	public String appName;
	public RunMode isOn;
	public boolean autoDeployEnable;
	public boolean isValid;
	@Deprecated
	public ArrayList<Skin> skinsList = new ArrayList<Skin>();
	@Deprecated
	public HashMap<String, Skin> skinsMap = new HashMap<String, Skin>();
	@Deprecated
	// public SynchroGlobalSetting syncroGlobalSettings;
	public int markAsReadMsDelay;

	public static final String vocabulary = "vocabulary.xml";
	public DataEngineImpl dbImpl;

	@Deprecated
	private String dbPassword;
	private String pwdEnco = "";
	private SecretKey key = null;
	private Cipher cipherAlgo = null;
	private File keyFile = null;
	private String globalFilePath = "";
	private String dbUserName;
	private String userNameEnco = "";

	public GlobalSetting(String path, AppEnv env) {
		globalFilePath = path;

		rulePath = "rule" + File.separator + env.appName;
		xsltAppsPath = "webapps" + File.separator + env.appName + File.separator + "xsl";

		try {
			Document doc = null;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;

			db = dbf.newDocumentBuilder();
			doc = db.parse(path);

			id = env.appName;
			if (id.equalsIgnoreCase("system")) {
				AppEnv.logger.warningLogEntry("a name \"system\" is reserved name of application.");
			}

			// AppEnv.logger.infoLogEntry("Loading: " +
			// this.getClass().getSimpleName() + ", id=" + id);
			if (XMLUtil.getTextContent(doc, "/rule/@mode").equalsIgnoreCase("on")) {
				isOn = RunMode.ON;
				isValid = true;
			}

			if (id.equalsIgnoreCase("workspace")) {
				isWorkspace = true;
			}

			description = XMLUtil.getTextContent(doc, "/rule/description");
			orgName = XMLUtil.getTextContent(doc, "/rule/orgname");
			logo = XMLUtil.getTextContent(doc, "/rule/logo");
			// appName = XMLUtil.getTextContent(doc, "/rule/appname");

			try {
				databaseName = XMLUtil.getTextContent(doc, "/rule/database/name");
				driver = XMLUtil.getTextContent(doc, "/rule/database/driver");
				if (!databaseName.trim().equals("") && !driver.trim().equals("")) {
					if (XMLUtil.getTextContent(doc, "/rule/database/@autodeploy").equalsIgnoreCase("on")) {
						autoDeployEnable = true;
					}

					dbURL = XMLUtil.getTextContent(doc, "/rule/database/url");
					dbUserName = XMLUtil.getTextContent(doc, "/rule/database/username");
					dbPassword = XMLUtil.getTextContent(doc, "/rule/database/password");

					Node username = doc.getElementsByTagName("username").item(0);
					Node pss = doc.getElementsByTagName("password").item(0);
					Node database = doc.getElementsByTagName("database").item(0);
					Node pw = doc.getElementsByTagName("connectionid").item(0);

					if (!dbUserName.trim().equals("") || !dbPassword.trim().equals("")
					        || database.getLastChild().getNodeName().contains("connectionid")) {

						if (dbUserName.trim().equals("") && dbPassword.trim().equals("")) {
							String temp = pw.getTextContent();
							StringTokenizer tknz = new StringTokenizer(temp, "@@@");
							userNameEnco = tknz.nextToken();
							pwdEnco = tknz.nextToken();
						} else if (!dbUserName.trim().equals("") && dbPassword.trim().equals("")) {
							String temp = pw.getTextContent();
							StringTokenizer tknz = new StringTokenizer(temp, "@@@");
							dbUserName = username.getTextContent();
							tknz.nextToken();
							pwdEnco = tknz.nextToken();
						} else if (dbUserName.trim().equals("") && !dbPassword.trim().equals("")) {
							String temp = pw.getTextContent();
							StringTokenizer tknz = new StringTokenizer(temp, "@@@");
							userNameEnco = tknz.nextToken();
							dbPassword = pss.getTextContent();
						}

						if (!dbUserName.trim().equals("") && !dbPassword.trim().equals("")
						        && database.getLastChild().getNodeName().contains("connectionid")) {
							database.removeChild(pw);
						}
						deserializeKey();
					}

					databaseHost = XMLUtil.getTextContent(doc, "/rule/database/host");
					databaseEnable = true;

				} else {
					AppEnv.logger.errorLogEntry("Unable to determine name of database");
				}

			} catch (Exception e) {
				AppEnv.logger.errorLogEntry("Unable to determine parameters of the database");
				databaseName = "";
			}

			entryPoint = XMLUtil.getTextContent(doc, "/rule/entrypoint");
			defaultRedirectURL = XMLUtil.getTextContent(doc, "/rule/defaultredirecturl");
			if (defaultRedirectURL.equalsIgnoreCase("")) {
				defaultRedirectURL = "Error?type=default_url_not_defined";
			}

			NodeList rules = XMLUtil.getNodeList(doc, "/rule/rules/entry");
			for (int i = 0; i < rules.getLength(); i++) {
				rulePath = XMLUtil.getTextContent(rules.item(i), "@path", false);
			}

			NodeList roles = XMLUtil.getNodeList(doc, "/rule/roles/entry");
			for (int i = 0; i < roles.getLength(); i++) {
				Role role = new Role(roles.item(i), id);

				if (role.isValid && role.isOn == RunMode.ON) {
					if (!role.name.equalsIgnoreCase("supervisor")) {

					} else {
						AppEnv.logger
						        .warningLogEntry("A role name \"supervisor\" is reserved name of system roles. The role has not added to application");
					}
				}
			}

		} catch (FileNotFoundException fnfe) {
			AppEnv.logger.errorLogEntry(fnfe.toString());
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
		}
	}

	public String getDbUserName() {
		return dbUserName;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public boolean isValid() {
		return true;
	}

	/*
	 * private static DatabaseType getDatabaseType(String driver) { if (
	 * driver.trim().equalsIgnoreCase("org.postgresql.Driver") ) { return
	 * DatabaseType.POSTGRESQL; } else if
	 * (driver.trim().equalsIgnoreCase("net.sourceforge.jtds.jdbc.Driver")) {
	 * return DatabaseType.MSSQL; } else { return DatabaseType.H2; } }
	 */

	private static DatabaseType getDatabaseType(String dbURL) {
		if (dbURL.contains("postgresql")) {
			return DatabaseType.POSTGRESQL;
		} else if (dbURL.contains("sqlserver")) {
			return DatabaseType.MSSQL;
		} else if (dbURL.contains("oracle")) {
			return DatabaseType.ORACLE;
		} else {
			return DatabaseType.H2;
		}
	}

	public void deserializeKey() throws IOException, InterruptedException {
		String tempPath = "";
		if (!"".equalsIgnoreCase(globalFilePath)) {
			File f = new File(globalFilePath);
			tempPath = f.getParent();
		}
		keyFile = new File(tempPath + File.separator + "global.dat");
		ObjectInputStream ois;
		try {
			if (keyFile.exists()) {
				ois = new ObjectInputStream(new FileInputStream(keyFile));
				key = (SecretKey) ois.readObject();
				ois.close();
				if ("".equalsIgnoreCase(dbPassword.trim()) || "".equalsIgnoreCase(dbUserName.trim())) {
					decode();
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void serializeKey() {
		try {
			KeyGenerator genKey = KeyGenerator.getInstance("Blowfish");
			key = genKey.generateKey();
			encode();
			ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(keyFile));
			objOut.writeObject(key);
			objOut.flush();
			objOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public void writeXml(String userName, String userNameEnco, String password, String passEnco) {
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(globalFilePath);
			doc.setXmlStandalone(true);

			Node username = doc.getElementsByTagName("username").item(0);
			Node pss = doc.getElementsByTagName("password").item(0);
			Node database = doc.getElementsByTagName("database").item(0);
			username.setTextContent(userName);
			pss.setTextContent(password);

			Node connId = doc.getElementsByTagName("connectionid").item(0);
			if (database.getLastChild().getNodeName().contains("connectionid")) {
				database.removeChild(connId);
			}
			if (!userNameEnco.equals("") && !passEnco.equals("")) {
				Element conn = doc.createElement("connectionid");
				conn.appendChild(doc.createTextNode(userNameEnco + "@@@" + passEnco));
				database.appendChild(conn);
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(globalFilePath));
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}
	}

	public void initAlgorithm(int method) {

		try {

			cipherAlgo = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
			switch (method) {
			case 1:
				cipherAlgo.init(Cipher.ENCRYPT_MODE, key);
				break;
			case 2:
				cipherAlgo.init(Cipher.DECRYPT_MODE, key);
				break;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("restriction")
	public void encode() {
		String userNameEncode = "";
		String passwordEncode = "";
		try {
			initAlgorithm(1);
			BASE64Encoder enc = new BASE64Encoder();
			byte[] encodeBytes = cipherAlgo.doFinal(dbUserName.getBytes(Charset.forName("UTF-8")));
			byte[] encodeBytes2 = cipherAlgo.doFinal(dbPassword.getBytes(Charset.forName("UTF-8")));
			userNameEncode = enc.encode(encodeBytes);
			passwordEncode = enc.encode(encodeBytes2);
			writeXml("", userNameEncode, "", passwordEncode);

		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("restriction")
	public void decode() {
		try {
			String userNameDecode = "";
			String passwordDecode = "";
			initAlgorithm(2);
			BASE64Decoder decoder = new BASE64Decoder();

			byte[] tmp1 = decoder.decodeBuffer(userNameEnco);
			byte[] encodeBytes = cipherAlgo.doFinal(tmp1);
			userNameDecode = new String(encodeBytes);

			byte[] tmp2 = decoder.decodeBuffer(pwdEnco);
			byte[] encodeBytes2 = cipherAlgo.doFinal(tmp2);
			passwordDecode = new String(encodeBytes2);

			if (dbPassword.equals("")) {
				dbPassword = passwordDecode;
			}
			if (dbUserName.equals("")) {
				dbUserName = userNameDecode;
			}

		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
