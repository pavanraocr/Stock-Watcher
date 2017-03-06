package com.example.pavan.stock_monitor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Stocks> stocksList = new ArrayList<>();
    RecyclerView rv;
    StockViewAdapter stocksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = (RecyclerView) findViewById(R.id.rvStockList);
        stocksAdapter = new StockViewAdapter(stocksList);

        rv.setAdapter(stocksAdapter);
        rv.setLayoutManager(new LinearLayoutManager(this));

        initForText();
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


}