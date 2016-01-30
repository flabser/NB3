package kz.flabs.runtimeobj.document.task;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.ComplexObjectExceptionType;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@XmlRootElement(name = "class")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExecsBlock extends AbstractComplexObject implements Serializable {

    @XmlElement(name = "executor")
	private ArrayList<Executor> executors = new ArrayList<Executor>();

    @XmlTransient
	private static final long serialVersionUID = 1L;

    @XmlAttribute
    private String className = ExecsBlock.class.getName();

	public void addExecutor(Executor taskExecutor) {
		executors.add(taskExecutor);		
	}

	public int getExecutorsCount() {
		return executors.size();
	}

	public int getResetedExecutorsCount() {
		int c = 0;
		for(Executor e:executors){
			if (e.isReset) c ++ ;
		}
		return c;
	}

	@Override
	public void init(IDatabase db, String initString) throws ComplexObjectException {
		try {
			initString = "<root>" + initString + "</root>";

			DocumentBuilderFactory DocFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder xmlDocBuilder;

			xmlDocBuilder = DocFactory.newDocumentBuilder();
			ByteArrayInputStream stream = new ByteArrayInputStream(initString.getBytes(Charset.forName("UTF-8")));

			org.w3c.dom.Document xmlDoc = xmlDocBuilder.parse(stream);
			org.w3c.dom.Element root = xmlDoc.getDocumentElement();

			NodeList nodes = root.getElementsByTagName("executor");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node execNode = nodes.item(i);

				Executor exec = new Executor();

				if (!XMLUtil.getTextContent(execNode, "num").equals("")
						&& XMLUtil.getTextContent(execNode, "num") != null) {
					exec.num = Integer.parseInt(XMLUtil.getTextContent(execNode,
							"num"));
				}

				exec.setID(XMLUtil.getTextContent(execNode, "usrid"));
                int type = XMLUtil.getNumberContent(execNode, "type", 1);
                switch (type) {
                    case 1:
                        exec.type = Executor.ExecutorType.INTERNAL;
                        break;
                    case 2:
                        exec.type = Executor.ExecutorType.EXTERNAL;
                        break;
                    default:
                        exec.type = Executor.ExecutorType.INTERNAL;
                        break;
                }

				exec.resetAuthorID = XMLUtil.getTextContent(execNode, "resauthid");
				exec.comment = XMLUtil.getTextContent(execNode, "comm");

				if (!XMLUtil.getTextContent(execNode, "isres").equals("")
						&& XMLUtil.getTextContent(execNode, "isres") != null) {
					exec.isReset = Boolean.parseBoolean(XMLUtil.getTextContent(execNode,
							"isres")); 
				}

				if (!XMLUtil.getTextContent(execNode, "resp").equals("")
						&& XMLUtil.getTextContent(execNode, "resp") != null) {
					exec.setResponsible(Integer.parseInt(XMLUtil.getTextContent(execNode,
							"resp")));
				}

				if (!XMLUtil.getTextContent(execNode, "rdate").equals("")
						&& XMLUtil.getTextContent(execNode, "rdate") != null) {
					SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
					Date date = format.parse(XMLUtil.getTextContent(execNode,"rdate"));			

					exec.setResetDate(date);
				}

				if (!XMLUtil.getTextContent(execNode, "execper").equals("")
						&& XMLUtil.getTextContent(execNode, "execper") != null) {
					exec.setPercentOfExecution(Integer.parseInt(XMLUtil.getTextContent(execNode,"execper")));
				}

				addExecutor(exec);

			}

		} catch (ParserConfigurationException e) {
			throw new ComplexObjectException(e, getClass().getName());
		} catch (SAXException e) {
			throw new ComplexObjectException(e, getClass().getName());
		} catch (IOException e) {
			throw new ComplexObjectException(e, getClass().getName());
		} catch (ParseException e) {
			throw new ComplexObjectException(ComplexObjectExceptionType.PARSER_ERROR, e.getMessage() + " " + getClass().getName());
		} catch (Exception e) {
			throw new ComplexObjectException(e, getClass().getName());
		}

	}

	@Override
	public String getContent() {
		if (!executors.isEmpty()){
			StringBuffer xmlContent = new StringBuffer(10000);
			for (int i = 0; i < executors.size(); i++) {
				xmlContent.append("<executor>");
				xmlContent.append("<num>" + executors.get(i).num + "</num>");
				xmlContent.append("<usrid>" + executors.get(i).getID() + "</usrid>");
				xmlContent.append("<type>" + (executors.get(i).type == Executor.ExecutorType.INTERNAL ? 1 : 2) + "</type>");
				xmlContent.append("<resauthid>" + executors.get(i).resetAuthorID + "</resauthid>");
				xmlContent.append("<comm>" + executors.get(i).comment + "</comm>");
				xmlContent.append("<isres>" + executors.get(i).isReset + "</isres>");
				xmlContent.append("<resp>" + executors.get(i).responsible + "</resp>");
				if(executors.get(i).getResetDate() != null){
					SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");			
					
					xmlContent.append("<rdate>" + format.format(executors.get(i).getResetDate()) + "</rdate>");
				}else{
					xmlContent.append("<rdate></rdate>");
				}
				xmlContent.append("</executor>");
			}

			return xmlContent.toString();
		}else{
			return null;
		}
	}

	public ArrayList<Executor> getExecutors() {
		return executors;
	}
	

}
