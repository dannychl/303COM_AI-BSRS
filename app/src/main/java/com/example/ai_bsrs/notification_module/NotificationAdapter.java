package com.example.ai_bsrs.notification_module;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_bsrs.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{

    public interface OnItemClickListener{
        void onItemClick(int position, String name, String date, String time);
    }

    Context context;
    LayoutInflater inflater;
    private ArrayList<Notification> mNotificationList;
    private OnItemClickListener mListener;

    public NotificationAdapter(Context context, ArrayList<Notification> notificationList) {
        this.context = context;
        this.inflater = inflater;
        mNotificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list,parent,false);
        return new NotificationAdapter.ViewHolder(view, mListener);
    }

    public void setOnItemClickListener(NotificationAdapter.OnItemClickListener listener){
        mListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position){
        holder.mTvNotifyBreadName.setText(mNotificationList.get(position).getBreadName());
        holder.mTvKeepDate.setText("Keep: " + mNotificationList.get(position).getDateKeep());
        holder.mTvKeepTime.setText("Ring On: " + mNotificationList.get(position).getTimeKeep());
        //holder.mEtNotes.setText(mNotificationList.get(position).getNotes());
    }

    @Override
    public int getItemCount() {
        return mNotificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvNotifyBreadName, mTvKeepDate, mTvKeepTime;
        //private TextInputEditText mEtNotes;
        //private ConstraintLayout mBackground;

        public ViewHolder(@NonNull View itemView, final NotificationAdapter.OnItemClickListener listener) {
            super(itemView);


            //mBackground = (ConstraintLayout) itemView.findViewById(R.id.background);
            mTvNotifyBreadName = (TextView) itemView.findViewById(R.id.tvNotifyBreadName);
            mTvKeepDate = (TextView) itemView.findViewById(R.id.tvKeepDate);
            mTvKeepTime = (TextView) itemView.findViewById(R.id.tvKeepTime);
            //mEtNotes = (TextInputEditText) itemView.findViewById(R.id.editNotes);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        String name = mTvNotifyBreadName.getText().toString();
                        String date = mTvKeepDate.getText().toString();
                        String time =  mTvKeepTime.getText().toString();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position, name, date, time);
                        }
                    }
                }
            });
        }

    }
}
