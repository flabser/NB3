package kz.pchelka.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.DailyRollingFileAppender;

public class DateFileAppender extends DailyRollingFileAppender{
	@Override
    public void  setFile(String fileName) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");
		super.setFile(fileName + dateFormat.format(new Date()) + ".log");
	}
}
