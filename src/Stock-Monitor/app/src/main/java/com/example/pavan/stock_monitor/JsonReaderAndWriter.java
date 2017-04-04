package com.example.pavan.stock_monitor;
import android.content.Context;
import android.net.Uri;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by pavan on 3/12/17.
 */

public class JsonReaderAndWriter {

    private final static String TAG = "jsonReaderAndWriter";
    private List<Stocks> listOfStocks;
    private String urlString;


    /* Getter and Setters */
    public List<Stocks> getListOfStocks() {
        return listOfStocks;
    }

    public void setListOfStocks(List<Stocks> listOfStocks) {
        this.listOfStocks = listOfStocks;
    }

    /**
     * Reads all the notes in the json file
     * @param c - Context of the application
     * @param jsonStr - String which is expected to be in json format
     * @throws IOException - If the rdr fails to read the fails
     */
    public void parseAllStocks(Context c, String jsonStr, int parseMode) {
        Log.d(TAG, "reading All stock names");
        JSONArray arr = null;

        try {
            arr = new JSONArray(jsonStr);
        } catch (JSONException e) {
            Log.d(TAG, "There json string is corrupted");
            e.printStackTrace();
            return;
        }

        try{
            for(int i = 0; i < arr.length(); i++){
                JSONObject obj = (JSONObject) arr.getJSONObject(i);
                listOfStocks.add(parseStock(c, obj, parseMode));
            }
        }
        catch(Exception e){
            Log.d(TAG, "no stock names received");
        }

        Log.d(TAG, "reading all stocks done");
    }

    /**
     * Reads each element of a note and creates a new object of note class
     * @param stockItem - JSONObject that contains the details of each item
     * @param c - Context of the application
     * @return  the new object with updated note data
     * @throws IOException
     */
    private Stocks parseStock(Context c, JSONObject stockItem, int parseMode) throws IOException {
        Log.d(TAG, "Parsing notes now");
        Stocks newStock = new Stocks();

        if(parseMode == 0){
            newStock.setCompanyName(stockItem.optString(c.getString(R.string.name_list_key_company_name)));
            newStock.setCompanySym(stockItem.optString(c.getString(R.string.name_list_key_company_sym)));
        }
        else if(parseMode == 1 || parseMode == 2){
            newStock.setCompanySym(stockItem.optString(c.getString(R.string.stock_detail_key_company_sym)));
            newStock.setStockPrice(Double.parseDouble(stockItem.optString(c.getString(R.string.stock_detail_key_stock_price)).replace(",","")));
            newStock.setStockChangePrice(Double.parseDouble(stockItem.optString(c.getString(R.string.stock_detail_key_change_price)).replace(",", "")));

            if(newStock.getStockChangePrice() > 0)
                newStock.setPercentageChangeIndecator(c.getString(R.string.upSolidArrow));
            else
                newStock.setPercentageChangeIndecator(c.getString(R.string.downSolidArrow));

            newStock.setStockChangePercentage(Double.parseDouble(stockItem.optString(c.getString(R.string.stock_detail_key_change_price_perc)).replace(",", "")));
        }

        return newStock;
    }
}
