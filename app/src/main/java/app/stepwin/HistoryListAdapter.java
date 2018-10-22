package app.stepwin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 03/09/2017.
 */

public class HistoryListAdapter extends ArrayAdapter<String> {

    List<String> day = new ArrayList<String>();
    List<String> progress = new ArrayList<String>();
    Context c;
    LayoutInflater inflater;
    TextView dayText, progressText;
    ProgressBar progressBar;
    public HistoryListAdapter(Context context, List<String> day, List<String>progress) {
        super(context, R.layout.history_custom_list, day);
        this.c = context;
        this.day = day;
        this.progress = progress;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.history_custom_list, null);
        }
        dayText = (TextView) convertView.findViewById(R.id.day);
        progressText = (TextView) convertView.findViewById(R.id.percentage);
        progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
        if(day.get(position) != null && progress.get(position) != null ) {
            dayText.setText(getDay(day.get(position)));
            progressText.setText(Math.round(Float.parseFloat(progress.get(position))) /100 + "%");
            progressBar.setProgress(Math.round(Float.parseFloat(progress.get(position))) /100);
        }


        return convertView;
    }

private String getDay(String day) {
    String dayvalue = day.substring(8, 10);
    return dayvalue + " " + getMonth(day);
}
    private String getMonth(String date) {
        String monthvalue = date.substring(5, 7), month ="";
        switch (monthvalue) {
            case "01":
                month = "Jan";
                break;
            case "02":
                month = "Feb";
                break;
            case "03":
                month = "Mars";
                break;
            case "04":
                month = "Apr";
                break;
            case "05":
                month = "May";
                break;
            case "06":
                month = "June";
                break;
            case "07":
                month = "July";
                break;
            case "08":
                month = "Aug";
                break;
            case "09":
                month = "Sep";
                break;
            case "10":
                month = "Oct";
                break;
            case "11":
                month = "Nov";
                break;
            case "12":
                month = "Dec";
                break;


        }
        return month ;
    }

}
