package kz.pchelka.digitalsign;


import java.rmi.RemoteException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class DigitalProperties {
    private static String ca;
    private static String provider = "JAVA_PROVIDER";
    private static String digestAlgo = "SHA1";
    //private static String signAlgo = "SHA1withDSA";
    private static String signAlgo = "SHA1withRSA";

    private PrivateKey privKey;
    private PublicKey pubKey;
    private KeyPair keys;
    public DigitalProperties() {
        //keys = JavaKeyProvider.getKeysFromStore();
        //privKey = keys.getPrivate();
        //pubKey = keys.getPublic();
    }
    public PrivateKey getPrivateKey()throws RemoteException{
        return privKey;
    }
    public PublicKey getPublicKey()throws RemoteException{
        return pubKey;
    }
    @SuppressWarnings("static-access")
    public void setProvider(String provider){
        this.provider = provider;
    }
    @SuppressWarnings("static-access")
    public void setSignAlgo(String signAlgo){
        this.signAlgo = signAlgo;
    }
    @SuppressWarnings("static-access")
    public void setDigestAlgo(String digestAlgo){
        this.digestAlgo = digestAlgo;
    }

    public static String getCA(){
        return ca;
    }
    public static String getProvider(){
        return provider;
    }
    public static String getSignAlgo(){
        return signAlgo;
    }
    public static String getDigestAlgo(){
        return digestAlgo;
    }
}
