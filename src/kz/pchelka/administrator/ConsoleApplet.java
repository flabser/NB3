package kz.pchelka.administrator;

import javax.swing.JApplet;

public class ConsoleApplet extends JApplet{
	private static final long serialVersionUID = -5040587732549322035L;

	public void init(){
		WebConsole wc = new WebConsole();
		wc.show(getContentPane());
	}
	
}
