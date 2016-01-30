package kz.nextbase.script.project;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.IStructure;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;
import kz.flabs.runtimeobj.document.project.Block;
import kz.flabs.runtimeobj.document.project.Coordinator;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.util.Util;
import kz.nextbase.script._Exception;
import kz.nextbase.script._ExceptionType;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script.constants._BlockStatusType;
import kz.nextbase.script.constants._BlockType;

import java.util.ArrayList;
import java.util.Date;

@Deprecated
public class _Block implements ICoordConst,_IXMLContent {
	private Block block; 
	int blockType = 0;


	public _Block(){
		block = new Block();		
	}

	public _Block(Block b) {
		block = b;
	}

	public void setAsSerial(){
		blockType = SERIAL_COORDINATION;
		block.type = blockType;
	}

	public void setAsParallel(){
		blockType = PARALLEL_COORDINATION;
		block.type = blockType;
	}

	public void setAsSignBlock(){
		blockType = TO_SIGN;
		block.type = blockType;
	}

	@Deprecated
	public String getType(){
		switch(block.getType()){
		case ICoordConst.PARALLEL_COORDINATION:
			return "par";
		case ICoordConst.SERIAL_COORDINATION:
			return "pos";
		case ICoordConst.TO_SIGN:
			return "sign";
		default:
			return "undefined";
		}
	}
	
	public _BlockType getBlockType(){
		switch(block.getType()){
		case ICoordConst.PARALLEL_COORDINATION:
			return _BlockType.PARALLEL_COORDINATION;
		case ICoordConst.SERIAL_COORDINATION:
			return _BlockType.SERIAL_COORDINATION;
		case ICoordConst.TO_SIGN:
			return _BlockType.TO_SIGN;
		default:
			return _BlockType.UNDEFINED;
		}
	}


	public void addCoordinator(_Coordinator coord){
		block.addCoordinator(coord.coordinator);
	}

	public ArrayList<_Coordinator> getCoordinators(){
		ArrayList<_Coordinator> coords = new ArrayList<_Coordinator>();
		for(Coordinator coord: block.getCoordinators()){
			coords.add(new _Coordinator(coord));
		}		
		return coords;
	}

	public ArrayList<_Coordinator> getCurrentCoordinators(){
		ArrayList<_Coordinator> coords = new ArrayList<_Coordinator>();
		for(Coordinator coord: block.getCoordinators()){
			if (coord.isCurrent() == 1){
				coords.add(new _Coordinator(coord));
			}
		}		
		return coords;
	}

	public _Coordinator getNextCoordinator(_Coordinator coord) {
		Coordinator c = block.getNextCoordinator(coord.getBaseObject());
		if (c != null){
			return new _Coordinator(c);
		}
		return null; 
	}

	public _Coordinator getFirstCoordinator(_Coordinator coord) {
		Coordinator c = block.getFirstCoordinator();
		if (c != null){
			return new _Coordinator(c);
		}
		return null;
	}

	@Deprecated
	public void setStatus(String status){
		if (status.equalsIgnoreCase("awaiting")){
			block.status = BLOCK_STATUS_AWAITING;
		}else if(status.equalsIgnoreCase("coordinating") || status.equalsIgnoreCase("signing")){
			block.status = BLOCK_STATUS_COORDINATING;
			block.setCoorDate(new Date());
		}else if(status.equalsIgnoreCase("coordinated")){
			block.status = BLOCK_STATUS_COORDINATED;
		}else{
			block.status = BLOCK_STATUS_UNDEFINED;
		}
	}	

	public void setBlockStatus(_BlockStatusType status){
		if (status == _BlockStatusType.AWAITING){
			block.status = BLOCK_STATUS_AWAITING;
		}else if(status == _BlockStatusType.COORDINATING || status == _BlockStatusType.SIGNING){
			block.status = BLOCK_STATUS_COORDINATING;
			block.setCoorDate(new Date());
		}else if(status == _BlockStatusType.COORDINATED){
			block.status = BLOCK_STATUS_COORDINATED;
		}else{
			block.status = BLOCK_STATUS_UNDEFINED;
		}
	}	

	@Deprecated
	public String getStatus(){
		switch (block.status){
		case ICoordConst.BLOCK_STATUS_AWAITING:
			return "awaiting";
		case ICoordConst.BLOCK_STATUS_COORDINATING:
			return "coordinating";
		case ICoordConst.BLOCK_STATUS_COORDINATED:
			return "coordinated";
		default: 
			return "undefined";
		}
	}

	public _BlockStatusType getBlockStatus(){
		switch (block.status){
		case ICoordConst.BLOCK_STATUS_AWAITING:
			return _BlockStatusType.AWAITING;
		case ICoordConst.BLOCK_STATUS_COORDINATING:
			return _BlockStatusType.COORDINATING;
		case ICoordConst.BLOCK_STATUS_COORDINATED:
			return _BlockStatusType.COORDINATED;
		default: 
			return _BlockStatusType.UNDEFINED;
		}
	}

	public void setDelayTime(int time){
		block.setDelayTime(time);
	}


	public Integer getBlockNumber(){
		return block.getBlockNumber();
	}

	public void setBlockNumber(int bn) {
		block.setBlockNum(bn);
	}

	Block getBaseObject(){
		return block;
	}

	public String toXML() throws _Exception{
		try{
			StringBuffer xmlContent = new StringBuffer(10000);
			_BlockType type = getBlockType();
			xmlContent.append("<entry num=\"" + getBlockNumber() + "\" type=\"" + type + "\" >");
			if (type != _BlockType.TO_SIGN){
				xmlContent.append("<status>" + getBlockStatus() + "</status>");
				xmlContent.append("<delaytime>" + block.delayTime + "</delaytime>");
				xmlContent.append("<coordinators>");
				for(_Coordinator coord: getCoordinators()){
					if (coord.coordinator.type == COORDINATOR_TYPE_REVIEWER){
						xmlContent.append(fillCoordPiece(coord));
					}
				}
				xmlContent.append("</coordinators>"); 
			}else{
				xmlContent.append("<status>" + getBlockStatus()  + "</status>");
				xmlContent.append("<delaytime>" + block.delayTime + "</delaytime>");
				xmlContent.append("<signers>");
				for(_Coordinator coord: getCoordinators()){
					if (coord.coordinator.type == COORDINATOR_TYPE_SIGNER){
						xmlContent.append(fillCoordPiece(coord));
					}
				}
				xmlContent.append("</signers>");
			}
			return xmlContent.append("</entry>").toString();	
		} catch (DocumentException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Document.toXML()()");
		}
	}


	public String toString(){
		return block.toString();
	}
	
	 private StringBuffer fillCoordPiece(_Coordinator coord) throws DocumentException{
	        StringBuffer xmlContent = new StringBuffer(500);
	        String userName = "", userID = "";
	        IStructure struct = block.db.getStructure();
	        Employer emp = struct.getAppUser(coord.coordinator.userID);
	        if(emp != null){
	            userName = emp.getFullName();
	            userID = emp.getUserID();
	        }else{
	            AppEnv.logger.warningLogEntry("Employer for \"" + coord.coordinator.userID + "\" has not found");
	        }
	        xmlContent.append("<entry  num=\"" + coord.coordinator.num + "\">");
	        xmlContent.append("<user attrval=\"" + userID + "\" >" + userName + "</user>");
	        xmlContent.append("<iscurrent>" + coord.isCurrent() + "</iscurrent>");
	        xmlContent.append("<decision>" + coord.getDecision() + "</decision>");
	        xmlContent.append("<decisiondate>" + (coord.getDecisionDate() != null ? Util.dateTimeFormat.format(coord.getDecisionDate()) : "") + "</decisiondate>");
	        xmlContent.append("<comment>" + coord.getComment() + "</comment>");
	        xmlContent.append("</entry>");
	        return xmlContent;
	    }

}
