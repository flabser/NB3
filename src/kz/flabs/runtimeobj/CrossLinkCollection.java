package kz.flabs.runtimeobj;

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
import java.nio.charset.Charset;
import java.util.ArrayList;

@XmlRootElement(name = "class")
@XmlAccessorType(XmlAccessType.FIELD)
public class CrossLinkCollection extends AbstractComplexObject{

    @XmlElement(name = "link")
    ArrayList<CrossLink> links = new ArrayList<CrossLink>();

    @XmlAttribute
    private String className = CrossLinkCollection.class.getName();

	@Override
	public void init(IDatabase db, String initString) throws ComplexObjectException {
        try {
            initString = "<root>" + initString + "</root>";
            DocumentBuilderFactory DocFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder xmlDocBuilder = DocFactory.newDocumentBuilder();
            ByteArrayInputStream stream = new ByteArrayInputStream(initString.getBytes(Charset.forName("UTF-8")));
            org.w3c.dom.Document xmlDoc = xmlDocBuilder.parse(stream);
            org.w3c.dom.Element root = xmlDoc.getDocumentElement();

            NodeList nodes = root.getElementsByTagName("link");
            for (int i = 0; i < nodes.getLength(); i++) {
                Node linkNode = nodes.item(i);
                CrossLink link = new CrossLink();
                link.setViewText(XMLUtil.getTextContent(linkNode,"viewText"));
                link.setURL(XMLUtil.getTextContent(linkNode,"url").replaceAll("&amp;", "&"));
                this.add(link);
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
        if (!links.isEmpty()) {
            StringBuffer xmlContent = new StringBuffer(10000);
            for (int i = 0; i < links.size(); i++) {
                xmlContent.append("<link>");
                xmlContent.append(links.get(i).getContent());
                xmlContent.append("</link>");
            }
            return xmlContent.toString();
        } else {
            return null;
        }

    }

	public void add(CrossLink o) {
		links.add(o);
	}
	
	public ArrayList<CrossLink> getLinkCollection(){
		return links;
	}
	
}
