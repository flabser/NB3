package kz.flabs.eds;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
//import java.security.PrivateKey;
//import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
//import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PKCS8 {

    private RSAPrivateCrtKey rsaPrivateKey;
    private RSAPublicKey rsaPublicKey;
    //private PrivateKey privateKey;
    //private PublicKey publicKey;
    private Signature signature;
    private byte[] realSign;
    private String keyAlg = "RSA";
    private String signAlg = "SHA1withRSA";

    public PKCS8(byte[] epk, char[] psw) throws IOException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, InvalidKeyException,
            InvalidAlgorithmParameterException, SignatureException
    {
        EncryptedPrivateKeyInfo ePKInfo = new EncryptedPrivateKeyInfo(epk);
        Cipher cipher = Cipher.getInstance(ePKInfo.getAlgName());
        PBEKeySpec pbeKeySpec = new PBEKeySpec(psw);

        // Now create the Key from the PBEKeySpec
        SecretKeyFactory skFac = SecretKeyFactory.getInstance(ePKInfo.getAlgName());
        Key pbeKey = skFac.generateSecret(pbeKeySpec);

        // Extract the iteration count and the salt
        AlgorithmParameters algParams = ePKInfo.getAlgParameters();
        cipher.init(Cipher.DECRYPT_MODE, pbeKey, algParams);

        // Decrypt the encryped private key into a PKCS8EncodedKeySpec
        KeySpec pkcs8KeySpec = ePKInfo.getKeySpec(cipher);

        // Now retrieve the RSA Public and private keys by using an RSA
        // keyfactory.
        KeyFactory rsaKeyFac = KeyFactory.getInstance(keyAlg);
        signature = Signature.getInstance(signAlg);

        // First get the private key
        rsaPrivateKey = (RSAPrivateCrtKey) rsaKeyFac.generatePrivate(pkcs8KeySpec);

        // Now derive the RSA public key from the private key
        RSAPublicKeySpec rsaPubKeySpec = new RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent());
        rsaPublicKey = (RSAPublicKey) rsaKeyFac.generatePublic(rsaPubKeySpec);
    }

    /*public PKCS8(byte[] pk) throws IOException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, InvalidKeyException,
            InvalidAlgorithmParameterException, SignatureException
    {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pk);
        KeyFactory keyFactory = KeyFactory.getInstance(keyAlg);
        privateKey = keyFactory.generatePrivate(keySpec);
        //publicKey = (PublicKey) keyFactory.generatePublic(keySpec);
        signature = Signature.getInstance(signAlg);
    }*/

    public void rsaSigning(byte[] msg) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        if (rsaPrivateKey == null) {
            throw new IllegalArgumentException();
        }

        signature.initSign(rsaPrivateKey);
        signature.update(msg);
        realSign = signature.sign();
    }

    public boolean rsaVerify(byte[] msg, byte[] sgn) throws InvalidKeyException, SignatureException
    {
        signature.initVerify(rsaPublicKey);
        signature.update(msg);

        return signature.verify(sgn);
    }


    /*public void signing(byte[] msg) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

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

        return signature.verify(sgn);
    }*/

    // Метод возвращает последнюю полученную цифровую подпись
    public byte[] getSign()
    {
        return realSign;
    }

    public static void main(String[] args) throws IOException,
        NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidKeySpecException, InvalidKeyException,
        InvalidAlgorithmParameterException, SignatureException
    {
        File f = new File("C:\\OpenSSL-Win32\\bin\\private_pkcs8.der");
        DataInputStream dis = new DataInputStream(new FileInputStream(f));
        byte[] theData = new byte[(int) f.length()];
        dis.readFully(theData);

        PKCS8 pkcs8 = new PKCS8(theData, "123456".toCharArray());
        pkcs8.rsaSigning("1".getBytes(Charset.forName("UTF-8")));

        byte[] signature = pkcs8.getSign();
        String signdoc = "";
        for (int z = 0; z < signature.length; z++) {
            signdoc += " " + Byte.toString(signature[z]);
        }
        System.out.println(signdoc);

        System.out.println(pkcs8.rsaVerify("1".getBytes(), pkcs8.getSign()));
    }
}
