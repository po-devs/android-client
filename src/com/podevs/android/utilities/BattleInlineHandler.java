package com.podevs.android.utilities;

import android.graphics.Color;
import android.text.*;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.View;
import com.podevs.android.poAndroid.Command;
import com.podevs.android.poAndroid.NetworkService;
import com.podevs.android.poAndroid.chat.Channel;
import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.HashMap;

@Deprecated
public class BattleInlineHandler implements Html.TagHandler {
    private NetworkService netServ;
    private HashMap<String, String> attributes = new HashMap<String, String>();
    public Channel currentChannel;

    public BattleInlineHandler(NetworkService service) {
        netServ = service;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.equalsIgnoreCase("watch")) {
            if (opening) {
                // Opening Tag
                attributes.clear();
                try {
                    getAttributes(xmlReader);

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
                    try {
                        Integer id = Integer.parseInt(stringid);

                        end((SpannableStringBuilder) output, ClickableSpan.class, spectateSpan(id));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return;
        } else if (tag.equalsIgnoreCase("ping")) {
            if (opening && currentChannel != null) {
                Log.e("TagHandler", "Attempt flash");
                netServ.tryFlashChannel(currentChannel);
            }
            return;
        } else if (tag.equalsIgnoreCase("background")) {
            try {
                if (opening) {
                    attributes.clear();

                    getAttributes(xmlReader);

                    String stringcolor = attributes.get("color");
                    if (stringcolor != null) {
                        Integer color = Color.parseColor(stringcolor);

                        start((SpannableStringBuilder) output, backgroundColor(color));
                    }
                } else {
                    String stringcolor = attributes.get("color");
                    if (stringcolor != null) {
                        Integer color = Color.parseColor(stringcolor);

                        end((SpannableStringBuilder) output, BackgroundColorSpan.class, backgroundColor(color));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        } else if (tag.equalsIgnoreCase("timestamp")) {
            if (opening) {
                if (netServ.getSettings().timeStamp) {
                    output.append("(" + StringUtilities.timeStamp() + ") ");
                }
            }
            return;
        } else if (tag.equalsIgnoreCase("tr")) {
            if (!opening) {
                output.append("\n");
            }
            return;
        } else if (tag.equalsIgnoreCase("strike")) {
            if (opening) {
                start((SpannableStringBuilder) output, new StrikethroughSpan());
            } else {
                end((SpannableStringBuilder) output, StrikethroughSpan.class, new StrikethroughSpan());
            }
        } else if (tag.equalsIgnoreCase("posend")) {
            if (opening) {
                attributes.clear();

                getAttributes(xmlReader);

                String message = attributes.get("m");
                if (message != null) {
                    start((SpannableStringBuilder) output, poSend(message));
                }
            } else {
                String message = attributes.get("m");
                if (message != null) {
                    end((SpannableStringBuilder) output, ClickableSpan.class, poSend(message));
                }
            }
        } else if (tag.equalsIgnoreCase("pojoin")) {
            if (opening) {
                attributes.clear();

                getAttributes(xmlReader);

                String message = attributes.get("c");
                if (message != null) {
                    start((SpannableStringBuilder) output, poJoin(message));
                }
            } else {
                String message = attributes.get("c");
                if (message != null) {
                    end((SpannableStringBuilder) output, ClickableSpan.class, poJoin(message));
                }
            }
        } else if (tag.equalsIgnoreCase("poappend")) {
            if (opening) {
                attributes.clear();

                getAttributes(xmlReader);

                String message = attributes.get("m");
                if (message != null) {
                    start((SpannableStringBuilder) output, poAppend(message));
                }
            } else {
                String message = attributes.get("m");
                if (message != null) {
                    end((SpannableStringBuilder) output, ClickableSpan.class, poAppend(message));
                }
            }
        }
        //Log.e("TagHandler", "Unhandled " + tag);
    }

    private void getAttributes(XMLReader xmlReader) {
        try {
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
        } catch (Exception e) {

        }
    }

    private BackgroundColorSpan backgroundColor(int color) {
        return new BackgroundColorSpan(color);
    }

    private ClickableSpan poSend(final String message) {
        final int id = currentChannel.id;
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Baos b = new Baos();
                b.write(1);
                b.write(0);
                b.putInt(id);
                b.putString(message);
                netServ.socket.sendMessage(b, Command.SendMessage);
            }
        };
    }

    private ClickableSpan poJoin(final String channel) {
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Baos join = new Baos();
                join.putString(channel);
                if (netServ != null && netServ.socket != null && netServ.socket.isConnected())
                    netServ.socket.sendMessage(join, Command.JoinChannel);
            }
        };
    }

    private ClickableSpan poAppend(final String message) {
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (netServ != null && netServ.chatActivity != null) {
                    netServ.chatActivity.chatAppend(message);
                }
            }
        };
    }

    private ClickableSpan spectateSpan(final int id) {
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
