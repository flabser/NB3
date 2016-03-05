package kz.lof.rule.page;

import java.util.ArrayList;

import kz.flabs.webrule.Caption;
import kz.lof.appenv.AppEnv;

public interface IElement {
	String getID();
	AppEnv getAppEnv();
	String getScriptDirPath();
	ArrayList<Caption> getCaptions();

}
