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

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.StockApplication;
import com.sam_chordas.android.stockhawk.entity.Query;
import com.sam_chordas.android.stockhawk.entity.Quote;
import com.sam_chordas.android.stockhawk.entity.Result;
import com.sam_chordas.android.stockhawk.entity.Results;
import com.sam_chordas.android.stockhawk.entity.Utility;
import com.sam_chordas.android.stockhawk.rest.QuoteService;
import com.squareup.okhttp.OkHttpClient;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import java.util.Date;
import java.util.logging.Level;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mChartHolder = (RelativeLayout)findViewById(R.id.container_main);
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
                    mChartHolder.removeAllViews();

                    Query mQuery = response.body().getQuery();
                    Results mResult = mQuery.getResults();
                    Quote[] mQuote = mResult.getQuote();


                    XYSeries series = new XYSeries("Stock value - Historic Data");
                    int hour = 0;
                    for (Quote hf : mQuote) {
                        series.add(hour++, Double.parseDouble(hf.getHigh()));
                        Log.d("value",hf.getHigh());
                    }
                    XYSeriesRenderer renderer = new XYSeriesRenderer();
                    renderer.setLineWidth(8);
                    renderer.setColor(Color.RED);
                    renderer.setDisplayBoundingPoints(true);
                    renderer.setPointStyle(PointStyle.CIRCLE);
                    renderer.setPointStrokeWidth(9);
                    XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

                    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                    dataset.addSeries(series);

                    mRenderer.addSeriesRenderer(renderer);
                   mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
                    mRenderer.setPanEnabled(false, false);
                    mRenderer.setYAxisMax(100);
                    mRenderer.setYAxisMin(0);
                    mRenderer.setShowGrid(true); // we show the grid
                    GraphicalView chartView = ChartFactory.getLineChartView(getApplicationContext(), dataset, mRenderer);

                    mChartHolder.addView(chartView);
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


