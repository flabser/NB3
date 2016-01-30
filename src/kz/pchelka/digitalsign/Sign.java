package kz.pchelka.digitalsign;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import kz.iola.jce.provider.IolaProvider;
import kz.pchelka.server.Server;
import netscape.javascript.JSObject;

@SuppressWarnings("serial")
public class Sign extends JApplet {
	private DigitalProperties digiKeys;
	private AlgorithmTool tool;
	Log4jLoggerApplet log;
	String path = "";

	@Override
	public void init() {
		for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}

	@Override
	public void start() {
		digiKeys = new DigitalProperties();
		tool = new AlgorithmTool();
		log = new Log4jLoggerApplet("");
		creatLoggerFile();
	}

	public String getSign(String fields) {
		try {

			PasswordChooser dialog = null;
			if (dialog == null) {
				dialog = new PasswordChooser();
			}

			final DataExchangeFrame frame = new DataExchangeFrame();

			AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					try {
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					} catch (Exception e) {
						for (StackTraceElement el : e.getStackTrace()) {
							Server.logger.errorLogEntry(el.toString());
						}
					}
					return Boolean.TRUE;
				}
			});

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			if (dialog.showDialog(frame, "Выбор сертификата")) {
				PrivateKey pk = getPrivateKeyFromStore(dialog.getPath(), new String(dialog.getPassword()));
				if (pk != null) {
					String sign = tool.setSign(fields.getBytes(Charset.forName("UTF-8")), pk);
					return sign;
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
			for (StackTraceElement el : e.getStackTrace()) {
				log.errorLogEntry(el.toString());
			}
			return "Fields are empty " + e.getMessage();
		}
		return "Invalid private key";
	}

	public PrivateKey getPrivateKeyFromStore(final String path, final String password) {
		try {
			AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					try {
						Security.addProvider(new IolaProvider());
					} catch (Exception e) {
						for (StackTraceElement el : e.getStackTrace()) {
							log.errorLogEntry(el.toString());
							System.out.println(el.toString());
						}
					}
					return Boolean.TRUE;
				}
			});
			final KeyStore ks = KeyStore.getInstance("PKCS12", IolaProvider.PROVIDER_NAME);
			AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
				@Override
				public Boolean run() {
					try {
						ks.load(new FileInputStream(path), password.toCharArray());
					} catch (Exception e) {
						for (StackTraceElement el : e.getStackTrace()) {
							log.errorLogEntry(el.toString());
							System.out.println(el.toString());
						}
					}
					return Boolean.TRUE;
				}
			});
			KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(password.toCharArray());
			Enumeration<String> aliases = ks.aliases();
			String alias = "";
			while (aliases.hasMoreElements()) {
				alias = aliases.nextElement();
			}
			if (!"".equalsIgnoreCase(alias)) {
				PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
				return privateKey;
			}

		} catch (Throwable e) {
			e.printStackTrace();
			for (StackTraceElement el : e.getStackTrace()) {
				log.errorLogEntry(el.toString());
			}
		}
		return null;
	}

	public int checkSign(String sign, String publicKey, String fields) {
		try {
			String clear_key = publicKey.replaceAll("-----BEGIN CERTIFICATE-----", "")
					.replaceAll("-----END CERTIFICATE-----", "");
			byte[] decoded = Base64.decodeBase64(clear_key);
			java.security.cert.Certificate cert_cert = CertificateFactory.getInstance("X.509")
					.generateCertificate(new ByteArrayInputStream(decoded));
			PublicKey cert = cert_cert.getPublicKey();
			boolean check = tool.verify(fields.getBytes(Charset.forName("UTF-8")), sign, cert);
			if (check) {
				return 1;
			} else {
				return 2;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			for (StackTraceElement el : e.getStackTrace()) {
				log.errorLogEntry(el.toString());
			}
		}
		return -1;
	}

	public void getCoockies() {
		JSObject myBrowser = JSObject.getWindow(this);
		JSObject myDocument = (JSObject) myBrowser.getMember("document");
		String myCookie = "";
		if (myDocument != null && myDocument.getMember("cookie") != null) {
			myCookie = (String) myDocument.getMember("cookie");
		}
		if (myCookie.length() > 0) {
			StringTokenizer tknz = new StringTokenizer(myCookie, ";");
			while (tknz.hasMoreElements()) {
				String temp = (String) tknz.nextElement();
				StringTokenizer tknzCoo = new StringTokenizer(temp, "=");
				while (tknzCoo.hasMoreElements()) {
					String temp2 = (String) tknzCoo.nextElement();
					if (temp2.trim().equals("provider")) {
						String temp3 = (String) tknzCoo.nextElement();
						digiKeys.setProvider(temp3);
					}
					if (temp2.trim().equals("digestAlgo")) {
						String temp3 = (String) tknzCoo.nextElement();
						digiKeys.setDigestAlgo(temp3);
					}
					if (temp2.trim().equals("signAlgo")) {
						String temp3 = (String) tknzCoo.nextElement();
						digiKeys.setSignAlgo(temp3);
					}
				}
			}
			log.normalLogEntry("cookies: " + myCookie);
		}
	}

	public void creatLoggerFile() {
		String path = System.getProperty("user.home") + File.separator + "logForApplet" + File.separator
				+ "weeklyLog.log";

		PatternLayout layout = new PatternLayout();
		String conversionPattern = "[%p] %d %c %M - %m%n";
		layout.setConversionPattern(conversionPattern);

		DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender();
		rollingAppender.setFile(path);
		rollingAppender.setDatePattern("'.'yyyy-ww");
		rollingAppender.setLayout(layout);
		rollingAppender.activateOptions();

		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.DEBUG);
		rootLogger.addAppender(rollingAppender);

		log.normalLogEntry("applet initialized");
		log.normalLogEntry("os.name: " + System.getProperty("os.name"));
		log.normalLogEntry("os.version: " + System.getProperty("os.version"));
		log.normalLogEntry("os.arch: " + System.getProperty("os.arch"));
		log.normalLogEntry("java.runtime.version: " + System.getProperty("java.runtime.version"));
		log.normalLogEntry("applet started");
	}

	@Override
	public void stop() {
	}

	@Override
	public void destroy() {
	}

	@Override
	public String getAppletInfo() {
		return "";
	}
}

class DataExchangeFrame extends JFrame {

	public static final int DEFAULT_WIDTH = 300;
	public static final int DEFAULT_HEIGHT = 200;

	public DataExchangeFrame() {
		setTitle("DataExchangeTest");
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
}

class PasswordChooser extends JPanel {

	private JLabel pathLabel;
	private JPasswordField password;
	private JButton chooseButton;
	private JButton okButton;
	private boolean ok;
	private JDialog dialog;

	public PasswordChooser() {
		setLayout(new BorderLayout());

		// Создание панели с полями для ввода имени пользователя и пароля

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 2));
		panel.add(new JLabel("Password:"));
		panel.add(password = new JPasswordField(""));
		panel.add(new JLabel("Path to cert:"));
		panel.add(pathLabel = new JLabel(""));
		chooseButton = new JButton("Choose cert");
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
					@Override
					public Boolean run() {
						try {
							JFileChooser fileChooser = new JFileChooser();
							int returnValue = fileChooser.showOpenDialog(null);
							if (returnValue == JFileChooser.APPROVE_OPTION) {
								File selectedFile = fileChooser.getSelectedFile();
								System.out.println(selectedFile.getAbsolutePath());
								pathLabel.setText(selectedFile.getAbsolutePath());
							}
						} catch (Exception e) {
							for (StackTraceElement el : e.getStackTrace()) {
								System.out.println(el.toString());
							}
						}
						return Boolean.TRUE;
					}
				});
			}
		});
		panel.add(chooseButton);
		add(panel, BorderLayout.CENTER);

		// Создание кнопок Ok и Cancel

		okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ok = true;
				dialog.setVisible(false);
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});

		// Включение кнопок в нижниюю часть окна

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	public String getPath() {
		return pathLabel.getText();
	}

	public char[] getPassword() {
		return password.getPassword();
	}

	public boolean showDialog(Component parent, String title) {
		ok = false;
		Frame owner = null;
		if (parent instanceof Frame) {
			owner = (Frame) parent;
		} else {
			owner = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
		}

		if (dialog == null || dialog.getOwner() != owner) {
			dialog = new JDialog(owner, true);
			dialog.add(this);
			dialog.getRootPane().setDefaultButton(okButton);
			dialog.pack();
		}

		dialog.setTitle(title);
		dialog.setVisible(true);
		return ok;
	}
}
