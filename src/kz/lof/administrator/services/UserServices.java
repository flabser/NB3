package kz.lof.administrator.services;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.ISystemDatabase;
import kz.lof.administrator.dao.ApplicationDAO;
import kz.lof.administrator.dao.UserDAO;
import kz.lof.administrator.model.Application;
import kz.lof.administrator.model.User;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.jpa.ViewPage;
import kz.lof.dataengine.system.IEmployee;
import kz.lof.dataengine.system.IEmployeeDAO;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.exception.SecureException;
import kz.lof.scripting._Session;
import kz.lof.server.Server;
import kz.lof.user.AnonymousUser;
import kz.lof.user.IUser;
import kz.lof.util.StringUtil;
import reference.dao.PositionDAO;
import staff.dao.EmployeeDAO;
import staff.dao.OrganizationDAO;
import staff.dao.OrganizationLabelDAO;
import staff.dao.RoleDAO;
import staff.model.Employee;
import staff.model.Organization;
import staff.model.OrganizationLabel;
import staff.model.Role;

public class UserServices {
    private AppEnv env = Environment.getAppEnv(EnvConst.ADMINISTRATOR_APP_NAME);

    @Deprecated
    public void importFromH2() {
        importFromH2(true);
    }

    @SuppressWarnings("deprecation")
    public void importFromH2(boolean showConsoleOutput) {
        List<User> entities = new ArrayList<User>();

        ISystemDatabase sysDb = null;
        try {
            sysDb = new kz.flabs.dataengine.h2.SystemDatabase();

            List<kz.flabs.users.User> users = sysDb.getAllUsers("", 0, 10000);
            int rCount = users.size();
            if (showConsoleOutput) {
                System.out.println("System users count = " + rCount);
            }
            ApplicationDAO aDao = new ApplicationDAO(new _Session(env, new AnonymousUser()));
            List<Application> appList = new ArrayList<Application>();
            appList.add(aDao.findByName("MunicipalProperty"));
            appList.add(aDao.findByName("Accountant"));
            appList.add(aDao.findByName("PropertyLeasing"));
            appList.add(aDao.findByName("Registry"));

            for (kz.flabs.users.User oldUser : users) {
                User entity = new User();
                entity.setLogin(oldUser.getLogin());
                entity.setPwd(oldUser.getPassword());
                entity.setPwdHash(oldUser.getPasswordHash());
                entity.setAllowedApps(appList);
                entities.add(entity);
            }

            UserDAO uDao = new UserDAO();
            for (User user : entities) {
                uDao.add(user);
            }

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | DatabasePoolException e) {
            Server.logger.errorLogEntry(e);
        }

    }

    public IUser<Long> getUser(String login, String pwd) {

        UserDAO uDao = new UserDAO();
        IUser<Long> user = uDao.findByLogin(login);

        if (user != null) {
            String pwdHash = StringUtil.encode(pwd);
            if (user.getPwdHash() != null && user.getPwdHash().equals(pwdHash)) {
                user.setAuthorized(true);
            } else {
                Server.logger.errorLogEntry("password has not been encoded");
            }

            if (user.isAuthorized()) {
                IEmployeeDAO eDao = null;
                try {
                    Class<?> clazz = Class.forName(EnvConst.STAFF_DAO_CLASS);
                    Class[] args = new Class[]{_Session.class};
                    Constructor<?> contructor = clazz.getConstructor(args);
                    _Session ses = new _Session(env, new AnonymousUser());
                    eDao = (IEmployeeDAO) contructor.newInstance(new Object[]{ses});
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | ClassNotFoundException | NoSuchMethodException | SecurityException e) {
                    Server.logger.errorLogEntry(e);
                }

                if (eDao != null) {
                    IEmployee emp = eDao.getEmployee(user.getId());
                    if (emp != null) {
                        user.setUserName(emp.getName());
                    } else {
                        user.setUserName(user.getLogin());
                    }
                }
            }

        } else {
            Server.logger.warningLogEntry("\"" + login + "\" user not found");
        }

        return user;

    }

    public void importFromOldStructure() {
        importFromOldStructure(true);
    }

    public void importFromOldStructure(boolean showConsoleOutput) {
        Connection conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", EnvConst.STRUCTDB_USER);
        connectionProps.put("password", EnvConst.STRUCTDB_PWD);

        try {
            conn = DriverManager.getConnection(EnvConst.STRUCTDB_URL, connectionProps);
            conn.setAutoCommit(false);
            Statement st = conn.createStatement();
            PreparedStatement pst;
            //ResultSet rs = st.executeQuery("select * from employers");
            //ResultSet rs = st.executeQuery("select * from employers where obl in (select docid from maindocs)");
            ResultSet rs = st.executeQuery("select * from employers");
            conn.commit();
            EmployeeDAO dao = new EmployeeDAO(new _Session(env, new AnonymousUser()));
            OrganizationDAO orgDAO = new OrganizationDAO(new _Session(env, new AnonymousUser()));
            ApplicationDAO aDao = new ApplicationDAO(new _Session(env, new AnonymousUser()));
            RoleDAO roleDAO = new RoleDAO(new _Session(env, new AnonymousUser()));
            PositionDAO posDAO = new PositionDAO(new _Session(env, new AnonymousUser()));
            OrganizationLabelDAO orgLabDAO = new OrganizationLabelDAO(new _Session(env, new AnonymousUser()));
            UserDAO uDao = new UserDAO();
            Employee entity;
            while (rs.next()) {
                /*----------------------------------------*/
                String login = rs.getString("userid");
                User user = (User) uDao.findByLogin(login);
                if (user != null) {
                    entity = dao.findByUserId(user.getId());
                    if (entity == null) {
                        if (dao.findAllByName(rs.getString("fullname"), 0, 50).getCount() == 0) {
                            entity = new Employee();
                            entity.setName(rs.getString("fullname"));

                            PreparedStatement ps_orgs = conn.prepareStatement("select * from maindocs where viewtext = ?");
                            ps_orgs.setString(1, rs.getString("shortname"));
                            ResultSet rs_orgs = ps_orgs.executeQuery();
                            if (rs_orgs.next()) {
                                PreparedStatement ps_cf = conn.prepareStatement("select value from custom_fields where docid = ? and value <> '' and name = 'bin'");
                                ps_cf.setInt(1, rs_orgs.getInt("docid"));
                                ResultSet rs_cf = ps_cf.executeQuery();
                                if (rs_cf.next()) {
                                    ViewPage<Organization> orgPage =  orgDAO.findAllequal("name", rs_orgs.getString("viewtext"), 0, 100);
                                    if (orgPage != null && orgPage.getCount() > 0) {
                                        entity.setOrganization(orgPage.getResult().get(0));
                                    } else {
                                        Organization newOrg = new Organization();
                                        newOrg.setName(rs_orgs.getString("viewtext"));
                                        newOrg.setRegDate(new java.util.Date());
                                        //newOrg.setOrgCategory(new OrgCategory());
                                        newOrg.setLabels(new ArrayList<>());
                                        newOrg.setRank(999);
                                        String bin = rs_cf.getString("value").replaceAll(" ", "");
                                        if (bin.length() > 12) {
                                            bin = bin.substring(0, 11);
                                        }
                                        newOrg.setBin(bin);
                                        newOrg.setForm("organization-form");

                                        OrganizationLabel orgCategories = orgLabDAO.findByName("balance_holder");
                                        if (orgCategories != null) {
                                            ArrayList<OrganizationLabel> labels = new ArrayList<>();
                                            labels.add(orgCategories);
                                            newOrg.setLabels(labels);
                                        }

                                        try {
                                            orgDAO.add(newOrg);
                                            entity.setOrganization(newOrg);
                                        } catch (SecureException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            //entity.setPosition(posDAO.findById(UUID.fromString(formData.getValue("position"))));
                            pst = conn.prepareStatement("select * from user_roles where empid = ? and type = 889");
                            pst.setInt(1, rs.getInt("empid"));
                            ResultSet rs_roles = pst.executeQuery();
                            conn.commit();
                            List<Role> roleList = new ArrayList<>();
                            while (rs_roles.next()) {
                                if (rs_roles.getString("name").equalsIgnoreCase("administrator")) {
                                    if (rs_roles.getString("appid").equalsIgnoreCase("Glossary")) {
                                        List<Role> role = roleDAO.findAllequal("name", "reference_admin", 0, 50).getResult();
                                        roleList.addAll(role);
                                    }
                                    if (rs_roles.getString("appid").equalsIgnoreCase("Structure")) {
                                        List<Role> role = roleDAO.findAllequal("name", "staff_admin", 0, 50).getResult();
                                        roleList.addAll(role);
                                    }
                                }
                            }

                            if (!roleList.isEmpty()) {
                                entity.setRoles(roleList);
                            }

                            List<Application> appList = new ArrayList<Application>();
                            appList.add(aDao.findByName("MunicipalProperty"));
                            appList.add(aDao.findByName("Accountant"));
                            appList.add(aDao.findByName("PropertyLeasing"));
                            appList.add(aDao.findByName("Registry"));
                            user.setAllowedApps(appList);
                            entity.setUser(user);

                            dao.add(entity);
                        }
                    }
                }
                /*----------------------------------------*/
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
