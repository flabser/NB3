package kz.flabs.runtimeobj.document.task;


import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.runtimeobj.document.AbstractComplexObject;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;


@XmlRootElement(name = "class")
@XmlAccessorType(XmlAccessType.FIELD)
public class GrantedBlockCollection extends AbstractComplexObject implements Serializable {

    @XmlTransient
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "grantblock")
    public ArrayList<GrantedBlock> blocks = new ArrayList<>();

    @XmlAttribute
    public String className = GrantedBlockCollection.class.getName();

    @Override
    public void init(IDatabase db, String initString) throws ComplexObjectException {
    }

    @Override
    public String getContent() {
        return null;
    }

    public void addBlock(GrantedBlock block) {
        blocks.add(block);
    }

    public ArrayList<GrantedBlock> getBlocks() {
        return blocks;
    }

}
