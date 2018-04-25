package com.cangevgeli.androidsync;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private ListView optionsList;
    private OptionsListAdapter optionsListAdapter;

    private ConstraintLayout mainLayout;
    private ConstraintLayout backupLayout;
    private ConstraintLayout syncLayout;

    private boolean assignedRingtone = false;
    private boolean unassignedRingtones = false;
    private boolean contactRingtones = false;

    private Drive drive;

    // Request code for READ_CONTACTS. It can be any number > 0.
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** Layout Definitions **/
        mainLayout = (ConstraintLayout) findViewById(R.id.content_main);
        backupLayout = (ConstraintLayout) findViewById(R.id.content_backup);
        optionsList = (ListView) findViewById(R.id.optionsList);

        // Floating Action Button Definition - BEGIN //
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Sync(getApplicationContext(), optionsList, drive);

                Snackbar.make(view, "Syncing to Drive", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fab.setVisibility(View.INVISIBLE);
        // Floating Action Button Definition - END //

        /** BackUp Functionality **/
        final TextView backup = (TextView) findViewById(R.id.backup);

        // Backup Functionality Listener Definition - BEGIN //
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** NEW UI for BackUp Options **/
                optionsListAdapter = new OptionsListAdapter((Activity) backup.getContext());
                optionsList.setAdapter(optionsListAdapter);

                final TranslateAnimation animate = new TranslateAnimation(backupLayout.getWidth(),0,0,0);
                animate.setDuration(750);
                animate.setFillAfter(false);
                animate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        backupLayout.setVisibility(View.VISIBLE);
                        backup.setClickable(false);
                        System.out.println("TA BEGIN!");
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //mainLayout.setVisibility(View.INVISIBLE);
                        System.out.println("TA END!");
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        System.out.println("TA REPEAT!");

                    }
                });

                backupLayout.startAnimation(animate);
                fab.setVisibility(View.VISIBLE);
            }
        });
        // Backup Functionality Listener Definition - END //

        /** Check if required permissions are granted, ask if not **/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            Log.v("READ_CONTACTS", "READ_CONTACTS Permission NOT Granted.");

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        else{
            Log.v("READ_CONTACTS", "READ_CONTACTS Permission Granted.");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            Log.v("READ_EXTERNAL_STORAGE", "READ_EXTERNAL_STORAGE Permission Granted.");

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        else {
            Log.v("READ_EXTERNAL_STORAGE", "READ_EXTERNAL_STORAGE Permission Granted.");
        }


        /** Google Drive **/
        drive = new Drive(this);


        /**Ringtones**/
        RingtoneManager myRingtones = new RingtoneManager(this);
        myRingtones.setType(RingtoneManager.TYPE_RINGTONE);
        Cursor myRingtonesCursor = myRingtones.getCursor();
        while (myRingtonesCursor.moveToNext())
        {
            if(!myRingtonesCursor.getString(2).contains("content://media/internal/")) {
                System.out.println(myRingtonesCursor.getString(0)
                           + " " + myRingtonesCursor.getString(1)
                           + " " + myRingtonesCursor.getString(2));
            }
        }
        //Pause-Restart hata veriyor şimdilik kapatma.
        //myRingtonesCursor.close();
        /**Ringtones**/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
