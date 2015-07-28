package com.example.abaehre.androidbeacontest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.util.Collection;

/**
 * Created by abaehre on 7/22/15.
 */
public class BeaconApplication extends Activity implements BeaconConsumer {
    private BeaconManager beaconManager;
    private DBHelper db;
    private TextView rangingText;
    private TextView monitorText;
    private BackgroundPowerSaver backgroundPowerSaver;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //fix this
        setContentView(R.layout.beaconapp);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
        ));
        beaconManager.bind(this);
        rangingText = (TextView)findViewById(R.id.rangingText);
        monitorText = (TextView)findViewById(R.id.monitorText);
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        db = new DBHelper(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        beaconManager.unbind(this);
    }


    @Override
    public void onBeaconServiceConnect(){

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                MyTask task = new MyTask();
                if(beacons.size()>0){
                    Beacon temp = beacons.iterator().next();
                    Log.d("TAG","Distance: " + temp.getDistance());
                    task.execute(new String[]{"add",Integer.toString(count),
                            "Distance: " + Double.toString
                            (temp.getDistance()),"ranging"});
                    count++;
                }

            }
        });
        beaconManager.setMonitorNotifier(new MonitorNotifier(){

            @Override
            public void didEnterRegion(Region region){
                MyTask task = new MyTask();
                Log.d("TAG","I saw a beacon");
                task.execute(new String[]{"add",Integer.toString(count),"I saw a beacon",
                        "monitor"});
                count++;
            }

            @Override
            public void didExitRegion(Region region){
                MyTask task = new MyTask();
                Log.d("TAG","I left a beacon area");
                task.execute(new String[]{"add", Integer.toString(count), "I left a " +
                        "beacon area","monitor"});
                count++;
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region){
                MyTask task = new MyTask();
                Log.d("TAG","I switched states");
                task.execute(new String[]{"add", Integer.toString(count), "I switched " +
                        "states","monitor"});
                count++;
            }
        });
        try{
            //black one name: rad beacon usb
            beaconManager.startMonitoringBeaconsInRegion(new Region
                    ("154275-F952-44F4-ADD22B1CF9FEBEC7",null,
                    null,null));
            //blue one name: front entrance
            beaconManager.startRangingBeaconsInRegion(new Region
                    ("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6", null,
                            null, null));
        }catch(RemoteException e){
            System.out.println("HII");
        }
    }

    private class MyTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if(db.getNumRows()>3){
                db.deleteAll(db);
            }
            if(params[0].equals("add")){
                db.add(params[1],params[2]);
                return params[1];
            }
            else{
                return "";
            }
        }

        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
            monitorText.setText(db.update());

        }
    }
}
