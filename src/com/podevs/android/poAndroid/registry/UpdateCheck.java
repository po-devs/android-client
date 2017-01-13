package com.podevs.android.poAndroid.registry;

import org.json.JSONObject;


public class UpdateCheck {
    private String version = "270";
    private String link = "www.test.com";

    public UpdateCheck() {}

    public UpdateCheck(String input) {
        try {
            JSONObject o = new JSONObject(input);
            version = o.getString("v");
            link = o.getString("l");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getVersion() {
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
