package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.StockApplication;
import com.sam_chordas.android.stockhawk.entity.Query;
import com.sam_chordas.android.stockhawk.entity.Quote;
import com.sam_chordas.android.stockhawk.entity.Result;
import com.sam_chordas.android.stockhawk.entity.Results;
import com.sam_chordas.android.stockhawk.rest.QuoteService;
import com.squareup.okhttp.OkHttpClient;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.security.spec.ECField;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;



public class StockDetailActivity extends Activity {

    private static final String TAG = StockDetailActivity.class.getSimpleName();
    private static final String BASE_URL = "https://query.yahooapis.com/v1";
    private  String diagnostics = "true";
    private  String env = "store://datatables.org/alltableswithkeys";
    private  String format = "json";
    Retrofit.Builder retroBuilder;
    Retrofit retrofit;
    QuoteService QuoteApi;
    RelativeLayout mChartHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mChartHolder = (RelativeLayout)findViewById(R.id.container_main);
        String symbol = "AAPL";
        if(getIntent() != null && getIntent().hasExtra("symbol")){
            symbol = getIntent().getStringExtra("symbol");
        }
        retroBuilder = new Retrofit.Builder()
                .baseUrl(BASE_URL);
        retrofit = retroBuilder.build();
        QuoteApi = retrofit.create(QuoteService.class);

    }

    private void fetchHistoricalData(String symbol,String[] dates){
        String query = "select * from yahoo.finance.historicaldata where symbol = \""+symbol+"\" and startDate = \""+dates[0]+"\" and endDate = \""+dates[1]+"\"";
        QuoteApi.getHistoricalData(query, diagnostics, env, format, new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if(response.isSuccessful()){
                    mChartHolder.removeAllViews();

                    Log.d("result",call.toString());

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
                    renderer.setLineWidth(2);
                    renderer.setColor(Color.RED);
                    renderer.setPointStyle(PointStyle.CIRCLE);
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
                    Log.d(TAG, "onResponse: not sucessfull");
                }
                
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {

            }
        });
    }
}
