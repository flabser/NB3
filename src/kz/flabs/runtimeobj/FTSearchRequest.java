package kz.flabs.runtimeobj;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import kz.flabs.dataengine.Const;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.FTIndexEngineException;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.outline.Outline;
import kz.flabs.users.User;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.outline.OutlineRule;

public class FTSearchRequest implements Const {
	private static final SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss");
	private IDatabase db;
	private String xmlFragment = "";
	private int colCount;

	@Deprecated
	public FTSearchRequest(AppEnv env, Set<String> complexUserID, String absoluteUserID, String keyWord, int pageNum, int pageSize) throws DocumentException, FTIndexEngineException, RuleException, QueryFormulaParserException, ComplexObjectException{
		StringBuffer xmlContent = new StringBuffer(5000);
		
		if (true){
			OutlineRule outlineRule = (OutlineRule) env.ruleProvider.getRule(OUTLINE_RULE, "navigator");
			Outline outline = new Outline(env, outlineRule, new User(absoluteUserID));
			xmlContent.append(outline.getAsXML("RUS"));
		}
		
		db = env.getDataBase();
		IFTIndexEngine ftEngine = db.getFTSearchEngine();
		colCount = ftEngine.ftSearchCount(complexUserID, absoluteUserID, keyWord);
		StringBuffer docs = ftEngine.ftSearch(complexUserID, absoluteUserID, keyWord, db.calcStartEntry(pageNum, pageSize), pageSize);

		xmlFragment = xmlContent + "<query count=\"" + colCount + "\" currentpage=\"" + pageNum + "\"" +
				        " maxpage=\"" + RuntimeObjUtil.countMaxPage(colCount, pageSize) + "\"" +
				        " time=\""+ dateformat.format(new Date()) + "\" keyword=\""+ XMLUtil.getAsTagValue(keyWord) + "\"" +
						" userid=\""+ absoluteUserID + "\">" + docs + "</query>";
	}
	
	public String getDataAsXML(){
		return xmlFragment;
	}
}
