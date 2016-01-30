package kz.flabs.appdaemon;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.coordination.ICoordConst;
import kz.flabs.runtimeobj.document.project.Block;
import kz.flabs.runtimeobj.document.project.Coordinator;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.nextbase.script._Session;
import kz.nextbase.script.mail._InstMessengerAgent;
import kz.nextbase.script.mail._MailAgent;
import kz.pchelka.scheduler.AbstractDaemon;
import kz.pchelka.scheduler.IProcessInitiator;
import kz.pchelka.server.Server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeWaiting extends AbstractDaemon implements ICoordConst, Const {


	@Override
	public int process(IProcessInitiator processOwner) {
		AppEnv env = (AppEnv) processOwner;
		try {
			IDatabase db = env.getDataBase();
			String sql = " COORDSTATUS = " + STATUS_COORDINTING;
			List<BaseDocument> projects = db.getProjects().getDocumentsByCondition(sql, sysGroupAsSet, sysUser);
			for (BaseDocument doc : projects) {
				Project prj = (Project) doc;
				Block currentBlock = prj.getCurrentBlock();
				int delayTime = currentBlock.getDelayTime();
				if ((currentBlock.getType() == PARALLEL_COORDINATION || currentBlock.getType() == SERIAL_COORDINATION) && delayTime > 0) {					
					Date currentTime = new Date();
					Date startCoordTime = currentBlock.getCoorDate();
					if (startCoordTime == null) {
						startCoordTime = new Date();
					}
					long dateDiffHours = (currentTime.getTime() - startCoordTime.getTime())/(1000*60*60);
					if (dateDiffHours > delayTime) {
						currentBlock.setStatus(BLOCK_STATUS_EXPIRED);
						prj.setStatus(STATUS_REJECTED);
						Document forAttach = new Document(db, sysUser);
						prj.copyAttachments(forAttach);
						prj.save(sysGroupAsSet, sysUser);
						/*create new version of project*/
						prj.setNewDoc(true);
						prj.setIsRejected(0);
						prj.setStatus(STATUS_NEWVERSION);
						int docversion = prj.getDocVersion() + 1;
						prj.setDocVersion(docversion);
						String vn = prj.getVn();
						prj.setVn(vn.contains(",") ? vn.replaceFirst(",.", ",") + docversion : vn + "," + docversion);
						prj.setProjectDate(new Date());
						prj.setDdbID(Util.generateRandomAsText());
						prj.clearEditors();
						prj.clearReaders();
						prj.addEditor(prj.getAuthorID());
						prj.addReader(prj.getAuthorID());
						for (Block block : prj.getBlocksList()) {
							block.setStatus(ICoordConst.BLOCK_STATUS_AWAITING);
							block.setCoorDate(null);
							for (Coordinator coordinator : block.getCoordinators()) {
								coordinator.resetCoordinator();
							}
						}
						forAttach.copyAttachments(prj);
						prj.save(sysGroupAsSet, sysUser);
						
						String msg = "brief content: \"" + prj.getBriefContent();
						msg += "\n " + prj.getFullURL();
						_InstMessengerAgent msgAgent = new _InstMessengerAgent();
						Employer author = db.getStructure().getAppUser(prj.getAuthorID());
						ArrayList<String> recipientsInstMsg = new ArrayList<String>();
						ArrayList<String> recipientsMails = new ArrayList<String>();
						recipientsInstMsg.add(author.getUser().getInstMsgAddress());
						recipientsMails.add(author.getUser().getEmail());
						msgAgent.sendMessage(recipientsInstMsg, msg);
						
						String msubject = "notify: \"" + prj.getBriefContent() + " "; 
						StringBuffer body = new StringBuffer(1000);
						body.append("<b><font color=\"#000080\" size=\"4\" face=\"Default Serif\"></font></b><hr>");
						body.append("<table cellspacing=\"0\" cellpadding=\"4\" border=\"0\" style=\"padding:10px; font-size:12px; font-family:Arial;\">");
						body.append("<tr>");
						body.append("<td style=\"border-bottom:1px solid #CCC;\" valign=\"top\" colspan=\"2\">");
						body.append(": \"" +  prj.getBriefContent() + "\" . <br/>");
						body.append("</td></tr><tr>");
						body.append("<td colspan=\"2\"></td>");
						body.append("</tr></table>");
						body.append("<p><font size=\"2\" face=\"Arial\"> <a href=\"" + prj.getFullURL() + "\"></a></p></font>");
						_Session ses = new _Session(env, new User(env), processOwner);
						_MailAgent mailAgent = new _MailAgent(ses);
						mailAgent.sendMail(recipientsMails, msubject, body.toString());
						
					}
				}
			}
			//System.out.println("Time waiting documents count: " + projects.size());
		} catch(Exception e) {
			Server.logger.errorLogEntry(env.appType, e);
			return -1;
		}
		return 0;
	}
	
}
