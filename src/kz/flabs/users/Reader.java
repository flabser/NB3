package kz.flabs.users;


import java.io.Serializable;

public class Reader implements Serializable{
    private static final long serialVersionUID = 1L;
    private String userID;
    private boolean isFavorite;

    public Reader(String userID, boolean favorite) {
        this.userID = userID;
        isFavorite = favorite;
    }

    public Reader(String userID) {
        this.userID = userID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reader reader = (Reader) o;

        if (!userID.equals(reader.userID)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return userID.hashCode();
    }

    public String getUserID() {
        return userID;

    }

    public void setUserID(String userID) {
        this.userID = userID;

    }

    public boolean isFavorite() {
        return isFavorite;

    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public String toString() {
        return userID;
    }

}
