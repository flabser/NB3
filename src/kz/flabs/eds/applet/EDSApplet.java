package kz.flabs.eds.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.StringTokenizer;

import javax.crypto.NoSuchPaddingException;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;

import org.apache.commons.codec.binary.Base64;

import kz.flabs.eds.PKCS8;

public class EDSApplet extends JApplet implements ActionListener {

    private static final long serialVersionUID = 1L;

    private JFrame frameConfig;
    private JPasswordField password;
    private JButton btnSign;

    private InputStream is;
    private String mode;

    private String signAlg = "SHA1withRSA";
    private String ksAlias = "kalimed";
    private String ksPassword = "123456";
    private String userPassword = "delkmo4";

    private FileManager fileManager = new FileManager();
    private Sign sign;
    private PKCS8 pkcs8;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    TextField errField;
    TextField pwdField;
    private JPasswordField passwordField;
    private JTextField certAlias;

    public void init() {
        try {
            File f = new File("C:\\OpenSSL-Win32\\bin\\private_pkcs8.der");
            DataInputStream dis = new DataInputStream(new FileInputStream(f));
            byte[] theData = new byte[(int) f.length()];
            dis.readFully(theData);

            pkcs8 = new PKCS8(theData, ksPassword.toCharArray());

            /*pkcs8.rsaSigning("1".getBytes());

            byte[] signature = pkcs8.getSign();
            String signdoc = "";
            for (int z = 0; z < signature.length; z++) {
                signdoc += " " + Byte.toString(signature[z]);
            }
            System.out.println(signdoc);

            System.out.println(pkcs8.rsaVerify("1".getBytes(), pkcs8.getSign()));

            sign = new Sign(signAlg, null);
*/
            mode = this.getParameter("mode");
            if( (mode!=null) && (mode.length()>0) ){
                initGUI();
            } else if( ! checkKeyStoreConfig() ) {
                initGUI();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void start() {}

    public void stop() {}

    public void destroy() {}

    public String getAppletInfo() { return ""; }


    /*public void getKeyStore(String msg) {
        try {

            

            String ksFilePath = fileManager.getLastPath();
            System.out.println(ksFilePath);
            if(ksFilePath == null){
                ksFilePath = fileManager.keyStoreFileDialog().getPath();
            }

            is = new FileInputStream(
                    new URL("file:///" + ksFilePath).getFile());

            sign.prepPair(is, ksAlias, ksPassword.toCharArray(), userPassword.toCharArray());

            sign.signing(msg.getBytes());
            
            // sign.readPrivateKey(is);
            byte[] realSig = sign.getSign();
            
            System.out.println(realSig);
            
            String signdoc = "";
            
            for (int z = 0; z < realSig.length; z++) {
                signdoc += "  " + Byte.toString(realSig[z]);
            }

            System.out.println("---");
            System.out.println(signdoc);
            System.out.println("---");

            System.out.println(verifySign(msg, signdoc));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public String getSign(String signStr)
    {
        try {
            pkcs8.rsaSigning(signStr.getBytes(Charset.forName("UTF-8")));
            byte[] s = pkcs8.getSign();
            String encodeSign = new String(Base64.encodeBase64(s));
            return encodeSign;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            //return "InvalidKeyException";
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //return "NoSuchAlgorithmException";
        } catch (SignatureException e) {
            e.printStackTrace();
            //return "SignatureException";
        } 
        return null;
    }


    public String verifySign(String msg, String sgn)
    {
        try {
            boolean res;

            System.out.println(msg + " " + sgn);
            
            StringTokenizer stoken = new StringTokenizer(sgn, "  ");
            byte[] sign_from_client = new byte[stoken.countTokens()];
            int i = 0;
            while (stoken.hasMoreTokens()) {
                sign_from_client[i] = Byte.parseByte(stoken.nextToken());
                i++;
            }

            res = pkcs8.rsaVerify(msg.getBytes(), sign_from_client);
            System.out.println(res);

            return res ? "true":"false";

        } catch (Exception e) {
            e.printStackTrace();
            return "false";
        }
    }


    public Boolean checkKeyStoreConfig(){

        return false;
    }


    public void actionPerformed(ActionEvent action)
    {
        if( action.getActionCommand() == "SelectCert" ){
            
        } else if( action.getActionCommand() == "okConfig" ){
            char[] tempPassArray = password.getPassword();
            StringBuffer tempPassWhole = new StringBuffer("");

            for (int i = 0; i < tempPassArray.length; i++) {
                tempPassWhole.append(tempPassArray[i]);
            }

            userPassword = tempPassWhole.toString();

            System.out.println(userPassword);
        } else if( action.getActionCommand() == "cancelConfig" ){
            frameConfig.dispose();
        }
    }

    private void initGUI()
    {
        //configForm();
        
        /*
        setBackground(Color.WHITE);
        JPanel panel = new JPanel();

        JPanel titlePanel = new JPanel();
        JPanel ksFileChoosePanel = new JPanel();


        JLabel lKSCheckInf = new JLabel("");


        btnKeyStoreFileChoose.setActionCommand("KeyStoreFileChoose");


        btnSign.setActionCommand("sign");
        btnSign.setEnabled(false);

        titlePanel.add(lTitle);
        ksFileChoosePanel.add(btnKeyStoreFileChoose);
        
        
        panel.setLayout(new BorderLayout());
        panel.add(titlePanel, BorderLayout.CENTER);
        ksFileChoosePanel.setLayout(new BorderLayout());
        ksFileChoosePanel.add(btnKeyStoreFileChoose, BorderLayout.WEST);
        panel.add(ksFileChoosePanel, BorderLayout.AFTER_LINE_ENDS);
        */

        //ImageIcon ksConfigIcon = new ImageIcon(getClass().getResource("images/cog.png"));
        //JButton btnKeyStoreConfig = new JButton(ksConfigIcon);
        //btnKeyStoreConfig.setPreferredSize(new Dimension(22, 22));
        //btnKeyStoreConfig.setActionCommand("KeyStoreConfig");

        //panel.setLayout(new BorderLayout());
        //panel.add(btnSign, BorderLayout.CENTER);
        //panel.add(btnKeyStoreConfig, BorderLayout.EAST);

        //getContentPane().add(panel);

        //btnKeyStoreConfig.addActionListener(this);
        //btnSign.addActionListener(this);
        //this.setSize(125, 22);
        //this.setVisible(true);
        //this.repaint();
    }

 
    private void configForm() {

        JLabel lblConfTitle = new JLabel("Параметры сертификата");
        lblConfTitle.setFont(new Font("Tahoma", Font.BOLD, 14));

        JButton btnSelectCert = new JButton("Выбрать сертификат");
        btnSelectCert.setActionCommand("SelectCert");
        btnSelectCert.addActionListener(this);

        JLabel lblPassword = new JLabel("\u041F\u0430\u0440\u043E\u043B\u044C");
        JLabel lblCertAlias = new JLabel("Название сертификата открытого ключа:");

        passwordField = new JPasswordField();
        passwordField.setBackground(Color.WHITE);
        
        JLabel lblCertAlias_1 = new JLabel("\u041D\u0430\u0437\u0432\u0430\u043D\u0438\u0435 \u0441\u0435\u0440\u0442\u0438\u0444\u0438\u043A\u0430\u0442\u0430 \u043E\u0442\u043A\u0440\u044B\u0442\u043E\u0433\u043E \u043A\u043B\u044E\u0447\u0430");
        
        certAlias = new JTextField();
        certAlias.setBackground(Color.WHITE);
        certAlias.setColumns(10);
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                            .addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
                                .addComponent(lblConfTitle, GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                                .addContainerGap())
                            .addGroup(groupLayout.createSequentialGroup()
                                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                                    .addComponent(lblPassword)
                                    .addComponent(lblCertAlias_1))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                    .addComponent(certAlias, GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                                    .addComponent(passwordField, GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE))
                                .addContainerGap()))
                        .addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
                            .addComponent(btnSelectCert)
                            .addContainerGap())))
        );
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblConfTitle)
                    .addGap(15)
                    .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblCertAlias_1)
                        .addComponent(certAlias, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblPassword)
                        .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnSelectCert)
                    .addGap(122))
        );
        getContentPane().setLayout(groupLayout);
        this.setSize(355, 200);
    }
}
