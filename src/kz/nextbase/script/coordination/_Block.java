package kz.nextbase.script.coordination;

import kz.flabs.runtimeobj.document.coordination.Block;
import kz.flabs.runtimeobj.document.coordination.Coordinator;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;
import kz.nextbase.script._Exception;
import kz.nextbase.script._ExceptionType;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._Session;
import kz.nextbase.script.constants._BlockStatusType;
import kz.nextbase.script.constants._BlockType;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class _Block  implements _IXMLContent {
	private Block bl;
	private _Session ses;

	public _Block(_Session ses, Block o) {
		this.bl = o;
		this.ses = ses;
	}

	public _Block(_Session ses) {
		bl = new Block();
		this.ses = ses;
	}

    public _Block(String data, _Session session) throws _Exception {
        this(session);
        try{
            StringTokenizer t = new StringTokenizer(data,"`");
            while(t.hasMoreTokens()){
                String blockID = t.nextToken();
                if (!blockID.equalsIgnoreCase("new")){
                    this.setBlockID(Integer.parseInt(blockID));
                }
                String coordType = t.nextToken();
                if (coordType.equals("par")){
                    this.setBlockType(_BlockType.PARALLEL_COORDINATION);
                }else if(coordType.equals("ser")){
                    this.setBlockType(_BlockType.SERIAL_COORDINATION);
                }else if(coordType.equals("tosign")){
                    this.setBlockType(_BlockType.TO_SIGN);
                }

                String delayTime = t.nextToken();
                this.setDelayTime(Integer.parseInt(delayTime));

                try{
                    StringTokenizer t1 = new StringTokenizer(t.nextToken(),"^");
                    while(t1.hasMoreTokens()){
                        String coordinator = t1.nextToken();
                        kz.nextbase.script.coordination._Coordinator coord = ses.createCoordinator();

                        if (this.getBlockType() == _BlockType.TO_SIGN){
                            coord.setAsSigner();
                        }else{
                            coord.setAsReviewer();
                        }
                        coord.setUserID(coordinator);
                        this.addCoordinator(coord);
                    }
                    String coordStatus = t.nextToken();
                    if (coordStatus.length() > 0) {
                        if (coordStatus.equalsIgnoreCase("awaiting")){
                            this.setBlockStatus(_BlockStatusType.AWAITING);
                        }
                    }
                }catch (java.util.NoSuchElementException nse){

                }


            }
        }catch(Exception e){
            e.printStackTrace();
            throw new _Exception(_ExceptionType.FORMDATA_INCORRECT,"Parser error :parseCoordinationBlock(" + data + ")" + e);
        }
    }

	public ArrayList<_Coordinator> getCurrentCoordinators(){
		ArrayList<_Coordinator> cc =  new ArrayList<_Coordinator>();
		for(Coordinator c: bl.getCurrentCoordinators()){
			cc.add(new _Coordinator(ses,c));
		}
		return cc;

	}
	
	public void setBlockID(int id){
		bl.blockID = id;
	}

	public void setBlockStatus(_BlockStatusType status){
		switch(status){
		case AWAITING: 
			bl.setStatus(ICoordConst.BLOCK_STATUS_AWAITING);
			break;
		case COORDINATED:
			bl.setStatus(ICoordConst.BLOCK_STATUS_COORDINATED);
			break;
		case COORDINATING: 
			bl.setStatus(ICoordConst.BLOCK_STATUS_COORDINATING);
			break;
		default:
			bl.setStatus(ICoordConst.BLOCK_STATUS_UNDEFINED);
		}		
	}


	_BlockStatusType getBlockStatus(){
		switch(bl.getStatus()){
		case ICoordConst.BLOCK_STATUS_AWAITING: 
			return _BlockStatusType.AWAITING;
		case ICoordConst.BLOCK_STATUS_COORDINATED:
			return _BlockStatusType.COORDINATED;
		case ICoordConst.BLOCK_STATUS_COORDINATING: 
			return _BlockStatusType.COORDINATING;
		default:
			return _BlockStatusType.UNDEFINED;
		}		

	}

	public void setBlockType(_BlockType bType){
		switch(bType){
		case PARALLEL_COORDINATION:
			bl.setType(ICoordConst.PARALLEL_COORDINATION);
			break;
		case SERIAL_COORDINATION:
			bl.setType(ICoordConst.SERIAL_COORDINATION);
			break;
		case TO_SIGN:
			bl.setType(ICoordConst.TO_SIGN);
			break;
		default:
			bl.setType(ICoordConst.UNKNOWN_COORDINATION);
		}		

	}

	public _BlockType getBlockType(){
		int t = bl.getType();
		switch(t){
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

	public _Coordinator getFirstCoordinator() throws _Exception{
		try{
			return new _Coordinator(ses, bl.getFirstCoordinator());
		}catch(Exception e){
			throw new _Exception(_ExceptionType.COMPLEX_OBJECT_ERROR, "Coordinators list is empty, function: _Block.getFirstCoordinator()");
		}

	}

	public _Coordinator getNextCoordinator(_Coordinator coord) {
		Coordinator c = bl.getNextCoordinator(coord.getBaseObject());
		if (c != null){
			return new _Coordinator(ses,c);
		}
		return null; 
	}

	public ArrayList<_Coordinator> getCoordinators(){
		ArrayList<_Coordinator> cc =  new ArrayList<_Coordinator>();
		for(Coordinator c: bl.getCoordinators()){
			cc.add(new _Coordinator(ses,c));
		}
		return cc;


	}
	
	public String getCoordinatorsAsText(){
		String result = "";

		for(Coordinator c: bl.getCoordinators()){
			result += c.getUserID() + ",";
		}
		return result;

	}
    public String getCurrentCoordinatorsAsText(){
		String result = "";

		for(Coordinator c: bl.getCurrentCoordinators()){
			result += c.getUserID() + ",";
		}
		return result;

	}

	public void setDelayTime(int time){
		bl.delayTime = time;
	}

	@Override
	public String toXML() throws _Exception {
		StringBuffer xmlContent = new StringBuffer(10000);

		xmlContent.append("<id>" + bl.blockID + "</id>");
		xmlContent.append("<num>" + bl.num + "</num>");
		xmlContent.append("<delaytime>" + bl.delayTime + "</delaytime>");
		xmlContent.append("<type>" + getBlockType() + "</type>");
		xmlContent.append("<status>" + getBlockStatus() + "</status>");
		xmlContent.append("<coordinators>");
		ArrayList<_Coordinator> c = getCoordinators();
		for (int i = 0; i < c.size(); i++) {
			xmlContent.append("<entry>" + c.get(i).toXML() + "</entry>");
		}
		xmlContent.append("</coordinators>");

		return xmlContent.toString();
	}

	public void addCoordinator(_Coordinator coord) {
		bl.addCoordinator(coord.getBaseObject());

	}

	public Block getBaseObject(){
		return bl;
	}
}


