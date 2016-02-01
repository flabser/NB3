package kz.flabs.appdaemon;

import kz.flabs.dataengine.Const;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.pchelka.scheduler.AbstractDaemon;
import kz.pchelka.scheduler.IProcessInitiator;

public class RemoveAttachments extends AbstractDaemon implements Const {

	@Override
	public int process(IProcessInitiator processOwner) throws DocumentAccessException, RuleException, QueryFormulaParserException, QueryException {
		// TODO Auto-generated method stub
		return 0;
	}

}
