package com.example.meydoon.BottomNavigation.profile;

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
 * Created by hooma on 3/3/2017.
 */
public class SettingsActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private TextView txtNoConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_fragment);

        txtNoConnection = (TextView) findViewById(R.id.settings_txt_no_internet);
        checkConnection();

        if (findViewById(R.id.settings_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            SettingsMainFragment settingsMainFragment = new SettingsMainFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            settingsMainFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.settings_container, settingsMainFragment).commit();
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
    public void settingsClickEvent(View view){
        switch (view.getId()){

            /** For product details,
             * @param img_settings_back
             * @param img_edit_profile_back_ **/
            case R.id.img_settings_back:
                finish();
                break;

            case R.id.img_broadcast_message_back:
                getSupportFragmentManager().popBackStackImmediate();
        }
    }
}
