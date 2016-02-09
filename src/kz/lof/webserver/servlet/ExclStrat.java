package kz.lof.webserver.servlet;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ExclStrat implements ExclusionStrategy {

	@Override
	public boolean shouldSkipClass(Class<?> arg0) {
		return false;
	}

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		return (!f.getName().equals("name")) && !(f.getName().equals("id"));
	}

}
