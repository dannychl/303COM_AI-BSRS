package com.example.ai_bsrs.transaction_module;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_bsrs.R;

import java.util.ArrayList;

public class TransactionDetailsAdapter extends RecyclerView.Adapter<TransactionDetailsAdapter.ViewHolder>{

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    Context context;
    LayoutInflater inflater;
    private ArrayList<Transaction> mTransactionList;
    private TransactionDetailsAdapter.OnItemClickListener mListener;

    public TransactionDetailsAdapter(Context context, ArrayList<Transaction> transactionList) {
        this.context = context;
        this.inflater = inflater;
        mTransactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionDetailsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_details_page_list,parent,false);
        return new TransactionDetailsAdapter.ViewHolder(view, mListener);
    }

    public void setOnItemClickListener(TransactionDetailsAdapter.OnItemClickListener listener){
        mListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionDetailsAdapter.ViewHolder holder, int position){
        holder.mTvTransactionBreadName.setText(mTransactionList.get(position).getBreadName());
        holder.mTvTransactionBreadPrice.setText(mTransactionList.get(position).getBreadPrice());
        holder.mTvTransactionBreadPriceEach.setText(mTransactionList.get(position).getBreadPriceEach());
        holder.mTvTransactionBreadQty.setText(mTransactionList.get(position).getBreadQty());
    }

    @Override
    public int getItemCount() {
        return mTransactionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvTransactionBreadPrice, mTvTransactionBreadName, mTvTransactionBreadQty, mTvTransactionBreadPriceEach;

        public ViewHolder(@NonNull View itemView, final TransactionDetailsAdapter.OnItemClickListener listener) {
            super(itemView);


            mTvTransactionBreadName = (TextView) itemView.findViewById(R.id.tvTransactionBreadName);
            mTvTransactionBreadPriceEach = (TextView) itemView.findViewById(R.id.tvTransactionBreadPriceEach);
            mTvTransactionBreadPrice = (TextView) itemView.findViewById(R.id.tvTransactionBreadPrice);
            mTvTransactionBreadQty = (TextView) itemView.findViewById(R.id.tvTransactionBreadQty);;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null ){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }

    }
}
