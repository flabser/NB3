package kz.flabs.servlets.eds;

import kz.flabs.appenv.AppEnv;
import kz.flabs.exception.PortalException;
import kz.flabs.servlets.ProviderExceptionType;
import kz.flabs.servlets.ProviderOutput;
import kz.flabs.servlets.ProviderResult;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.users.AuthFailedException;
import kz.flabs.users.UserException;
import kz.flabs.users.UserSession;
import kz.flabs.util.ResponseType;
import kz.flabs.util.Util;
import kz.flabs.util.XMLResponse;
import kz.flabs.webrule.eds.EDSSetting;
import kz.iola.jce.provider.IolaProvider;
import kz.iola.ocsp.*;
import kz.iola.util.encoders.Base64;
import kz.pchelka.server.Server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

public class SignProvider extends HttpServlet {

    private static final long serialVersionUID = 8746005561222051697L;
    private AppEnv env;
    private ServletContext context;

    public void init(ServletConfig config) throws ServletException {
        try {
            context = config.getServletContext();
            env = (AppEnv) context.getAttribute("portalenv");
        } catch (Exception e) {
            Server.logger.errorLogEntry(e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        String type = request.getParameter("type");
        String id = request.getParameter("id");
        ProviderResult result = null;
        UserSession userSession = null;
        HttpSession jses = null;

        try {
            jses = request.getSession(false);
            if (jses == null) {
                jses = request.getSession(true);
                userSession = new UserSession(context, request, response, jses);
                jses.setAttribute("usersession", userSession);
            } else {
                userSession = (UserSession) jses.getAttribute("usersession");
                if (userSession == null) {
                    userSession = new UserSession(context, request, response, jses);
                    jses.setAttribute("usersession", userSession);
                }
            }

            if (type.equalsIgnoreCase("service")) {
                result = new ProviderResult();
                String operation = request.getParameter("operation");

                if (operation.equalsIgnoreCase("check_cert")) {
                    EDSSetting es = env.ruleProvider.global.edsSettings;
                    XMLResponse xmlResp = new XMLResponse(ResponseType.RESULT_OF_SIGN_PROVIDER_SERVICE);

                    String cacert_str = "-----BEGIN CERTIFICATE-----\n" +
                            "MIIH+DCCBeCgAwIBAgIUKBJg8TKQqul9nZaHuznDUHy6NvQwDQYJKoZIhvcNAQEF\n" +
                            "BQAwggEPMRowGAYDVQQDDBHQndCj0KYg0KDQmiAoUlNBKTFDMEEGA1UECww60JjQ\n" +
                            "vdGE0YDQsNGB0YLRgNGD0LrRgtGD0YDQsCDQvtGC0LrRgNGL0YLRi9GFINC60LvR\n" +
                            "jtGH0LXQuTFxMG8GA1UECgxo0J3QsNGG0LjQvtC90LDQu9GM0L3Ri9C5INGD0LTQ\n" +
                            "vtGB0YLQvtCy0LXRgNGP0Y7RidC40Lkg0YbQtdC90YLRgCDQoNC10YHQv9GD0LHQ\n" +
                            "u9C40LrQuCDQmtCw0LfQsNGF0YHRgtCw0L0xFTATBgNVBAcMDNCQ0YHRgtCw0L3Q\n" +
                            "sDEVMBMGA1UECAwM0JDRgdGC0LDQvdCwMQswCQYDVQQGEwJLWjAeFw0xMTEyMjgw\n" +
                            "NTEyMjhaFw0xNjEyMjgwNTEyMjhaMIIBDzEaMBgGA1UEAwwR0J3Qo9CmINCg0Jog\n" +
                            "KFJTQSkxQzBBBgNVBAsMOtCY0L3RhNGA0LDRgdGC0YDRg9C60YLRg9GA0LAg0L7R\n" +
                            "gtC60YDRi9GC0YvRhSDQutC70Y7Rh9C10LkxcTBvBgNVBAoMaNCd0LDRhtC40L7Q\n" +
                            "vdCw0LvRjNC90YvQuSDRg9C00L7RgdGC0L7QstC10YDRj9GO0YnQuNC5INGG0LXQ\n" +
                            "vdGC0YAg0KDQtdGB0L/Rg9Cx0LvQuNC60Lgg0JrQsNC30LDRhdGB0YLQsNC9MRUw\n" +
                            "EwYDVQQHDAzQkNGB0YLQsNC90LAxFTATBgNVBAgMDNCQ0YHRgtCw0L3QsDELMAkG\n" +
                            "A1UEBhMCS1owggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCpMpV5dMoe\n" +
                            "hVNX5SAUopYKOMEU4OpEiasNXdP5zZDcdDAe5j5uEOklxwAH+QEamzm+0Hf0CnyM\n" +
                            "6jK+/88d3EeKM/0Lx9XTFl1xBMyavAPvVJEOPAbZFjYC4R2Iy6h4Is+RMgqtY4wF\n" +
                            "OYoeMqYO6YZfOPDB9qtQ4hzMmkhznABQQ7k2UuwYABxIgY+VFhpEyrOGWE+WEhWJ\n" +
                            "R3qxd4uUB9g77PubpvAf4ug3cU5ADjFRR4q5kwrnvC3gjmaXXhtJPreEMIYcVgLj\n" +
                            "9zDmIXhXr19c/yUq9tARXvFFeMPFhvmD8pBp+lzeaW6uRlfC/KK2yn0Po2KuwVGf\n" +
                            "tnxVmU2ZUjAfgH5c4pYAzruCzPECjWgNP1jr/acE7fxd6E+M8XcIkd6a3bKbevHe\n" +
                            "HJE79abSy7mDma89sVOxwMij350vhofponFmd6v/Lk58Q7yVb25CAHTrKWFAcdfg\n" +
                            "8IVHrksQLS3MVuePolp1cUTHReuHLnH9AaDd/5LJhlbctssc5sFp0QQWLDcz33FS\n" +
                            "6DCO+tKJKdin3BqZ/xqG/D7Xa76CoRD+wDOdNgPHXQTFz6XkHgzaGeM9gK1ViQhj\n" +
                            "zpR4bNtRWH9jDzGDQd2wigR4tIRYuu3+yDjU8LZGSOjZS0fIHDFAviK3q+FbAwum\n" +
                            "s+VJqA10LKeMyWKBZO9POyF5/226oJJBtQIDAQABo4IBRjCCAUIwEgYDVR0TAQH/\n" +
                            "BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAIYwggEOBgNVHSAEggEFMIIBATCB/gYH\n" +
                            "KoMOAwMBATCB8jAwBggrBgEFBQcCARYkaHR0cDovL3BraS5nb3Yua3ovaW5mby9j\n" +
                            "YV9wb2xpY3kucGRmMIG9BggrBgEFBQcCAjCBsBqBrdHl8PLo9Ojq4PIg7/Dl5O3g\n" +
                            "5+3g9+XtIOTr/yDi++/z8ergIPDl4+jx8vDg9uju7e379SDx4ujk5fLl6/zx8uIg\n" +
                            "9Ojn6Pfl8ero9SDoICD+8Ojk6Pfl8ero9SDr6PYsIOAg8uDq5uUg8OXj6PHy8OD2\n" +
                            "6O7t7fv1IPHi6OTl8uXr/PHy4iDy5fXt6Pfl8ero9SDx8OXk8fLiIOgg4uXhLfHl\n" +
                            "8OLl8O7iIChTU0wpMAoGA1UdDgQDBAECMA0GCSqGSIb3DQEBBQUAA4ICAQB6C/NK\n" +
                            "hMdE3lOIUq1R934k1OwUHrljggxPSQL+dqJ2pwCW9wdSsd/9+bZujgrS/AZ4Bj2Q\n" +
                            "NDJVL9tduuKwEjlp4GnVoRz86QtxErGo+ZY0kIlGCAS4UQ3qykUe26I9SA4Crqnu\n" +
                            "jbC/ooSML/pWi5FVL8wLD1vaJVHDGu34J3F3BPdiTnrGMBWsOJWIluk1lu6M6esW\n" +
                            "OlnJEl4bj08BdBNX2rfQwYv9my9oF74BUVSKn3yr6ibu7bGuawADXhk5DaFmRdtX\n" +
                            "uTLg3G26PYvelEK6rb+Ri70Z1Y5a4y15dOb7hl2AHG1ij/isqqqUm1Nt3/+ecEPp\n" +
                            "FFWc3eqyL5Cn1PxXUWM7TCjKAiWX4JI1Q9hMi/4UR3zTdEomE3XtJFOMgUIkEpQG\n" +
                            "U/kCQ7Fxmckh3ZRVgd8SD2un9svL9gHGjjEve6+KnrOUw/vIvqhm0aoX4zWla4GY\n" +
                            "LzoG6AtVTK2+S5Oyn5zp4jq6RMvsfbOHu3V6LYLkgkigFR/jDvQxISO07uDjVm6s\n" +
                            "dRnG5HWnFpVp5YQ6PGb7rpZOPuElgPKevcuM82JExAyKZN5DjY6Hll2QbEkVrIzF\n" +
                            "lTmdCHx2sTXoxvxd/ZzIhCsUl/3hQNwn9k9qnvx1zVRCRswdQSYNjLmDC9ofXsG0\n" +
                            "HNn/2DseWlKXSM5F4sGy2RmKqZMvhzFbtaIF6Q==\n" +
                            "-----END CERTIFICATE-----";
                    Security.addProvider(new IolaProvider());
                    ByteArrayInputStream stream = new ByteArrayInputStream(cacert_str.getBytes(Charset.forName("UTF-8")));
                    CertificateFactory cf = CertificateFactory.getInstance("X509", new IolaProvider());
                    X509Certificate cacert = (X509Certificate) cf.generateCertificate(stream);

               /*     String cert_str = "-----BEGIN CERTIFICATE-----";
                    cert_str += Base64.derequest.getParameter("cert").replaceAll(" ", "+");
                    cert_str += "-----END CERTIFICATE-----";*/


                    byte[] decoded = Base64.decode(request.getParameter("cert").replaceAll(" ", "+"));

                    stream = new ByteArrayInputStream(decoded);
                    cf = CertificateFactory.getInstance("X509", new IolaProvider());
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);
                    stream.close();
                    String expired = "";
                    String revoked = "";
                    String unknown = "";
                    boolean has_error = false;
                    if (cert.getNotAfter().before(new Date())) {
                        expired = "Срок действия сертификата истёк: " + Util.convertDataTimeToString(cert.getNotAfter());
                        has_error = true;
                    }

                    URL url = new URL(es.ocsp_url);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoOutput(true);
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/ocsp-request");
                    OutputStream os = con.getOutputStream();


                    OCSPReqGenerator gen = new OCSPReqGenerator();
                    CertificateID certId = new CertificateID(CertificateID.HASH_SHA1, (X509Certificate) cacert, cert.getSerialNumber());
                    gen.addRequest(certId);
                    OCSPReq req;
                    req = gen.generate();

                    os.write(req.getEncoded());

                    InputStream in = con.getInputStream();
                    OCSPResp ocsp_response = new OCSPResp(in);
                    in.close();

                    if (ocsp_response.getStatus() != 0) {
                        throw new OCSPException("Unsuccessful request. Status: " + ocsp_response.getStatus());
                    }
                    BasicOCSPResp brep = (BasicOCSPResp) ocsp_response.getResponseObject();

                    SingleResp[] singleResps = brep.getResponses();
                    Object status = singleResps[0].getCertStatus();

                    if (status == null) {
                        //xmlResp.addMessage("good", "ocsp_resp");
                    }
                    if (status instanceof RevokedStatus) {
                        revoked = "Сертификат был отозван: ";
                        revoked += Util.convertDataTimeToString(((RevokedStatus) status).getRevocationTime());
                        if (((RevokedStatus) status).hasRevocationReason()) {
                            revoked += " " + ((RevokedStatus) status).getRevocationReason();
                            has_error = true;
                        }                    }
                    if (status instanceof UnknownStatus) {
                        unknown = "Статус неизвестен";
                        has_error = true;
                    }

                    con.disconnect();
                    os.close();

                    if (has_error) {
                       result.output.append("<eds_stat stat=\"error\">" + expired + "\n" + revoked + "\n" + unknown + "</eds_stat>");
                    } else {
                       result.output.append("<eds_stat stat=\"ok\"/>");
                    }


                }
            }

            if (result.publishAs == PublishAsType.XML) {
                response.setContentType("text/xml;charset=utf-8");
                ProviderOutput po = new ProviderOutput(type, id, result.output, request, response, userSession, jses, "", false);
                String outputContent = po.getStandartUTF8Output();
                PrintWriter out = response.getWriter();
                out.println(outputContent);
                out.close();
            }
        } catch (AuthFailedException e) {
            new PortalException(e, env, response, ProviderExceptionType.AUTHFAILED);
        } catch (UserException e) {
            new PortalException(e, env, response, ProviderExceptionType.INTERNAL);
        } catch (Exception e) {
            new PortalException(e, env, response, ProviderExceptionType.INTERNAL);
        }

    }
}






