package com.bongiovanni.smartretail;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.bongiovanni.smartretail.models.MyBeacon;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    private BluetoothAdapter bluetoothState = BluetoothAdapter.getDefaultAdapter();
    private BroadcastReceiver observeBluetoothState;

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private BeaconManager beaconManager;
    private boolean showBeacon = true;

    private HashMap<Integer, MyBeacon> beaconSaved = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(bluetoothState == null) {
            Toast.makeText(this, getString(R.string.bluetooth_not_supported), Toast.LENGTH_LONG).show();
            finish();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        isPermissionGranted();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24")); // iBeacon - IBEACON_LAYOUT

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleScan(true);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        showBeacon = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(observeBluetoothState);
        toggleScan(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showBeacon = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem bluetoothButton = menu.findItem(R.id.action_bluetooth);
        setBluetoothIcon(bluetoothButton, bluetoothState.isEnabled());

        observeBluetoothState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            setBluetoothIcon(bluetoothButton, false);
                            Toast.makeText(MainActivity.this, "Bluetooth disabled", Toast.LENGTH_SHORT).show();
                            toggleScan(false);
                            break;
                        case BluetoothAdapter.STATE_ON:
                            setBluetoothIcon(bluetoothButton, true);
                            Toast.makeText(MainActivity.this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(observeBluetoothState, filter);
        return true;
    }


    /** Set Bluetooth icon **/
    private void setBluetoothIcon(MenuItem item, boolean status){
        Drawable icon = AppCompatResources.getDrawable(MainActivity.this, status ? R.drawable.ic_round_bluetooth_24px : R.drawable.ic_round_bluetooth_disabled_24px).mutate();
        item.setIcon(icon);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_bluetooth) {
            if (!bluetoothState.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 0);
            }
            else bluetoothState.disable();
            return true;
        }

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /** Identify the beacons **/
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if(beaconManager.isBound(MainActivity.this) != true) return;
                if (beacons.size() > 0) {
                    for (Beacon beacon : beacons) {
                        if(!beaconSaved.containsKey(beacon.hashCode())){
                            MyBeacon mBeacon = new MyBeacon(beacon);
                            if(!showBeacon) sendNotification(mBeacon);
                            beaconSaved.put(beacon.hashCode(), mBeacon);
                        }
                        else beaconSaved.get(beacon.hashCode()).update(beacon);
                    }
                }
                showBeacons();
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("com.bongiovanni.smartretail", null, null, null));
        } catch (RemoteException e) { e.printStackTrace(); }
    }

    /** Scan Start or Stop **/
    private void toggleScan(boolean enable) {
        boolean isScan = false;
        if(beaconManager.isBound(MainActivity.this) == true) beaconManager.unbind(MainActivity.this);
        else if (enable && isPermissionGranted()) {
            if (!bluetoothState.isEnabled()) Toast.makeText(MainActivity.this, R.string.enable_bluetooth_to_start_scanning, Toast.LENGTH_LONG).show();
            else {
                beaconManager.bind(MainActivity.this);
                isScan = true;
            }
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        findViewById(R.id.progressBar).setVisibility(isScan ? View.VISIBLE : View.GONE);
        fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, isScan ? R.color.colorPauseFab : R.color.colorSecondary)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimatedVectorDrawableCompat anim = AnimatedVectorDrawableCompat.create(MainActivity.this, isScan ? R.drawable.play_to_pause : R.drawable.pause_to_play);
            fab.setImageDrawable(anim);
            anim.start();
        } else fab.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, isScan ? R.drawable.pause_icon : R.drawable.play_icon));
    }

    /** Show beacons **/
    private void showBeacons(){
        if(!showBeacon) return;

        LinearLayout dynamicContent = findViewById(R.id.dynamic_content);
        dynamicContent.removeAllViews();

        if(beaconSaved.size() == 0) {
            dynamicContent.addView(getLayoutInflater().inflate(R.layout.item_beacon_empty, dynamicContent, false));
            return;
        }

        for (final MyBeacon beacon: beaconSaved.values()) {
            if(new Date().getTime() - beacon.getLastSeen() >= 10000){
                beaconSaved.remove(beacon.hashCode());
                continue;
            }

            final View newBeaconView = getLayoutInflater().inflate(R.layout.item_beacon, dynamicContent, false);
            newBeaconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!showBeacon) return;
                    showBeacon = false;

                    Intent beaconResultsIntent = new Intent(MainActivity.this, BeaconResultsActivity.class);
                    beaconResultsIntent.putExtra("BeaconHash", beacon.getHashCode());
                    beaconResultsIntent.putExtra("BeaconName", beacon.getBeaconName());
                    startActivity(beaconResultsIntent);
                }
            });

            SharedPreferences sharedPref = getSharedPreferences("MyData", MODE_PRIVATE);
            Set<String> myset = new HashSet<String>(){{
                add("a");
                add("b");
                add("c");
            }};
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putStringSet("name",myset);
            editor.commit();



            ((TextView)newBeaconView.findViewById(R.id.beacon_address)).setText(beacon.getBeaconAddress());
            ((TextView)newBeaconView.findViewById(R.id.beacon_type)).setText(beacon.getBeaconType());
            ((TextView)newBeaconView.findViewById(R.id.beacon_name)).setText(beacon.getBeaconName());
            ((TextView)newBeaconView.findViewById(R.id.beacon_distance)).setText(beacon.getDistance());
            dynamicContent.addView(newBeaconView);
        }
    }

    /** Send a notification if the app is paused **/
    private void sendNotification(MyBeacon mBeacon) {
        String NOTIFICATION_CHANNEL_ID = "channel_id";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent resultIntent = new Intent(this, BeaconResultsActivity.class);
        resultIntent.putExtra("BeaconHash", mBeacon.getHashCode());
        resultIntent.putExtra("BeaconName", mBeacon.getBeaconName());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.found_beacon) + " " + mBeacon.getBeaconName())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);
        builder.setContentIntent(resultPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(101, builder.build());
    }



    /** Access to location **/
    private boolean isPermissionGranted(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.permission_needed));
                    builder.setMessage(getString(R.string.since_location));
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
                return false;
            }
        } return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_FINE_LOCATION || requestCode == PERMISSION_REQUEST_BACKGROUND_LOCATION){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.function_limited));
                builder.setMessage(getString((requestCode == PERMISSION_REQUEST_FINE_LOCATION) ? R.string.since_location : R.string.since_background_location));
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });
                builder.show();
            }
        }
        return;
    }



}
