package kz.pchelka.console.rmi.client;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

class Console implements DocumentListener {

    private Document doc;
    private JTextPane console;
    private static SimpleAttributeSet ERROR_ENTRY = new SimpleAttributeSet();
    private static SimpleAttributeSet NORMAL_ENTRY = new SimpleAttributeSet();
    private static SimpleAttributeSet WARN_ENTRY = new SimpleAttributeSet();
    private static String CRLF = "\r\n";

    public Console(String cname) {
        console = new JTextPane();
        console.setName(cname);
        console.setEditable(false);
        console.setFont(new Font("Courier New", 0, 14));
        //console.setForeground(new Color(255, 255, 255));
        //console.setBackground(new Color(0, 0, 0));
        doc = console.getDocument();
        doc.addDocumentListener( this );

        StyleConstants.setForeground(ERROR_ENTRY, new Color(255, 0, 0));
        StyleConstants.setForeground(NORMAL_ENTRY, Color.BLACK);
        StyleConstants.setForeground(WARN_ENTRY, Color.BLUE);

        /*
        StyleConstants.setForeground(ERROR_ENTRY, new Color(255,126,126));
        StyleConstants.setForeground(NORMAL_ENTRY, Color.white);
        StyleConstants.setForeground(WARN_ENTRY, Color.ORANGE);
        */
    }

    public JTextPane getConsole() {
        return this.console;
    }

    private void insertText(String text, AttributeSet set) {
        try {
            doc.insertString(doc.getLength(), text + CRLF, set);
        } catch (BadLocationException e) {
            System.err.println(e);
        }
    }

    public void printMsg(String message) {

        if(message.indexOf(" ERROR ")>0){
            printErrorMsg(message);
        } else if(message.indexOf(" DEBUG ")>0){
            printDebugMsg(message);
        } else if(message.indexOf(" WARN ")>0){
            printWarnMsg(message);
        } else {
            printInfoMsg(message);
        }
    }

    public void printInfoMsg(String message) {
        insertText(message, NORMAL_ENTRY);
    }

    public void printErrorMsg(String message) {
        insertText(message, ERROR_ENTRY);
    }

    public void printDebugMsg(String message) {
        insertText(message, NORMAL_ENTRY);
    }

    public void printWarnMsg(String message) {
        insertText(message, WARN_ENTRY);
    }

    public void setText(String message) {
        console.setText(message);
    }

    public void clear() {
        console.setText("");
    }

    //DocumentListener events
    public void removeUpdate(DocumentEvent e) {

    }

    public void insertUpdate(DocumentEvent e) {
        if (RemoteConsole.chb_autoScrl.isSelected()) {
            console.setCaretPosition(doc.getLength());
        }
    }

    public void changedUpdate(DocumentEvent e) {

    }
}