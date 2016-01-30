package kz.flabs.dataengine.mssql.structure;

import kz.flabs.dataengine.*;
import kz.flabs.dataengine.mssql.queryformula.StructSelectFormula;
import kz.flabs.parser.FormulaBlocks;


public class Structure extends kz.flabs.dataengine.h2.structure.Structure implements IStructure, Const{	

	public Structure(IDatabase db, IDBConnectionPool structDbPool) {	
		super(db, structDbPool);
	}

    @Override
    public ISelectFormula getSelectFormula(FormulaBlocks blocks) {
        ISelectFormula sf = new StructSelectFormula(blocks);
        return sf;
    }

}
