package com.cangevgeli.androidsync;

import java.util.ArrayList;

public class RingtoneConfiguration {

    private String defaultRingtone;
    private ArrayList<String[]> contactRingtones;

    RingtoneConfiguration(){
        this.defaultRingtone = null;
        this.contactRingtones = new ArrayList<String[]>();
    }

    public void setDefaultRingtone(String defaultRingtone) {
        this.defaultRingtone = defaultRingtone;
    }

    public String getDefaultRingtone() {
        return defaultRingtone;
    }

    public void setContactRingtones(ArrayList<String[]> contactRingtones) {
        this.contactRingtones = contactRingtones;
    }

    public ArrayList<String[]> getContactRingtones() {
        return contactRingtones;
    }
}
