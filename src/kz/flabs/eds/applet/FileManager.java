package kz.flabs.eds.applet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;


public class FileManager {

    private String lastDirPath = "";
    private static String ksFileName = "nb_ks_0001.txt";

    public FileManager()
    {

    }

    //
    private File fileChooser(String filterDesc, String fileExt) throws IOException
    {
        JFileChooser chooser = new JFileChooser(lastDirPath);

        if ( (fileExt != null) && (filterDesc != null) ) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(filterDesc, fileExt);
            chooser.setFileFilter(filter);
        }

        int returnVal = chooser.showOpenDialog(chooser);
        if ( returnVal == JFileChooser.APPROVE_OPTION ) {
            lastDirPath = chooser.getCurrentDirectory().getAbsolutePath();
            saveLastPath(chooser.getSelectedFile().getAbsolutePath());

            return chooser.getSelectedFile();
        }

        return null;
    }

    //
    public File keyStoreFileDialog() throws IOException
    {
        return fileChooser("keystore", "jks");
    }

    //
    private void saveLastPath(String ksFilePath) throws IOException
    {
        BufferedWriter bfw = null;
        try {
            File temp = new File(System.getProperty("java.io.tmpdir") + File.separator + ksFileName);

            bfw = new BufferedWriter(new FileWriter(temp));
            bfw.write(ksFilePath);
            bfw.close();
        } catch (IOException e) {
            System.out.println(e);
            if(bfw!=null) bfw.close();
        }
    }

    //
    public String getLastPath() throws IOException
    {
        BufferedReader bfr = null;
        try {
            File file = new File(System.getProperty("java.io.tmpdir") + File.separator + ksFileName);

            bfr = new BufferedReader(new FileReader(file));
            String ksFilePath = bfr.readLine();
            bfr.close();

            if(ksFilePath!=null){
                file = new File(ksFilePath);
                if( file.exists() ){
                    return ksFilePath;
                }
            }

            return null;
        } catch (IOException e) {
            System.out.println(e);
            if( bfr!=null ) bfr.close();

            return null;
        }
    }

    //
    public static byte[] readFile(String fileName) throws IOException
    {
        File f = new File(fileName);
        DataInputStream dis = new DataInputStream(new FileInputStream(f));
        byte[] theData = new byte[(int) f.length()];
        dis.readFully(theData);
        return theData;
    }
}
