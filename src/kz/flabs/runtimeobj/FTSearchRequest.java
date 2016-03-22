package kz.flabs.runtimeobj;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.FTIndexEngineException;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.RuleException;
import kz.flabs.util.XMLUtil;
import kz.lof.appenv.AppEnv;

public class FTSearchRequest implements Const {
	private static final SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss");
	private IDatabase db;
	private String xmlFragment = "";
	private int colCount;

	@Deprecated
	public FTSearchRequest(AppEnv env, Set<String> complexUserID, String absoluteUserID, String keyWord, int pageNum, int pageSize)
	        throws DocumentException, FTIndexEngineException, RuleException, ComplexObjectException {
		StringBuffer xmlContent = new StringBuffer(5000);

		if (true) {

		}

		db = env.getDataBase();

		xmlFragment = xmlContent + "<query count=\"" + colCount + "\" currentpage=\"" + pageNum + "\"" + " maxpage=\""
		        + RuntimeObjUtil.countMaxPage(colCount, pageSize) + "\"" + " time=\"" + dateformat.format(new Date()) + "\" keyword=\""
		        + XMLUtil.getAsTagValue(keyWord) + "\"" + " userid=\"" + absoluteUserID + "\"></query>";
	}

	public String getDataAsXML() {
		return xmlFragment;
	}
}
