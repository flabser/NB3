package kz.lof.webserver.valve;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RequestURL {
    private String appType = "";
    private String url;
    private String pageID = "";

    public RequestURL(String url) {
        this.url = url;
        String urlVal = url != null ? url.trim() : "";
        Pattern pattern = Pattern.compile("^/(\\p{Alpha}+)(/[\\p{Lower}0-9]{16})?.*$");
        Matcher matcher = pattern.matcher(urlVal);
        if (matcher.matches()) {
            appType = matcher.group(1) == null ? "" : matcher.group(1);
        }

        if (!isPage()) {
            return;
        }

        for (String pageIdRegex : new String[]{"^.*/page/([\\w\\-~\\.]+)", "^.*/Provider.*?[\\?{1}|&{1}]id=([\\w\\-~\\.]+)[\\w\\-~\\.=&]*"}) {
            if (urlVal.matches(pageIdRegex)) {
                pageID = urlVal.replaceAll(pageIdRegex, "$1");
                break;
            }
        }
    }

    public String getAppType() {
        return appType;
    }

    public boolean isDefault() {
        return url.matches("/" + appType + "(/(Provider)?)?/?") || url.trim().isEmpty();
    }

    public boolean isAuthRequest() {
        String ulc = url.toLowerCase();
        return ulc.contains("login") || ulc.contains("logout");
    }

    public boolean isPage() {
        // return url.matches(".*/Provider\\?(\\w+=\\w+)(&\\w+=\\w+)*") || url.matches(".*/page/[\\w\\.]+");
        return url.trim().isEmpty() || url.matches(".*/Provider.*") || url.matches("/" + appType + "/*");
    }

    public String getPageID() {
        return pageID;
    }

    public String getUrl() {
        return url;
    }

    public boolean isProtected() {
        return !(url.startsWith("/SharedResources") || url.startsWith("/Workspace") || isSimpleObject());
    }

    private boolean isSimpleObject() {
        return url.matches(".+\\.((css)|(js)|(htm)|(html)|(png)|(jpg)|(gif)|(bmp))$");
    }

    public void setAppType(String templateType) {
        appType = templateType;
    }

    @Override
    public String toString() {
        return url;
    }

    public static void main(String[] args) {

        // ломай меня полностью )
        String pageId = "user-form_page_form-user";
        String urls[] = {"/Administrator/Provider?id=" + pageId,
                "/Administrator/Provider?&id=" + pageId,
                "/Administrator/Provider?id=" + pageId + "&",
                "/Administrator/Provider?&id=" + pageId,
                "/Administrator/Provider?id=" + pageId + "&docid=1",
                "/Administrator/Provider?&id=" + pageId + "&docid=1",
                "/Administrator/Provider?type=1&id=" + pageId + "&docid=1",
                "/Administrator/Provider?&type=1&id=" + pageId + "&docid=1",
                "/Administrator/Provider?&type=1&docid=2&id=" + pageId + "&docid=1",
                "/Administrator/Provider?2&id=" + pageId + "&1",
                "/Administrator/Provider?&2&id=" + pageId + "&1",
                "/Administrator/Provider?&&&2&677&pageid=user&id=" + pageId + "&1",
                "/Administrator/Provider?&&&2&677&page_id=user&id=" + pageId + "&1",
                "/Administrator/Provider?&&&2&677&page-id=user&id=" + pageId + "&1"};
        boolean hasError = false;

        for (String url : urls) {
            RequestURL r = new RequestURL(url);

            if (!r.getPageID().equals(pageId)) {
                hasError = true;
                System.err.println("Сломали:( " + r.getAppType() + ", " + r.getPageID() + ", " + r.getUrl());
            }
        }

        if (!hasError) {
            System.out.println("Не смогли сломать!");
        }
    }
}
