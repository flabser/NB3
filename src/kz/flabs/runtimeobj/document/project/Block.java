package kz.flabs.runtimeobj.document.project;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;


public class Block implements Serializable, ICoordConst{
	public int blockID;
	public boolean isNew;
	public boolean isFromWebForm = false;
	public int type;
	public int delayTime;
	public int status;
	public String coordinator;
	public transient IDatabase db;
	
	protected HashMap<Number, Coordinator> coordinators = new HashMap<Number, Coordinator>();

	private int blockNum;
	private Project project;
	private static final long serialVersionUID = 1L;
	private Date coorDate;

	public Block() {

	}

	public Block(Project project) {
		this.project = project;
		db = project.db;
	}

	public void setCoorDate(Date coorDate) {
		this.coorDate = coorDate;
	}

	public Date getCoorDate() {
		return this.coorDate;
	}

	public int getBlockID() {
		return blockID;
	}

	public void setBlockID(int blockID) {
		this.blockID = blockID;
	}

	public Coordinator addCoordinator(){
		Coordinator coord =  new Coordinator(this);
		coord.num = coordinators.size() + 1;
		coordinators.put(coord.num, coord);
		return coord;
	}

	public void addCoordinator(Coordinator coord){	
		coord.num = coordinators.size() + 1;
		coordinators.put(coord.num, coord);		
	}

	public void setStatus(int blockStat){
		status = blockStat;				
	}

	public void setBlockNum(int bn){
		blockNum = bn;				
	}

	public int getStatus(){
		return status;
	}

	public Project getParentProject() {
		return project;
	}

	public Coordinator getFirstCoordinator() {
		return coordinators.get(1);
	}

	public Coordinator getNextCoordinator(Coordinator coord) {
		int n = coord.num + 1;
		return coordinators.get(n);
	}

	public Collection<Coordinator> getCoordinators() {
		return coordinators.values();
	}

	public ArrayList<Coordinator> getCurrentCoordinators(){
		ArrayList<Coordinator> coords = new ArrayList<Coordinator>();
		for(Coordinator coord: coordinators.values()){
			if (coord.isCurrent() == 1){
				coords.add(coord);
			}
		}		
		return coords;
	}

	public void skipToNextCoordinator(){

	}

	public int getType(){
		return type;
	}

	public void setDelayTime(int time){
		delayTime = time;
	}

	public int getDelayTime(){
		return delayTime;
	}

	public int getBlockNumber(){
		return blockNum;
	}

	// currentRecipient
	public void setCurrentRecipient(String currentRecipient){
		this.project.addStringField("currentRecipient", currentRecipient);
	}
	public String getCurrentRecipient() throws DocumentException {
		return "getValueAsString(\"currentRecipient\")";
	}

	public String toString(){
		return "type=" + type + ", status=" + status + ", blocknum=" + blockNum; 
	}
	
}