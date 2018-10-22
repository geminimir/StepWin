
package app.stepwin.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserHistoryList implements Parcelable
{

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("step")
    @Expose
    private String step;
    @SerializedName("distant")
    @Expose
    private String distant;
    @SerializedName("calories")
    @Expose
    private String calories;
    @SerializedName("points")
    @Expose
    private String points;
    @SerializedName("unlocl_counter")
    @Expose
    private String unlocl_counter;
    @SerializedName("minute")
    @Expose
    private String minute;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("date")
    @Expose
    private String date;
    public final static Creator<UserHistoryList> CREATOR = new Creator<UserHistoryList>() {


        @SuppressWarnings({
            "unchecked"
        })
        public UserHistoryList createFromParcel(Parcel in) {
            return new UserHistoryList(in);
        }

        public UserHistoryList[] newArray(int size) {
            return (new UserHistoryList[size]);
        }

    }
    ;

    protected UserHistoryList(Parcel in) {
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.step = ((String) in.readValue((String.class.getClassLoader())));
        this.distant = ((String) in.readValue((String.class.getClassLoader())));
        this.calories = ((String) in.readValue((String.class.getClassLoader())));
        this.points = ((String) in.readValue((String.class.getClassLoader())));
        this.unlocl_counter = ((String)in.readValue((String.class.getClassLoader())));
        this.minute = ((String)in.readValue((String.class.getClassLoader())));
        this.username = ((String) in.readValue((String.class.getClassLoader())));
        this.email = ((String) in.readValue((String.class.getClassLoader())));
        this.date = ((String) in.readValue((String.class.getClassLoader())));
    }

    public UserHistoryList() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getDistant() {
        return distant;
    }

    public void setDistant(String distant) {
        this.distant = distant;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUnlocl_counter() {
        return unlocl_counter;
    }

    public void setUnlocl_counter(String unlocl_counter) {
        this.unlocl_counter = unlocl_counter;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(step);
        dest.writeValue(distant);
        dest.writeValue(calories);
        dest.writeValue(points);
        dest.writeValue(unlocl_counter);
        dest.writeValue(minute);
        dest.writeValue(username);
        dest.writeValue(email);
        dest.writeValue(date);
    }

    public int describeContents() {
        return  0;
    }

}
