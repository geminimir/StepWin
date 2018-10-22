package app.stepwin.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

public class AppAlerts {

    public static void show(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showSnackBar(View container, String msg) {
        Snackbar.make(container, msg, Snackbar.LENGTH_LONG).show();
    }

    public static void showSnackBarWithAction(View container, String msg, String btnTitle, View.OnClickListener onClickListener) {
        Snackbar snackbar = Snackbar.make(container, msg, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(btnTitle, onClickListener);
        snackbar.show();
    }

    public static void showAlertMessage(Context mContext, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", null);
        builder.create().show();
    }

    public static void showAlertWithAction(Context mContext, String title, String msg, String actionString, String negativeString,
                                           Dialog.OnClickListener clickListener, boolean isSetNegativeButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(actionString, clickListener);
        if (isSetNegativeButton)
            builder.setNegativeButton(negativeString, null);
        builder.create().show();
    }

}