package kz.flabs.dataengine.mssql;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.ExceptionType;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.project.Block;
import kz.flabs.runtimeobj.document.project.Coordinator;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.project.Recipient;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.users.User;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.query.QueryFieldRule;
import kz.pchelka.env.Environment;

import java.io.*;
import java.sql.*;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;

public class ProjectOnDatabase extends kz.flabs.dataengine.h2.ProjectOnDatabase implements IProjects, Const {

    public ProjectOnDatabase(IDatabase db) {
        super(db);
    }

    public StringBuffer getStatisticsByAllObjects() {
        StringBuffer xmlContent = new StringBuffer();
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "select VIEWTEXT, DOCID from GLOSSARY where DOCID in (select PROJECT from projects) order by viewtext;";
            ResultSet objrs = s.executeQuery(sql);
            xmlContent.append("<entries>");
            while (objrs.next()) {
                xmlContent.append("<entry docid=\"" + objrs.getInt("DOCID") + "\">");
                String entrysql = "SELECT COUNT(*) as total, SUM(CASE WHEN coordstatus = 362 THEN 1 ELSE 0 END) as executed from projects where project = ?";
                PreparedStatement eps = conn.prepareStatement(entrysql);
                eps.setInt(1, objrs.getInt("DOCID"));
                ResultSet entryrs = eps.executeQuery();
                if (entryrs.next()) {
                    int total = entryrs.getInt("total");
                    int executed = entryrs.getInt("executed");
                    xmlContent.append("<total>");
                    xmlContent.append(total);
                    xmlContent.append("</total>");
                    xmlContent.append("<executed>");
                    xmlContent.append(executed);
                    xmlContent.append("</executed>");
                    xmlContent.append("<unexecuted>");
                    xmlContent.append(total - executed);
                    xmlContent.append("</unexecuted>");
                }
                entrysql = "    select ' ' + (select glossary.viewtext from GLOSSARY where glossary.DOCID = projects.CITY) + ' ' + PROJECTS.STREET + ' ' + projects.HOUSE + ' ' + projects.FLOOR + ' ' as address, PROJECTs.COORDINATS from GLOSSARY\n" +
                        "inner join PROJECTS\n " +
                        "on GLOSSARY.DOCID = PROJECTs.PROJECT\n " +
                        "where PROJECT = ?";
                eps = conn.prepareStatement(entrysql);
                eps.setInt(1, objrs.getInt("DOCID"));
                entryrs = eps.executeQuery();
                if (entryrs.next()) {
                    xmlContent.append("<address>");
                    xmlContent.append(entryrs.getString("address"));
                    xmlContent.append("</address>");
                    xmlContent.append("<coordinats>");
                    xmlContent.append(entryrs.getString("coordinats"));
                    xmlContent.append("</coordinats>");
                }
                xmlContent.append("<viewtext>" + objrs.getString("VIEWTEXT") + "</viewtext>");
                xmlContent.append("</entry>");
            }
            xmlContent.append("</entries>");
            File file_with_id = new File("ids.txt");
            Writer writer = new FileWriter(file_with_id.getName());
            writer.write(xmlContent.toString());
            writer.flush();
            writer.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dbPool.returnConnection(conn);
        }

        return xmlContent;
    }

    @Override
    public StringBuffer getStatisticByContragent(int objectID) {
        StringBuffer xmlContent = new StringBuffer(1000);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            String sql = "select projects.docid as remarkid, projects.project as projectid, amountdamage, contragent.VIEWTEXT as contragentname, CONTENTSOURCE, coordstatus, projectsprav.VIEWTEXT as projectname from PROJECTS " +
                    "inner join (select viewtext, docid from GLOSSARY where FORM = 'contractor') as contragent\n" +
                    "on projects.CONTRAGENT = contragent.DOCID\n" +
                    "inner join (select viewtext, docid from GLOSSARY where FORM = 'projectsprav') as projectsprav\n" +
                    "on PROJECTS.PROJECT = projectsprav.DOCID\n" +
                    "where CONTRAGENT = " + objectID + " order by PROJECT";
            ResultSet rs = s.executeQuery(sql);
            xmlContent.append("<entries>");
          /*  if (rs.isBeforeFirst()) {
                docid="" + objectID + "\" viewtext=\"" + rs.getString("contragentname") + ""
            }*/

            StringBuffer temp = new StringBuffer(10000);
            int amountdamage = 0;
            while (rs.next()) {

                temp.append("<entry docid=\"" + rs.getInt("remarkid") + "\">");
                temp.append("");

                temp.append("<viewtext>");
                temp.append(rs.getString("CONTENTSOURCE"));
                temp.append("</viewtext>");

                temp.append("<status>");
                temp.append(rs.getString("coordstatus"));
                temp.append("</status>");

                temp.append("<project docid=\"" + rs.getInt("projectid") + "\">");
                temp.append(rs.getString("projectname"));
                temp.append("</project>");

                try {
                    amountdamage += Integer.parseInt(rs.getString("amountdamage"));
                    if (amountdamage != 0) {
                        temp.append("<damage>true</damage>");
                    } else {
                        temp.append("<damage>false</damage>");
                    }
                } catch(NumberFormatException e) {
                    temp.append("<damage>false</damage>");
                }
              /*  if (rs.getString("amountdamage") != null && !"".equalsIgnoreCase(rs.getString("amountdamage"))) {
                    try {
                        amountdamage += Integer.parseInt(rs.getString("amountdamage"));
                    } catch(NumberFormatException e) {
                    }
                    temp.append("<damage>true</damage>");
                } else {
                    temp.append("<damage>false</damage>");
                }*/


                temp.append("</entry>");

                if (rs.isLast()) {
                    xmlContent.append("<entry docid=\"" + objectID + "\" viewtext=\"" + rs.getString("contragentname") + "\">");

                    xmlContent.append("<remarks>");
                    xmlContent.append(temp);
                    xmlContent.append("</remarks>");

                    xmlContent.append("<amountdamage>");
                    xmlContent.append(amountdamage);
                    xmlContent.append("</amountdamage>");

                    xmlContent.append("</entry>");
                }
            }
            xmlContent.append("</entries>");
            File file_with_id = new File("ids1.txt");
            Writer writer = new FileWriter(file_with_id.getName());
            writer.write(xmlContent.toString());
            writer.flush();
            writer.close();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(this.db.getDbID(), e);
        } catch (IOException e) {
            DatabaseUtil.errorPrint(this.db.getDbID(), e);
        }
        return xmlContent;
    }

    @Override
    public StringBuffer getStatisticByContragentByProject(int contragentID, int projectID) {
        StringBuffer xmlContent = new StringBuffer(10000);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            String sql = "select contragent.VIEWTEXT as contragentname, CONTENTSOURCE, COORDSTATUS from PROJECTS\n" +
                    "inner join (select viewtext, docid from GLOSSARY where FORM = 'contractor') as contragent\n" +
                    "on PROJECTS.CONTRAGENT = contragent.DOCID\n" +
                    "where CONTRAGENT = " + contragentID + " and PROJECT = " + projectID;
            ResultSet rs = s.executeQuery(sql);
            StringBuffer temp = new StringBuffer(1000);
            xmlContent.append("<entries>");
            int counter = 0;
            while (rs.next()) {

                counter ++;

                temp.append("<counter>");
                temp.append(rs.getString("COORDSTATUS"));
                temp.append("</counter>");

                temp.append("<remark>");
                temp.append(rs.getString("CONTENTSOURCE"));
                temp.append("</remark>");

                temp.append("<status>");
                temp.append(rs.getString("COORDSTATUS"));
                temp.append("</status>");

                if (rs.isLast()) {
                    xmlContent.append("<entry docid=\"" + contragentID + "\" viewtext=\"" + rs.getString("contragentname") + "\">");
                    xmlContent.append(temp);
                    xmlContent.append("<>");
                    xmlContent.append("</entry>");
                }
            }
            xmlContent.append("</entries>");
            File file_with_id = new File("ids1.txt");
            Writer writer = new FileWriter(file_with_id.getName());
            writer.write(xmlContent.toString());
            writer.flush();
            writer.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    @Override
    public StringBuffer getStatisticsByObject(int objectID) {
        StringBuffer xmlContent = new StringBuffer();
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            String sql = "select viewtext, techname.SHORTNAME as techsupervision, managername.SHORTNAME as projectmanager from GLOSSARY\n" +
                    "inner join (select value, docid from CUSTOM_FIELDS_GLOSSARY where NAME = 'techsupervision') as techid\n" +
                    "on GLOSSARY.DOCID = techid.DOCID\n" +
                    "inner join (select shortname, userid from EMPLOYERS) as techname \n" +
                    "on techid.VALUE = techname.USERID\n" +
                    "inner join (select value, docid from CUSTOM_FIELDS_GLOSSARY where NAME = 'projectmanager') as managerid\n" +
                    "on GLOSSARY.DOCID = managerid.DOCID\n" +
                    "inner join (select shortname, userid from EMPLOYERS) as managername \n" +
                    "on managerid.VALUE = managername.USERID\n" +
                    "where glossary.DOCID = " + objectID;
            ResultSet objrs = s.executeQuery(sql);
            xmlContent.append("<entries>");
            if (objrs.next()) {
                xmlContent.append("<entry>");

                xmlContent.append("<viewtext>");
                xmlContent.append(objrs.getString("viewtext"));
                xmlContent.append("</viewtext>");

                xmlContent.append("<techsupervision>");
                xmlContent.append(objrs.getString("techsupervision"));
                xmlContent.append("</techsupervision>");

                xmlContent.append("<projectmanager>");
                xmlContent.append(objrs.getString("projectmanager"));
                xmlContent.append("</projectmanager>");

                sql = "SELECT COUNT(*) as total, SUM(CASE WHEN coordstatus = 362 THEN 1 ELSE 0 END) as executed from projects where project = " + objectID;
                PreparedStatement countpst = conn.prepareStatement(sql);
                ResultSet countrs = countpst.executeQuery();
                if (countrs.next()) {
                    int total = countrs.getInt("total");
                    int executed = countrs.getInt("executed");

                    xmlContent.append("<total>");
                    xmlContent.append(total);
                    xmlContent.append("</total>");

                    xmlContent.append("<executed>");
                    xmlContent.append(executed);
                    xmlContent.append("</executed>");

                    xmlContent.append("<unexecuted>");
                    xmlContent.append(total - executed);
                    xmlContent.append("</unexecuted>");
                }

                sql = "select contragent.DOCID as contractorid,  contragent.VIEWTEXT as contractor, cat.DOCID as subcatid,  cat.VIEWTEXT as subcat, amountdamage from PROJECTS " +
                        "inner join (select viewtext, docid from GLOSSARY where FORM = 'contractor') as contragent \n" +
                        "on PROJECTS.CONTRAGENT = contragent.DOCID\n" +
                        "left join (select viewtext, docid from GLOSSARY where FORM = 'subcat') as cat\n" +
                        "on PROJECTS.SUBCATEGORY = cat.DOCID\n" +
                        "where PROJECT = " + objectID + " order by contragent.viewtext ";
                PreparedStatement contractorpst = conn.prepareStatement(sql);
                ResultSet contractorrs = contractorpst.executeQuery();
                String prevvalue = "";
                int previd = 0;
                int project = 0;
                int amountdamage = 0;
                StringBuilder temp = new StringBuilder(1000);
                while (contractorrs.next()) {
                    String contractorName = contractorrs.getString("contractor");
                    amountdamage += contractorrs.getInt("amountdamage");
                    if (!contractorName.equalsIgnoreCase(prevvalue)) {
                        if (prevvalue.length() > 0) {
                            sql = "select count(*) from projects where CONTRAGENT = " + previd;
                            Statement cs = conn.createStatement();
                            ResultSet crs = cs.executeQuery(sql);
                            int all = 0;
                            if (crs.next()) {
                                all = crs.getInt(1);
                            }
                            xmlContent.append("<contractor name=\"" + prevvalue + "\" project=\"" + project + "\" total=\"" + all + "\">");
                            xmlContent.append(temp);
                            temp.delete(0, temp.length());
                            project = 0;
                            xmlContent.append("</contractor>");
                        }
                        prevvalue = contractorName;
                        previd = contractorrs.getInt("contractorid");
                    }
                    temp.append("<subcat>");
                    temp.append(contractorrs.getString("subcat"));
                    temp.append("</subcat>");
                    project++;
                    if (contractorrs.isLast()) {
                        sql = "select count(*) from projects where CONTRAGENT = " + contractorrs.getInt("contractorid");
                        Statement cs = conn.createStatement();
                        ResultSet crs = cs.executeQuery(sql);
                        int all = 0;
                        if (crs.next()) {
                            all = crs.getInt(1);
                        }
                        xmlContent.append("<contractor name=\"" + prevvalue + "\" project=\"" + project + "\" total=\"" + all + "\">");
                        xmlContent.append(temp);
                        temp.delete(0, temp.length());
                        project = 0;
                        xmlContent.append("</contractor>");
                    }
                }
                xmlContent.append("<amountdamage>");
                xmlContent.append(amountdamage);
                xmlContent.append("</amountdamage>");
                xmlContent.append("</entry>");

            }

            xmlContent.append("</entries>");
            File file_with_id = new File("ids1.txt");
            Writer writer = new FileWriter(file_with_id.getName());
            writer.write(xmlContent.toString());
            writer.flush();
            writer.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dbPool.returnConnection(conn);
        }

        return xmlContent;
    }

    @Override
    public StringBuffer getProjectsByCondition(IQueryFormula condition, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields, int offset, int pageSize, Set<DocID> toExpandResponses) throws DocumentException {
        StringBuffer xmlContent = new StringBuffer(10000);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = condition.getSQL(complexUserID, pageSize, offset);
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                int docID = rs.getInt("DOCID");
                int docType = rs.getInt("DOCTYPE");
                int attach = rs.getInt("HAS_ATTACHMENT");
                String ddbid = rs.getString("DDBID");
                String form = rs.getString("FORM");
                StringBuffer value = new StringBuffer(1000);

                for (QueryFieldRule field : fields) {
                    String tmpValue = "";
                    if (rs.getObject(field.value) instanceof Timestamp) {
                        tmpValue = Database.sqlDateTimeFormat.format(rs.getTimestamp(field.value));
                    } else {
                        tmpValue = XMLUtil.getAsTagValue(rs.getString(field.value));
                    }
                    value.append("<" + field.name + ">" + tmpValue
                            + "</" + field.name + ">");
                }


                if (this.db.hasResponse(conn, docID, docType, complexUserID, null)) {
                    value.append("<hasresponse>true</hasresponse>");
                }

			     Employer emp =  db.getStructure().getAppUser(absoluteUserID);
                xmlContent.append("<entry isread=\"" + usersActivity.isRead(conn, docID, docType, absoluteUserID) + "\" hasattach=\"" + Integer.toString(attach) + "\" doctype=\"" + DOCTYPE_PROJECT
                        + "\"  " + "docid=\"" + docID + "\" id=\"" + ddbid + "\" "
                        + "favourites=\"" + db.isFavourites(conn, docID, docType, emp) + "\" topicid=\"" + rs.getInt("TOPICID") + "\" "
                        + "url=\"Provider?type=edit&amp;element=project&amp;id=" + form
                        + "&amp;key=" + docID + "\">" + getViewContent(rs) + value);

                if (toExpandResponses.size() > 0) {
                    for (DocID doc : toExpandResponses) {
                        if (doc.id == docID && doc.type == DOCTYPE_PROJECT) {
                            DocumentCollection responses = this.db.getDescendants(docID, DOCTYPE_PROJECT, null, 1, complexUserID, absoluteUserID);
                            if (responses.count > 0) {
                                xmlContent.append("<responses>" + responses.xmlContent + "</responses>");
                            }
                        }
                    }
                }
                xmlContent.append("</entry>");

            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    @Override
    public int insertProject(Project doc, User user)
            throws DocumentException {
        int id = doc.getDocID();
        Connection conn = dbPool.getConnection();
        int keyProject = 0;
        try {
            conn.setAutoCommit(false);
        	Date viewDate = doc.getViewDate();
            String fieldsAsText = "LASTUPDATE, AUTHOR, AUTOSENDAFTERSIGN, "
                    + "AUTOSENDTOSIGN, BRIEFCONTENT, CONTENTSOURCE, COORDSTATUS, "
                    + "REGDATE, PROJECTDATE,  VN, VNNUMBER, DOCVERSION, ISREJECTED, DOCTYPE,"
                    + "DDBID, VIEWTEXT, VIEWICON, FORM, DOCFOLDER, DELIVERYTYPE, SENDER, "
                    + "NOMENTYPE, REGDOCID, HAR, PROJECT, VIEWTEXT1, VIEWTEXT2, VIEWTEXT3, VIEWTEXT4, VIEWTEXT5, VIEWTEXT6, VIEWTEXT7, VIEWNUMBER, "
                    + "VIEWDATE, DEFAULTRULEID, HAS_ATTACHMENT, PARENTDOCID, PARENTDOCTYPE, CATEGORY, "
                    + "ORIGIN, COORDINATS, CITY, STREET, HOUSE, PORCH, FLOOR, APARTMENT, RESPONSIBLE, "
                    + "CTRLDATE, SUBCATEGORY, CONTRAGENT, PODRYAD, SUBPODRYAD, EXECUTOR, RESPOST, AMOUNTDAMAGE ";
            String valuesAsText = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
            if (id != 0 && doc.hasField("recID")) {
                fieldsAsText = "DOCID, " + fieldsAsText;
                valuesAsText = id + ", " + valuesAsText;
                conn.createStatement().execute("SET IDENTITY_INSERT projects ON");
                conn.commit();
            }
            PreparedStatement statProject = conn.prepareStatement("INSERT INTO PROJECTS (" + fieldsAsText + ") values (" + valuesAsText + ")", Statement.RETURN_GENERATED_KEYS);
            statProject.setTimestamp(1, new Timestamp(doc.getLastUpdate().getTime()));
            statProject.setString(2, doc.getAuthorID());
            statProject.setInt(3, doc.getAutoSendAfterSign());
            statProject.setInt(4, doc.getAutoSendToSign());
            statProject.setString(5, doc.getBriefContent());
            statProject.setString(6, doc.getContentSource());
            statProject.setInt(7, doc.getCoordStatus());
            statProject.setTimestamp(8, new Timestamp(new java.util.Date().getTime()));
            statProject.setTimestamp(9, new Timestamp(doc.getProjectDate().getTime()));
            statProject.setString(10, doc.getVn());
            statProject.setInt(11, doc.getVnNumber());
            statProject.setInt(12, doc.getDocVersion());
            statProject.setInt(13, doc.getIsRejected());
            statProject.setInt(14, Const.DOCTYPE_PROJECT);
            statProject.setString(15, doc.getDdbID());
            statProject.setString(16, doc.getViewText());
            statProject.setString(17, doc.getViewIcon());
            statProject.setString(18, doc.getValueAsString("form")[0]);
            statProject.setString(19, doc.getDocFolder());
            statProject.setString(20, doc.getDeliveryType());
            statProject.setString(21, doc.getSender());
            statProject.setInt(22, doc.getNomenType());

            if (doc.getRegDocID() != 0) {
                statProject.setInt(23, doc.getRegDocID());
            } else {
                statProject.setNull(23, Types.INTEGER);
            }

            if (doc.getHar() != 0) {
                statProject.setInt(24, doc.getHar());
            } else {
                statProject.setNull(24, Types.INTEGER);
            }

            if (doc.getProject() != 0) {
                statProject.setInt(25, doc.getProject());
            } else {
                statProject.setNull(25, Types.INTEGER);
            }

            String vt1 = (doc.getViewTextList().size() >= 2 ? doc.getViewTextList().get(1) : "");
            vt1 = (vt1 != null ? vt1.replace("'", "''") : "");
            String vt2 = (doc.getViewTextList().size() >= 3 ? doc.getViewTextList().get(2) : "");
            vt2 = (vt2 != null ? vt2.replace("'", "''") : "");
            String vt3 = (doc.getViewTextList().size() >= 4 ? doc.getViewTextList().get(3) : "");
            vt3 = (vt3 != null ? vt3.replace("'", "''") : "");
            String vt4 = (doc.getViewTextList().size() >= 5 ? doc.getViewTextList().get(4) : "");
            vt4 = (vt4 != null ? vt4.replace("'", "''") : "");
            String vt5 = (doc.getViewTextList().size() >= 6 ? doc.getViewTextList().get(5) : "");
            vt5 = (vt5 != null ? vt5.replace("'", "''") : "");
            String vt6 = (doc.getViewTextList().size() >= 7 ? doc.getViewTextList().get(6) : "");
            vt6 = (vt6 != null ? vt6.replace("'", "''") : "");
            String vt7 = (doc.getViewTextList().size() >= 8 ? doc.getViewTextList().get(7) : "");
            vt7 = (vt7 != null ? vt7.replace("'", "''") : "");

            statProject.setString(26, vt1);
            statProject.setString(27, vt2);
            statProject.setString(28, vt3);
            statProject.setString(29, vt4);
            statProject.setString(30, vt5);
            statProject.setString(31, vt6);
            statProject.setString(32, vt7);


            statProject.setBigDecimal(33, doc.getViewNumber());


            if (viewDate != null) {
                statProject.setTimestamp(34, new Timestamp(viewDate.getTime()));
            } else {
                statProject.setNull(34, Types.TIMESTAMP);
            }
            statProject.setString(35, doc.getDefaultRuleID());
            statProject.setInt(36, doc.blobFieldsMap.size());
            statProject.setInt(37, doc.parentDocID);
            statProject.setInt(38, doc.parentDocType);
            if (doc.getCategory() != 0) {
                statProject.setInt(39, doc.getCategory());
            } else {
                statProject.setNull(39, Types.INTEGER);
            }
            statProject.setString(40, doc.getOrigin());

            statProject.setString(41, doc.getCoordinats());
            if (doc.getCity() != 0) {
                statProject.setInt(42, doc.getCity());
            } else {
                statProject.setNull(42, Types.INTEGER);
            }
            statProject.setString(43, doc.getStreet());
            statProject.setString(44, doc.getHouse());
            statProject.setString(45, doc.getPorch());
            statProject.setString(46, doc.getFloor());
            statProject.setString(47, doc.getApartment());
            statProject.setString(48, doc.getResponsibleSection());
            if (doc.getControlDate() != null) {
                statProject.setTimestamp(49, new Timestamp(doc.getControlDate().getTime()));
            } else {
                statProject.setNull(49, Types.TIMESTAMP);
            }

            if (doc.getSubcategory() != 0) {
                statProject.setInt(50, doc.getSubcategory());
            } else {
                statProject.setNull(50, Types.INTEGER);
            }
            statProject.setString(51, doc.getContragent());
            statProject.setString(52, doc.getPodryad());
            statProject.setString(53, doc.getSubpodryad());
            statProject.setString(54, doc.getExecutor());
            statProject.setString(55, doc.getResponsiblePost());
            statProject.setString(56, doc.getAmountDamage());

            statProject.executeUpdate();
            ResultSet rsp = statProject.getGeneratedKeys();
            if (rsp.next()) {
                keyProject = rsp.getInt(1);
            }

            if (id != 0 && doc.hasField("recID")) {
                //conn.createStatement().execute("SET IDENTITY_INSERT projects OFF");
                //conn.commit();
            }

            for (Recipient recipient : doc.getRecipients()) {
                insertRecipient(keyProject, recipient, conn);
            }

            for (Block block : doc.getBlocksList()) {
                int blockID = insertBlock(keyProject, block, conn);
                for (Coordinator coordinator : block.getCoordinators()) {
                    insertCoordinator(blockID, coordinator, conn);
                }
            }

			/* BLOBS */
            if (id != 0 && !doc.hasField("recID")) {
                PreparedStatement s0 = conn.prepareStatement("SELECT * FROM CUSTOM_BLOBS_PROJECTS WHERE DOCID = " + id);
                ResultSet rs0 = s0.executeQuery();
                while (rs0.next()) {
                    PreparedStatement s1 = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_PROJECTS (DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE, COMMENT)values(?, ?, ?, ?, ?, ?)");
                    s1.setInt(1, keyProject);
                    s1.setString(2, rs0.getString("NAME"));
                    s1.setString(3, rs0.getString("ORIGINALNAME"));
                    s1.setString(4, rs0.getString("CHECKSUM"));
                    s1.setString(6, rs0.getString("COMMENT"));
                    InputStream is = rs0.getBinaryStream("VALUE");
                    s1.setBinaryStream(5, is, is.toString().length());
                    s1.executeUpdate();
                    s1.close();
                }
                rs0.close();
                s0.close();
            } else {
                for (Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
                    PreparedStatement ps = conn
                            .prepareStatement("INSERT INTO CUSTOM_BLOBS_PROJECTS (DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE, COMMENT)values(?, ?, ?, ?, ?, ?)");
                    BlobField bf = blob.getValue();
                    for (BlobFile bfile : bf.getFiles()) {
                        ps.setInt(1, keyProject);
                        ps.setString(2, bf.name);
                        ps.setString(3, bfile.originalName);
                        ps.setString(4, bfile.checkHash);
                        ps.setString(6, bfile.comment);
                        if (!bfile.path.equalsIgnoreCase("")) {
                            File file = new File(bfile.path);
                            FileInputStream fin = new FileInputStream(file);
                            ps.setBinaryStream(5, fin, (int) file.length());
                            ps.executeUpdate();
                            fin.close();
                            Environment.fileToDelete.add(bfile.path);
                        } else {
                            ps.setBytes(5, bfile.getContent());
                            ps.executeUpdate();
                        }
                    }
                    ps.close();
                }
            }

            conn.commit();
            db.insertToAccessTables(conn, "PROJECTS", keyProject, doc);

            //CachePool.flush();
            conn.commit();
            statProject.close();
            if (!doc.hasField("recID")) {
                IUsersActivity ua = db.getUserActivity();
                ua.postCompose(doc, user);
                ua.postMarkRead(keyProject, doc.docType, user);
            }
        } catch (FileNotFoundException fe) {
            DatabaseUtil.errorPrint(db.getDbID(), fe);
            return -1;
        } catch (DocumentException de) {
            DatabaseUtil.errorPrint(db.getDbID(), de);
            return -1;
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
            return -1;
        } catch (IOException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
            return -1;
        } finally {
            dbPool.returnConnection(conn);
        }
        return keyProject;
    }

    public int updateProject(Project doc, User user) throws DocumentAccessException, DocumentException {
        if (doc.hasEditor(user.getAllUserGroups())) {
            Project oldDoc = this.getProjectByID(doc.getDocID(), user.getAllUserGroups(), user.getUserID());
            Connection conn = dbPool.getConnection();
            try {
                Statement statProject = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                conn.setAutoCommit(false);
            	Date viewDate = doc.getViewDate();
                PreparedStatement updatePrj = conn.prepareStatement("UPDATE PROJECTS set LASTUPDATE = ?, " +
                        "AUTHOR = ?, AUTOSENDAFTERSIGN = ?, AUTOSENDTOSIGN = ?, BRIEFCONTENT = ?, " +
                        "CONTENTSOURCE = ?, COORDSTATUS = ?, VNNUMBER = ?, DOCVERSION = ?, ISREJECTED = ?, " +
                        "SENDER = ?, DOCTYPE = ?, DDBID = ?, VIEWTEXT = ?, VIEWICON = ?, " +
                        "FORM = ?, " +
                        "SYNCSTATUS = ?, DOCFOLDER = ?, DELIVERYTYPE = ?, REGDATE = ?, PROJECTDATE = ?, NOMENTYPE = ?, " +
                        "REGDOCID = ?, HAR = ?, PROJECT = ?, VIEWTEXT1 = ?, VIEWTEXT2 = ?, VIEWTEXT3 = ?, VIEWTEXT4 = ?, VIEWTEXT5 = ?, VIEWTEXT6 = ?, VIEWTEXT7 = ?, VIEWNUMBER = ?, " +
                        "VIEWDATE = ?, DEFAULTRULEID = ?, CATEGORY = ?, ORIGIN = ?, COORDINATS = ?, CITY = ?, STREET = ?, " +
                        "HOUSE = ?, PORCH = ?, FLOOR = ?, APARTMENT = ?, RESPONSIBLE = ?, " +
                        " CTRLDATE = ?, SUBCATEGORY = ?, CONTRAGENT = ?, PODRYAD = ?, SUBPODRYAD = ?, EXECUTOR = ?, VN = ?, " +
                        " RESPOST = ?, AMOUNTDAMAGE = ? " +
                        "WHERE DOCID = " + doc.getDocID());
                updatePrj.setTimestamp(1, new Timestamp(doc.getLastUpdate().getTime()));
                updatePrj.setString(2, doc.getAuthorID());
                updatePrj.setInt(3, doc.getAutoSendAfterSign());
                updatePrj.setInt(4, doc.getAutoSendToSign());
                updatePrj.setString(5, doc.getBriefContent());

                updatePrj.setString(6, doc.getContentSource());

                updatePrj.setInt(7, doc.getCoordStatus());
                updatePrj.setInt(8, doc.getVnNumber());
                updatePrj.setInt(9, doc.getDocVersion());
                updatePrj.setInt(10, doc.getIsRejected());
                updatePrj.setString(11, doc.getSender());
                updatePrj.setInt(12, Const.DOCTYPE_PROJECT);
                updatePrj.setString(13, doc.getDdbID());
                updatePrj.setString(14, doc.getViewText());
                updatePrj.setString(15, doc.getViewIcon());
                updatePrj.setString(16, doc.getValueAsString("form")[0]);
                updatePrj.setInt(17, 0);
                updatePrj.setString(18, doc.getDocFolder());
                updatePrj.setString(19, doc.getDeliveryType());
                updatePrj.setTimestamp(20, new Timestamp(doc.getRegDate().getTime()));
                updatePrj.setTimestamp(21, new Timestamp(doc.getProjectDate().getTime()));
                updatePrj.setInt(22, doc.getNomenType());

                if (doc.getRegDocID() != 0) {
                    updatePrj.setInt(23, doc.getRegDocID());
                } else {
                    updatePrj.setNull(23, Types.INTEGER);
                }

                if (doc.getHar() != 0) {
                    updatePrj.setInt(24, doc.getHar());
                } else {
                    updatePrj.setNull(24, Types.INTEGER);
                }

                if (doc.getProject() != 0) {
                    updatePrj.setInt(25, doc.getProject());
                } else {
                    updatePrj.setNull(25, Types.INTEGER);
                }

                String vt1 = (doc.getViewTextList().size() >= 2 ? doc.getViewTextList().get(1) : "");
                vt1 = (vt1 != null ? vt1.replace("'", "''") : "");
                String vt2 = (doc.getViewTextList().size() >= 3 ? doc.getViewTextList().get(2) : "");
                vt2 = (vt2 != null ? vt2.replace("'", "''") : "");
                String vt3 = (doc.getViewTextList().size() >= 4 ? doc.getViewTextList().get(3) : "");
                vt3 = (vt3 != null ? vt3.replace("'", "''") : "");
                String vt4 = (doc.getViewTextList().size() >= 5 ? doc.getViewTextList().get(4) : "");
                vt4 = (vt4 != null ? vt4.replace("'", "''") : "");
                String vt5 = (doc.getViewTextList().size() >= 6 ? doc.getViewTextList().get(5) : "");
                vt5 = (vt5 != null ? vt5.replace("'", "''") : "");
                String vt6 = (doc.getViewTextList().size() >= 7 ? doc.getViewTextList().get(6) : "");
                vt6 = (vt6 != null ? vt6.replace("'", "''") : "");
                String vt7 = (doc.getViewTextList().size() >= 8 ? doc.getViewTextList().get(7) : "");
                vt7 = (vt7 != null ? vt7.replace("'", "''") : "");

                updatePrj.setString(26, vt1);
                updatePrj.setString(27, vt2);
                updatePrj.setString(28, vt3);
                updatePrj.setString(29, vt4);
                updatePrj.setString(30, vt5);
                updatePrj.setString(31, vt6);
                updatePrj.setString(32, vt7);


                updatePrj.setBigDecimal(33, doc.getViewNumber());


                if (viewDate != null) {
                    updatePrj.setTimestamp(34, new Timestamp(viewDate.getTime()));
                } else {
                    updatePrj.setNull(34, Types.TIMESTAMP);
                }
                updatePrj.setString(35, doc.getDefaultRuleID());

                if (doc.getCategory() != 0) {
                    updatePrj.setInt(36, doc.getCategory());
                } else {
                    updatePrj.setNull(36, Types.INTEGER);
                }

                updatePrj.setString(37, doc.getOrigin());

                updatePrj.setString(38, doc.getCoordinats());
                if (doc.getCity() != 0) {
                    updatePrj.setInt(39, doc.getCity());
                } else {
                    updatePrj.setNull(39, Types.INTEGER);
                }
                updatePrj.setString(40, doc.getStreet());
                updatePrj.setString(41, doc.getHouse());
                updatePrj.setString(42, doc.getPorch());
                updatePrj.setString(43, doc.getFloor());
                updatePrj.setString(44, doc.getApartment());
                updatePrj.setString(45, doc.getResponsibleSection());
                if (doc.getControlDate() != null) {
                    updatePrj.setTimestamp(46, new Timestamp(doc.getControlDate().getTime()));
                } else {
                    updatePrj.setNull(46, Types.TIMESTAMP);
                }
                if (doc.getSubcategory() != 0) {
                    updatePrj.setInt(47, doc.getSubcategory());
                } else {
                    updatePrj.setNull(47, Types.INTEGER);
                }
                updatePrj.setString(48, doc.getContragent());
                updatePrj.setString(49, doc.getPodryad());
                updatePrj.setString(50, doc.getSubpodryad());
                updatePrj.setString(51, doc.getExecutor());
                updatePrj.setString(52, doc.getVn());
                updatePrj.setString(53, doc.getResponsiblePost());
                updatePrj.setString(54, doc.getAmountDamage());

                updatePrj.executeUpdate();
                String deleteRecipients = "DELETE FROM PROJECTRECIPIENTS WHERE DOCID = " + doc.getDocID();
                statProject.executeUpdate(deleteRecipients);
                for (Recipient recipient : doc.getRecipients()) {
                    insertRecipient(doc.getDocID(), recipient, conn);
                }
                String deleteBlocks = "DELETE FROM COORDBLOCKS WHERE DOCID = " + doc.getDocID();
                statProject.executeUpdate(deleteBlocks);
                for (Block block : doc.getBlocksList()) {
                    int blockID = insertBlock(doc.getDocID(), block, conn);
                    for (Coordinator coordinator : block.getCoordinators()) {
                        insertCoordinator(blockID, coordinator, conn);
                    }
                }
                // =========== BLOBS ===========
                ResultSet blobs = statProject.executeQuery("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE, COMMENT " +
                        "FROM CUSTOM_BLOBS_PROJECTS " +
                        "WHERE DOCID = " + doc.getDocID());
                while (blobs.next()) {
                    if (!doc.blobFieldsMap.containsKey(blobs.getString("NAME"))) {
                        blobs.deleteRow();
                        continue;
                    }
                    BlobField existingBlob = doc.blobFieldsMap.get(blobs.getString("NAME"));
                    BlobFile tableFile = new BlobFile();
                    tableFile.originalName = blobs.getString("ORIGINALNAME");
                    tableFile.checkHash = blobs.getString("CHECKSUM");
                    if (existingBlob.findFile(tableFile) == null) {
                        blobs.deleteRow();
                        continue;
                    }
                    BlobFile existingFile = existingBlob.findFile(tableFile);
                    if (!existingFile.originalName.equals(tableFile.originalName)) {
                        blobs.updateString("ORIGINALNAME", existingFile.originalName);
                        blobs.updateRow();
                    }
                    if (!existingFile.comment.equals(tableFile.comment)) {
                        blobs.updateString("COMMENT", existingFile.comment);
                        blobs.updateRow();
                    }
                }
                /* now add files that are absent in database */
                for (Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
                    PreparedStatement ps = conn.prepareStatement("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE, COMMENT " +
                            "FROM CUSTOM_BLOBS_PROJECTS " +
                            "WHERE DOCID = ? AND NAME = ? AND CHECKSUM = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                    for (BlobFile bfile : blob.getValue().getFiles()) {
                        ps.setInt(1, doc.getDocID());
                        ps.setString(2, blob.getKey());
                        ps.setString(3, bfile.checkHash);
                        blobs = ps.executeQuery();
                        if (blobs.next()) {
                            if (!bfile.originalName.equals(blobs.getString("ORIGINALNAME"))) {
                                blobs.updateString("ORIGINALNAME", bfile.originalName);
                                blobs.updateRow();
                            }
                            if (!bfile.comment.equals(blobs.getString("COMMENT"))) {
                                blobs.updateString("COMMENT", bfile.comment);
                                blobs.updateRow();
                            }
                        } else {
                            blobs.moveToInsertRow();
                            blobs.updateInt("DOCID", doc.getDocID());
                            blobs.updateString("NAME", blob.getKey());
                            blobs.updateString("ORIGINALNAME", bfile.originalName);
                            blobs.updateString("CHECKSUM", bfile.checkHash);
                            blobs.updateString("COMMENT", bfile.comment);
                            File file = new File(bfile.path);
                            FileInputStream is = new FileInputStream(file);
                            blobs.updateBinaryStream(6, is, (int) file.length());
                            //blobs.updateBinaryStream("VALUE", is);
                            blobs.insertRow();
                            is.close();
                        }
                        Environment.fileToDelete.add(bfile.path);
                    }
                    ps.close();
                }
                db.updateAccessTables(conn, doc, baseTable);
                conn.commit();
                statProject.close();
                updatePrj.close();
                IUsersActivity ua = db.getUserActivity();
                ua.postModify(oldDoc, doc, user);
            } catch (DocumentException de) {
                AppEnv.logger.errorLogEntry(de);
                return -1;
            } catch (SQLException e) {
                DatabaseUtil.errorPrint(db.getDbID(), e);
                return -1;
            } catch (FileNotFoundException e) {
                DatabaseUtil.errorPrint(db.getDbID(), e);
                return -1;
            } catch (IOException e) {
                DatabaseUtil.errorPrint(db.getDbID(), e);
                return -1;
            } finally {
                dbPool.returnConnection(conn);
            }
            return doc.getDocID();
        } else {
            throw new DocumentAccessException(ExceptionType.DOCUMENT_WRITE_ACCESS_RESTRICTED, user.getUserID());
        }
    }

    @Override
    public int recalculate() {
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            conn.setAutoCommit(false);
            String update = "update projects set dbd = (select ROUND(EXTRACT(EPOCH from ctrldate - current_timestamp) / 86400))";
            s.executeUpdate(update);
            conn.commit();
            s.close();
        } catch (SQLException e) {
            AppEnv.logger.errorLogEntry(e);
            return -1;
        } finally {
            dbPool.returnConnection(conn);
        }
        return 0;
    }

    public int recalculate(int docID) {
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            conn.setAutoCommit(false);

            String update = "update projects set dbd = DATEDIFF(select ROUND(EXTRACT(DAY from (ctrldate - current_timestamp)))) where docid =" + docID;
            String select = "select dbd from projects where docid =" + docID;

            s.executeUpdate(update);
            conn.commit();
            ResultSet rs = s.executeQuery(select);
            conn.commit();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            AppEnv.logger.errorLogEntry(e);
            return -1;
        } finally {
            dbPool.returnConnection(conn);
        }
    }

}
