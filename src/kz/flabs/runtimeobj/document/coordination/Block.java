package kz.flabs.runtimeobj.document.coordination;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.ComplexObjectExceptionType;
import kz.flabs.runtimeobj.document.AbstractComplexObject;
import kz.flabs.util.Util;
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
@XmlAccessorType(XmlAccessType.FIELD)
public class Block extends AbstractComplexObject implements Serializable{
    @XmlTransient
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "id")
    public int blockID;

    @XmlTransient
	public boolean isNew;

    @XmlElement(name = "type")
    public int type;

    @XmlElement(name = "delaytime")
	public int delayTime;

    @XmlElement(name = "status")
    public int status;

    @XmlElement(name = "num")
    public int num;

    @XmlElement(name = "coordinator")
    ArrayList<Coordinator> coordinators = new ArrayList<>();

    public Date coordDate;

    public Block(){
    	blockID = Util.generateRandom();
    }

    public Date getCoordDate() {
        return coordDate;
    }

    public void setCoordDate(Date coordDate) {
        this.coordDate = coordDate;
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

            if (!XMLUtil.getTextContent(xmlDoc, "/root/block/num").equals("")
                    && XMLUtil.getTextContent(xmlDoc, "/root/block/num") != null) {
                this.setNumber(Integer.parseInt(XMLUtil.getTextContent(xmlDoc,
                        "/root/block/num")));
            }
            if (!XMLUtil.getTextContent(xmlDoc, "/root/block/type").equals("")
                    && XMLUtil.getTextContent(xmlDoc, "/root/block/type") != null) {
                this.setType(Integer.parseInt(XMLUtil.getTextContent(xmlDoc,
                        "/root/block/type")));
            }
            if (!XMLUtil.getTextContent(xmlDoc, "/root/block/status").equals("")
                    && XMLUtil.getTextContent(xmlDoc, "/root/block/status") != null) {
                this.setStatus(Integer.parseInt(XMLUtil.getTextContent(xmlDoc,
                        "/root/block/status")));
            }

            if (!XMLUtil.getTextContent(xmlDoc, "/root/block/delaytime").equals("")
                    && XMLUtil.getTextContent(xmlDoc, "/root/block/delaytime") != null) {
                this.delayTime = Integer.parseInt(XMLUtil.getTextContent(xmlDoc,
                        "/root/block/delaytime"));
            }

            if (!XMLUtil.getTextContent(xmlDoc, "/root/block/id").equals("")
                    && XMLUtil.getTextContent(xmlDoc, "/root/block/id") != null) {
                this.blockID = Integer.parseInt(XMLUtil.getTextContent(xmlDoc,
                        "/root/block/id"));
            }

            NodeList nodes = root.getElementsByTagName("coordinator");
            for (int i = 0; i < nodes.getLength(); i++) {
                Node coordNode = nodes.item(i);
                String userid = XMLUtil.getTextContent(coordNode, "userid");
                Coordinator coord = new Coordinator(db, userid);

                if (!XMLUtil.getTextContent(coordNode, "type").equals("")
                        && XMLUtil.getTextContent(coordNode, "type") != null) {
                    coord.type = Integer.parseInt(XMLUtil.getTextContent(coordNode,
                            "type"));
                }

                if (!XMLUtil.getTextContent(coordNode, "current").equals("")
                        && XMLUtil.getTextContent(coordNode, "current") != null) {
                    coord.setCurrent(Boolean.parseBoolean(XMLUtil.getTextContent(coordNode,
                            "current")));
                }

                if (!XMLUtil.getTextContent(coordNode, "num").equals("")
                        && XMLUtil.getTextContent(coordNode, "num") != null) {
                    coord.num = Integer.parseInt(XMLUtil.getTextContent(coordNode,
                            "num"));
                }

                if (!XMLUtil.getTextContent(coordNode, "decision").equals("")
                        && XMLUtil.getTextContent(coordNode, "decision") != null) {
                    coord.getDecision().decision = Integer.parseInt(XMLUtil.getTextContent(coordNode,
                            "decision"));
                }

                coord.setComment(XMLUtil.getTextContent(coordNode, "comm"));

                if (!XMLUtil.getTextContent(coordNode, "decisiondate").equals("")
                        && XMLUtil.getTextContent(coordNode, "decisiondate") != null) {
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
                    Date date = format.parse(XMLUtil.getTextContent(coordNode,"decisiondate"));
                    coord.setDecisionDate(date);
                }

                if (!XMLUtil.getTextContent(coordNode, "coorddate").equals("")
                        && XMLUtil.getTextContent(coordNode, "coorddate") != null) {
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
                    Date date = format.parse(XMLUtil.getTextContent(coordNode,"coorddate"));
                    coord.setCoorDate(date);
                }
                
                addCoordinator(coord);
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


    public int getNumber() {
        return num;
    }

    public void setNumber(int num) {
        this.num = num;
    }

    public boolean isCurrent() {
        return this.getStatus() == ICoordConst.BLOCK_STATUS_COORDINATING;
    }

    public void addCoordinator(Coordinator coord) {
    	int num = coordinators.size() + 1;
    	coord.setNumber(num);
        this.coordinators.add(coord);
    }

    public void start() {
        this.status = ICoordConst.BLOCK_STATUS_COORDINATING;
        switch (this.type) {
            case ICoordConst.PARALLEL_COORDINATION:
                for (Coordinator coord : coordinators) {
                    coord.setCurrent(true);
                    coord.setCoorDate(new Date());
                }
                break;
            case ICoordConst.SERIAL_COORDINATION:
                for (Coordinator coord : coordinators) {
                    if ("1".equalsIgnoreCase(coord.getCoordNumber())) {
                        coord.setCurrent(true);
                    }
                }
                break;
        }
    }

    public void stop() {
        this.status = ICoordConst.BLOCK_STATUS_COORDINATED;
    }

    public ArrayList<Coordinator> getCurrentCoordinators() {
        ArrayList<Coordinator> cur_coords = new ArrayList<>();
        for (Coordinator coord : this.coordinators) {
            if (coord.isCurrent()) {
                cur_coords.add(coord);
            }
        }
        return cur_coords;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

    @Override
    public String getContent() {
        StringBuffer xmlContent = new StringBuffer(10000);
        xmlContent.append("<num>" + this.getNumber() + "</num>");
        xmlContent.append("<type>" + this.getType() + "</type>");
        xmlContent.append("<status>" + this.getStatus() + "</status>");
        xmlContent.append("<id>" +this.blockID + "</id>");
        xmlContent.append("<delaytime>" +this.delayTime + "</delaytime>");
            for (int i = 0; i < coordinators.size(); i++) {
                xmlContent.append("<coordinator>");
                xmlContent.append("<userid>" + coordinators.get(i).getUserID() + "</userid>");
                xmlContent.append("<type>" + coordinators.get(i).getCoordType() + "</type>");
                xmlContent.append("<current>" + coordinators.get(i).isCurrent() + "</current>");
                Decision d = coordinators.get(i).getDecision();
                xmlContent.append("<comm>" + d.getComment() + "</comm>");
                xmlContent.append("<num>" + coordinators.get(i).getCoordNumber() + "</num>");
                xmlContent.append("<decision>" + d.decision + "</decision>");
                if (d.getDecisionDate() != null) {
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
                    xmlContent.append("<decisiondate>" + format.format(d.getDecisionDate()) + "</decisiondate>");
                } else {
                    xmlContent.append("<coorddate></coorddate>");
                }

                if (coordinators.get(i).getCoorDate() != null) {
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
                    xmlContent.append("<coorddate>" + format.format(coordinators.get(i).getCoorDate()) + "</coorddate>");
                } else {
                    xmlContent.append("<coorddate></coorddate>");
                }
                xmlContent.append("</coordinator>");
            }
            return xmlContent.toString();
    }


	public ArrayList<Coordinator> getCoordinators() {
		return coordinators;
	}

	public Coordinator getFirstCoordinator() {
		return coordinators.get(0);
	}

    public Coordinator getNextCoordinator(Coordinator baseObject) {
        int num = baseObject.num;
        if (coordinators.size() > num) {
            return coordinators.get(num);
        } else {
            return null;
        }
    }

}
