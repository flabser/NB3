package kz.pchelka.daemon.system;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.dataengine.h2.queryformula.QueryFormula;
import kz.flabs.exception.*;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.util.Util;
import kz.flabs.webrule.constants.QueryType;
import kz.pchelka.env.Environment;
import kz.pchelka.scheduler.AbstractDaemon;
import kz.pchelka.scheduler.IProcessInitiator;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class BackupService extends AbstractDaemon {

	@Override
	public int process(IProcessInitiator processOwner) throws DocumentAccessException, RuleException, QueryFormulaParserException, QueryException {
		Collection<AppEnv> apps = Environment.getApplications();

		FormulaBlocks queryFormulaBlocks = new FormulaBlocks("", QueryType.DOCUMENT);
		IQueryFormula qf = new QueryFormula("", queryFormulaBlocks);

		for (AppEnv app : apps) {
			try{              

				IDatabase db = app.getDataBase();
				if(!db.getDbID().equals("NoDatabase")){
					ArrayList<BaseDocument> docCollection = db.getDocumentsByCondition(qf, Const.supervisorGroupAsSet, Const.sysUser, 0, 0 );
					if (docCollection.size() > 0) {
						File backupPath = new File(Environment.backupDir);

						File path = new File(backupPath.getAbsolutePath() + File.separator + "Backup_" + app.appType + "_" + Util.simpleDateFormat.format(new Date()));
						if (!path.exists())
							path.mkdirs();

						for(BaseDocument doc : docCollection){
							File docPath = new File(path.getAbsolutePath() + File.separator + "doc_" + String.valueOf(doc.getDocID()));
							docPath.mkdirs();

							try {
								PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(docPath.getAbsolutePath() + File.separator + "Doc.xml"))));
								out.print(doc.toXML(false));
								out.flush();
								out.close();
							} catch (IOException | ComplexObjectException e) {
								e.printStackTrace();
							}

							doc.getAttachments("rtfcontent", docPath.getAbsolutePath());
						}

						PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(path.getAbsolutePath() + File.separator + "descriptor.xml"))));
						out.print("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");

						for(File f: path.listFiles()){
							out.print("<dir name=\"" + f.getName() + "\"> \n" +
									getFileProp(f) +
									"</dir>\n");
						}

						out.flush();
						out.close();
					}
				}
			}catch(ComplexObjectException | DocumentException | IOException e){
				e.printStackTrace();
			}
		}

		return 0;
	}

	private static String getFileProp(File file){
		String result = "";
		if(file.isDirectory()){
			for(File f : file.listFiles()){
				result += getFileProp(f);
			}
		}else{
			return "    <file name=\"" + file.getName() + "\" checksum=\"" + getMD5Checksum(file.getAbsolutePath()) +"\"/>\n";
		}
		return result;
	}

	public static String getMD5Checksum(String filename) {
		String result = "";
		try {
			InputStream fis = new FileInputStream(filename);
			byte[] buffer = new byte[1024];
			MessageDigest complete = MessageDigest.getInstance("MD5");
			int numRead;

			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);

			fis.close();
			byte[] b =  complete.digest();
			for (byte aB : b) {
				result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}

		return result;
	}

}
