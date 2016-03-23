package kz.flabs.runtimeobj.document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import kz.flabs.dataengine.Const;
import kz.flabs.util.ListConvertor;
import kz.flabs.webrule.constants.FieldType;

public class Field implements Const, Serializable {

	private static final long serialVersionUID = 1L;

	public String name = "undefined";
	public String valueAsText;
	public Date valueAsDate;
	public BigDecimal valueAsNumber;
	public Enum valueAsEnum;
	@Deprecated
	public Set<String> valuesAsStringList = new HashSet<String>();
	@Deprecated
	public ArrayList<Integer> valuesAsGlossaryData = new ArrayList<Integer>();

	private FieldType type = FieldType.UNKNOWN;
	private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private static final SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy");

	public Field(String name) {
		this.name = name;
		valueAsText = "";
	}

	public Field(String name, String value) {
		if (name != null) {
			this.name = name;
			valueAsText = value;
			valuesAsStringList.add(value);
			this.type = FieldType.TEXT;
		}
	}

	public <T extends Number> Field(String name, T value) {
		this.name = name;
		valueAsNumber = new BigDecimal(value.toString());
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(0);
		df.setGroupingUsed(false);
		valueAsText = df.format(valueAsNumber);
		valuesAsStringList.add(valueAsText);
		valuesAsGlossaryData.add(value.intValue());
		this.type = FieldType.NUMBER;
	}

	public <T extends Number> Field(String name, T value, FieldType type) {
		this.name = name;
		valueAsNumber = new BigDecimal(value.toString());
		valueAsText = valueAsNumber.toPlainString();
		valuesAsGlossaryData.add(value.intValue());
		valuesAsStringList.add(valueAsText);
		this.type = type;
	}

	public Field(String name, Date value) {
		this.name = name;
		this.type = FieldType.DATETIME;
		try {
			valueAsText = dateTimeFormat.format(value);
			valueAsDate = value;
			valuesAsStringList.add(valueAsText);
		} catch (Exception e) {
			// AppEnv.logger.errorLogEntry("dt Unable  get or convert value to Date(value="
			// + value + ") from database (for "+name+")");
			// this.type = FieldType.UNKNOWN;
		}
	}

	public Field(String name, Enum value) {
		this.name = name;
		this.type = FieldType.CONSTANT;
		try {
			valueAsText = value.toString();
			valueAsEnum = value;
			valuesAsStringList.add(valueAsText);
		} catch (Exception e) {
			// AppEnv.logger.errorLogEntry("dt Unable  get or convert value to Date(value="
			// + value + ") from database (for "+name+")");
			this.type = FieldType.UNKNOWN;
		}
	}

	@Deprecated
	public Field(String name, ArrayList<String> list) {
		this.name = name;
		valuesAsStringList.addAll(list);
		this.type = FieldType.LIST;
		valueAsText = ListConvertor.listToString(valuesAsStringList);
	}

	public Field(String name, Collection<Integer> list, boolean fake) {
		this.name = name;
		valuesAsGlossaryData.addAll(list);
		this.type = FieldType.GLOSSARY;
		valueAsText = ListConvertor.listToString(valuesAsGlossaryData);
	}

	@Deprecated
	public Field(String name, String[] list) {
		this.name = name;
		for (String val : list) {
			valuesAsStringList.add(val);
		}

		this.type = FieldType.TEXTLIST;
		valueAsText = ListConvertor.listToString(valuesAsStringList);
	}

	public Field(String name, String value, FieldType type) {
		this.name = name;
		this.type = type;
		valueAsText = value;
		valuesAsStringList.add(valueAsText);

		switch (type) {
		case DATETIME:
			try {
				if (valueAsText.length() == 10) {
					valueAsDate = dateformat.parse(valueAsText);
				} else {
					valueAsDate = dateTimeFormat.parse(valueAsText);
				}
			} catch (Exception e) {
				this.type = FieldType.UNKNOWN;
			}
			break;
		case DATE:
			try {
				valueAsDate = dateformat.parse(valueAsText);
			} catch (Exception e) {
				this.type = FieldType.UNKNOWN;
			}
			break;
		case NUMBER:
			try {
				valueAsNumber = new BigDecimal(valueAsText);
				valuesAsGlossaryData.add(valueAsNumber.intValue());
			} catch (Exception e) {
				valueAsNumber = BigDecimal.valueOf(0);
				valuesAsGlossaryData.add(valueAsNumber.intValue());
				this.type = FieldType.UNKNOWN;
			}
			break;
		case TEXT:

			break;
		case RICHTEXT:
			this.type = FieldType.RICHTEXT;
			break;
		case GLOSSARY:
			try {
				valueAsNumber = new BigDecimal(valueAsText);
				valuesAsGlossaryData.add(valueAsNumber.intValue());
			} catch (Exception e) {
				valuesAsGlossaryData.add(0);
				valueAsNumber = BigDecimal.valueOf(0);
			}
			valueAsText = ListConvertor.listToString(valuesAsGlossaryData);
			break;
		default:

		}
	}

	public void setType(FieldType t) {
		type = t;
	}

	public FieldType getType() {
		return type;
	}

	public int getTypeAsDatabaseType() {
		switch (type) {
		case TEXT:
			return Const.TEXT;
		case LIST:
			return Const.TEXT;
		case COMPLEX_OBJECT:
			return Const.COMPLEX_OBJECT;
		case DATETIME:
			return Const.DATETIMES;
		case DATE:
			return Const.DATE;
		case NUMBER:
			return Const.NUMBERS;
		case AUTHOR:
			return Const.AUTHORS;
		case TEXTLIST:
			return Const.TEXTLIST;
		case VECTOR:
			return Const.TEXTLIST;
		case READER:
			return Const.READERS;
		case FILE:
			return Const.FILES;
		case GLOSSARY:
			return Const.GLOSSARY;
		case RICHTEXT:
			return Const.RICHTEXT;
		case COORDINATION:
			return Const.COORDINATION;
		}
		return UNKNOWN;
	}

	public void addValue(String value) {
		if (value != null && (!value.equalsIgnoreCase(""))) {
			Collection<String> col = ListConvertor.stringToList(valueAsText);
			col.add(value);
			type = FieldType.LIST;
			valueAsText = ListConvertor.listToString(col);
		}
	}

	@Override
	public String toString() {
		return "[name=" + name + ", value=" + valueAsText + ", type=" + type + "]";
	}

}