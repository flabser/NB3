package kz.flabs.runtimeobj;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.runtimeobj.document.AbstractComplexObject;
import kz.flabs.util.XMLUtil;
import org.xml.sax.SAXException;

import javax.xml.bind.annotation.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
@XmlRootElement(name = "class")
@XmlAccessorType(XmlAccessType.FIELD)
public class CrossLink extends AbstractComplexObject implements Serializable{
	
	private String viewText;
	private String URL;

    @XmlAttribute
    private String className = CrossLink.class.getName();

    @XmlTransient
    private static final long serialVersionUID = 1L;
	
	public String getViewText() {
		return viewText;
	}

	public void setViewText(String viewText) {
		this.viewText = viewText;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	@Override
	public void init(IDatabase db, String initString) throws ComplexObjectException {
		try {
			initString = "<root>" + initString + "</root>";

			DocumentBuilderFactory DocFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder xmlDocBuilder = DocFactory.newDocumentBuilder();
			ByteArrayInputStream stream = new ByteArrayInputStream(
					initString.getBytes(Charset.forName("UTF-8")));
			org.w3c.dom.Document xmlDoc = xmlDocBuilder.parse(stream);
			
			setViewText(XMLUtil.getTextContent(xmlDoc,"/root/viewText"));
			setURL(XMLUtil.getTextContent(xmlDoc,"/root/url").replaceAll("&amp;", "&"));
			
		} catch (ParserConfigurationException e) {
			throw new ComplexObjectException(e, getClass().getName());
		} catch (SAXException e) {
			throw new ComplexObjectException(e, getClass().getName());
		} catch (IOException e) {
			throw new ComplexObjectException(e, getClass().getName());
		}  catch (Exception e) {
			throw new ComplexObjectException(e, getClass().getName());
		}
		
	}

	@Override
	public String getContent() {
		StringBuffer xmlContent = new StringBuffer(10000);
		xmlContent.append("<viewText>" + getViewText().replaceAll("&", "&amp;") + "</viewText>");
		xmlContent.append("<url>" + getURL().replaceAll("&", "&amp;") + "</url>");
		
		return xmlContent.toString();
	}

}
