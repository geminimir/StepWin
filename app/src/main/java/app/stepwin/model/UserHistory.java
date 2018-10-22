
package app.stepwin.model;

import java.util.ArrayList;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserHistory implements Parcelable
{

    @SerializedName("User_history")
    @Expose
    private List<UserHistoryList> userHistory = new ArrayList<UserHistoryList>();
    public final static Creator<UserHistory> CREATOR = new Creator<UserHistory>() {


        @SuppressWarnings({
            "unchecked"
        })
        public UserHistory createFromParcel(Parcel in) {
            return new UserHistory(in);
        }

        public UserHistory[] newArray(int size) {
            return (new UserHistory[size]);
        }

    }
    ;

    protected UserHistory(Parcel in) {
        in.readList(this.userHistory, (UserHistoryList.class.getClassLoader()));
    }

    public UserHistory() {
    }

    public List<UserHistoryList> getUserHistory() {
        return userHistory;
    }

    public void setUserHistory(List<UserHistoryList> userHistory) {
        this.userHistory = userHistory;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(userHistory);
    }

    public int describeContents() {
        return  0;
    }

}
