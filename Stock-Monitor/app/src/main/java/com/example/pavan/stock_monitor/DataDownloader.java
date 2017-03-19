package com.example.pavan.stock_monitor;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pavan on 3/12/17.
 */

public class DataDownloader extends AsyncTask<Integer, Void, Integer> {
    private String searchedStr;
    private String TAG = "NameDownloader";
    private List<Stocks> nameList;
    private MainActivity mainAct;
    private final String apiKey = "f157c1d447e09d737a64fd963e0b9a5ad725ad7b";
    private String baseURLForNameSearchAPI = "http://stocksearchapi.com/api";//"/?api_key=";
    private String baseURLForStockDetailsAPI = "http://finance.google.com/finance/info";
    private JsonReaderAndWriter readWriteObj = new JsonReaderAndWriter();
    public boolean isAsyncTaskRunning;

    private String jsonFormatDataList;
    public Stocks stockToRetrieve;
    public ArrayList<Stocks> allStocksToRetrieve;

    public DataDownloader(MainActivity ma) {
        mainAct = ma;
    }


    /**
     * Creates the URL with the apikey and search text
     *
     * @return URL String
     */
    private String createQuerry(int querryType){
        //return baseURLForNameSearchAPI + apiKey + "&search_text=" + searchedStr;
        Uri.Builder uRLBuilder;

        switch (querryType){
            case 0:
                uRLBuilder = Uri.parse(baseURLForNameSearchAPI).buildUpon();
                uRLBuilder.appendQueryParameter("api_key", apiKey);
                uRLBuilder.appendQueryParameter("search_text", searchedStr);
                return uRLBuilder.build().toString();
            case 1:
                uRLBuilder = Uri.parse(baseURLForStockDetailsAPI).buildUpon();
                uRLBuilder.appendQueryParameter("client", "ig");
                uRLBuilder.appendQueryParameter("q", stockToRetrieve.getCompanySym());
                return uRLBuilder.build().toString();
            case 2:
                uRLBuilder = Uri.parse(baseURLForStockDetailsAPI).buildUpon();
                uRLBuilder.appendQueryParameter("client", "ig");

                StringBuilder sb = new StringBuilder();
                for(Stocks s: allStocksToRetrieve){
                    if(!sb.toString().isEmpty()){
                        sb.append(",");
                    }

                    sb.append(s.getCompanySym());
                }

                uRLBuilder.appendQueryParameter("q", sb.toString());
                return uRLBuilder.build().toString();
            default:
                Log.d(TAG, "Unknown type of querry creation requested");
        }

        return "";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        nameList = new ArrayList<>();
        readWriteObj.setListOfStocks(nameList);
    }

    /**
     * Retrieves the current searched string from the Main activity
     */
    private void getSearchText() {
        searchedStr = mainAct.getSearchedName();
    }

    @Override
    protected Integer doInBackground(Integer... params) { //param[0] {0 - download name list, 1 - download stock details}
        if(isAsyncTaskRunning){
            Log.d(TAG, "There is already an async task running in the background");
            return -1;
        }

        switch (params[0]){
            case 0:{
                //This choice will execute the task for
                getSearchText();
                //Toast.makeText(mainAct, "Loading Names that associate with the search key " + searchedStr, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "async task started with namelist size = " + String.valueOf(nameList.size()));
                break;
            }
            case 1:{
                //This choice takes care of stockDetails of a particular stock
                //Toast.makeText(mainAct, "Loading data for the stock " + stockToRetrieve.getCompanySym(), Toast.LENGTH_SHORT).show();
                break;
            }
            case 2:{
                //This takes care of stockDetails of all the stocks in allTheStocksToRetrieve
                break;
            }
            default:
                Log.d(TAG, "wrong asnyc task requested");
                return -1;
        }


        isAsyncTaskRunning = true;
        boolean loadStatus = false;
        loadStatus = downloadData(params[0]);
        isAsyncTaskRunning = false;
        Log.d(TAG, "async task ended and returning status " + String.valueOf(loadStatus));

        if(loadStatus)
            return params[0];
        else
            return -1;
    }

    /**
     * Function that downloads the data using the URL and the api key and stores it as a string
     * in jsonFormatDataList
     *
     * @throws IOException if there is reading error
     */
    private boolean downloadData(int querryType) {
        String url = createQuerry(querryType);

        Log.d(TAG, "The url from which the file is being downloaded from: " + url);

        StringBuilder fileContent = new StringBuilder();

        try{
            HttpURLConnection conn = openHTTPConnection(url);
            BufferedReader downloadReader = openConnectionForGet(conn);
            String line;

            while ((line = downloadReader.readLine()) != null){
                if(line.startsWith("//")){
                    line = line.substring(2);
                }

                fileContent.append(line).append("\n");
            }
        }
        catch(Exception e){
            Log.d(TAG, "ERROR in downloading data check the stack trace");
            e.printStackTrace();
            return false;
        }

        jsonFormatDataList = fileContent.toString();
        return true;
    }

    /**
     * Function that creates the
     * @param conn
     * @return
     * @throws IOException
     */
    private BufferedReader openConnectionForGet(HttpURLConnection conn) throws IOException {
        BufferedReader reader;
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            Log.d(TAG, "The connection is trying to set a request method which is invalid");
            e.printStackTrace();
            return null;
        }

        InputStream is = (InputStream) conn.getInputStream();
        reader = new BufferedReader((new InputStreamReader(is)));

        return reader;
    }

    /**
     * Creates the connection object and returns that instance with the connection open to the URL sent
     * @param url - URL to which the http connection is open
     * @return the object reference to the open conntection
     * @throws IOException - if the connection is not through then this exception is thrown
     */
    private HttpURLConnection openHTTPConnection(String url) throws IOException {
        URL downLink = null;

        try {
            downLink = new URL(url);
        } catch (MalformedURLException e) {
            Log.d(TAG, "Error in opening the link: \"" + url + "\"");
            e.printStackTrace();
            return null;
        }

        HttpURLConnection conn = (HttpURLConnection) downLink.openConnection();

        return conn;
    }

    @Override
    protected void onPostExecute(Integer loadOption) { //loadoption - {-1 - failure, 0 - nameList task completion, 1 - stock details task completion}
        super.onPostExecute(loadOption);

        switch (loadOption){
            case -1:
                //on failure of any task
                mainAct.createDialogForDisplayingNoName();
                Log.d(TAG, "Load error");
                break;
            case 0:
                //post name list retrieval task completion
                readWriteObj.parseAllStocks(mainAct.getApplicationContext(), jsonFormatDataList, loadOption);
                mainAct.processNameList((ArrayList<Stocks>) nameList);
                Log.d(TAG, "onPostexe NameList size = " + String.valueOf(nameList.size()));
                break;
            case 1:
                //post sotck details retrieval task completion
                readWriteObj.parseAllStocks(mainAct.getApplicationContext(), jsonFormatDataList, loadOption);
                try{
                    Stocks s = nameList.get(0);

                    //This is to be done because the google api doesn't give us the company name
                    //So we have to retrieve this from the previous API call
                    s.setCompanyName(stockToRetrieve.getCompanyName());

                    //verifying if the data retrieved from google and stock search is correct
                    if(stockToRetrieve.getCompanySym().equals(s.getCompanySym())){
                        mainAct.addStock(s);
                    }
                    else{
                        Log.d(TAG, "The details retrieved from google API and stockSearch API are different");
                        mainAct.createDialogForDisplayingNoName();
                    }

                    Log.d(TAG, "onPostExec Stock details retrieval task completed");
                }
                catch (Exception e){
                    Log.d(TAG, "onPostExe: case 1 error");
                    e.getStackTrace();
                }
                break;
            case 2:
                //post retrieval of all the stockdetails task completion
                readWriteObj.parseAllStocks(mainAct.getApplicationContext(), jsonFormatDataList, loadOption);
                List<Stocks> stocks = new ArrayList<>();

                for(Stocks stocksItem: nameList){
                    stocksItem.setCompanyName(getCompanyNameFromSearchedStockList(stocksItem.getCompanySym()));
                    stocks.add(stocksItem);
                }

                mainAct.addAllStocks(stocks);
                Log.d(TAG, "all stocks are updated");

                break;
            default:
                Log.d(TAG, "Unknown post completion code returned");
        }

    }

    /**
     * Returns the company name of the searched stock
     * @param companySym
     * @return
     */
    private String getCompanyNameFromSearchedStockList(String companySym) {
        for(Stocks s: allStocksToRetrieve){
            if(s.getCompanySym().equals(companySym)){
                return s.getCompanyName();
            }
        }
        return "";
    }
}
