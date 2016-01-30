package kz.pchelka.administrator;

import java.awt.*;
import javax.swing.*;

public class MemoryIndicator extends JPanel{
	private static final long serialVersionUID = -4389902588303044445L;
	private JProgressBar progress;
	private BoundedRangeModel model;
	
	public MemoryIndicator(String name){
		super();	
		setLayout(new BorderLayout());
		progress = new JProgressBar();
		model = progress.getModel();
		progress.setStringPainted(true);
		add(progress,BorderLayout.SOUTH);		
	}
	
	public void setMax(int dc){
		model.setMaximum(dc);
		model.setMinimum(0);
	}

	public void setLevel(long level){
		int l = (int)level;
		model.setValue(l);
	}

}
