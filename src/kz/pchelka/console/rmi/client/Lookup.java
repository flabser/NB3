package kz.pchelka.console.rmi.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;

public class Lookup extends Thread implements Remote, Runnable
{
    Thread thrd;
    private static ILogListener rLog = null;
    private Console console;
    private String uid;
    private String log;
    private Boolean running = true;
    private int tsleep = 50;
    private int tstep = 100;
    private int tminsleep = 50;
    private int tmaxsleep = 1000;

    public Lookup(Console console, String lookup_url, String uid)
    {
        try {
            System.setProperty("java.security.policy", "client.policy");
            System.setSecurityManager(new RMISecurityManager());
        }
        catch (Exception ex) {
            console.printWarnMsg("Error in RMIRegistry setSecurityManager: " + ex);
            console.printWarnMsg("Unable to set permissions. Perhaps applet is not signed.");
        }

        this.console = console;
        this.uid = uid;
        rLog = this.Connect(lookup_url);

        if (running) {
            thrd = new Thread(this);
            thrd.setDaemon(true);
            thrd.start();
        }
    }

    private ILogListener Connect(String lookup_url) {
        try {
            running = true;
            return (ILogListener) Naming.lookup(lookup_url);
        } catch (RemoteException re) {
            console.printErrorMsg("RemoteException: "+re.getMessage());
            console.printWarnMsg("Could not connect to server");
        } catch (MalformedURLException me) {
            console.printErrorMsg("MalformedURLException: "+me.getMessage());
        } catch (NotBoundException ne) {
            console.printErrorMsg("NotBoundException: "+ne.getMessage());
        } catch (Exception e) {
            console.printErrorMsg("Exception: "+e.getMessage());
        }

        running = false;
        return null;
    }

    public void run() {
        while (running) {
            try {
                Thread.sleep(tsleep);

                log = rLog.getLog(uid).trim();
                if(log.length()>0) {
                    console.printMsg(log);
                    tsleep = tminsleep;
                } else {
                    if (tsleep < tmaxsleep) tsleep += tstep;
                }

            } catch (RemoteException re) {
                running = false;
                console.printErrorMsg("RemoteException: "+re.getMessage());
                console.printWarnMsg("Lost connection to the server");
            } catch (Exception e) {
                running = false;
                console.printErrorMsg("Exception: "+e.getMessage());
            }
        }
    }
}
