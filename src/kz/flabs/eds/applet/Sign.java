package kz.flabs.eds.applet;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;


public class Sign {

    private static final long serialVersionUID = -7809666854390973501L;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Signature signature;
    private byte[] realSign;

    public Sign(String signAlg, String provName) throws NullPointerException, NoSuchAlgorithmException, NoSuchProviderException
    {
        if (signAlg == null) {
            throw new NullPointerException();
        } else {
            if (provName == null) {
                signature = Signature.getInstance(signAlg);
            } else {
                signature = Signature.getInstance(signAlg, provName);
            }
        }
    }

    public void signing(byte[] msg) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        if (privateKey == null) {
            throw new IllegalArgumentException();
        }

        signature.initSign(privateKey);
        signature.update(msg);
        realSign = signature.sign();
    }


    public boolean verify(byte[] msg, byte[] sgn) throws InvalidKeyException, IOException, SignatureException
    {
        signature.initVerify(publicKey);
        signature.update(msg);

        boolean result = signature.verify(sgn);
        return result;
    }



    public Boolean prepPair(InputStream in, String alias, char[] passKeyStore, char[] passAlias)
        throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException
    {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(in, passKeyStore);
        Key key = ks.getKey(alias, passAlias);

        if (key instanceof PrivateKey) {
            Certificate cert = ks.getCertificate(alias);
            PublicKey publicKey = cert.getPublicKey();

            setPrivateKey((PrivateKey) key);
            setPublicKey(publicKey);

            return true;
        }

        return false;
    }

    public byte[] getSign()
    {
        return realSign;
    }


    public void setPrivateKey(PrivateKey prk)
    {
        privateKey = prk;
    }

    public PrivateKey getPrivateKey()
    {
        return privateKey;
    }


    public void setPublicKey(PublicKey pbk)
    {
        publicKey = pbk;
    }

    public PublicKey getPublicKey()
    {
        return publicKey;
    }
}
