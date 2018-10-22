package app.stepwin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.stepwin.constants.Constants;
import app.stepwin.utils.AppPreference;

/**
 * Created by user on 16/08/2017.
 */

public class ListAdapter extends ArrayAdapter<String> {

    List<String> title, buttontitle = new ArrayList<String>();
    Context c;
    LayoutInflater inflater;
    Context context;
    TextView titleTv;
    Button edit;

    public ListAdapter(Context context, List<String> title, List<String> buttontitle, Context con) {
        super(context, R.layout.custom_list, title);
        this.c = context;
        this.context = con;
        this.title = title;
        this.buttontitle = buttontitle;
    }


    SharedPreferences sharedPreferences;
    String pref = "MyPref";
    SharedPreferences.Editor editor;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        sharedPreferences = c.getSharedPreferences(pref, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (convertView == null) {
            inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_list, null);
        }
        titleTv = (TextView) convertView.findViewById(R.id.textmoney);
        edit = (Button) convertView.findViewById(R.id.pay);
        edit.setFocusable(false);
        edit.setText(buttontitle.get(position));
        titleTv.setText(title.get(position));

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int points = AppPreference.getIntegerPreference(context, Constants.POINTS);
                if (points >= Integer.parseInt(buttontitle.get(position))) {
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle(c.getResources().getString(R.string.giftsrequest));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, c.getResources().getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    int restPoint = points - Integer.parseInt(buttontitle.get(position));

                                    AppPreference.setIntegerPreference(context, Constants.POINTS, restPoint);
                                    editor.putInt("points", sharedPreferences.getInt("points", restPoint));
                                    editor.commit();

                                    String u_Email = AppPreference.getStringPreference(context, Constants.EMAIL);
                                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                    sendIntent.setType("plain/text");
                                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"s.cherki@yahoo.fr"});//pradeepnatrajinfotech03@gmail.com
                                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, c.getResources().getString(R.string.app_name) + " Buying Request");
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Text");
                                    sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(Intent.createChooser(sendIntent, ""));

                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, c.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.show();
                } else
                    Toast.makeText(c, c.getResources().getString(R.string.gifts), Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }


}
