package kz.flabs.webrule.constants;

import java.io.Serializable;

public enum FieldType implements Serializable {
	TEXT,
	DOCID,
	DATETIME,
	NUMBER,
	AUTHOR,
	TEXTLIST,
	BLOB,
	READER,
	EDITOR,
	FILE,
	VIEWTEXT,
	UNKNOWN,
	DATE,
	OBJECT,
	@Deprecated
	VECTOR,	
	CONSTANT,
	@Deprecated
	GLOSSARY,
	COMPLEX_OBJECT,
	@Deprecated
	USERID, 
	LIST,
    RICHTEXT,
    COORDINATION
	
}
