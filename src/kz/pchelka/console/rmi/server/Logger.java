package kz.pchelka.console.rmi.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map.Entry;

import kz.pchelka.console.rmi.client.ILogListener;

public class Logger implements ILogListener {

    private static SimpleDateFormat df;
    @SuppressWarnings("rawtypes")
    private HashMap <String, Queue> userLogs = new HashMap<String, Queue>();
    private Queue<String> logs = new LinkedList<String>();
    private String tmp = "";
    private String msg = "";

    public Logger() {
        df = (SimpleDateFormat)DateFormat.getTimeInstance(DateFormat.DEFAULT);
        df.applyPattern("HH:mm:ss");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setLog(String msg) {

        try {
            if(userLogs.size() > 15){
                userLogs.clear();
            }

            for (Entry <String, Queue> entry : userLogs.entrySet()) {
                try {
                    if(userLogs.get(entry.getKey()).size()>10){
                        userLogs.get(entry.getKey()).clear();
                    }
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e){}

        for (Entry <String, Queue> entry : userLogs.entrySet()) {
            try {
                userLogs.get(entry.getKey()).add(msg);
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    public void normalLogEntry(String log) {
        tmp = df.format(new Date())+" INFO "+log;
        setLog(tmp);
    }

    public void warnLogEntry(String log) {
        tmp = df.format(new Date())+" WARN "+log;
        setLog(tmp);
    }

    public void debugLogEntry(String log) {
        tmp = df.format(new Date())+" DEBUG "+log;
        setLog(tmp);
    }

    public void errorLogEntry(String log) {
        tmp = df.format(new Date())+" ERROR "+log;
        setLog(tmp);
    }

    public String getLog(String uid) {

        try {
            msg = "";
            if(userLogs.get(uid).size()>0){
                msg = userLogs.get(uid).poll().toString();
            }
        } catch (Exception e){
            if( ! userLogs.containsKey(uid)){
                logs = new LinkedList<String>();
                userLogs.put(uid, logs);
                System.out.println("RemoteConsole connected. uid: "+uid);
            } else {
                System.err.println("RMI Logger error: "+e.getMessage());
            }

            msg = "";
        }

        return msg.trim();
    }
}
