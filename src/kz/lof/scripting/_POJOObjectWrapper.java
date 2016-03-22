package kz.lof.scripting;

import kz.lof.scriptprocessor.page.IOutcomeObject;


public class _POJOObjectWrapper implements IOutcomeObject {
    private IPOJOObject object;
    private _Session ses;

    public _POJOObjectWrapper(IPOJOObject object, _Session ses) {
        this.object = object;
        this.ses = ses;
    }

    @Override
    public String toXML() {
        String result;
        if (object.getClass().getSimpleName().equals("_EnumWrapper")) {
            result = object.getFullXMLChunk(ses);
        } else {
            result = "<document entity=\"" + object.getClass().getSimpleName().toLowerCase() + "\"  docid=\"" + object.getIdentifier() + "\" editable=\""
                    + object.isEditable() + "\"><fields>" + object.getFullXMLChunk(ses) + "</fields></document>";
        }
        return result;
    }

    @Override
    public Object toJSON() {
        return object.getJSONObj(ses);
    }
}
