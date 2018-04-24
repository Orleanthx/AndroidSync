package com.cangevgeli.androidsync;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class Drive {
    private GoogleSignInClient googleSignInClient;
    private GoogleSignInAccount googleSignInAccount;
    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;
    private Context context;


    /** Initialize Drive **/
    public Drive(Context context){
        this.context = context;
        googleSignInClient = buildGoogleSignInClient(context);
        updateViewWithGoogleSignInAccountTask(googleSignInClient.silentSignIn());
    }

    /** Build a Google SignIn Client. */
    private GoogleSignInClient buildGoogleSignInClient(Context context) {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        //.requestIdToken("1001563987200-83luo4kvdqbk13ftj60o2c44idfvipuo.apps.googleusercontent.com")
                        .requestScopes(com.google.android.gms.drive.Drive.SCOPE_APPFOLDER, com.google.android.gms.drive.Drive.SCOPE_FILE)
                        .build()
                ;

        return GoogleSignIn.getClient(context, signInOptions);
    }

    public void updateViewWithGoogleSignInAccountTask(Task<GoogleSignInAccount> task) {
        Log.v("Drive Access", "Update view with sign in account task");
        task.addOnSuccessListener(
                new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        Log.v("Drive Access", "Sign in success");
                        initializeDrive(googleSignInAccount);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Drive Access", "Sign in failed", e);
                                context.startActivity(googleSignInClient.getSignInIntent());
                                if ( GoogleSignIn.getLastSignedInAccount(context) != null )
                                    initializeDrive(GoogleSignIn.getLastSignedInAccount(context));

                            }
                        });
    }

    private void initializeDrive(GoogleSignInAccount googleSignInAccount) {
        // Store Google Sign In Account
        this.googleSignInAccount = googleSignInAccount;
        // Build a drive client.
        driveClient = com.google.android.gms.drive.Drive.getDriveClient(context, googleSignInAccount);
        // Build a drive resource client.
        driveResourceClient = com.google.android.gms.drive.Drive.getDriveResourceClient(context, googleSignInAccount);

        Log.v("Drive Access", "Drive Access Successful. " + driveClient.toString() + " " + driveResourceClient.getAppFolder().toString());
    }

    public void createFolder(final String folderName) {
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
                                .setTitle(folderName)
                                .setMimeType(DriveFolder.MIME_TYPE)
                                .setStarred(true)
                                .build();
                        return driveResourceClient.createFolder(parentFolder, changeSet);
                    }
                })
                .addOnSuccessListener((Activity) context,
                        new OnSuccessListener<DriveFolder>() {
                            @Override
                            public void onSuccess(DriveFolder driveFolder) {
                                Log.v("Folder Creation", "File created " + driveFolder.getDriveId().encodeToString());
                            }
                        })
                .addOnFailureListener((Activity) context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("Folder Creation", "Unable to create file", e);
                    }
                });
    }

}
