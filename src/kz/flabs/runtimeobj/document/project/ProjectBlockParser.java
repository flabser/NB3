package kz.flabs.runtimeobj.document.project;

import java.util.*;

import kz.flabs.runtimeobj.document.coordination.ICoordConst;

public class ProjectBlockParser implements ICoordConst {

	public Block parse(Project prj, String complexString){
		Block block = new Block(prj);
		return parse(block, complexString);	
	}

	private Block parse(Block block, String complexString){
		block.isFromWebForm = true;
		StringTokenizer t = new StringTokenizer(complexString,"`");
		while(t.hasMoreTokens()){
			String blockNum = t.nextToken();
			if (blockNum.equalsIgnoreCase("new")){
				block.isNew = true;
				block.setBlockNum(0);
			}else{
				block.setBlockNum(Integer.parseInt(blockNum));
			}
			String coordType = t.nextToken();
			if (coordType.equals("par") || coordType.equals("328")){
				block.type = PARALLEL_COORDINATION;	
			}else if(coordType.equals("ser") || coordType.equals("329")){
				block.type = SERIAL_COORDINATION;	
			}else if(coordType.equals("tosign")){
				block.type = TO_SIGN;	
			}else{
				block.type = UNKNOWN_COORDINATION;	
			}
			String delayTime = t.nextToken();
			block.delayTime = Integer.parseInt(delayTime);

			try{
				StringTokenizer t1 = new StringTokenizer(t.nextToken(),"^");
				while(t1.hasMoreTokens()){
					String coordinator = t1.nextToken();
					Coordinator coord = new Coordinator(block);
					coord.isFromWebForm = true;
					if (block.type == TO_SIGN){
						coord.setCoordType(COORDINATOR_TYPE_SIGNER);
					}else{
						coord.setCoordType(COORDINATOR_TYPE_REVIEWER);
					}
					coord.userID = coordinator;
					block.addCoordinator(coord);
				}
				String coordStatus = t.nextToken();
				if (coordStatus.length() > 0) {
					if (coordStatus.equalsIgnoreCase("awaiting")){					
						block.setStatus(BLOCK_STATUS_AWAITING);
					}
				}
			}catch (java.util.NoSuchElementException nse){
				
			}


		}

		return block;
	}

}
