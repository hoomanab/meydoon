package com.example.meydoon.BottomNavigation;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
//import com.example.meydoon.EndlessScrollListener;
import com.example.meydoon.BottomNavigation.AddProduct.AddProductActivity;
import com.example.meydoon.EndlessScrollListener;
import com.example.meydoon.MainActivity;
import com.example.meydoon.R;
import com.example.meydoon.adapter.FeedListAdapter;
import com.example.meydoon.app.AppController;
import com.example.meydoon.app.Config;
import com.example.meydoon.data.FeedItem;
import com.example.meydoon.helper.PrefManager;
import com.example.meydoon.receiver.ConnectivityReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * To handle the view when Home icon is selected from bottom navigation!
 */
public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private long now;

    private BottomNavigationView bottomNavigationView;
    private static final String TAG = HomeFragment.class.getSimpleName();
    private ListView listView;
    private FeedListAdapter listAdapter;
    private List<FeedItem> feedItems;
    private String URL_FEED = Config.URL_HOME_FEED;

    private PrefManager pref;
    private Boolean logginStatus;

    private int current_page;



    private Button btnLoadMore;

    private MenuItem menuItem;

    private SwipeRefreshLayout swipeRefreshLayout; /**  ===============> Implement Later <=============== */

    private Cache cache;

    private ProgressDialog pDialog;

    private GradientDrawable colorForbutton;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (ConnectivityReceiver.isConnected()) {
            cache.clear();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_fragment, container, false);
        return view;
    }

    @SuppressLint("NewApi")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /** Custom Action Bar*/
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setCustomView(R.layout.actionbar_home);

        pref = new PrefManager(getActivity().getApplicationContext());
        logginStatus = pref.isLoggedIn();



        now = System.currentTimeMillis();
        cache = AppController.getInstance().getRequestQueue().getCache();
        /*if (pref.isLoggedIn()) {
            AddProductActivity addProductActivity = new AddProductActivity();
            addProductActivity.getShopId();
        }*/
        current_page = 1;

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.home_swipe_refresh_layout);

        listView = (ListView)view.findViewById(R.id.list);
        //btnLoadMore = (Button) view.findViewById(R.id.btn_load_more_feed);
        btnLoadMore = new Button(getActivity());
        btnLoadMore.setVisibility(View.GONE);
        btnLoadMore.setText("بیشتر");

        /*colorForbutton = new GradientDrawable();
        colorForbutton.setColor(getResources().getColor(R.color.blue)); // Changes this drawbale to use a single color instead of a gradient
        colorForbutton.setCornerRadius(3);
        colorForbutton.setStroke(1, getResources().getColor(R.color.black));

        btnLoadMore.setBackgroundDrawable(colorForbutton);
        btnLoadMore.setTextColor(getResources().getColor(R.color.white));*/

        //btnLoadMore.setPadding(0, 20, 0 ,20);

        listView.addFooterView(btnLoadMore);

        feedItems = new ArrayList<FeedItem>();


        listAdapter = new FeedListAdapter(getActivity(), feedItems);
        listView.setAdapter(listAdapter);

        //fetchFeed();
        swipeRefreshLayout.setOnRefreshListener(this);


        btnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoadMoreListView(getActivity(), listView).execute();
            }
        });



        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //listView.setAdapter(null);
                                        swipeRefreshLayout.setRefreshing(true);
                                        fetchFeed();
                                    }
                                }
        );

        /*listView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadNextDataFromApi(current_page + 1);
            }
        });*/
/*
        btnLoadMore = new Button(getActivity());
        btnLoadMore.setText("بیشتر");
        listView.addFooterView(btnLoadMore);
        btnLoadMore.setBackgroundColor(getContext().getResources().getColor(R.color.blue));
        btnLoadMore.setPadding(10, 10, 10, 10);*/

        /** ========> If logginStatus == false then
         *              show Home for Guest
         *
         *            else
         *              pref.checkLogin();
         *              HashMap<String, String> user = getUserDetails();
         *
         *              String user_id = user.get(PrefManager.KEY_ID);
         *
         *              Then send user_id as JSONObject to server and get it's latest following shops products!**/




       /* btnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoadMoreListView().execute();
            }
        });*/

       // listView.setOnScrollListener(new EndlessScrollListener() {
       //     @Override
       //     public boolean onLoadMore(int page, int totalItemsCount) {
                //new LoadMoreListView().execute();
                //return true;
        //    }
      //  });


    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        if (ConnectivityReceiver.isConnected()) {
            cache.clear();

        }
        //listAdapter.clearFeedAdapter();
        //swipeRefreshLayout.setRefreshing(true);
        fetchFeed();
    }

    private void fetchFeed(){

        btnLoadMore.setVisibility(View.GONE);

        listAdapter.clearFeedAdapter();
        swipeRefreshLayout.setRefreshing(true);

        JSONObject feedJsonObject = new JSONObject();
        try {
            feedJsonObject.put("user_id", pref.getUserId());
            feedJsonObject.put("page_number", 1);

        }catch (JSONException e){
            e.printStackTrace();
        }

        // These two lines not needed,
        // just to get the look of facebook (changing background color & hiding the icon)
        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3b5998")));
        //getActionBar().setIcon(
        //        new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        // We first check for cached request

        Cache.Entry entry = new Cache.Entry();

        final long cacheHitButRefreshed = 3 * 60 * 1000;
        final long cacheExpired = 5 * 24 * 60 * 60 * 1000;
        final long softExpire = now + cacheHitButRefreshed;
        final long ttl = now + cacheExpired;

        entry.softTtl = softExpire;
        entry.ttl = ttl;

        entry = cache.get(URL_FEED);
        if (entry != null) {
            // fetch the data from cache
            try {
                String data = new String(entry.data, "UTF-8");
                try {
                    parseJsonFeed(new JSONObject(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else {



            // making fresh volley request and getting json
            JsonObjectRequest requestFeed = new JsonObjectRequest(Request.Method.POST,
                    URL_FEED, feedJsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    VolleyLog.d(TAG, "Response: " + response.toString());
                    if (response != null) {
                        parseJsonFeed(response);
                        btnLoadMore.setVisibility(View.VISIBLE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error: " + error.getMessage());
                    Toast.makeText(getActivity().getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_SHORT).show();

                    // stopping swipe refresh
                    swipeRefreshLayout.setRefreshing(false);
                }
            }) {


                @Override
                public String getBodyContentType() {
                    return String.format("application/json; charset=utf-8");
                }

            };


            int socketTimeout = 10000; // 30 seconds. You can change it
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            requestFeed.setRetryPolicy(policy);

            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(requestFeed);
        }
    }

    /**
     * Parsing json reponse and passing the data to feed view list adapter
     * */
    private void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("Feed");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject responseObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                item.setProductId(responseObj.getInt("product_id"));
                item.setShopId(responseObj.getInt("shop_id"));
                item.setShopName(responseObj.getString("shop_name"));

                // Image might be null sometimes
                String productImage = responseObj.isNull("product_picture_address") ? null : responseObj
                        .getString("product_picture_address");
                item.setProductImage(productImage);
                item.setProductDescription(responseObj.getString("product_description"));

                String shopImage = responseObj.isNull("shop_picture_address") ? null : responseObj
                        .getString("shop_picture_address");
                item.setShopProfilePic(responseObj.getString("shop_picture_address"));
                item.setProductRegisterDate(responseObj.getString("product_register_date"));
                //item.setShipableStatus(feedObj.getBoolean("shipable"));

                // url might be null sometimes
                //String productTitle = responseObj.isNull("product_name") ? null : responseObj
                  //      .getString("product_name");
                item.setProductTitle(responseObj.getString("product_name"));
                item.setShopPhoneNumber(responseObj.getString("shop_phone"));
                String shopTelegramId = responseObj.isNull("shop_telegram_id") ? null : responseObj
                        .getString("shop_telegram_id");
                item.setShopTelegramId(shopTelegramId);
                item.setProductPrice(responseObj.getString("product_price"));
                item.setShipableStatus(responseObj.getInt("product_shippable_status"));
                item.setShopCity(responseObj.getString("shop_city"));


                feedItems.add(item);
            }

            // notify data changes to list adapater
            listAdapter.notifyDataSetChanged();

            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void loadNextDataFromApi(int offset){
        /*pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("لطفا صبر کنید..");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);
        pDialog.show();*/





        /** Begin ************************************/
        JSONObject feedJsonObject = new JSONObject();
        try {
            feedJsonObject.put("user_id", pref.getUserId());
            feedJsonObject.put("page_number", offset);

        }catch (JSONException e){
            e.printStackTrace();
        }

        // making fresh volley request and getting json
        JsonObjectRequest requestFeed = new JsonObjectRequest(Request.Method.POST,
                URL_FEED, feedJsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "Response: " + response.toString());

                try {
                    // Parsing json object response
                    // response will be a json object
                    String error = response.getString("error");
                    String statusCode = response.getString("StatusCode");

                    // checking for error, if not error SMS is initiated
                    // device should receive it shortly
                    if (error.equals("0")) {
                        parseJsonFeed(response);
                    } else if (error.equals("1") && statusCode.equals("1000")) {

                        /** Continue From HERE ================================================ */
                        btnLoadMore.setVisibility(View.INVISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getActivity().getApplicationContext(),
                        "خطا در دریافت اطلاعات ", Toast.LENGTH_SHORT).show();

                //pDialog.hide();
            }
        }) {


            @Override
            public String getBodyContentType() {
                return String.format("application/json; charset=utf-8");
            }

        };


        int socketTimeout = 10000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        requestFeed.setRetryPolicy(policy);

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(requestFeed);

        /** End **************************************/



        // get listview current position - used to maintain scroll position
        //int currentPosition = listView.getFirstVisiblePosition();

        // Setting new scroll position
        //listView.setSelectionFromTop(currentPosition + 1, 0);

    }


    /**
     * Async Task that send a request to url
     * Gets new list view data
     * Appends to list view
     **/
    public class LoadMoreListView extends AsyncTask<Void, Void, Void> {

        private ListView mListView;
        private FragmentActivity mContext;

        public LoadMoreListView(FragmentActivity context, ListView lView){
            this.mContext = context;
            this.mListView = lView;
        }

        @Override
        protected void onPreExecute() {
            // Showing progress dialog before sending http request
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("دریافت محصولات بیشتر..");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();


        }

        protected Void doInBackground(Void... unused) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    // increment current page
                    current_page += 1;

                    loadNextDataFromApi(current_page);

                    // get listview current position - used to maintain scroll position
                    int currentPosition = listView.getFirstVisiblePosition();


                    // Setting new scroll position
                    listView.setSelectionFromTop(currentPosition + 1, 0);

                }
            });
            return (null);
        }

        protected void onPostExecute(Void unused) {
            // closing progress dialog
            pDialog.dismiss();
        }
    }
}





