package kz.flabs.runtimeobj.document.structure;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.runtimeobj.document.AbstractComplexObject;
import kz.flabs.util.XMLUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.annotation.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "class")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmployerCollection extends AbstractComplexObject implements Serializable{
    @XmlTransient
	private static final long serialVersionUID = 1490415831963429183L;

    @XmlElement(name = "employer")
    private List<Employer> emps = new ArrayList<>();


    private transient IDatabase db;

    @XmlAttribute
    private String className = EmployerCollection.class.getName();

    public EmployerCollection(){
		
	}
	
	public EmployerCollection(IDatabase db){
		this.db = db;
	}
	
	public void addEmployer(Employer emp){
		emps.add(emp);
	}
	
	public void addEmployer(String userID){
		Employer emp = db.getStructure().getAppUser(userID);
		addEmployer(emp);
	}
	
	public List<Employer> getEmployers(){
		return emps;
	}
	
	@Override
	public void init(IDatabase db, String initString) throws ComplexObjectException {
        try {
            initString = "<root>" + initString + "</root>";
            DocumentBuilderFactory DocFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder xmlDocBuilder = DocFactory.newDocumentBuilder();
            ByteArrayInputStream stream = new ByteArrayInputStream(initString.getBytes(Charset.forName("UTF-8")));
            org.w3c.dom.Document xmlDoc = xmlDocBuilder.parse(stream);
            org.w3c.dom.Element root = xmlDoc.getDocumentElement();

            NodeList nodes = root.getElementsByTagName("employer");
            for (int i = 0; i < nodes.getLength(); i++) {
                Node empNode = nodes.item(i);
                Employer employer = db.getStructure().getAppUser(XMLUtil.getTextContent(empNode, "userid"));
                addEmployer(employer);
            }
        } catch (ParserConfigurationException e) {
            throw new ComplexObjectException(e, getClass().getName());
        } catch (SAXException e) {
            throw new ComplexObjectException(e, getClass().getName());
        } catch (IOException e) {
            throw new ComplexObjectException(e, getClass().getName());
        } catch (Exception e) {
            throw new ComplexObjectException(e, getClass().getName());
        }
		
	}

	@Override
	public String getContent() {
        if (!emps.isEmpty()) {
            StringBuffer xmlContent = new StringBuffer(10000);
            for (int i = 0; i < emps.size(); i++) {
                xmlContent.append("<employer>");
                xmlContent.append("<userid>" + emps.get(i).getUserID() + "</userid>");
                xmlContent.append("</employer>");
            }
            return xmlContent.toString();
        } else {
            return null;
        }
	}

}
