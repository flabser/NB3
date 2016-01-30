package kz.pchelka.administrator;

import java.awt.*;
import javax.swing.*;

public class WebConsole implements IConsole {
	public static MemoryIndicator memInd;	
	public static JTextArea textArea;
	
	private static final long serialVersionUID = -5349963649557571093L;
	private static JLabel lbl = new JLabel();

	//	private static ImageIcon imagesR = new ImageIcon("img\\redpoint.gif");	
	//	private static ImageIcon imagesG = new ImageIcon("img\\greenpoint.gif");

	public void show(Container container){
		try{
			textArea = new JTextArea();
			textArea.setEditable(false);
			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			textArea.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 14));

			JPanel commandPanel = new JPanel();		
			commandPanel.setBackground(new Color(255,255,255));
			BorderLayout layout = new BorderLayout();
			commandPanel.setLayout(layout);
			commandPanel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.SystemColor.control));

			JPanel bottomPanel = new JPanel();		
			bottomPanel.setBackground(new Color(255,255,255));		
			bottomPanel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.SystemColor.control));
			bottomPanel.setBackground(java.awt.SystemColor.control);		
			bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
			bottomPanel.add(Box.createHorizontalStrut(10));	
			memInd = new MemoryIndicator("");
			bottomPanel.add(memInd);
			bottomPanel.add(Box.createHorizontalStrut(5));
			bottomPanel.add(lbl);
			bottomPanel.add(Box.createHorizontalStrut(4));

			setBusyQueue();

			commandPanel.add(bottomPanel,BorderLayout.SOUTH);

		
			container.add(new JScrollPane(textArea),BorderLayout.CENTER);
			container.add(commandPanel,BorderLayout.SOUTH);
		}catch(Exception ioe){
			ioe.printStackTrace();
		}
	}
	

	public static void setFreeQueue(){
		//		  lbl.setIcon(imagesG);
	}

	public static void setBusyQueue(){
		//		  lbl.setIcon(imagesR);
	}
	
	public void setTextAreaText(String message){
		textArea.setText(message);
	}
}
