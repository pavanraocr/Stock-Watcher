package com.example.pavan.stock_monitor;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    private static String TAG = "MainActivity";

    private List<Stocks> stocksList = new ArrayList<>();
    RecyclerView rv;
    StockViewAdapter stocksAdapter;
    private String searchedName;
    private SwipeRefreshLayout swipeRefresh;

    /*------ Constants -----------*/
    private final int requestFromAdd = 0;
    private final int requestFromUpdate = 1;

    /*-----------Getter and Setters------------*/
    public String getSearchedName() {
        return searchedName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = (RecyclerView) findViewById(R.id.rvStockList);
        stocksAdapter = new StockViewAdapter(stocksList, this);

        rv.setAdapter(stocksAdapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRef);

        //initForText();

        gainPermissions();

        DatabaseHandler.getInstance(this).setupDb();

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        if(!checkForNetworkConnectivity()){
            warnUserAboutNetwork(requestFromUpdate);
        }

        //reloadStockData();
    }

    private void doRefresh() {
        Log.d(TAG, "doRefresh: Swipe refresh is activated");
        reloadStockData();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: reloading the data");
        DatabaseHandler.getInstance(this).dumpLog();
        reloadStockData();
        super.onResume();
    }

    /**
     * Downloads the data for all the stocks
     */
    private void reloadStockData() {
        if(!checkForNetworkConnectivity()){
            warnUserAboutNetwork(requestFromUpdate);
            swipeRefresh.setRefreshing(false);
            return;
        }

        Log.d(TAG, "reloadStockData: reloading all stocks");
        stocksList.clear();
        ArrayList<Stocks> list = DatabaseHandler.getInstance(this).loadStocks();

        if(list.size() > 0){
            Log.d(TAG, "data present for reloading");
            downloadAllStockDetails(list);
        }
    }

    @Override
    protected void onDestroy() {
        DatabaseHandler.getInstance(this).shutDown();
        super.onDestroy();
    }

    private boolean gainPermissions() {
        Log.d(TAG, "Requesting Permission");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 5);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: CALL: " + permissions.length);
        Log.d(TAG, "onRequestPermissionsResult: PERM RESULT RECEIVED");

        if (requestCode == 5) {
            Log.d(TAG, "onRequestPermissionsResult: permissions.length: " + permissions.length);
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_NETWORK_STATE)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onRequestPermissionsResult: HAS PERM");
                    } else {
                        Log.d(TAG, "onRequestPermissionsResult: NO PERM");
                    }
                }
            }
        }
        Log.d(TAG, "onRequestPermissionsResult: Calling super onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: Exiting onRequestPermissionsResult");
    }

    /**
     * This is just for testing
     */
    private void initForText() {
        String ind;
        double change;

        for(int i = 0; i < 10 ; i++){
            if(i%2 == 0){
                ind = getString(R.string.downSolidArrow);
                change = i*1000*(-1);
            }
            else{
                ind = getString(R.string.upSolidArrow);
                change = i * 1000;
            }
            stocksList.add(new Stocks("SYM"+String.valueOf(i), i*100, ind, change, change/100));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.menu_addStock:
                boolean isNetworkAvailable = checkForNetworkConnectivity();

                if(!isNetworkAvailable){
                    warnUserAboutNetwork(requestFromAdd);
                    return true;
                }

                createDialogForNameRequest();
                break;
            default:
                Log.d(TAG, "Unknown Menu item selected");
        }

        return true;
    }

    /**
     * Creates the dialog box for the user to enter the company symbol that he wishes to add
     * to the list of watcher
     */
    private void createDialogForNameRequest() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        et.setGravity(Gravity.CENTER_HORIZONTAL);

        builder.setView(et);

        builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                searchedName = et.getText().toString();
                if(checkForDuplicate(searchedName)){
                    createDialogForDuplicateData(searchedName);
                }
                else{
                    initiateAsyncTaskToFetchNames();
                }
            }
        });

        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        builder.setMessage(R.string.lbl_name_list_msg);
        builder.setTitle(R.string.lbl_name_list_ttl);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Creates a dialog which warns the user about the duplicate entry
     */
    private void createDialogForDuplicateData(String searchedName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_ttl_Dup_stock);
        builder.setIcon(R.drawable.ic_warning_black_24dp);
        builder.setMessage("Stock symbol " + searchedName + "is already displayed");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Checks if the company symbol searched for is already present in the list of stocks monitored
     * @param name - name of the string that is searched for
     *
     * @return boolean {true - if it is duplicate, false - otherwise}
     */
    private boolean checkForDuplicate(String name) {
        for(Stocks s: stocksList){
            if(s.getCompanySym().equals(name)){
                return true;
            }
        }
        return false;
    }

    /**
     * executes the async task that is responsible for retrieving the names associating with the
     * search task
     */
    private void initiateAsyncTaskToFetchNames() {
        new DataDownloader(this).execute(0);
    }

    /**
     * creates the dialog box that warns the user about the network not being active at the moment
     */
    private void warnUserAboutNetwork(int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_ttl_No_Network);

        if(requestCode == requestFromAdd){
            builder.setMessage("Stocks cannot be added without a network connection");
        }
        else if(requestCode == requestFromUpdate){
            builder.setMessage("Stocks cannot be updated without a network connection");
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Checks whether the network is active at the current moment
     *
     * @return network availability status
     */
    private boolean checkForNetworkConnectivity() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called from the Async task once it finishes its job
     * Checks whether there exists more than one match to the searched stock name if so then initiates
     * another dialog box and let the user decide on which stock they want to monitor else the this
     * method initiates the process that fetches the stockDetails directly
     *
     * @param nList
     */
    public void processNameList(ArrayList<Stocks> nList) {
        if(nList.size() < 1){
            createDialogForDisplayingNoName();
        }
        else if(nList.size() == 1){
            downloadStockDetails(nList.get(0));
            return;
        }else{
            //This is when another dialog box is required to show the list of all the matches
            createDialogForNameSelect(nList);
        }

    }

    /**
     * If there exists no data on the searched string then this displays the message to the user
     */
    public void createDialogForDisplayingNoName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_ttl_sym_not_found) + searchedName);
        builder.setMessage(getString(R.string.dialog_msg_no_data));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Creates a dialog to let the user select the items from the 
     * @param nList
     */
    private void createDialogForNameSelect(final ArrayList<Stocks> nList) {

        final CharSequence[] sArray = new CharSequence[nList.size()];
        for (int i = 0; i < nList.size(); i++)
            sArray[i] = nList.get(i).serializeToNameListItemFormat();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tit_name_lst_multiple);

        builder.setItems(sArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Stocks s = new Stocks();
                //s.deserializeNameListFormat(sArray[which]);
                Stocks s = nList.get(which);
                if(checkForDuplicate(s.getCompanySym())){
                    createDialogForDuplicateData(s.getCompanyName());
                }
                else
                    downloadStockDetails(s);
            }
        });

        builder.setNegativeButton(R.string.btn_name_lst_mult_lbl, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    /**
     * Downloads the details of the stock item using its name and symbol
     * @param stockItem
     */
    public void downloadStockDetails(Stocks stockItem){
        DataDownloader downloader = new DataDownloader(this);
        downloader.stockToRetrieve = stockItem;
        downloader.execute(1);
    }

    public void downloadAllStockDetails(ArrayList<Stocks> stocks){
        DataDownloader downloader = new DataDownloader(this);
        downloader.allStocksToRetrieve = stocks;
        downloader.execute(2);
    }

    /**
     * Sorts the stock list based on the company symbol
     */
    private void sortStockList() {
        Collections.sort(stocksList, new Comparator<Stocks>() {
            @Override
            public int compare(Stocks o1, Stocks o2) {
                return o1.getCompanySym().compareTo(o2.getCompanySym());
            }
        });
    }

    /**
     * Adds the stock item to the list being monitored
     * @param stockItem
     */
    public void addStock(Stocks stockItem){
        DatabaseHandler.getInstance(this).addStocks(stockItem);
        stocksList.add(stockItem);
        sortStockList();
        refreshRecyclerView();
    }

    /**
     * Adds all the stock item to the list being monitored
     * @param stocks
     */
    public void addAllStocks(List<Stocks> stocks){
        stocksList.addAll(stocks);
        sortStockList();
        refreshRecyclerView();
        swipeRefresh.setRefreshing(false);
        Toast.makeText(this, "List content Updated", Toast.LENGTH_SHORT).show();
    }

    /**
     * Deletes the stock at the position pos
     * @param pos
     */
    private void deleteStock(int pos) {
        DatabaseHandler.getInstance(this).deleteStocks(stocksList.get(pos).getCompanySym());
        stocksList.remove(pos);
        refreshRecyclerView();
    }


    /**
     * notifies the recycler view about the data being changed
     */
    private void refreshRecyclerView(){
        stocksAdapter.notifyDataSetChanged();
    }

    /**
     * Deletes the stock on long press
     */
    private void createDialogForDeleteStock(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_ttl_delete_stock);
        builder.setMessage(getString(R.string.dialog_msg_delete_stock) + stocksList.get(pos).getCompanySym());
        builder.setIcon(R.drawable.ic_delete_forever_black_24dp);

        builder.setPositiveButton(getString(R.string.btn_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteStock(pos);
            }
        });

        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //User clicked cancel
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        //TODO implicite intent to a web browser

        String url = "http://www.marketwatch.com/investing/stock/" + stocksList.get(rv.getChildLayoutPosition(v)).getCompanySym();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public boolean onLongClick(View v) {
        createDialogForDeleteStock(rv.getChildLayoutPosition(v));
        return false;
    }
}