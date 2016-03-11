package kz.lof.dataengine.jpadatabase.ftengine;

import java.util.List;

import kz.lof.dataengine.jpa.IDAO;

public class FTEntity {
	private String tableName;
	private List<String> fieldNames;
	private Class<? extends IDAO> daoImpl;

	@SuppressWarnings("unchecked")
	public FTEntity(String tableName, List<String> fieldNames, String daoImpl) {
		this.tableName = tableName;
		this.fieldNames = fieldNames;
		try {
			this.daoImpl = (Class<? extends IDAO>) Class.forName(daoImpl);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	public Class<? extends IDAO> getDaoImpl() {
		return daoImpl;
	}

	public String getTableName() {
		return tableName;
	}

	public List<String> getFieldNames() {
		return fieldNames;
	}

}
