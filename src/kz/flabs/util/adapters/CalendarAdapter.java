package kz.flabs.util.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarAdapter extends XmlAdapter<String, Calendar> {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Calendar unmarshal(String v) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateFormat.parse(v));
        return calendar;
    }

    @Override
    public String marshal(Calendar v) throws Exception {
        //TODO TimeZone
        return dateFormat.format(v.getTime());
    }
}
