package com.podevs.android.poAndroid.registry;

import org.json.JSONException;
import org.json.JSONObject;


public class UpdateCheck {
    public static String GITHUB_LINK= "https://raw.githubusercontent.com/po-devs/android-client/master/updatecheck";
    private int version = 272;
    private String link = "www.test.com";

    public UpdateCheck() {}

    public UpdateCheck(String input) throws JSONException {
        JSONObject o = new JSONObject(input);
        version = o.getInt("v");
        link = o.getString("l");
    }

    public int getVersion() {
        return version;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        try {
            o.put("v", version);
            o.put("l", link);
            return o.toString();
        } catch (Exception e) {
            return "ERROR";
        }
    }
}
