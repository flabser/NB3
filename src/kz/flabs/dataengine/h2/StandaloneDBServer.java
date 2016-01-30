package kz.flabs.dataengine.h2;

import kz.flabs.dataengine.DatabaseUtil;
import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class StandaloneDBServer {
    static Server server;

    public void start() {
        try {
            if (server == null || server.isRunning(false)) {
                System.out.println("Starting Tcp Server.......");
                server = Server.createTcpServer(new String[]{"-tcp","-tcpAllowOthers","-tcpPort","8043"});
                server.start();
                System.out.println(server.getStatus());
                System.out.println(server.getURL());
                System.out.println("Started Tcp Server.......");
                Connection conn = DriverManager.getConnection("jdbc:h2:tcp://192.168.0.45:8043/C:/workspace/CashTracker/CashTracker2013_data/CashTracker;");
                System.out.println(conn.getClientInfo().stringPropertyNames());
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            DatabaseUtil.errorPrint(e);
        }
    }

    public void stop() {
        if (server != null && server.isRunning(true)) {
            System.out.println("Stoping Tcp Server.......");
            server.stop();
            System.out.println("Stoped Tcp Server.......");
        }
    }
    
    public static void main(String[] args){
    	new StandaloneDBServer().start();
    }
}
