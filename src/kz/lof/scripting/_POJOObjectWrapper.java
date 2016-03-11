package kz.lof.scripting;

import kz.lof.webserver.servlet.IOutcomeObject;


public class _POJOObjectWrapper implements IOutcomeObject {
    private IPOJOObject object;
    private _Session ses;

    public _POJOObjectWrapper(IPOJOObject object, _Session ses) {
        this.object = object;
        this.ses = ses;
    }

    @Override
    public String toXML() {
        String entity = "";
        if (!object.getClass().getSimpleName().equals("_EnumWrapper")) {
            entity = object.getClass().getSimpleName().toLowerCase();
        }
        return "<document entity=\"" + entity + "\"  docid=\"" + object.getIdentifier() + "\" editable=\""
                + object.isEditable() + "\"><fields>" + object.getFullXMLChunk(ses) + "</fields></document>";
    }

    @Override
    public Object toJSON() {
        return object.getJSONObj(ses);
    }
}
