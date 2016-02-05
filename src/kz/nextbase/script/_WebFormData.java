package kz.nextbase.script;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import kz.flabs.dataengine.Const;
import kz.flabs.util.Util;

public class _WebFormData {
	private Map<String, String[]> formData;
	private String sign = "pochemu pustoi? podumai!";

	public _WebFormData(Map<String, String[]> formData) {
		this.formData = formData;
	}

	public _WebFormData() {

	}

	@Deprecated
	public String[] get(String fn) throws _Exception {
		String value[] = formData.get(fn);
		if (value != null) {
			return value;
		} else {
			throw new _Exception(_ExceptionType.FORMDATA_INCORRECT, "field=" + fn);
		}

	}

	public String[] getListOfValuesSilently(String fn) {
		String value[] = formData.get(fn);
		if (value != null) {
			return value;
		} else {
			String val[] = { "" };
			return val;
		}

	}

	public String getValueSilently(String fn) {
		try {
			String value[] = formData.get(fn);
			if (value[0].equals("null")) {
				return "";
			} else {
				Object r = value[0].trim();
				return (String) r;
			}
		} catch (Exception e) {
			return "";
		}
	}

	public String getValueSilently(String fn, String defaultValue) {
		try {
			String value[] = formData.get(fn);
			return value[0].trim();
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public String getEncodedValueSilently(String fn) {
		try {
			return new String(getValueSilently(fn).getBytes("ISO-8859-1"), "UTF-8");
		} catch (Exception e) {
			return "";
		}
	}

	public int getNumberValueSilently(String fn, int defaultValue) {
		try {
			String value[] = formData.get(fn);
			return Integer.parseInt(value[0].trim());
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public Integer[] getNumberValuesSilently(String fn, int defaultValue) {
		String value[] = formData.get(fn);
		Integer[] nValue = new Integer[value.length];
		for (int i = 0; i < value.length; i++) {
			try {
				nValue[i] = Integer.parseInt(value[i].trim());
			} catch (Exception e) {
				nValue[i] = defaultValue;
			}
		}
		return nValue;
	}

	public double getNumberDoubleValueSilently(String fn, double defaultValue) {
		try {
			String value[] = formData.get(fn);
			return Double.parseDouble(value[0].trim());
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public String[] getListOfValues(String fn) throws _Exception {
		String value[] = formData.get(fn);
		if (value != null) {
			return value;
		} else {
			throw new _Exception(_ExceptionType.FORMDATA_INCORRECT, "value of field=" + fn + " has not resolved");
		}

	}

	// TODO need to replace _Exception to WebFormValueExceptionType
	public String getValue(String fn) throws _Exception {
		try {
			String value[] = formData.get(fn);
			return value[0].trim();
		} catch (Exception e) {
			throw new _Exception(_ExceptionType.FORMDATA_INCORRECT, "value of field=" + fn + " has not resolved");
		}
	}

	public Map<String, String[]> getFormData() {
		return formData;
	}

	public boolean containsField(String key) {
		return formData.containsKey(key);
	}

	public int getSizeOfField(String fn) {
		try {
			String value[] = formData.get(fn);
			return value.length;
		} catch (Exception e) {
			return 0;
		}
	}

	@Deprecated
	public int[] getParentDocID() throws _Exception {
		int[] prop = new int[2];
		try {
			prop[0] = Integer.parseInt(getValue("parentdocid"));
		} catch (Exception nfe) {
			prop[0] = 0;
		}
		try {
			prop[1] = Integer.parseInt(getValue("parentdoctype"));
		} catch (Exception nfe) {
			prop[1] = Const.DOCTYPE_UNKNOWN;
		}
		return prop;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getSign() throws _Exception {
		try {
			String value[] = formData.get("srctext");
			return value[0].trim();
		} catch (Exception e) {
			throw new _Exception(_ExceptionType.FORMDATA_INCORRECT, "value of field= sign has not resolved");
		}
	}

	public String getAppletType() throws _Exception {
		try {
			String value[] = formData.get("applettype");
			System.out.println("value: " + value[0].trim());
			return value[0].trim();
		} catch (Exception e) {
			throw new _Exception(_ExceptionType.FORMDATA_INCORRECT, "value of field= applettype has not resolved");
		}
	}

	public Date getDateSilently(String fn) {
		try {
			String value[] = formData.get(fn);
			return Util.convertStringToSimpleDate(value[0].trim());
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String toString() {
		String result = "-----------begin of list of web form data-----------\n";

		Iterator<String> en = formData.keySet().iterator();

		while (en.hasNext()) {
			String webFormFieldName = en.next();
			String[] val = formData.get(webFormFieldName);
			String v = "";
			for (int i = 0; i < val.length; i++) {
				v += val[i] + "[" + Integer.toString(i) + "],";
			}
			result += webFormFieldName + "=" + v + "\n";
		}

		result += "----------------- end of list-----------------------";
		return result;

	}

}
