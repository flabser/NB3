package reference.navigator;

import kz.flabs.localization.LanguageType;
import kz.nextbase.script.*;
import kz.nextbase.script.events._DoPage;
import kz.nextbase.script.outline._Outline;
import kz.nextbase.script.outline._OutlineEntry;

import java.util.ArrayList;
import java.util.List;


public class MainNavigator extends _DoPage {

    @Override
    public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
        List<_IXMLContent> list = new ArrayList<>();

        _Tag currentTag = new _Tag("current");
        currentTag.setAttr("id", formData.getValueSilently("id").replace("-form", "-view"));

        _Outline common_outline = new _Outline(getLocalizedWord("common_reference_data", lang), "common");

        // if(cuser.hasRole(["struct_keeper", "supervisor"])) {
        common_outline.addEntry(new _OutlineEntry(getLocalizedWord("countries", lang), "country-view"));
        common_outline.addEntry(new _OutlineEntry(getLocalizedWord("regions", lang), "region-view"));
        common_outline.addEntry(new _OutlineEntry(getLocalizedWord("districts", lang), "district-view"));
        common_outline.addEntry(new _OutlineEntry(getLocalizedWord("localities", lang), "locality-view"));
        common_outline.addEntry(new _OutlineEntry(getLocalizedWord("streets", lang), "street-view"));
        common_outline.addEntry(new _OutlineEntry(getLocalizedWord("positions", lang), "position-view"));
        common_outline.addEntry(new _OutlineEntry(getLocalizedWord("tags", lang), "tag-view"));

        _Outline specific_outline = new _Outline(getLocalizedWord("specific_reference_data", lang), "specific");
        specific_outline.addEntry(new _OutlineEntry(getLocalizedWord("property_codes", lang), "propertycode-view"));
        specific_outline.addEntry(new _OutlineEntry(getLocalizedWord("receiving_reason", lang), "receivingreason-view"));
        specific_outline.addEntry(new _OutlineEntry(getLocalizedWord("structure_type", lang), "structuretype-view"));
        specific_outline.addEntry(new _OutlineEntry(getLocalizedWord("building_materials", lang), "buildingmaterial-view"));

        list.add(common_outline);
        list.add(specific_outline);

        setContent(new _XMLDocument(currentTag));
        setContent(list);
    }

    @Override
    public void doPOST(_Session session, _WebFormData formData, LanguageType lang) {

    }
}
