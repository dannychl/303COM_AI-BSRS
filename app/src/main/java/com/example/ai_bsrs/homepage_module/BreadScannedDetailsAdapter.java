package com.example.ai_bsrs.homepage_module;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_bsrs.R;

import java.util.ArrayList;


public class BreadScannedDetailsAdapter extends RecyclerView.Adapter<BreadScannedDetailsAdapter.ViewHolder> {

    public interface OnItemClickListener{
        void onItemClick(int position, String name, String price, String qty);
    }

    Context context;
    LayoutInflater inflater;
    private ArrayList<Bread> mBreadList;
    private OnItemClickListener mListener;

    public BreadScannedDetailsAdapter(Context ctx, ArrayList<Bread>mBreadList) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;
        this.mBreadList = mBreadList;
    }


    @NonNull
    @Override
    public BreadScannedDetailsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.breadscanned_list,parent,false);
        return new BreadScannedDetailsAdapter.ViewHolder(view, mListener);
    }


    @Override
    public void onBindViewHolder(@NonNull final BreadScannedDetailsAdapter.ViewHolder holder, int position) {

        holder.mTvBreadName.setText(mBreadList.get(position).getName());
        holder.mTvBreadQty.setText(mBreadList.get(position).getQuantity());
        holder.mTvBreadPrice.setText(mBreadList.get(position).getPrice());
    }

    @Override
    public int getItemCount() {
        return mBreadList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvBreadPrice, mTvBreadName, mTvBreadQty;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            mTvBreadName = (TextView) itemView.findViewById(R.id.tvBreadName);
            mTvBreadPrice = (TextView) itemView.findViewById(R.id.tvBreadPrice);
            mTvBreadQty = (TextView) itemView.findViewById(R.id.tvBreadQty);;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        String name = mTvBreadName.getText().toString();
                        String price = mTvBreadPrice.getText().toString();
                        String quantity = mTvBreadQty.getText().toString();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position, name, price, quantity);
                        }
                    }
                }
            });
        }

    }
}
