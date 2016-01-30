package kz.flabs.runtimeobj.document.coordination;

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
@XmlRootElement(name = "class")
@XmlAccessorType(XmlAccessType.FIELD)
public class BlockCollection extends AbstractComplexObject implements Serializable, ICoordConst {
	@XmlTransient
    private static final long serialVersionUID = -587515068485558947L;

    @XmlElement(name = "block")
	private ArrayList<Block> blocks = new ArrayList<>();

    @XmlElement(name = "status")
	private int status = ICoordConst.STATUS_UNDEFINED;

    @XmlAttribute
    private String className = BlockCollection.class.getName();

	public BlockCollection(){

	}

	public void addBlock(Block block) {
		if (block.getNumber() == 0) {
            int num = calculateBlockNumber(block);
            block.setNumber(num);
        }
        blocks.add(block);
	}

	public void start() {

	}

	public void stop() {

	}

    public int calculateBlockNumber(Block block) {
        if (block.getType() ==  ICoordConst.TO_SIGN){
            return 999;
        }else{
            int num = 1;
            for (Block bl : blocks) {
                if (bl.getType() != ICoordConst.TO_SIGN) {
                    num++;
                }
            }
            return num;
        }
    }

    public void clearBlocks() {
        blocks.clear();
    }

    public void reset() {
        blocks.clear();
        status = ICoordConst.STATUS_UNDEFINED;
    }

	public Block getCurrentBlock() {
		for (Block block : blocks) {
			if (block.isCurrent()) {
				return block;
			}
		}
		return null;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public ArrayList<Block> getBlocks() {
		return blocks;
	}

	public  ArrayList<Block> getCoordBlocks() {
		ArrayList<Block> cBlocks = new ArrayList<>();
		for (Block block : blocks) {
			if (block.getType() == ICoordConst.PARALLEL_COORDINATION || block.getType() == ICoordConst.SERIAL_COORDINATION) {
				cBlocks.add(block);
			}
		}
		return cBlocks;
	}

	public Block getSignBlock() {
		for (Block block : blocks) {
			if (block.getType() == ICoordConst.TO_SIGN) {
				return block;
			}
		}
		return null;
	}

	public void updateBlock(Block baseObject) {
		for (Block block : this.blocks) {
			if (block.blockID == baseObject.blockID) {
				blocks.remove(block);
				blocks.add(baseObject);
				return;
			}
		}
		
		if (baseObject.getType() ==  ICoordConst.TO_SIGN){
			baseObject.setNumber(999);
		}else{
			int num = blocks.size() + 1;
			baseObject.setNumber(num);
		}
        blocks.add(baseObject);
	}

	public Block getNextBlock(Block prevBlock) {
        Block block = null;
        if (prevBlock.getNumber() == 999 || prevBlock.getType() == TO_SIGN) {
            return block;
        }
        int startBlockNum = prevBlock.getNumber();
        int nextBlockNum = 0;

        if (startBlockNum < blocks.size() - 1) {
            nextBlockNum = startBlockNum + 1;
        }
        if (startBlockNum == blocks.size() - 1) {
            nextBlockNum = 999;
        }

        for (Block b : blocks) {
            if (b.getNumber() == nextBlockNum) {
                return b;
            }
        }
        return block;
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

			if (!XMLUtil.getTextContent(xmlDoc, "/root/status").equals("")
					&& XMLUtil.getTextContent(xmlDoc, "/root/status") != null) {
				this.setStatus(Integer.parseInt(XMLUtil.getTextContent(xmlDoc,
						"/root/status")));
			}

			NodeList nodes = root.getElementsByTagName("block");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node blockNode = nodes.item(i);
				Block block = new Block();
				block.init(db, XMLUtil.convertNodeToString(blockNode));
				this.addBlock(block);
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
		if (!blocks.isEmpty()) {
			StringBuffer xmlContent = new StringBuffer(10000);
			xmlContent.append("<status>" + getStatus() + "</status>");
			for (int i = 0; i < blocks.size(); i++) {
				xmlContent.append("<block>");
				xmlContent.append(blocks.get(i).getContent());
				xmlContent.append("</block>");
			}
			return xmlContent.toString();
		} else {
			return null;
		}
	}



}
