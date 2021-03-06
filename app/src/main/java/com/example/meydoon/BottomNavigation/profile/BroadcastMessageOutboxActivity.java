package com.example.meydoon.BottomNavigation.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.meydoon.R;
import com.example.meydoon.app.AppController;
import com.example.meydoon.receiver.ConnectivityReceiver;

/**
 * Created by hooma on 3/1/2017.
 */
public class BroadcastMessageOutboxActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {
    private static final String TAG = BroadcastMessageOutboxActivity.class.getSimpleName();

    private TextView txtNoConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_notification_fragment);

        txtNoConnection = (TextView) findViewById(R.id.profile_notification_txt_no_internet);
        checkConnection();

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.profile_notification_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            BroadcastMessageOutboxFragment goToProfileNotifications = new BroadcastMessageOutboxFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            goToProfileNotifications.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.profile_notification_container, goToProfileNotifications).commit();
        }
    }

    // Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showNoConnection(isConnected);
    }

    private void showNoConnection(boolean isConnected){
        if(isConnected) {
            txtNoConnection.setVisibility(View.GONE);
        } else{
            txtNoConnection.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v(TAG, "MainActivity resumed!");

        checkConnection();
        AppController.getInstance().setConnectivityListener(this);
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showNoConnection(isConnected);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    /** Handling clicks on actionbar icons */
    public void broadcastMessageOutboxClickEvent(View view){
        switch (view.getId()){

            /** For product details,
             * @param img_profile_notification_back **/
            case R.id.img_profile_notification_back:
                super.onBackPressed();
                break;

            case R.id.img_broadcast_message_back:
                getSupportFragmentManager().popBackStackImmediate();
                break;
        }
    }

}
