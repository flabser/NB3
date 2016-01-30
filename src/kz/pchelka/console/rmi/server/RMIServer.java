package kz.pchelka.console.rmi.server;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import kz.pchelka.console.rmi.client.ILogListener;
import kz.pchelka.env.Environment;
import kz.pchelka.server.Server;

public final class RMIServer {

    private static Registry registry;
    private static RMIServer _instance = null;
    public Logger rlogger;

    private RMIServer() {
        try {
            rlogger = new Logger();
            Registry registry = LocateRegistry.createRegistry(Environment.remoteConsolePort);
            ILogListener stub = (ILogListener)UnicastRemoteObject.exportObject(rlogger, 0);
            registry.rebind(Environment.remoteConsoleServer, stub);

            Server.logger.verboseLogEntry("RMIServer started on port: " + Environment.remoteConsolePort + 
            		" server-name: " + Environment.remoteConsoleServer);            
            
        } catch (Exception e) {
            try {
                registry.unbind(Environment.remoteConsoleServer);
            } catch (AccessException e1) {
            } catch (RemoteException e1) {
            } catch (NotBoundException e1) {
            } catch (Exception e1) {	
            }
            
            Server.logger.errorLogEntry("Error occured RMIServer: " + e.getMessage());
          
        }
    }

    public static synchronized RMIServer getInstance() {
        if (_instance == null){
                _instance = new RMIServer();
        }
        return _instance;
    }
}
