package kz.flabs.util.adapters;

import kz.flabs.users.User;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class UserAdapter extends XmlAdapter<String, User> {
    @Override
    public User unmarshal(String value) throws Exception {
        User user = new User(value);
        return user;
    }

    @Override
    public String marshal(User value) throws Exception {
        return value.getUserID();
    }
}
