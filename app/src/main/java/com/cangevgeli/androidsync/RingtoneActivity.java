package com.cangevgeli.androidsync;

import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

public class RingtoneActivity {
    Context context;
    ArrayList<String[]> audioFiles = new ArrayList<String[]>();
    ArrayList<String[]> contactRingtoneAssignments = new ArrayList<String[]>();

    RingtoneActivity(Context context){
        this.context = context;

        String[] projection = { MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DISPLAY_NAME,MediaStore.Audio.Media.DATA};// Can include more data for more details and check it.

        Cursor audioCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        if(audioCursor != null){
            if(audioCursor.moveToFirst()){
                do{
                    String[] values = {audioCursor.getString(0), audioCursor.getString(1), audioCursor.getString(2)};
                    audioFiles.add(values);
                    Log.v("Audio Files", values[2]);
                    }while(audioCursor.moveToNext());
            }
        }
        audioCursor.close();
    }

    public String[] getDefaultRingtone(){

        /** Identify Default Ringtone **/
        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        //final Ringtone defaultRingtone = RingtoneManager.getRingtone(context, defaultRingtoneUri);
        //final String defaultRingtoneTitle = defaultRingtone.getTitle(context);
        String ringtoneID = defaultRingtoneUri.getPath().substring(defaultRingtoneUri.getPath().lastIndexOf("/")+1);


        /** Locate Actual File Path for Default Ringtone **/
        String[] result = new String[3];
        for(int i=0; i<audioFiles.size(); i++) {
            if(audioFiles.get(i)[0].equals(ringtoneID)) {
                result = audioFiles.get(i);
            }
        }

        return result;
    }

    public ArrayList<String[]> getContactRingtones(){

        ArrayList<String[]> contactRingtones = new ArrayList<String[]>();

        /**Contact Ringtones**/
        String[] projection = new String[]{ ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                            ContactsContract.CommonDataKinds.Phone.CUSTOM_RINGTONE,
                                            ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor myCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);

        while (myCursor.moveToNext())
        {
            String ringtoneURI = myCursor.getString(2);
            if(ringtoneURI != null ) {

                String ringtoneID = ringtoneURI.substring(ringtoneURI.lastIndexOf("/")+1);
                String ringtone = null;
                for(int i=0; i<audioFiles.size(); i++) {
                    if(audioFiles.get(i)[0].equals(ringtoneID)) {
                        if(!contactRingtones.contains(audioFiles.get(i))) {
                            contactRingtones.add(audioFiles.get(i));
                            ringtone = audioFiles.get(i)[1];
                        }
                    }
                }
            contactRingtoneAssignments.add(new String[]{myCursor.getString(3),myCursor.getString(1),ringtone});
                //System.out.println("Contact Ringtone Assignments: " +
                //        myCursor.getString(0)
                //        + " " + myCursor.getString(1)
                //        + " " + myCursor.getString(2));
            }
        }

        myCursor.close();
        /**Contact Ringtones**/

        return contactRingtones ;
    }

    public ArrayList<String[]> getContactRingtoneAssingments(){
       return contactRingtoneAssignments ;
    }
}
