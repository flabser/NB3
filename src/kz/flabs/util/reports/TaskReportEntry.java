package kz.flabs.util.reports;

import java.util.Date;

public class TaskReportEntry {

    private String author;
    private Date regdate;
    private String briefcontent;
    private String controltype;
    private Date ctrldate;
    private int duedatediff;

    public TaskReportEntry() {
    }

    //@NotNull(message="Имя должно быть задано")
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getRegdate() {
        return regdate;
    }

    public void setRegdate(Date regdate) {
        this.regdate = regdate;
    }

    public String getBriefcontent() {
        return briefcontent;
    }

    public void setBriefcontent(String briefcontent) {
        this.briefcontent = briefcontent;
    }

    public String getControltype() {
        return controltype;
    }

    public void setControltype(String controltype) {
        this.controltype = controltype;
    }

    public Date getCtrldate() {
        return ctrldate;
    }

    public void setCtrldate(Date ctrldate) {
        this.ctrldate = ctrldate;
    }

    public int getDuedatediff() {
        return duedatediff;
    }

    public void setDuedatediff(int duedatediff) {
        this.duedatediff = duedatediff;
    }

}
