package com.example.pavan.stock_monitor;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by pavan on 3/6/17.
 */

public class StockViewHolder extends RecyclerView.ViewHolder {
    public TextView companySym;
    public TextView stockPrice;
    public TextView percentageChange;
    public TextView companyName;
    public TextView percentageChangeIndecator;


    public StockViewHolder(View itemView) {
        super(itemView);

        companySym = (TextView) itemView.findViewById(R.id.stockSymbol);
        stockPrice = (TextView) itemView.findViewById(R.id.price);
        percentageChange = (TextView) itemView.findViewById(R.id.textPercentage);
        companyName = (TextView) itemView.findViewById(R.id.companyName);
        percentageChangeIndecator = (TextView) itemView.findViewById(R.id.stockChangeDirection);
    }
}