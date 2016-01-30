package kz.flabs.runtimeobj.document;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;

import javax.xml.bind.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public abstract class AbstractComplexObject implements IComplexObject {

    @Override
    public abstract void init(IDatabase db, String initString) throws ComplexObjectException;

    @Override
    public abstract String getContent();

    public String getPersistentValue() {
        return this.getClass().getName() + "~" + getContent();
    }

    public static IComplexObject unmarshall(String className, String value) {
        try(StringReader reader = new StringReader(value)) {
            JAXBContext context = JAXBContext.newInstance(Class.forName(className));
            Unmarshaller um = context.createUnmarshaller();
            um.setEventHandler(
                    new ValidationEventHandler() {
                        public boolean handleEvent(ValidationEvent event) {
                            throw new RuntimeException(event.getMessage(),
                                    event.getLinkedException());
                        }
                    });
            return (IComplexObject) um.unmarshal(reader);
        } catch (JAXBException e) {
            AppEnv.logger.errorLogEntry(e);
        } catch (ClassNotFoundException e) {
            AppEnv.logger.errorLogEntry(e);
        }
        return null;
    }

    public static String marshall(String className, IComplexObject object) {
        try(StringWriter writer = new StringWriter()) {
            JAXBContext context = JAXBContext.newInstance(Class.forName(className));
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, true);
            m.marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            AppEnv.logger.errorLogEntry(e);
        } catch (IOException e) {
            AppEnv.logger.errorLogEntry(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
