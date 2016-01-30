package kz.pchelka.console.rmi.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILogListener extends Remote {
    public String getLog (String uid) throws RemoteException;
}
