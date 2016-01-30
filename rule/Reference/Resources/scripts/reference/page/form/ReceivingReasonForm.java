package reference.page.form;

import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.users.User;
import kz.nextbase.script._Exception;
import kz.nextbase.script._POJOObjectWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import reference.dao.ReceivingReasonDAO;
import reference.model.ReceivingReason;

public class ReceivingReasonForm extends ReferenceForm {

	@Override
	public void doGET(_Session session, _WebFormData formData, LanguageType lang) {
		String id = formData.getValueSilently("docid");
		User user = session.getUser();
		ReceivingReason entity;
		if (!id.equals("")) {
			ReceivingReasonDAO dao = new ReceivingReasonDAO(session);
			entity = dao.findById(UUID.fromString(id));
		} else {
			entity = new ReceivingReason();
			entity.setAuthor(user);
		}
		setContent(new _POJOObjectWrapper(entity, lang));
		setContent(getSimpleActionBar(session, lang));
	}

	@Override
	public void doPOST(_Session session, _WebFormData formData, LanguageType lang) {
		try {
			boolean v = validate(formData, lang);
			if (v == false) {
				setBadRequest();
				return;
			}

			boolean isNew = false;
			String id = formData.getValueSilently("docid");
			ReceivingReasonDAO dao = new ReceivingReasonDAO(session);
			ReceivingReason entity;

			if (id.equals("")) {
				isNew = true;
				entity = new ReceivingReason();
			} else {
				entity = dao.findById(UUID.fromString(id));
				if (entity == null) {
					isNew = true;
					entity = new ReceivingReason();
				}
			}

			entity.setName(formData.getValue("name"));

			if (isNew) {
				dao.add(entity);
			} else {
				dao.update(entity);
			}

			addMsg(getLocalizedWord("document_was_saved_succesfully", lang));
		} catch (_Exception e) {
			log(e);
		}
	}
}
