package com.podevs.android.utilities;

import android.text.*;
import android.text.style.ClickableSpan;
import android.view.View;
import com.podevs.android.poAndroid.NetworkService;
import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.HashMap;

public class BattleInlineHandler implements Html.TagHandler {
    private NetworkService netServ;
    private HashMap<String, String> attributes;

    public BattleInlineHandler(NetworkService service) {
        netServ = service;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.equalsIgnoreCase("watch")) { // Disabled for now
            if (opening) {
                // Opening Tag
                attributes = new HashMap<String, String>();
                try { // Reflection is messy
                    Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
                    elementField.setAccessible(true);
                    Object element = elementField.get(xmlReader);
                    Field attsField = element.getClass().getDeclaredField("theAtts");
                    attsField.setAccessible(true);
                    Object atts = attsField.get(element);
                    Field dataField = atts.getClass().getDeclaredField("data");
                    dataField.setAccessible(true);
                    String[] data = (String[]) dataField.get(atts);
                    Field lengthField = atts.getClass().getDeclaredField("length");
                    lengthField.setAccessible(true);
                    int len = (Integer) lengthField.get(atts);

                    for (int i = 0; i < len; i++) {
                        attributes.put(data[i * 5 + 1], data[i * 5 + 4]);
                    }

                    String stringid = attributes.get("id");
                    if (stringid != null) {
                        Integer id = Integer.parseInt(stringid);

                        start((SpannableStringBuilder) output, spectateSpan(id));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Closing Tag
                String stringid = attributes.get("id");
                if (stringid != null) {
                    Integer id = Integer.parseInt(stringid);

                    end((SpannableStringBuilder) output, ClickableSpan.class, spectateSpan(id));
                }
            }
        }
    }

    private ClickableSpan spectateSpan(int id) {
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                netServ.startWatching(id);
            }
        };
    }

    private static void start(SpannableStringBuilder text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void end(SpannableStringBuilder text, Class kind, Object repl) {
        int len = text.length();
        Object object = getLast(text, kind);
        int where = text.getSpanStart(object);

        text.removeSpan(object);

        if (where != len) {
            text.setSpan(repl, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static Object getLast(Spanned text, Class kind) {
        Object[] objects = text.getSpans(0, text.length(), kind);

        if (objects.length == 0) {
            return null;
        } else {
            return objects[objects.length - 1];
        }
    }
}
