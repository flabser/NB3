package kz.pchelka.console.rmi.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Remote;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;


public class RemoteConsole extends JApplet implements Remote, ActionListener {

	private static Container container;
	// private static JLabel lbl = new JLabel();
	protected static JCheckBox chb_autoScrl;
	// protected static JButton reconnect;
	private static final long serialVersionUID = -5349963649557571093L;
	private Console console;
	public String rmiServer = "NextBase";
	public int rmiPort = 1299;
	private String uid = "";
	private String host = "192.168.0.11";

	public void init()
	{

		showStatus("Loading...");
		try {
			setBackground(new Color(255, 255, 255));

			uid = this.getUID();
			if (this.getParameter("HOST") != null) {
				host = this.getParameter("HOST");
			} else if (this.getCodeBase().getHost().length() > 0) {
				host = this.getCodeBase().getHost();
			}
			if (this.getParameter("SERVER") != null) rmiServer = this.getParameter("SERVER");
			if (this.getParameter("PORT") != null) rmiPort = Integer.parseInt(this.getParameter("PORT"));

			console = new Console(rmiServer);

			chb_autoScrl = new JCheckBox("Auto scroll");
			chb_autoScrl.setFont(new Font("Verdana", 0, 12));
			chb_autoScrl.setSelected(true);

			// reconnect = new JButton("Reconnect");
			// reconnect.setFont(new Font("Verdana", 0, 12));

			JButton btn_tarea_clear = new JButton("Clear console");
			btn_tarea_clear.setFont(new Font("Verdana", 0, 12));
			btn_tarea_clear.setActionCommand("console_clear");
			btn_tarea_clear.addActionListener(this);

			JPanel commandPanel = new JPanel();
			BorderLayout layout = new BorderLayout();
			commandPanel.setLayout(layout);
			commandPanel.setBorder(BorderFactory.createLineBorder(SystemColor.control));

			JPanel bottomPanel = new JPanel();
			// bottomPanel.setBackground(new Color(255, 255, 255));
			bottomPanel.setBorder(BorderFactory.createLineBorder(SystemColor.control));
			bottomPanel.setBackground(SystemColor.control);
			bottomPanel.setLayout(new BoxLayout(bottomPanel, 0));
			bottomPanel.add(Box.createHorizontalStrut(10));

			JTabbedPane jtp = new JTabbedPane();
			jtp.addTab(console.getConsole().getName(), new JScrollPane(console.getConsole()));

			// lbl.setFont(new Font("Verdana", 0, 12));
			// bottomPanel.add(lbl);
			bottomPanel.add(chb_autoScrl, 0);
			bottomPanel.add(btn_tarea_clear, 0);
			commandPanel.add(bottomPanel, "South");

			container = getContentPane();
			container.add(jtp);
			container.add(commandPanel, "South");
			container.repaint();
		} catch (Exception ex) {
			System.err.println("Error in applet init: " + ex);
		}
	}

	public void start()
	{
		try {
			new Lookup(console, "rmi://" + host + ":" + rmiPort + "/" + rmiServer, uid);
			// lbl.setText("Server://"+host+":"+rmiPort+"/"+rmiServer+" [uid: "+uid+"]");
		} catch (Exception ex) {
			System.err.println("Error in applet start: " + ex);
		}
	}

	public void stop()
	{
		super.stop();
	}

	private String getUID()
	{
		uid = this.getParameter("UID");

		if (uid == null || uid.length() == 0) {
			uid = "";
			Random rnd = new Random();
			while (uid.length() < 16) {
				uid += rnd.nextInt(9);
			}
		}

		return uid;
	}

	public void actionPerformed(ActionEvent action)
	{
		if ("console_clear".equals(action.getActionCommand())) {
			console.clear();
		}

		container.repaint();
	}
}
