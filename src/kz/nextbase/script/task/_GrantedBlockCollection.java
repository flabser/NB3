package kz.nextbase.script.task;


import kz.flabs.runtimeobj.document.task.GrantedBlock;
import kz.flabs.runtimeobj.document.task.GrantedBlockCollection;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._Session;

import java.util.ArrayList;

public class _GrantedBlockCollection  implements _IXMLContent {
    private GrantedBlockCollection collection;
    private _Session session;

    public _GrantedBlockCollection(_Session session) {
        this.session = session;
        collection = new GrantedBlockCollection();
    }

    public _GrantedBlockCollection(_Session session, GrantedBlockCollection collection) {
        this.session = session;
        this.collection = collection;
    }

    public ArrayList<_GrantedBlock> getBlocks() {
        ArrayList<_GrantedBlock> col = new ArrayList<>();
        for(GrantedBlock b: collection.getBlocks()){
            col.add(new _GrantedBlock(session,b));
        }
        return col;
    }

    @Override
    public String toXML() throws _Exception {
        StringBuffer xmlContent = new StringBuffer(10000);
        ArrayList<_GrantedBlock> arr_blocks;
        arr_blocks = getBlocks();
        if (!arr_blocks.isEmpty()) {
            xmlContent.append("<blocks>");
            ArrayList<_GrantedBlock> bl = getBlocks();
            for (int i = 0; i < bl.size(); i++) {
                _GrantedBlock b = bl.get(i);
                xmlContent.append("<entry>" + b.toXML() + "</entry>");
            }
            xmlContent.append("</blocks>");
        }

        return xmlContent.toString();
    }

    public void addBlock(_GrantedBlock block) {
        collection.addBlock(block.getBaseObject());
    }

    public GrantedBlockCollection getBaseObject() {
        return collection;
    }
}
