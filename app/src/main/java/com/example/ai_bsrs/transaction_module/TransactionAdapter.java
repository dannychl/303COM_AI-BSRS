package com.example.ai_bsrs.transaction_module;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_bsrs.R;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder>{

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    Context context;
    LayoutInflater inflater;
    private ArrayList<Transaction> mTransactionList;
    private TransactionAdapter.OnItemClickListener mListener;

    public TransactionAdapter(Context context, ArrayList<Transaction> transactionList) {
        this.context = context;
        this.inflater = inflater;
        mTransactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_list,parent,false);
        return new TransactionAdapter.ViewHolder(view, mListener);
    }

    public void setOnItemClickListener(TransactionAdapter.OnItemClickListener listener){
        mListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter.ViewHolder holder, int position){
        holder.mTvTransactionId.setText("ID: " + mTransactionList.get(position).getTransactionId());
        holder.mTvDateTransaction.setText("Date: "+ mTransactionList.get(position).getKEY_BREAD_DATE_CHECKOUT());
        holder.mTvTimeTransaction.setText("Time: "+ mTransactionList.get(position).getKEY_BREAD_TIME_CHECKOUT());
        holder.mTvTotalPriceTransaction.setText("Total Price: RM "+ mTransactionList.get(position).getKEY_BREAD_TOTAL_PRICE());
        if (mTransactionList.get(position).getKEY_STATUS().equals("Paid")){
            holder.mIvStatus.setBackgroundResource(R.drawable.paid);
        }else if (mTransactionList.get(position).getKEY_STATUS().equals("Changes Made")){
            holder.mIvStatus.setBackgroundResource(R.drawable.change);
        }else{
            holder.mIvStatus.setBackgroundResource(R.drawable.cancel);
        }
    }

    @Override
    public int getItemCount() {
        return mTransactionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvStatus, mTvTransactionId, mTvTotalPriceTransaction, mTvDateTransaction, mTvTimeTransaction;
        private ImageView mIvStatus;

        public ViewHolder(@NonNull View itemView, final TransactionAdapter.OnItemClickListener listener) {
            super(itemView);


            mTvStatus = (TextView) itemView.findViewById(R.id.tvStatus);
            mTvTransactionId = (TextView) itemView.findViewById(R.id.transactionId);
            mTvTotalPriceTransaction = (TextView) itemView.findViewById(R.id.totalPriceTransaction);
            mTvDateTransaction = (TextView) itemView.findViewById(R.id.dateTransaction);
            mTvTimeTransaction = (TextView) itemView.findViewById(R.id.timeTransaction);
            mIvStatus = (ImageView) itemView.findViewById(R.id.ivStatus);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
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

