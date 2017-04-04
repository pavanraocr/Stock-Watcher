package com.example.pavan.stock_monitor;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by pavan on 3/6/17.
 */

public class StockViewAdapter extends RecyclerView.Adapter<StockViewHolder> {
    //never assign a new memory for this variable
    private List<Stocks> stocksList;
    private MainActivity ma;

    /**
     * Sets the adapter with the list of data that is use to set the recycler view
     * @param stocksList - list of objects of type Stocks
     */
    public StockViewAdapter(List<Stocks> stocksList, MainActivity mainAct) {
        this.stocksList = stocksList;
        ma = mainAct;
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_items, parent, false);
        v.setOnClickListener(ma);
        v.setOnLongClickListener(ma);

        return new StockViewHolder(v);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        Stocks stock = stocksList.get(position);

        holder.companySym.setText(stock.getCompanySym());
        holder.companyName.setText(stock.getCompanyName());
        holder.stockPrice.setText(stock.getStockPriceString());
        holder.percentageChange.setText(stock.getStockChangeString());
        holder.percentageChangeIndecator.setText(stock.getPercentageChangeIndecator());

        updateViewElementProperties(holder, stock);
    }

    /**
     * Updates the elements text and properties based on the price
     * @param holder
     * @param stock
     */
    private void updateViewElementProperties(StockViewHolder holder, Stocks stock) {

        int dynamicColor;

        //Checks the value of the stock price change and updates the properties of the view
        if(stock.getStockChangePrice() >= 0){
            dynamicColor = Color.GREEN;
        }
        else{
            dynamicColor = Color.RED;
        }

        holder.companySym.setTextColor(dynamicColor);
        holder.stockPrice.setTextColor(dynamicColor);
        holder.percentageChangeIndecator.setTextColor(dynamicColor);
        holder.percentageChange.setTextColor(dynamicColor);
        holder.companyName.setTextColor(dynamicColor);
    }

    @Override
    public int getItemCount() {
        return stocksList.size();
    }
}
