package kz.nextbase.script.coordination;

import kz.flabs.runtimeobj.document.coordination.Block;
import kz.flabs.runtimeobj.document.coordination.BlockCollection;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._Session;
import kz.nextbase.script.constants._CoordStatusType;

import java.util.ArrayList;

public class _BlockCollection  implements _IXMLContent {
	private BlockCollection bc;
	private _Session ses;
	
	public _BlockCollection(_Session ses){
		this.ses = ses;
		bc = new BlockCollection();
		bc.setStatus(ICoordConst.STATUS_DRAFT);		
	}
	
	public _BlockCollection(_Session ses, BlockCollection o) {
		this.bc = o;
		this.ses = ses;
	}

	public _CoordStatusType getStatus(){
		switch(bc.getStatus()){
		case ICoordConst.STATUS_COORDINTED: 
			return _CoordStatusType.COORDINATED; 
		case ICoordConst.STATUS_COORDINTING:
			return _CoordStatusType.COORDINATING;
		case ICoordConst.STATUS_DRAFT: 
			return _CoordStatusType.DRAFT;
		case ICoordConst.STATUS_EXECUTED: 
			return _CoordStatusType.EXECUTED;
		case ICoordConst.STATUS_EXECUTING: 
			return _CoordStatusType.EXECUTING;
		case ICoordConst.STATUS_EXPIRED: 
			return _CoordStatusType.EXPIRED;
		case ICoordConst.STATUS_NEWVERSION: 
			return _CoordStatusType.NEWVERSION;
		case ICoordConst.STATUS_NOCOORDINATION: 
			return _CoordStatusType.NOCOORDINATION;
		case ICoordConst.STATUS_REJECTED: 
			return _CoordStatusType.REJECTED;
		case ICoordConst.STATUS_SIGNED: 
			return _CoordStatusType.SIGNED;
		case ICoordConst.STATUS_SIGNING: 
			return _CoordStatusType.SIGNING;
		default:
			return _CoordStatusType.UNDEFINED;
		}		
	}
	
	public _Block getCurrentBlock(){
		 Block b = bc.getCurrentBlock();
		 if (b != null){
			 return new _Block(ses, b);
		 }else{
			 return null;
		 }
		
	}
	
	public  boolean hasCoordination() {
        return getCoordBlocks().size() > 0;
	}
	
		
	public  ArrayList<_Block> getCoordBlocks() {
		ArrayList<_Block> bl =  new ArrayList<_Block>();
		for(Block b: bc.getCoordBlocks()){
			bl.add(new _Block(ses,b));
		}
		return bl;
	}

    public void setBlocks(ArrayList<_Block> blocks) {
        bc.clearBlocks();
        for (_Block block : blocks) {
            bc.addBlock(block.getBaseObject());
        }
    }

	public ArrayList<_Block> getBlocks() {
		ArrayList<_Block> bl =  new ArrayList<_Block>();
		for(Block b: bc.getBlocks()){
			bl.add(new _Block(ses,b));
		}
		return bl;
	}
	
	public void updateBlock(_Block block){
		bc.updateBlock(block.getBaseObject());
	}
	
	public _Block getNextBlock(_Block prevBlock){
		Block b = bc.getNextBlock(prevBlock.getBaseObject());
		return new _Block(ses,b);
	}
	
	public void setCoordStatus(_CoordStatusType status){
		switch(status){
		case COORDINATED: 
			bc.setStatus(ICoordConst.STATUS_COORDINTED);
			break;
		case COORDINATING:
			bc.setStatus(ICoordConst.STATUS_COORDINTING);
			break;
		case DRAFT: 
			bc.setStatus(ICoordConst.STATUS_DRAFT);
			break;
		case EXECUTED: 
			bc.setStatus(ICoordConst.STATUS_EXECUTED);
			break;
		case EXECUTING: 
			bc.setStatus(ICoordConst.STATUS_EXECUTING);
			break;
		case EXPIRED: 
			bc.setStatus(ICoordConst.STATUS_EXPIRED);
			break;
		case NEWVERSION: 
			bc.setStatus(ICoordConst.STATUS_NEWVERSION);
			break;
		case NOCOORDINATION: 
			bc.setStatus(ICoordConst.STATUS_NOCOORDINATION);
			break;
		case REJECTED: 
			bc.setStatus(ICoordConst.STATUS_REJECTED);
			break;
		case SIGNED: 
			bc.setStatus(ICoordConst.STATUS_SIGNED);
			break;
		case SIGNING: 
			bc.setStatus(ICoordConst.STATUS_SIGNING);
			break;
		default:
			bc.setStatus(ICoordConst.STATUS_UNDEFINED);
		}		
	}
		
	public void setIsRejected(){
		
	}
	
	public _Block getSignBlock(){
		return new _Block(ses, bc.getSignBlock());
	}

		
	public BlockCollection getBaseObject() {
		return bc;
	}

	@Override
	public String toXML() throws _Exception {
		StringBuffer xmlContent = new StringBuffer(10000);
		xmlContent.append("<status>" + getStatus() + "</status>");	
		if (!getBlocks().isEmpty()) {
			xmlContent.append("<blocks>");
			ArrayList<_Block> bl = getBlocks();
			for (int i = 0; i < bl.size(); i++) {
				_Block b = bl.get(i);
				xmlContent.append("<entry>" + b.toXML() + "</entry>");
			}
			xmlContent.append("</blocks>");
		}

		return xmlContent.toString();
	}
}
