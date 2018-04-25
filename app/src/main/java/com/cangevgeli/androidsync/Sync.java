package com.cangevgeli.androidsync;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.json.JSONStringer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Can on 24.09.2017.
 */

public class Sync {

    private boolean defaultRingtone = false;
    private boolean allRingtones = false;
    private boolean contactRingtones = false;

    private View v;
    private TextView tv;
    private CheckBox cb;

    Sync(Context context, ListView optionsList, Drive drive){
        for (int i = 0; i < optionsList.getCount(); i++)
        {
            v = optionsList.getChildAt(i);
            tv = (TextView) v.findViewById(R.id.optionText);
            cb =(CheckBox) v.findViewById(R.id.checkBox);

            if(tv.getText().equals("Assigned Ringtone")){
                if (cb.isChecked())
                    defaultRingtone = true;
            }

            else if(tv.getText().equals("Unassigned Ringtones")){
                if (cb.isChecked())
                    allRingtones = true;
            }

            else if(tv.getText().equals("Contact Ringtones")){
                if (cb.isChecked())
                    contactRingtones = true;
            }

        }


        //System.out.println("Sync: " + defaultRingtone + allRingtones + contactRingtones);
        if(defaultRingtone || allRingtones || contactRingtones) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String currentDateandTime = sdf.format(new Date());
            String syncName = Build.BRAND.substring(0,1).toUpperCase()
                    + Build.BRAND.substring(1)
                    + " " + Build.MODEL
                    + " "  + currentDateandTime;

            Task<DriveFolder> createDriveFolderTask = drive.createFolder(syncName);
            Task<DriveFile> uploadFileTask = drive.uploadFile(createDriveFolderTask, "applicationLog", null, null);

            RingtoneActivity ra = new RingtoneActivity(context);
            RingtoneConfiguration rc = new RingtoneConfiguration();
            if(defaultRingtone){
                String[] defaultRingtoneDetails = ra.getDefaultRingtone();
                rc.setDefaultRingtone(defaultRingtoneDetails[1]);

                uploadFileTask = drive.uploadFile(uploadFileTask, "ringtone", defaultRingtoneDetails, null);
            }

            if(contactRingtones){
                ArrayList<String[]> contactRingtoneDetails = ra.getContactRingtones();
                rc.setContactRingtones(ra.getContactRingtoneAssingments());
                for(int i=0; i<contactRingtoneDetails.size(); i++){
                    uploadFileTask = drive.uploadFile(uploadFileTask, "ringtone", contactRingtoneDetails.get(i), null);
                }
            }

            String ringtoneConfiguration = new Gson().toJson(rc);
            System.out.println("JSON: " + ringtoneConfiguration);
            uploadFileTask = drive.uploadFile(uploadFileTask, "ringtoneConfiguration", null, ringtoneConfiguration);
            //RingtoneConfiguration rc2 = gson.fromJson(json, RingtoneConfiguration.class);
            //System.out.println(" From JSON: " + rc2.getDefaultRingtone() + rc.getContactRingtones().get(0)[0]+" "+ rc.getContactRingtones().get(0)[1]);
        }

    }

}
