package app.stepwin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by user on 05/09/2017.
 */

public class HistoryFullListAdapter extends ArrayAdapter<String> {

    String[] date = new String[1000], distance = new String[1000], time = new String[1000], calories = new String[1000];
    String[] stepsArray = new String[1000];
    List<String> month = new ArrayList<String>();
    List<String> steps = new ArrayList<String>();
    Context c;
    LayoutInflater inflater;
    ListView listView;
    TextView monthText, StepsText;
    int stepsmonthly = 0;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public HistoryFullListAdapter(Context context, List<String> month, List<String>steps) {
        super(context, R.layout.history_custom_list, month);
        this.c = context;
        this.month = month;
        this.steps = steps;
    }
    List<String> DaysTitles;
    List<String>progressTitles;


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_full_history_page, null);
        }
        sharedPreferences = c.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        monthText = (TextView)convertView.findViewById(R.id.month);
        StepsText = (TextView)convertView.findViewById(R.id.stepsSum);
        monthText.setText(month.get(position));
//        StepsText.setText(steps.get(position) + " step");
        listView = (ListView)convertView.findViewById(R.id.listview);


        ReadCSVFile(sharedPreferences.getString("user", ""));
        HistoryListAdapter adapter = new HistoryListAdapter(c, DaysTitles, progressTitles);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("listclicktest", DaysTitles.get(position) + "\t" + progressTitles.get(position) + " shit");
            }
        });
        return convertView;
    }


    private void ReadCSVFile(String path) {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + c.getResources().getString(R.string.app_name) + File.separator + path + ".csv");

//Get the text file

//Read text from file
        String[] columns = new String[1000];
        String[] rows = new String[5];
        int i = 0;
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                i++;
                columns[i] = line;
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        date[0] = "0";
        stepsArray[0] = "0";
        DaysTitles = new ArrayList<String>();
        progressTitles = new ArrayList<String>();
        int b = 1;
        for(int a = 1; a < columns.length; a++) {
            if(columns[a] != null) {
                Log.i("filecontent", a + "  " + columns[a]);
                rows =  columns[a].split(",");
                    date[a] = rows[0].replace("\"", "");
                    stepsArray[a] = rows[1].replace("\"", "");
                    distance[a] = rows[2].replace("\"", "");
                    time[a] = rows[3].replace("\"", "");
                    calories[a] = rows[4].replace("\"", "");
                if(!date[a].equals(date[a-1])) {
                    Log.i("progresstitles", stepsArray[a] + "");
                    if(stepsArray[a-1] != "0")
                        progressTitles.add(b -1, stepsArray[a-1]);
                    else
                        progressTitles.add(b -1, stepsArray[a]);
                        DaysTitles.add(b - 1, date[a]);
                        b++;
                   /* if(date[a-1] != "0")
                    if(getMonth(date[a]).equals(getMonth(date[a-1]))) {
                        stepsmonthly += Math.round(Float.parseFloat(stepsArray[a]));
                        monthText.setText(getMonth(date[a]));
                    }*/
                }
            }

        }
        Collections.reverse(progressTitles);
        Collections.reverse(DaysTitles);
       // StepsText.setText(String.valueOf(stepsmonthly) +  " step");

    }

    private String getMonth(String date) {
        String monthvalue = date.substring(5, 7), month ="";
        switch (monthvalue) {
            case "01":
                month = "January";
            break;
            case "02":
                month = "February";
            break;
            case "03":
                month = "Mars";
                break;
            case "04":
                month = "April";
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
                month = "August";
                break;
            case "09":
                month = "September";
                break;
            case "10":
                month = "October";
                break;
            case "11":
                month = "November";
                break;
            case "12":
                month = "December";
                break;


        }
        return month + " " + date.substring(0, 4);
    }
}
