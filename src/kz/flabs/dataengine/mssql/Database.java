package kz.flabs.dataengine.mssql;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.forum.Forum;
import kz.flabs.dataengine.h2.help.Help;
import kz.flabs.dataengine.h2.queryformula.SelectFormula;
import kz.flabs.dataengine.mssql.filters.Filters;
import kz.flabs.dataengine.mssql.glossary.Glossaries;
import kz.flabs.dataengine.mssql.glossary.GlossaryQueryFormula;
import kz.flabs.dataengine.mssql.queryformula.ProjectQueryFormula;
import kz.flabs.dataengine.mssql.queryformula.QueryFormula;
import kz.flabs.dataengine.mssql.queryformula.StructSelectFormula;
import kz.flabs.dataengine.mssql.queryformula.TaskQueryFormula;
import kz.flabs.dataengine.mssql.structure.StructQueryFormula;
import kz.flabs.dataengine.mssql.useractivity.UsersActivity;
import kz.flabs.exception.*;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.ParserUtil;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.document.*;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.servlets.sitefiles.UploadedFile;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.QueryType;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.pchelka.env.Environment;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class Database extends kz.flabs.dataengine.h2.Database implements IDatabase, Const {

    public Database(AppEnv env) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        super(env, true);
        databaseType = DatabaseType.MSSQL;
    }

    @Override
    public IFTIndexEngine getFTSearchEngine() {
        return new kz.flabs.dataengine.mssql.ftengine.FTIndexEngine(this);
    }

    @Override
    public int calcStartEntry(int pageNum, int pageSize) {
        return pageNum;
    }

    @Override
    public IUsersActivity getUserActivity() {
        return new UsersActivity(this);
    }

    @Override
    public IMyDocsProcessor getMyDocsProcessor() {
        return new MyDocsProcessor(this);
    }

    @Override
    public IHelp getHelp() {
        return new Help(this);
    }

    @Override
    public IForum getForum() {
        return new Forum(this, forumDbPool);
    }

    @Override
    public StringBuffer getFavorites(IQueryFormula nf, Set<String> complexUserID, String absoluteUserID, int offset, int pageSize, String fieldsCond, Set<DocID> toExpandResponses, Set<String> toExpandCategory, TagPublicationFormatType publishAs, int page) {
        StringBuffer xmlContent = new StringBuffer(10000);
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sqlWithPaging = " DECLARE @intStartRow int; " +
                    " DECLARE @intEndRow int; " +
                    " DECLARE @intPage int = " + offset + ";" +
                    " DECLARE @intPageSize int = " + pageSize + ";" +
                    " SET @intStartRow = (@intPage - 1) * @intPageSize + 1;" +
                    " SET @intEndRow = @intPage * @intPageSize;" +
                    " WITH blogs AS" +
                    " (SELECT " + " has_attachment, form, ddbid, docid, doctype, viewtext, " + DatabaseUtil.getViewTextList("") + ", viewnumber, viewdate " + ", " +
                    " ROW_NUMBER() OVER(ORDER BY " + Const.DEFAULT_SORT_COLUMN + " " + Const.DEFAULT_SORT_ORDER + ") as intRow, " +
                    " COUNT(" + Const.DEFAULT_SORT_COLUMN + ") OVER() AS intTotalHits " +
                    " FROM " + " MAINDOCS " + " " + " where docid in (select docid from readers_maindocs where username = '" + absoluteUserID + "' and favorites = 1) " + " ) " +
                    " SELECT " + " has_attachment, form, ddbid, docid, doctype, viewtext, " + DatabaseUtil.getViewTextList("") + ", viewnumber, viewdate " + " FROM blogs" +
                    " WHERE intRow BETWEEN @intStartRow AND @intEndRow";
            ResultSet rs = s.executeQuery(sqlWithPaging);
            while (rs.next()) {
                xmlContent.append(getDocumentEntry(conn, complexUserID, absoluteUserID, rs, fieldsCond, toExpandResponses, page));
            }
            sqlWithPaging = " DECLARE @intStartRow int; " +
                    " DECLARE @intEndRow int; " +
                    " DECLARE @intPage int = " + offset + ";" +
                    " DECLARE @intPageSize int = " + pageSize + ";" +
                    " SET @intStartRow = (@intPage - 1) * @intPageSize + 1;" +
                    " SET @intEndRow = @intPage * @intPageSize;" +
                    " WITH blogs AS" +
                    " (SELECT " + " has_attachment, form, ddbid, docid, doctype, viewtext, " + DatabaseUtil.getViewTextList("") + ", viewnumber, viewdate " + ", " +
                    " ROW_NUMBER() OVER(ORDER BY " + Const.DEFAULT_SORT_COLUMN + " " + Const.DEFAULT_SORT_ORDER + ") as intRow, " +
                    " COUNT(" + Const.DEFAULT_SORT_COLUMN + ") OVER() AS intTotalHits " +
                    " FROM " + " TASKS " + " " + " where docid in (select docid from readers_tasks where username = '" + absoluteUserID + "' and favorites = 1) " + " ) " +
                    " SELECT " + " has_attachment, form, ddbid, docid, doctype, viewtext, " + DatabaseUtil.getViewTextList("") + ", viewnumber, viewdate " + " FROM blogs" +
                    " WHERE intRow BETWEEN @intStartRow AND @intEndRow";
            rs = s.executeQuery(sqlWithPaging);
            while (rs.next()) {
                xmlContent.append(getDocumentEntry(conn, complexUserID, absoluteUserID, rs, fieldsCond, toExpandResponses, page));
            }
            sqlWithPaging = " DECLARE @intStartRow int; " +
                    " DECLARE @intEndRow int; " +
                    " DECLARE @intPage int = " + offset + ";" +
                    " DECLARE @intPageSize int = " + pageSize + ";" +
                    " SET @intStartRow = (@intPage - 1) * @intPageSize + 1;" +
                    " SET @intEndRow = @intPage * @intPageSize;" +
                    " WITH blogs AS" +
                    " (SELECT " + " has_attachment, form, ddbid, docid, doctype, viewtext, " + DatabaseUtil.getViewTextList("") + ", viewnumber, viewdate " + ", " +
                    " ROW_NUMBER() OVER(ORDER BY " + Const.DEFAULT_SORT_COLUMN + " " + Const.DEFAULT_SORT_ORDER + ") as intRow, " +
                    " COUNT(" + Const.DEFAULT_SORT_COLUMN + ") OVER() AS intTotalHits " +
                    " FROM " + " EXECUTIONS " + " " + " where docid in (select docid from readers_executions where username = '" + absoluteUserID + "' and favorites = 1) " + " ) " +
                    " SELECT " + " has_attachment, form, ddbid, docid, doctype, viewtext, " + DatabaseUtil.getViewTextList("") + ", viewnumber, viewdate " + " FROM blogs" +
                    " WHERE intRow BETWEEN @intStartRow AND @intEndRow";
            rs = s.executeQuery(sqlWithPaging);
            while (rs.next()) {
                xmlContent.append(getDocumentEntry(conn, complexUserID, absoluteUserID, rs, fieldsCond, toExpandResponses, page));
            }
            sqlWithPaging = " DECLARE @intStartRow int; " +
                    " DECLARE @intEndRow int; " +
                    " DECLARE @intPage int = " + offset + ";" +
                    " DECLARE @intPageSize int = " + pageSize + ";" +
                    " SET @intStartRow = (@intPage - 1) * @intPageSize + 1;" +
                    " SET @intEndRow = @intPage * @intPageSize;" +
                    " WITH blogs AS" +
                    " (SELECT " + " has_attachment, form, ddbid, docid, doctype, viewtext, " + DatabaseUtil.getViewTextList("") + ", viewnumber, viewdate " + ", " +
                    " ROW_NUMBER() OVER(ORDER BY " + Const.DEFAULT_SORT_COLUMN + " " + Const.DEFAULT_SORT_ORDER + ") as intRow, " +
                    " COUNT(" + Const.DEFAULT_SORT_COLUMN + ") OVER() AS intTotalHits " +
                    " FROM " + " projects " + " " + " where docid in (select docid from readers_projects where username = '" + absoluteUserID + "' and favorites = 1) " + " ) " +
                    " SELECT " + " has_attachment, form, ddbid, docid, doctype, viewtext, " + DatabaseUtil.getViewTextList("") + ", viewnumber, viewdate " + " FROM blogs" +
                    " WHERE intRow BETWEEN @intStartRow AND @intEndRow";
            rs = s.executeQuery(sqlWithPaging);
            while (rs.next()) {
                xmlContent.append(getDocumentEntry(conn, complexUserID, absoluteUserID, rs, fieldsCond, toExpandResponses, page));
            }
            s.close();
            rs.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } catch (DocumentException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    @Override
    public ArrayList<BaseDocument> getDocumentsByCondition(String query, Set<String> complexUserID, String absoluteUserID, int limit, int offset) throws DocumentException, DocumentAccessException, QueryFormulaParserException, ComplexObjectException {
        FormulaBlocks preparedQueryFormula = new FormulaBlocks(query, QueryType.DOCUMENT);
        QueryFormula queryFormula = new QueryFormula("", preparedQueryFormula);
        return getDocumentsByCondition(queryFormula, complexUserID, absoluteUserID, this.calcStartEntry(limit, offset), offset);
    }

    public ArrayList<BaseDocument> getAllDocuments(int docType, Set<String> complexUserID, String absoluteUserID, String[] fields, int start, int end) throws DocumentException, DocumentAccessException, ComplexObjectException {
        ArrayList<BaseDocument> documents = new ArrayList<BaseDocument>();
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            String sql;
            switch (docType) {
                case DOCTYPE_MAIN:
                    if (start == -1 && --end == 0) {
                        sql = "select distinct MAINDOCS.DOCID from MAINDOCS JOIN READERS_MAINDOCS on(MAINDOCS.DOCID=READERS_MAINDOCS.DOCID) "
                                + "and READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
                    } else {
                        sql = " DECLARE @intStartRow int;  " +
                                " DECLARE @intEndRow int;  " +
                                " DECLARE @intPage int = " + start + "; " +
                                " DECLARE @intPageSize int = " + end + "; " +
                                " SET @intStartRow = (@intPage - 1) * @intPageSize + 1; " +
                                " SET @intEndRow = @intPage * @intPageSize; " +
                                " WITH blogs AS " +
                                " (SELECT distinct MAINDOCS.DOCID,  ROW_NUMBER() OVER(ORDER BY VIEWDATE ASC) as intRow,  " +
                                " COUNT(VIEWDATE) OVER() AS intTotalHits  " +
                                " FROM  MAINDOCS JOIN READERS_MAINDOCS on(MAINDOCS.DOCID=READERS_MAINDOCS.DOCID) " +
                                " and READERS_MAINDOCS.USERNAME IN ('[observer]'))  " +
                                " SELECT distinct blogs.DOCID FROM blogs " +
                                " WHERE intRow BETWEEN @intStartRow AND @intEndRow";
                    }
                    ResultSet rs = s.executeQuery(sql);
                    while (rs.next()) {
                        Document doc = getMainDocumentByID(rs.getInt("DOCID"), complexUserID, absoluteUserID);
                        documents.add(doc);
                    }
                    break;
                case DOCTYPE_TASK:
                    if (start == -1 && --end == 0) {
                        sql = "select distinct TASKS.DOCID from TASKS JOIN READERS_TASKS on(TASKS.DOCID=READERS_TASKS.DOCID) "
                                + "and READERS_TASKS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
                    } else {
                        sql = " DECLARE @intStartRow int;  " +
                                " DECLARE @intEndRow int;  " +
                                " DECLARE @intPage int = " + start + "; " +
                                " DECLARE @intPageSize int = " + end + "; " +
                                " SET @intStartRow = (@intPage - 1) * @intPageSize + 1; " +
                                " SET @intEndRow = @intPage * @intPageSize; " +
                                " WITH blogs AS " +
                                " (SELECT distinct TASKS.DOCID,  ROW_NUMBER() OVER(ORDER BY VIEWDATE ASC) as intRow,  " +
                                " COUNT(VIEWDATE) OVER() AS intTotalHits  " +
                                " FROM  TASKS JOIN READERS_TASKS on(TASKS.DOCID=READERS_TASKS.DOCID) " +
                                " and READERS_TASKS.USERNAME IN ('[observer]'))  " +
                                " SELECT distinct blogs.DOCID FROM blogs " +
                                " WHERE intRow BETWEEN @intStartRow AND @intEndRow";
                    }
                    rs = s.executeQuery(sql);
                    while (rs.next()) {
                        Task task = getTasks().getTaskByID(rs.getInt("DOCID"), complexUserID, absoluteUserID);
                        documents.add(task);
                    }
                    break;
                case DOCTYPE_EXECUTION:
                    if (start == -1 && --end == 0) {
                        sql = "select distinct EXECUTIONS.DOCID from EXECUTIONS JOIN READERS_EXECUTIONS on(EXECUTIONS.DOCID=READERS_EXECUTIONS.DOCID) "
                                + "and READERS_EXECUTIONS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
                    } else {
                        sql = " DECLARE @intStartRow int;  " +
                                " DECLARE @intEndRow int;  " +
                                " DECLARE @intPage int = " + start + "; " +
                                " DECLARE @intPageSize int = " + end + "; " +
                                " SET @intStartRow = (@intPage - 1) * @intPageSize + 1; " +
                                " SET @intEndRow = @intPage * @intPageSize; " +
                                " WITH blogs AS " +
                                " (SELECT distinct EXECUTIONS.DOCID,  ROW_NUMBER() OVER(ORDER BY VIEWDATE ASC) as intRow,  " +
                                " COUNT(VIEWDATE) OVER() AS intTotalHits  " +
                                " FROM  EXECUTIONS JOIN READERS_EXECUTIONS on(EXECUTIONS.DOCID=READERS_EXECUTIONS.DOCID) " +
                                " and READERS_EXECUTIONS.USERNAME IN ('[observer]'))  " +
                                " SELECT distinct blogs.DOCID FROM blogs " +
                                " WHERE intRow BETWEEN @intStartRow AND @intEndRow";
                    }
                    rs = s.executeQuery(sql);
                    while (rs.next()) {
                        Execution exec = getExecutions().getExecutionByID(rs.getInt("DOCID"), complexUserID, absoluteUserID);
                        documents.add(exec);
                    }
                    break;
                case DOCTYPE_PROJECT:
                    if (start == -1 && --end == 0) {
                        sql = "select distinct PROJECTS.DOCID from PROJECTS JOIN READERS_PROJECTS on(PROJECTS.DOCID=READERS_PROJECTS.DOCID) "
                                + "and READERS_PROJECTS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
                    } else {
                        sql = " DECLARE @intStartRow int;  " +
                                " DECLARE @intEndRow int;  " +
                                " DECLARE @intPage int = " + start + "; " +
                                " DECLARE @intPageSize int = " + end + "; " +
                                " SET @intStartRow = (@intPage - 1) * @intPageSize + 1; " +
                                " SET @intEndRow = @intPage * @intPageSize; " +
                                " WITH blogs AS " +
                                " (SELECT distinct PROJECTS.DOCID,  ROW_NUMBER() OVER(ORDER BY VIEWDATE ASC) as intRow,  " +
                                " COUNT(VIEWDATE) OVER() AS intTotalHits  " +
                                " FROM  PROJECTS JOIN READERS_PROJECTS on(PROJECTS.DOCID=READERS_PROJECTS.DOCID) " +
                                " and READERS_PROJECTS.USERNAME IN ('[observer]'))  " +
                                " SELECT distinct blogs.DOCID FROM blogs " +
                                " WHERE intRow BETWEEN @intStartRow AND @intEndRow";
                    }
                    rs = s.executeQuery(sql);
                    while (rs.next()) {
                        Project prj = getProjects().getProjectByID(rs.getInt("DOCID"), complexUserID, absoluteUserID);
                        documents.add(prj);
                    }
                    break;
            }
            s.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return documents;
    }

    public StringBuffer getDocsByCondition(IQueryFormula blocks, Set<String> complexUserID, String absoluteUserID, int offset, int pageSize, String fieldsCond, Set<DocID> toExpandResponses, Set<String> toExpandCategory, TagPublicationFormatType publishAs, int page) throws DocumentException {
        StringBuffer xmlContent = new StringBuffer(10000);

        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = blocks.getSQL(complexUserID, pageSize, offset);
            ResultSet rs = s.executeQuery(sql);
            if (blocks.isGroupBy()) {
                SourceSupplier ss = new SourceSupplier(env);
                while (rs.next()) {
                    String categoryID = rs.getString(1);
                    if (categoryID != null) {
                        int groupCount = rs.getInt(2);
                        String categoryVal[] = {categoryID};
                        String viewText = ss.publishAs(publishAs, categoryVal).get(0)[0];
                        xmlContent.append("<entry  doctype=\"" + CATEGORY + "\" count=\"" + groupCount + "\" " +
                                " categoryid=\"" + categoryID + "\" " +
                                " docid=\"" + categoryID + "\" " +
                                XMLUtil.getAsAttribute("viewtext", viewText) +
                                "url=\"Provider?type=view&amp;id=" + blocks.getQueryID() + "&amp;command=expand`" + categoryID + "\" ><viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext>");

                        for (String category : toExpandCategory) {
                            if (categoryID.equalsIgnoreCase(category)) {
                                StringBuffer categoryValue = getGetOneCategory(complexUserID, absoluteUserID, blocks.getGroupCondition(category), fieldsCond, toExpandResponses, page);
                                int catID;
                                String catName;
                                try {
                                    catID = Integer.valueOf(category);
                                    catName = this.getGlossaryCustomFieldValueByID(catID, "name");
                                } catch (NumberFormatException e) {
                                    catID = 0;
                                    catName = "";
                                }

                                xmlContent.append("<category id=\"" + catID + "\" name=\"" + catName + "\">" + categoryValue + "</category>");
                                break;
                            }
                        }
                    } else {
                        xmlContent.append("<entry  doctype=\"" + DOCTYPE_UNKNOWN + "\" count=\"0\" " +
                                " categoryid=\"null\"" + XMLUtil.getAsAttribute("viewtext", "category is null") + "><viewtext>category is null</viewtext>");
                    }
                    xmlContent.append("</entry>");
                }

            } else {
                while (rs.next()) {
                    xmlContent.append(getDocumentEntry(conn, complexUserID, absoluteUserID, rs, fieldsCond, toExpandResponses, page));
                }
            }
            conn.commit();
            s.close();
            rs.close();

        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbPool.returnConnection(conn);
        }
        return xmlContent;
    }

    @Override
    public IGlossaries getGlossaries() {
        return new Glossaries(env);
    }

    public static String sqlDateTimeFormat(Date date) {
        SimpleDateFormat sqlDateTimeFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        return sqlDateTimeFormat.format(date);
    }

    public void removeUnrelatedAttachments() {
        Connection conn = dbPool.getConnection();
        try {
            Statement s = conn.createStatement();
            s.addBatch("delete from custom_blobs_maindocs where (select ROUND(EXTRACT(EPOCH from regdate - current_timestamp) / 86400)) < 0 and docid is null");
            s.addBatch("delete from custom_blobs_glossary where (select ROUND(EXTRACT(EPOCH from regdate - current_timestamp) / 86400)) < 0 and docid is null");
            s.addBatch("delete from custom_blobs_employers where (select ROUND(EXTRACT(EPOCH from regdate - current_timestamp) / 86400)) < 0 and docid is null");
            s.addBatch("delete from custom_blobs_topics where (select ROUND(EXTRACT(EPOCH from regdate - current_timestamp) / 86400)) < 0 and docid is null");
            s.addBatch("delete from custom_blobs_posts where (select ROUND(EXTRACT(EPOCH from regdate - current_timestamp) / 86400)) < 0 and docid is null");
            s.executeBatch();
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
    }

    @Override
    public ArrayList<UploadedFile> insertBlobTables(List<FileItem> fileItems) throws SQLException, IOException {
        Connection conn = dbPool.getConnection();
        ArrayList<UploadedFile> files = new ArrayList<>();
        try {
            String tableSuffix = "MAINDOCS";
            for (FileItem item : fileItems) {
                if (item != null && item.getName() != null && !"".equalsIgnoreCase(item.getName())) {
                    PreparedStatement ps = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_" + tableSuffix + " (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE, REGDATE) values (?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    String hash = Util.getHexHash(item.getInputStream());
                    ps.setInt(1, 1);
                    ps.setString(2, "rtfcontent");
                    ps.setString(3, FilenameUtils.getName(item.getName()));
                    ps.setString(4, hash);
                    ps.setString(5, "");
                    ps.setBytes(6, item.get());
                    ps.setTimestamp(7, new Timestamp(new Date().getTime()));
                    ps.executeUpdate();
                    conn.commit();
                    int key = 0;
                    ResultSet rs = ps.getGeneratedKeys();
                    while (rs.next()) {
                        key = rs.getInt(1);
                    }
                    String name = item.getName();
                    long filelen = item.getSize();
                    files.add(new UploadedFile(name, hash, filelen, item.getContentType(), String.valueOf(key)));
                    ps.close();
                }
            }

        } catch (Exception e) {
            DatabaseUtil.errorPrint(dbID, e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return files;
    }

    public void insertBlobTables(Connection conn, int id, int key, Document doc, String tableSuffix) throws SQLException, IOException {
        if (id != 0 && !doc.hasField("recID")) {
            PreparedStatement s0 = conn.prepareStatement("SELECT * FROM CUSTOM_BLOBS_" + tableSuffix + " WHERE DOCID = " + id);
            ResultSet rs0 = s0.executeQuery();
            while (rs0.next()) {
                PreparedStatement s1 = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_" + tableSuffix + " (DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE)values(?, ?, ?, ?, ?)");
                s1.setInt(1, key);
                s1.setString(2, rs0.getString("NAME"));
                s1.setString(3, rs0.getString("ORIGINALNAME"));
                s1.setString(4, rs0.getString("CHECKSUM"));
                s1.setBinaryStream(5, rs0.getBinaryStream("VALUE"));
                s1.executeUpdate();
                s1.close();
            }
            rs0.close();
            s0.close();

        } else {
            for (Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
                PreparedStatement ps = conn
                        .prepareStatement("INSERT INTO CUSTOM_BLOBS_" + tableSuffix + " (DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE)values(?, ?, ?, ?, ?)");
                BlobField bf = blob.getValue();
                for (BlobFile bfile : bf.getFiles()) {
                    ps.setInt(1, key);
                    ps.setString(2, bf.name);
                    ps.setString(3, bfile.originalName);
                    ps.setString(4, bfile.checkHash);
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
    }

    @Override
    public int insertMainDocument(Document doc, User user) throws DocumentException {
        Date viewDate = doc.getViewDate();
        String fieldsAsText = "AUTHOR, REGDATE, DOCTYPE, LASTUPDATE, DDBID, PARENTDOCDDBID, VIEWTEXT, PARENTDOCID, PARENTDOCTYPE, " +
                " FORM, DEFAULTRULEID, " + DatabaseUtil.getViewTextList("") + ", VIEWNUMBER, VIEWDATE, SIGN, SIGNEDFIELDS, HAS_ATTACHMENT";
        int count_files = 0;
        for (BlobField bfield : doc.blobFieldsMap.values()) {
            count_files += bfield.getFilesCount();
        }
        String valuesAsText = "'" + doc.getAuthorID()
                + "', '" + sqlDateTimeFormat.format(doc.getRegDate())
                + "', " + doc.docType
                + ", '" + sqlDateTimeFormat.format(doc.getLastUpdate())
                + "', '" + doc.getDdbID()
                + "', '" + doc.getParentDocumentID()
                + "', '" + doc.getViewText().replace("'", "''")
                + "', " + doc.parentDocID
                + ", " + doc.parentDocType
                + "','" + doc.form
                + "', '" + doc.getDefaultRuleID()
                + "', " + DatabaseUtil.getViewTextValues(doc) + " , " + doc.getViewNumber()
                + ", " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'" : "null")
                + ", '" + doc.getSign()
                + "', '" + doc.getSignedFields()
                + "', " + count_files;
        Connection conn = dbPool.getConnection();
        int id = doc.getDocID();

        if (id != 0 && doc.hasField("recID")) {
            fieldsAsText = "DOCID, " + fieldsAsText;
            valuesAsText = id + ", " + valuesAsText;
        }
        try {
            conn.setAutoCommit(false);
            PreparedStatement pst;
            String sql = "";
            try {
                sql = "insert into MAINDOCS(" + fieldsAsText + ") values (" + valuesAsText + ")";
                pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                pst.executeUpdate();
                int key = 0;
                ResultSet rs = pst.getGeneratedKeys();
                while (rs.next()) {
                    key = rs.getInt(1);
                }
                for (Field field : doc.fields()) {
                    switch (field.getTypeAsDatabaseType()) {
                        case TEXT:
                            try {
                                String sqlStatement = "insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)"
                                        + "values ("
                                        + key
                                        + ", '"
                                        + field.name
                                        + "', '"
                                        + field.valueAsText.replace("'", "''").trim()
                                        + "', "
                                        + field.getTypeAsDatabaseType() + ")";
                                pst = conn
                                        .prepareStatement(sqlStatement);
                                pst.executeUpdate();
                                pst.close();
                            } catch (SQLException se) {
                                DatabaseUtil.errorPrint(dbID, se);
                                conn.rollback();
                                return -1;
                            }
                            break;
                        case TEXTLIST:
                            pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)"
                                    + " values ("
                                    + key
                                    + ", '"
                                    + field.name
                                    + "', ?,"
                                    + field.getTypeAsDatabaseType() + ")");
                            for (String value : field.valuesAsStringList) {
                                pst.setString(1, value);
                                try {
                                    pst.executeUpdate();

                                } catch (SQLException se) {
                                    DatabaseUtil.errorPrint(dbID, se);
                                    return -1;
                                }
                            }
                            pst.close();
                            break;
                        case NUMBERS:
                            pst = conn
                                    .prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASNUMBER, TYPE)"
                                            + "values("
                                            + key
                                            + ", '"
                                            + field.name
                                            + "', "
                                            + field.valueAsNumber
                                            + ", " + field.getTypeAsDatabaseType() + ")");
                            pst.executeUpdate();
                            pst.close();
                            break;
                        case DATETIMES:
                            if (field.valueAsDate == null) {
                                Database.logger.errorLogEntry("Unable to convert empty date to DB format: " + field.name);
                                continue;
                            }
                            try {
                                pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASDATE, TYPE)"
                                        + "values(" + key + ", '" + field.name + "', '" + Util.convertDateTimeToDerbyFormat(field.valueAsDate) + "', "
                                        + field.getTypeAsDatabaseType() + ")");
                            } catch (DataConversionException e) {
                                Database.logger.errorLogEntry(e + ", field=" + field.name);
                                return -1;
                            }
                            pst.executeUpdate();
                            pst.close();
                            break;
                        case DATE:
                            if (field.valueAsDate == null) {
                                Database.logger.errorLogEntry("Unable to convert \"null\" to date : " + field.name);
                                continue;
                            }
                            try {
                                pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASDATE, TYPE)"
                                        + "values(" + key + ", '" + field.name + "', '" + Util.convertDateTimeToDerbyFormat(field.valueAsDate) + "', "
                                        + field.getTypeAsDatabaseType() + ")");
                            } catch (DataConversionException e) {
                                Database.logger.errorLogEntry(e + ", field=" + field.name);
                                return -1;
                            }
                            pst.executeUpdate();
                            pst.close();
                            break;
                        case GLOSSARY:
                            pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASGLOSSARY, TYPE)"
                                    + " values ("
                                    + key
                                    + ", '"
                                    + field.name
                                    + "', ?,"
                                    + field.getTypeAsDatabaseType() + ")");
                            for (Integer value : field.valuesAsGlossaryData) {
                                if (value == 0) {
                                    pst.setNull(1, Types.INTEGER);
                                } else {
                                    pst.setInt(1, value);
                                }

                                try {
                                    pst.executeUpdate();
                                } catch (SQLException se) {
                                    DatabaseUtil.errorPrint(dbID, se);
                                    return -1;
                                }
                            }
                            pst.close();
                            break;
                        case RICHTEXT:
                            try {
                                String sqlStatement = "insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASCLOB, TYPE)"
                                        + "values ("
                                        + key
                                        + ", '"
                                        + field.name
                                        + "', '"
                                        + field.valueAsText.replace("'", "''").trim()
                                        + "', "
                                        + field.getTypeAsDatabaseType() + ")";
                                pst = conn
                                        .prepareStatement(sqlStatement);
                                pst.executeUpdate();
                                pst.close();
                            } catch (SQLException se) {
                                DatabaseUtil.errorPrint(dbID, se);
                                conn.rollback();
                                return -1;
                            }
                            break;
                    }
                }

                conn.commit();
                insertToAccessTables(conn, "MAINDOCS", key, doc);
                insertBlobTables(conn, id, key, doc, baseTable);//CachePool.flush();
                conn.commit();
                pst.close();
                if (!doc.hasField("recID")) {
                    IUsersActivity ua = getUserActivity();
                    ua.postCompose(doc, user);
                    ua.postMarkRead(key, doc.docType, user);
                }
                return key;
            } catch (SQLException e) {
                conn.rollback();
                DatabaseUtil.errorPrint(dbID, e);
                return -1;
            } catch (FileNotFoundException e) {
                DatabaseUtil.errorPrint(dbID, e);
                return -1;
            } catch (IOException e) {
                DatabaseUtil.errorPrint(dbID, e);
                return -1;
            }
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
            return -1;
        } finally {
            dbPool.returnConnection(conn);
        }
    }

    @Override
    public IQueryFormula getQueryFormula(String id, FormulaBlocks blocks) {
        switch (blocks.docType) {
            case STRUCTURE:
                StructQueryFormula sqf = new StructQueryFormula(id, blocks);
                return sqf;
            case GLOSSARY:
                return new GlossaryQueryFormula(id, blocks);
            case PROJECT:
                return new ProjectQueryFormula(id, blocks);
            case TASK:
                return new TaskQueryFormula(id, blocks);
            default:
                QueryFormula queryFormula = new QueryFormula(id, blocks);
                return queryFormula;
        }
    }

    @Override
    public ISelectFormula getSelectFormula(FormulaBlocks blocks) {
        switch (blocks.docType) {
            case STRUCTURE:
                StructSelectFormula ssf = new StructSelectFormula(blocks);
                return ssf;
            case DOCUMENT:
            default:
                SelectFormula sf = new SelectFormula(blocks);
                return sf;
        }
    }


    @Override
    public IFilters getFilters() {
        return new Filters(this);
    }

    public void updateBlobTables(Connection conn, Document doc, String tableSuffix) throws SQLException, IOException {
        Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        ResultSet blobs = s.executeQuery("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE " +
                "FROM CUSTOM_BLOBS_" + tableSuffix + " " +
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
        }
                /* now add files that are absent in database */
        for (Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
            PreparedStatement ps = conn.prepareStatement("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE " +
                    "FROM CUSTOM_BLOBS_" + tableSuffix + " " +
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
                } else {
                    blobs.moveToInsertRow();
                    blobs.updateInt("DOCID", doc.getDocID());
                    blobs.updateString("NAME", blob.getKey());
                    blobs.updateString("ORIGINALNAME", bfile.originalName);
                    blobs.updateString("CHECKSUM", bfile.checkHash);
                    File file = new File(bfile.path);
                    FileInputStream is = new FileInputStream(file);
                    blobs.updateBinaryStream("VALUE", is, (int) file.length());
                    blobs.insertRow();
                    is.close();
                }
                Environment.fileToDelete.add(bfile.path);
            }
        }
    }

    @Override
    public int updateMainDocument(Document doc, User user) throws DocumentAccessException, DocumentException, ComplexObjectException {
        if (doc.hasEditor(user.getAllUserGroups())) {
            Document oldDoc = this.getMainDocumentByID(doc.getDocID(), user.getAllUserGroups(), user.getUserID());
            Connection conn = dbPool.getConnection();
            try {
                Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                conn.setAutoCommit(false);
                Date viewDate = doc.getViewDate();
                String viewTextList = "";
                for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
                    viewTextList += "VIEWTEXT" + i + " =  '" + doc.getViewTextList().get(i).replaceAll("'", "''") + "',";
                }
                if (viewTextList.endsWith(",")) {
                    viewTextList = viewTextList.substring(0, viewTextList.length() - 1);
                }
                int count_files = 0;
                for (BlobField bfield : doc.blobFieldsMap.values()) {
                    count_files += bfield.getFilesCount();
                }
                String mainDocUpd = "update MAINDOCS set LASTUPDATE = '"
                        + sqlDateTimeFormat.format(doc.getLastUpdate())
                        + "', VIEWTEXT='" + doc.getViewText().replace("'", "''")
                        + "', NOTESID='" + doc.getDdbID()
                        + "', DEFAULTRULEID='" + doc.getDefaultRuleID()
                        + "', " + viewTextList + ", VIEWNUMBER = " + doc.getViewNumber()
                        + ", VIEWDATE = " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'" : "null")
                        + ", SIGN = '" + doc.getSign()
                        + "', PARENTDOCDDBID = '" + doc.getParentDocumentID()
                        + "', SIGNEDFIELDS = '" + doc.getSignedFields() + "' "
                        + ", PARENTDOCID = " + doc.parentDocID
                        + ", PARENTDOCTYPE = " + doc.parentDocType
                        + ", HAS_ATTACHMENT = " + count_files
                        + " where DOCID = "
                        + doc.getDocID();
                s.executeUpdate(mainDocUpd);
                for (Field field : doc.fields()) {

                    String upCustomFields = "";
                    switch (field.getTypeAsDatabaseType()) {
                        case TEXT:
                            upCustomFields = "update custom_fields set value = '" + field.valueAsText.replace("'", "''").trim() +
                                    "', type = " + field.getTypeAsDatabaseType() + "where docid =  " + doc.getDocID() + "and name = '" + field.name + "';" +
                                    " insert into custom_fields (docid, name, value, type) " +
                                    " select " + doc.getDocID() + ", '" + field.name + "', '" + field.valueAsText.replace("'", "''").trim() + "', " +
                                    field.getTypeAsDatabaseType() +
                                    " where 1 not in (select 1 from custom_fields where docid = " + doc.getDocID() + " and name = '" + field.name + "')";
                            break;
                        case RICHTEXT:
                            upCustomFields = "update custom_fields set valueasclob = '" + field.valueAsText.replace("'", "''").trim() +
                                    "', type = " + field.getTypeAsDatabaseType() + "where docid =  " + doc.getDocID() + "and name = '" + field.name + "';" +
                                    " insert into custom_fields (docid, name, valueasclob, type) " +
                                    " select " + doc.getDocID() + ", '" + field.name + "', '" + field.valueAsText.replace("'", "''").trim() + "', " +
                                    field.getTypeAsDatabaseType() +
                                    " where 1 not in (select 1 from custom_fields where docid = " + doc.getDocID() + " and name = '" + field.name + "')";
                            break;
                        case TEXTLIST:
                            PreparedStatement ps = conn.prepareStatement("DELETE FROM CUSTOM_FIELDS"
                                    + " WHERE DOCID="
                                    + doc.getDocID()
                                    + " and NAME='"
                                    + field.name
                                    + "'");
                            ps.executeUpdate();
                            ps = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)"
                                    + " values ("
                                    + doc.getDocID()
                                    + ", '"
                                    + field.name
                                    + "', ?,"
                                    + field.getTypeAsDatabaseType() + ")");
                            for (String value : field.valuesAsStringList) {
                                ps.setString(1, value);
                                ps.executeUpdate();
                            }
                            break;
                        case NUMBERS:
                            upCustomFields = "update custom_fields set valueasnumber = " + field.valueAsNumber +
                                    ", type = " + field.getTypeAsDatabaseType() + "where docid =  " + doc.getDocID() + "and name = '" + field.name + "';" +
                                    " insert into custom_fields (docid, name, valueasnumber, type) " +
                                    " select " + doc.getDocID() + ", '" + field.name + "', " + field.valueAsNumber + ", " +
                                    field.getTypeAsDatabaseType() +
                                    " where 1 not in (select 1 from custom_fields where docid = " + doc.getDocID() + " and name = '" + field.name + "')";
                            break;
                        case DATETIMES:
                            if (field.valueAsDate == null) {
                                Database.logger.errorLogEntry("Unable to convert empty date to DB format: " + field.name);
                                continue;
                            }
                            try {
                                upCustomFields = "update custom_fields set valueasdate = '" + Util.convertDateTimeToDerbyFormat(field.valueAsDate) +
                                        "', type = " + field.getTypeAsDatabaseType() + "where docid =  " + doc.getDocID() + "and name = '" + field.name + "';" +
                                        " insert into custom_fields (docid, name, valueasdate, type) " +
                                        " select " + doc.getDocID() + ", '" + field.name + "', '" + Util.convertDateTimeToDerbyFormat(field.valueAsDate) + "', " +
                                        field.getTypeAsDatabaseType() +
                                        " where 1 not in (select 1 from custom_fields where docid = " + doc.getDocID() + " and name = '" + field.name + "')";
                            } catch (DataConversionException e) {
                                Database.logger.errorLogEntry(e + ", field=" + field.name);
                                return -1;
                            }
                            break;
                        case GLOSSARY:
                            PreparedStatement pst = conn.prepareStatement("DELETE FROM CUSTOM_FIELDS WHERE DOCID = " + doc.getDocID() + " and NAME = '" + field.name + "'");
                            pst.executeUpdate();
                            pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASGLOSSARY, TYPE)"
                                    + " values ("
                                    + doc.getDocID()
                                    + ", '"
                                    + field.name
                                    + "', ?,"
                                    + field.getTypeAsDatabaseType() + ")");
                            for (Integer value : field.valuesAsGlossaryData) {
                                if (value == 0) {
                                    pst.setNull(1, Types.INTEGER);
                                } else {
                                    pst.setInt(1, value);
                                }

                                try {
                                    pst.executeUpdate();
                                } catch (SQLException se) {
                                    DatabaseUtil.errorPrint(dbID, se);
                                    return -1;
                                }
                            }
                            break;
                    }
                    if (upCustomFields != "") {
                        s.executeUpdate(upCustomFields);
                    }
                }
                // =========== BLOBS ===========

                updateAccessTables(conn, doc, baseTable);
                updateBlobTables(conn, doc, baseTable);
                conn.commit();
                s.close();

                IUsersActivity ua = getUserActivity();
                ua.postModify(oldDoc, doc, user);

                return doc.getDocID();
            } catch (SQLException e) {
                DatabaseUtil.errorPrint(dbID, e);
                return -1;
            } catch (IOException e) {
                DatabaseUtil.errorPrint(dbID, e);
                return -1;
            } finally {
                dbPool.returnConnection(conn);
            }
        } else {
            throw new DocumentAccessException(ExceptionType.DOCUMENT_WRITE_ACCESS_RESTRICTED, user.getUserID());
        }
    }

    @Override
    public DocumentCollection getDescendants(int docID, int docType, SortByBlock sortBlock, int level, Set<String> complexUserID, String absoluteUserID) {
        int col = 0;
        DocumentCollection documents = new DocumentCollection();
        StringBuffer xmlContent = new StringBuffer(10000);
        String value = "";
        if (docID != 0 && docType != DOCTYPE_UNKNOWN) {
            Connection conn = dbPool.getConnection();
            try {
                conn.setAutoCommit(false);
                Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                String sql = "SELECT distinct resp.docid, doctype, form, resp.viewtext, regdate, has_attachment, resp.allcontrol, ctype"
                        + " FROM TASKS as resp"
                        + " LEFT JOIN"
                        + " (SELECT g.viewtext AS ctype, t.docid AS taskid, g.docid FROM GLOSSARY AS g, TASKS AS t WHERE g.form = 'controltype' AND g.docid = t.controltype) as temp"
                        + " ON resp.docid = taskid, READERS_TASKS r WHERE parentdocid = " + docID + " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
                        + " UNION SELECT distinct resp.docid, doctype, form, viewtext, regdate, has_attachment, -1 as allcontrol, '' as ctype"
                        + " FROM EXECUTIONS as resp, READERS_EXECUTIONS r WHERE parentdocid = "
                        + docID + " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
                        + " UNION SELECT distinct resp.docid, resp.doctype, resp.form, resp.viewtext, resp.regdate, resp.has_attachment, c.valueasnumber AS allcontrol, ctype"
                        + " FROM CUSTOM_FIELDS as c "
                        + " RIGHT JOIN MAINDOCS resp"
                        + " ON resp.docid = c.docid AND c.name = 'allcontrol'"
                        + " LEFT JOIN"
                        + " (SELECT g.viewtext AS ctype, cf.docid AS cfdocid FROM GLOSSARY AS g, CUSTOM_FIELDS AS cf, MAINDOCS AS m"
                        + " WHERE g.docid = cf.valueasnumber AND cf.docid = m.docid AND cf.name = 'controltype') as temp1"
                        + " ON resp.docid = cfdocid,"
                        + " READERS_MAINDOCS r "
                        + " WHERE parentdocid = " + docID + " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
                        + " and resp.form != 'discussion' "
                        + " ORDER BY REGDATE";

                ResultSet rs = statement.executeQuery(sql);

                while (rs.next()) {
                    String viewText = rs.getString("VIEWTEXT");
                    value = "";
                    int respDocID = rs.getInt("DOCID");
                    int repsDocType = rs.getInt("DOCTYPE");
                    int allControl = rs.getInt("ALLCONTROL");
                    String controlType = rs.getString("CTYPE");
                    String form = rs.getString("FORM");
                    xmlContent.append("<entry isread=\"" + usersActivity.isRead(conn, respDocID, repsDocType, absoluteUserID) + "\" hasattach=\"" + Integer.toString(rs.getInt("HAS_ATTACHMENT")) + "\" doctype=\"" + repsDocType
                            + "\"  docid=\"" + respDocID + "\" form=\"" + form + "\" url=\"Provider?type=edit&amp;element=" + resolveElement(form) + "&amp;id=" + form + "&amp;key="
                            + respDocID + "\"" + XMLUtil.getAsAttribute("viewtext", viewText) + (!form.equalsIgnoreCase("KI") ? " allcontrol =\"" + allControl + "\"" : "")
                            + (!form.equalsIgnoreCase("KI") ? " controltype =\"" + controlType + "\"" : ""));
                    col++;

                    int l = level + 1;
                    DocumentCollection responses = getDescendants(respDocID, repsDocType, null, l, complexUserID, absoluteUserID);
                    if (responses.count > 0) {
                        xmlContent.append(" hasresponse=\"true\" >");
                        value += responses.xmlContent;
                    } else {
                        xmlContent.append(" >");
                    }

                    xmlContent.append(value += "<viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext></entry>");
                    col++;

                }

                documents.xmlContent.append(xmlContent);
                rs.close();
                statement.close();
                conn.commit();
                documents.count = col;
            } catch (SQLException e) {
                DatabaseUtil.errorPrint(dbID, e);
            } finally {
                dbPool.returnConnection(conn);
            }
        }
        return documents;
    }


    public ArrayList<BaseDocument> getDescendantsArray(int docID, int docType, DocID[] toExpand, int level, Set<String> complexUserID, String absoluteUserID) throws DocumentException, ComplexObjectException {
        ArrayList<BaseDocument> documents = new ArrayList<BaseDocument>();
        if (docID != 0 && docType != DOCTYPE_UNKNOWN) {
            Connection conn = dbPool.getConnection();
            try {
                conn.setAutoCommit(false);
                Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                String sql = "SELECT distinct resp.docid, doctype, form, resp.viewtext, regdate, has_attachment, resp.allcontrol, ctype"
                        + " FROM TASKS as resp"
                        + " LEFT JOIN"
                        + " (SELECT g.viewtext AS ctype, t.docid AS taskid, g.docid FROM GLOSSARY AS g, TASKS AS t WHERE g.form = 'controltype' AND g.docid = t.controltype) as temp"
                        + " ON resp.docid = taskid, READERS_TASKS r WHERE parentdocid = " + docID + " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
                        + " UNION SELECT distinct resp.docid, doctype, form, viewtext, regdate, has_attachment, -1 as allcontrol, '' as ctype"
                        + " FROM EXECUTIONS as resp, READERS_EXECUTIONS r WHERE parentdocid = "
                        + docID + " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
                        + " UNION SELECT distinct resp.docid, resp.doctype, resp.form, resp.viewtext, resp.regdate, resp.has_attachment, c.valueasnumber AS allcontrol, ctype"
                        + " FROM CUSTOM_FIELDS as c "
                        + " RIGHT JOIN MAINDOCS resp"
                        + " ON resp.docid = c.docid AND c.name = 'allcontrol'"
                        + " LEFT JOIN"
                        + " (SELECT g.viewtext AS ctype, cf.docid AS cfdocid FROM GLOSSARY AS g, CUSTOM_FIELDS AS cf, MAINDOCS AS m"
                        + " WHERE g.docid = cf.valueasnumber AND cf.docid = m.docid AND cf.name = 'controltype') as temp1"
                        + " ON resp.docid = cfdocid,"
                        + " READERS_MAINDOCS r "
                        + " WHERE parentdocid = " + docID + " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
                        + " and resp.form != 'discussion' "
                        + " ORDER BY REGDATE";

                ResultSet rs = statement.executeQuery(sql);

                while (rs.next()) {
                    BaseDocument doc = null;
                    int respDocID = rs.getInt("DOCID");
                    int respDocType = rs.getInt("DOCTYPE");

                    switch (respDocType) {
                        case DOCTYPE_MAIN:
                            doc = getMainDocumentByID(respDocID, complexUserID, absoluteUserID);
                            break;
                        case DOCTYPE_TASK:
                            doc = getTasks().getTaskByID(respDocID, complexUserID, absoluteUserID);
                            break;
                        case DOCTYPE_EXECUTION:
                            doc = getExecutions().getExecutionByID(respDocID, complexUserID, absoluteUserID);
                            break;
                        case DOCTYPE_PROJECT:
                            doc = getProjects().getProjectByID(respDocID, complexUserID, absoluteUserID);
                            break;
                    }

                    if (doc != null) {
                        documents.add(doc);
                    }

                    int l = level + 1;

                    ArrayList<BaseDocument> responses = getDescendantsArray(respDocID, respDocType, null, l, complexUserID, absoluteUserID);
                    if (responses.size() > 0) {
                        documents.addAll(responses);
                    }
                }
                rs.close();
                statement.close();
                conn.commit();
            } catch (SQLException e) {
                DatabaseUtil.errorPrint(dbID, e);
            } catch (DocumentAccessException e) {
                DatabaseUtil.errorPrint(dbID, e);
            } finally {
                dbPool.returnConnection(conn);
            }
        }
        return documents;
    }

    @Override
    public StringBuffer getUsersRecycleBin(int offset, int pageSize, String userID) {
        return getUserActivity().getActivity(userID, offset, pageSize, UsersActivityType.DELETED.getCode());
    }

    @Override
    public int getUsersRecycleBinCount(int offset, int pageSize, String userID) {
        return getUserActivity().getActivitiesCount(userID, UsersActivityType.DELETED.getCode());
    }

    @Override
    public ITasks getTasks() {
        return new kz.flabs.dataengine.postgresql.TaskOnDatabase(this);
    }

    @Override
    public IExecutions getExecutions() {
        return new kz.flabs.dataengine.postgresql.ExecutionsOnDatabase(this);
    }

    @Override
    public IProjects getProjects() {
        return new ProjectOnDatabase(this);
    }

    @Override
    public ArrayList<ViewEntry> getGroupedEntries(String fieldName, int limit, int offset, User user) {
        ArrayList<ViewEntry> vec = new ArrayList<ViewEntry>();
        String result[] = ParserUtil.resolveFiledTypeBySuffix(fieldName);
        Set<String> users = user.getAllUserGroups();

        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "select cf." + result[1] + ", count(cf." + result[1] + ") from custom_fields as cf where cf.name = '" + result[0] + "' and " +
                    " cf.docid in (select docid from readers_maindocs as rm where rm.username in (" + DatabaseUtil.prepareListToQuery(users) + ")) " +
                    " group by cf." + result[1] + " order by cf." + result[1];

            String sqlWithPaging = " DECLARE @intStartRow int; " +
                    " DECLARE @intEndRow int; " +
                    " DECLARE @intPage int = " + limit + ";" +
                    " DECLARE @intPageSize int = " + offset + ";" +
                    " SET @intStartRow = (@intPage - 1) * @intPageSize + 1;" +
                    " SET @intEndRow = @intPage * @intPageSize;" +
                    " WITH blogs AS" +
                    " (SELECT " + " cf." + result[1] + ", count(cf." + result[1] + ")" + ", " +
                    " ROW_NUMBER() OVER(" + "cf." + result[1] + ") as intRow, " +
                    " COUNT(" + Const.DEFAULT_SORT_COLUMN + ") OVER() AS intTotalHits " +
                    " FROM custom_fields as cf " + " where cf.name = '" + result[0] + "' and " +
                    " cf.docid in (select docid from readers_maindocs as rm where rm.username in (" + DatabaseUtil.prepareListToQuery(users) + ")) " + " ) " +
                    " SELECT cf." + result[1] + ", count(cf." + result[1] + ") FROM custom_fields as cf " +
                    " WHERE intRow BETWEEN @intStartRow AND @intEndRow" + " group by cf." + result[1];

            ResultSet rs;
            if (limit == -1 && --offset == 0) {
                rs = s.executeQuery(sql);
            } else {
                rs = s.executeQuery(sqlWithPaging);
            }

            while (rs.next()) {
                vec.add(new ViewEntry(rs.getString(1), rs.getInt(2)));
            }
            conn.commit();
            s.close();
            rs.close();

        } catch (SQLException e) {
            DatabaseUtil.errorPrint(dbID, e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbPool.returnConnection(conn);
        }
        return vec;
    }
}
