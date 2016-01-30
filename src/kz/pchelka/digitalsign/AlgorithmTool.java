package kz.pchelka.digitalsign;

import kz.iola.jce.provider.IolaProvider;
import org.apache.commons.codec.binary.Base64;

import java.security.*;


public class AlgorithmTool {
    private DigitalProperties digiProp = new DigitalProperties();
    Log4jLoggerApplet log = new Log4jLoggerApplet("");
    public AlgorithmTool() {}

    public String setSign(byte[] signObj, PrivateKey privateKey){
        String signResult = null;
        try{
            AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    try {
                        Security.addProvider(new IolaProvider());
                    } catch (Exception e) {
                        for (StackTraceElement el : e.getStackTrace()) {
                            log.errorLogEntry(el.toString());
                        }
                    }
                    return Boolean.TRUE;
                }
            });
            String alg = "SHA1withRSA";
            if (!privateKey.getAlgorithm().equalsIgnoreCase("RSA")) {
                alg = "ECGOST3410";
            }
            final Signature sign = Signature.getInstance(alg, IolaProvider.PROVIDER_NAME);
            sign.initSign(privateKey);
            sign.update(signObj);
            signObj = sign.sign();
            signResult = Base64.encodeBase64String(signObj);
        }catch(Throwable e){
            System.out.println(e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) {
                log.errorLogEntry(el.toString());
                System.out.println(el.toString());
            }
            return null;
        }
        return signResult;
    }

    public boolean verify(byte[] signObj, String signature, PublicKey key){
        boolean validate = false;
        try {
            AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    try {
                        Security.addProvider(new IolaProvider());
                    } catch (Exception e) {
                        for (StackTraceElement el : e.getStackTrace()) {
                            log.errorLogEntry(el.toString());
                        }
                    }
                    return Boolean.TRUE;
                }
            });
            String alg = "SHA1withRSA";
            if (!key.getAlgorithm().equalsIgnoreCase("RSA")) {
                alg = "ECGOST3410";
            }
            final Signature sign = Signature.getInstance(alg, IolaProvider.PROVIDER_NAME);
            sign.initVerify(key);
            sign.update(signObj);
            byte[] temp = Base64.decodeBase64(signature);
            validate = sign.verify(temp);
        } catch (NoSuchAlgorithmException e) {
            log.errorLogEntry(e);
        } catch (SignatureException e) {
            log.errorLogEntry(e);
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            log.errorLogEntry(e);
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return validate;

    }
}
