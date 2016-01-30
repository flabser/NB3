package kz.pchelka.webservices;

import javax.xml.namespace.QName;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

    public class TestClient {

    public static void main(String [] args){
        try{
                String endpoint = "http://localhost:15045/WS/services/Simple";  

                Service service = new Service();
                Call call = (Call) service.createCall();

                call.setTargetEndpointAddress( new java.net.URL(endpoint) );
                call.setOperationName( new QName("webservices.lof.kz", "doSomething") );

                String ret = (String) call.invoke( new Object[] {"vasys"} );
        }catch(Exception e){
                System.err.println(e.toString());
        }
    }
}
