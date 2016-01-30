package kz.flabs.runtimeobj.document.project;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.coordination.Decision;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;
import kz.flabs.users.User;
import kz.flabs.users.UserSession;
import kz.flabs.util.ResponseType;
import kz.flabs.util.XMLResponse;

import java.util.ArrayList;
import java.util.Collection;

public class CoordHandler implements Const, ICoordConst{
	private User user;
	private AppEnv env;
	private IDatabase db;
	private Project prj;
	
	public CoordHandler(AppEnv env, String key, UserSession userSes) throws DocumentException, DocumentAccessException{	
		db = env.getDataBase();
		this.env = env;
		this.user = userSes.currentUser;
		int docID = Integer.parseInt(key);
		prj = db.getProjects().getProjectByID(docID, user.getAllUserGroups(), user.getUserID());
	}
	
	public XMLResponse startCoord() throws DocumentAccessException, DocumentException{
		XMLResponse result = new XMLResponse(ResponseType.START_COORD);
		
		if (prj.isValid){
			prj.setCoordStatus(ICoordConst.STATUS_COORDINTING);
			Block block = prj.getFirstBlock();
			block.setStatus(ICoordConst.BLOCK_STATUS_COORDINATING);
			if (block.type == ICoordConst.PARALLEL_COORDINATION){
				Collection<Coordinator> coordinators = block.getCoordinators();
				for(Coordinator coord: coordinators){
					coord.setCurrent(true);
					prj.addReader(coord.userID);
				}
			}else if(block.type == ICoordConst.SERIAL_COORDINATION){
				Coordinator coord = block.getFirstCoordinator();
				coord.setCurrent(true);
				prj.addReader(coord.userID);
			}
			
			int docID = env.getDataBase().getProjects().updateProject(prj, user);
			if (docID > -1){							
				result.setResponseStatus(true);
			}else{
				result.setResponseStatus(false);
				result.addMessage(" DataEngine, ");
				AppEnv.logger.errorLogEntry(" DataEngine, ");
			}
		}
		
		return result;
		
	}

	public XMLResponse nextCoord(String blockNum, String decision, String comment){
		XMLResponse result = new XMLResponse(ResponseType.NEXT_COORD);
	
		if (prj.isValid){
			Block currentBlock = prj.getCurrentBlock();
			
			
			currentBlock.setStatus(BLOCK_STATUS_COORDINATED);
		//	Block nextBlock = prj.getBlocksMap().get(currentBlock.blockNum ++);
		//	nextBlock.setStatus(ICoordConst.BLOCK_STATUS_COORDINATING);
			
		}
		
		return result;
		
	}
	
	public XMLResponse toSign(String decision, String comment){
		XMLResponse result = new XMLResponse(ResponseType.TO_SIGN);
		int dec = 0; 
			
		if (prj.isValid){
			if (decision.equalsIgnoreCase("yes")){
				dec = DECISION_YES;
			}else{
				dec = DECISION_NO;
			}
			
			Block block = prj.getCurrentBlock();
			ArrayList<Coordinator> coords = block.getCurrentCoordinators();
			for(Coordinator coord:coords){
				coord.setDecision(new Decision(dec, comment));
			}
		}
		
		return result;
		
	}

	
	public XMLResponse stopCoord(){
		XMLResponse result = new XMLResponse(ResponseType.STOP_COORD);
		if (prj.isValid){
			
		}
		
		return result;
		
	}
	
}
