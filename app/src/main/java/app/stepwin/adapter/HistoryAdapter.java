package app.stepwin.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.stepwin.R;
import app.stepwin.model.UserHistoryList;
import app.stepwin.ui.HistoryDetailActivity;

/**
 * Created by natraj on 10/10/17.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    Context mContext;

    private List<UserHistoryList> android;

    public HistoryAdapter(Context context,List<UserHistoryList> android) {
        this.mContext = context;
        this.android = android;
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_history_list, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.ViewHolder viewHolder, final int i) {

        viewHolder.tv_date.setText(android.get(i).getDate());
        viewHolder.tv_step.setText(android.get(i).getStep());
        viewHolder.tv_cal.setText(android.get(i).getCalories());

        final String date = android.get(i).getDate();
        final String step = android.get(i).getStep();
        final String cal = android.get(i).getCalories();
        final String distance = android.get(i).getDistant();
        final String uc = android.get(i).getUnlocl_counter();
        final String minute = android.get(i).getMinute();
        final String point = android.get(i).getPoints();

        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, HistoryDetailActivity.class);
                intent.putExtra("step",step);
                intent.putExtra("cal",cal);
                intent.putExtra("distance",distance);
                intent.putExtra("uc",uc);
                intent.putExtra("minute",minute);
                intent.putExtra("date",date);
                intent.putExtra("point",point);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return android.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_date,tv_step,tv_cal;
        private LinearLayout linearLayout;
        public ViewHolder(View view) {
            super(view);

            tv_date = (TextView)view.findViewById(R.id.tv_date);
            tv_step = (TextView)view.findViewById(R.id.tv_steps);
            tv_cal = (TextView)view.findViewById(R.id.tv_calories);
            linearLayout = (LinearLayout) view.findViewById(R.id.row_history_list);

        }
    }

}
