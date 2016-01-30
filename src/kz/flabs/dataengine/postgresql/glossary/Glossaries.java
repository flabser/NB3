package kz.flabs.dataengine.postgresql.glossary;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IGlossaries;
import kz.flabs.dataengine.IGlossariesTuner;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.QueryExceptionType;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.TagPublicationFormatType;


public class Glossaries extends kz.flabs.dataengine.h2.glossary.Glossaries implements IGlossaries, Const {	

	public Glossaries(AppEnv env) {	
		super(env);
	}

	@Override
	public IGlossariesTuner getGlossariesTuner() {
		return new GlossariesTuner(db);
	}
	
	@Override
	public StringBuffer getGlossaryByCondition(IQueryFormula condition, int offset, int pageSize, String fieldsCond, Set<String> toExpand, Set<DocID> toExpandResp, TagPublicationFormatType publishAs) throws DocumentException, QueryException {
		StringBuffer xmlContent = new StringBuffer(10000);		
		Connection conn = dbPool.getConnection();		
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			//String sql = condition.getSQL(new HashSet<String>(Arrays.asList(Const.sysGroup))) + " LIMIT " + pageSize + " OFFSET " + offset;
			String sql = condition.getSQL() + " LIMIT " + (pageSize != -1 ? pageSize : "ALL") + " OFFSET " + offset;
			ResultSet rs = s.executeQuery(sql);
			if (condition.isGroupBy()){
				SourceSupplier ss = new SourceSupplier(env);
				while (rs.next()) {	
					String categoryID = rs.getString(1);
					if (categoryID != null){
						int groupCount = rs.getInt(2);
						String categoryVal[] = {categoryID};
						String viewText = ss.publishAs(publishAs, categoryVal).get(0)[0]; 					


						xmlContent.append("<entry  doctype=\"" + CATEGORY + "\" count=\"" + groupCount + "\" " +
								" categoryid=\"" + categoryID + "\" " +
								" docid=\"" + categoryID + "\" " +
								XMLUtil.getAsAttribute("viewtext", viewText) +
								"url=\"Provider?type=view&amp;id=" + condition.getQueryID() + "&amp;command=expand`" + categoryID + "\" >" +
								"<viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext>");

						if (toExpand != null && toExpand.size() > 0) {
							for (String category : toExpand) {
								if (categoryID.equalsIgnoreCase(category)) {
									StringBuffer categoryValue = getOneCategory(condition.getGroupCondition(category), fieldsCond);
									xmlContent.append("<responses>" + categoryValue + "</responses>");
								}
							}
						}
					}else{
						xmlContent.append("<entry  doctype=\"" + DOCTYPE_UNKNOWN + "\" count=\"0\" " +
								" categoryid=\"null\"" + XMLUtil.getAsAttribute("viewtext", "category is null") + "><viewtext>category is null</viewtext>");
					}
					xmlContent.append("</entry>");
				}

			}else{
				while (rs.next()) {					
						xmlContent.append(getGlossaryEntry(conn, Const.sysGroupAsSet, Const.sysUser, rs, fieldsCond, toExpandResp));	
				}
			}
			s.close();
			rs.close();
			conn.commit();		
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
			throw new QueryException(QueryExceptionType.RUNTIME_ERROR);
		} finally {		
			dbPool.returnConnection(conn);
		}		
		return xmlContent;
	}

}
