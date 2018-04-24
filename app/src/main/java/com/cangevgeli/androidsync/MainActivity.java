package com.cangevgeli.androidsync;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity {

    private ListView optionsList;
    private OptionsListAdapter optionsListAdapter;

    private ConstraintLayout mainLayout;
    private ConstraintLayout backupLayout;
    private ConstraintLayout syncLayout;

    private GoogleSignInClient googleSignInClient;
    private GoogleSignInAccount googleSignInAccount;
    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;

    // Request code for READ_CONTACTS. It can be any number > 0.
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** Google Drive **/
        googleSignInClient = buildGoogleSignInClient();
        updateViewWithGoogleSignInAccountTask(googleSignInClient.silentSignIn());

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("New file")
                .setMimeType("text/plain")
                .setStarred(true)
                .build();

        //System.out.println("Drive Resource Client: " + driveResourceClient.toString());
        //System.out.println("Root Folder: " + driveResourceClient.getRootFolder().toString());
        //System.out.println("App Folder: " + driveResourceClient.getAppFolder().toString());
        //driveResourceClient.createFile(driveResourceClient.getAppFolder().getResult(), changeSet, null);


        /** Ringtone Part **/
        final TextView backup = (TextView) findViewById(R.id.backup);
        Uri defaultRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(this.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        final Ringtone defaultRingtone = RingtoneManager.getRingtone(this, defaultRintoneUri);
        final String defaultRingtoneTitle = defaultRingtone.getTitle(this);
        /** Ringtone Part **/

        mainLayout = (ConstraintLayout) findViewById(R.id.content_main);
        backupLayout = (ConstraintLayout) findViewById(R.id.content_backup);
        optionsList = (ListView) findViewById(R.id.optionsList);

        // Floating Action Button Definition - BEGIN //
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //Test Amaçlı Eklenmişti
                //backupLayout.setVisibility(View.INVISIBLE);

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                /** Ringtone Check! **/
                //defaultRingtone.play();
                //backup.setText(defaultRingtoneTitle);
                /** Ringtone Check! **/
                new Sync(getApplicationContext(), optionsList);
                System.out.println("FAB END!");
            }
        });

        fab.setVisibility(View.INVISIBLE);
        // Floating Action Button Definition - END //

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
                System.out.println("TV END!");
                fab.setVisibility(View.VISIBLE);
            }
        });
        // Backup Functionality Listener Definition - END //


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        /**Contact Ringtones**/
        String[] myProjection = new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                             ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                             ContactsContract.CommonDataKinds.Phone.CUSTOM_RINGTONE};

        Cursor myCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, myProjection, null, null, null);
        while (myCursor.moveToNext())
        {
            if(myCursor.getString(2) != null ) {
               System.out.println(myCursor.getString(0)
                        + " " + myCursor.getString(1)
                        + " " + myCursor.getString(2));
            }
        }
        //Pause-Restart hata veriyor şimdilik kapatma.
        //myCursor.close();
        /**Contact Ringtones**/

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

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

    /** Build a Google SignIn client. */
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        //.requestIdToken("1001563987200-83luo4kvdqbk13ftj60o2c44idfvipuo.apps.googleusercontent.com")
                        .requestScopes(Drive.SCOPE_APPFOLDER, Drive.SCOPE_FILE)
                        .build()
                        ;

        return GoogleSignIn.getClient(this, signInOptions);
    }

    private void updateViewWithGoogleSignInAccountTask(Task<GoogleSignInAccount> task) {
        Log.i("Drive Access", "Update view with sign in account task");
        task.addOnSuccessListener(
                new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        Log.i("Drive Access", "Sign in success");
                        initializeDrive(googleSignInAccount);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Drive Access", "Sign in failed", e);
                                startActivity(googleSignInClient.getSignInIntent());
                                if ( GoogleSignIn.getLastSignedInAccount(getApplicationContext()) != null )
                                    initializeDrive(GoogleSignIn.getLastSignedInAccount(getApplicationContext()));

                            }
                        });
    }

    private void initializeDrive(GoogleSignInAccount googleSignInAccount) {
        // Store Google Sign In Account
        this.googleSignInAccount = googleSignInAccount;
        // Build a drive client.
        driveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
        // Build a drive resource client.
        driveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);

        createFolder();

        Log.v("Drive Access", "Drive Access Successful. " + driveClient.toString() + " " + driveResourceClient.getAppFolder().toString());
    }


    private void createFolder() {
        driveResourceClient
                //Yaratılan Klasörü Test Edebilmek İçin Root Folder
                .getRootFolder()
                //.getAppFolder()
                .continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
                    @Override
                    public Task<DriveFolder> then(@NonNull Task<DriveFolder> task)
                            throws Exception {
                        DriveFolder parentFolder = task.getResult();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("New folder")
                                .setMimeType(DriveFolder.MIME_TYPE)
                                .setStarred(true)
                                .build();
                        return driveResourceClient.createFolder(parentFolder, changeSet);
                    }
                })
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveFolder>() {
                            @Override
                            public void onSuccess(DriveFolder driveFolder) {
                                Log.v("Folder Creation", "File created " + driveFolder.getDriveId().encodeToString());
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("Folder Creation", "Unable to create file", e);
                        finish();
                    }
                });
    }
}
