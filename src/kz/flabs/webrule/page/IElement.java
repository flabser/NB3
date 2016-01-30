package kz.flabs.webrule.page;

import java.util.ArrayList;

import kz.flabs.appenv.AppEnv;
import kz.flabs.webrule.Caption;

public interface IElement {
	String getID();
	AppEnv getAppEnv();
	String getScriptDirPath();
	ArrayList<Caption> getCaptions();

}
