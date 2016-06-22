package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.StockApplication;
import com.sam_chordas.android.stockhawk.entity.Query;
import com.sam_chordas.android.stockhawk.entity.Quote;
import com.sam_chordas.android.stockhawk.entity.Result;
import com.sam_chordas.android.stockhawk.entity.Results;
import com.sam_chordas.android.stockhawk.entity.Utility;
import com.sam_chordas.android.stockhawk.rest.QuoteService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class StockDetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = StockDetailActivity.class.getSimpleName();
    private static final String BASE_URL = "https://query.yahooapis.com/";
    private String symbol="goog";
    Retrofit.Builder retroBuilder;
    Retrofit retrofit;
    QuoteService QuoteApi;
    RelativeLayout mChartHolder;
    GraphView graph;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mChartHolder = (RelativeLayout)findViewById(R.id.container_main);
        graph = (GraphView) findViewById(R.id.graph);
        if(getIntent() != null && getIntent().hasExtra("symbol")){
            symbol = getIntent().getStringExtra("symbol");
        }


        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okhttp3.OkHttpClient client =  new okhttp3.OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        retroBuilder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create());

        retrofit = retroBuilder.build();
        QuoteApi = retrofit.create(QuoteService.class);

    }

    private void fetchHistoricalData(String symbol,String startDate,String endDate){
        String q = "select * from yahoo.finance.historicaldata where symbol = \""+symbol+"\" and startDate = \""+endDate+"\" and endDate = \""+startDate+"\"";
        String diagnostics = "true";
        String env = "store://datatables.org/alltableswithkeys";
        String format = "json";
        Call<Result> resultCall = QuoteApi.getHistoricalData(q,diagnostics,env,format);
        resultCall.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful()){

                    Query mQuery = response.body().getQuery();
                    Results mResult = mQuery.getResults();
                    Quote[] mQuote = mResult.getQuote();

                    List<DataPoint> dataPoints = new ArrayList<DataPoint>();
                    int hour = 0;
                    for (Quote hf : mQuote) {
                        dataPoints.add(new DataPoint(hour++,Double.parseDouble(hf.getHigh())));
                        Log.d("value",hf.getHigh());
                    }
                    int sizeOfFetchedData = dataPoints.size();
                    DataPoint[] dataPointsArray = new DataPoint[sizeOfFetchedData];
                    LineGraphSeries<DataPoint> fetchedSeries =
                            new LineGraphSeries<DataPoint>(dataPoints.toArray(new DataPoint[dataPoints.size()]));
                    graph.addSeries(fetchedSeries);
                }else {
                    Log.d(TAG, "onResponse: not sucessfull : "+response.message() );
               }

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.d(TAG, "onFailure: failed with message : "+t.getMessage());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_detail,menu);
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.duration_period, R.layout.spinner_layout_custom);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_custom);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        String startDate = Utility.getFormattedDate(System.currentTimeMillis());
        Date date = new Date();
        String[] dates = null;
        switch (item){
            case "Week":
                dates = new String[]{startDate,Utility.get1WeekBackDate(date)};
                Log.d(TAG, "onItemSelected: dates week:"+dates[0]+" and :"+dates[1]);
                fetchHistoricalData(symbol,startDate,Utility.get1WeekBackDate(date));
                break;
            case "Month":
                dates = new String[]{startDate,Utility.get1MonthBackDate(date)};
                Log.d(TAG, "onItemSelected: dates Month:"+dates[0]+" and :"+dates[1]);
                fetchHistoricalData(symbol,startDate,Utility.get1MonthBackDate(date));
                break;
            case "Year":
                dates = new String[]{startDate,Utility.get1YearBackDate(date)};
                Log.d(TAG, "onItemSelected: dates Year:"+dates[0]+" and :"+dates[1]);
                fetchHistoricalData(symbol,startDate,Utility.get1YearBackDate(date));
                break;
           default:
               dates = new String[]{startDate,Utility.get1WeekBackDate(date)};
               Log.d(TAG, "onItemSelected: dates Week :"+dates[0]+" and :"+dates[1]);
               fetchHistoricalData(symbol,startDate,Utility.get1YearBackDate(date));
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}


