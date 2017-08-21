package com.podevs.android.utilities;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.*;
import android.text.style.*;
import android.view.View;
import com.podevs.android.poAndroid.Command;
import com.podevs.android.poAndroid.NetworkService;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.chat.Channel;
import org.ccil.cowan.tagsoup.*;
import org.xml.sax.*;

import java.io.IOException;
import java.io.StringReader;

// Custom implementation of android.text.html

public class MyHtml {

    public interface ImageGetter {
        Drawable getDrawable(String source, Attributes attributes);
    }

    private MyHtml() { }

    public static Spanned fromHtml(String source) {
        return fromHtml(source, null, null, null, null);
    }

    private static class HtmlParser {
        private static final HTMLSchema schema = new HTMLSchema();
    }

    public static Spanned fromHtml(String source, ImageGetter imageGetter, Html.TagHandler tagHandler, Channel chan, NetworkService netServ) {
        org.ccil.cowan.tagsoup.Parser parser = new org.ccil.cowan.tagsoup.Parser();
        try {
            parser.setProperty(org.ccil.cowan.tagsoup.Parser.schemaProperty, HtmlParser.schema);
        } catch (org.xml.sax.SAXNotRecognizedException | org.xml.sax.SAXNotSupportedException e) {
            throw new RuntimeException(e);
        }

        HtmlToSpannedConverter converter = new HtmlToSpannedConverter(source, imageGetter, tagHandler, parser, chan, netServ);
        return converter.convert();
    }

    public static String escapeHtml(CharSequence text) {
        StringBuilder out = new StringBuilder();
        withinStyle(out, text, 0, text.length());
        return out.toString();
    }

    private static void withinStyle(StringBuilder out, CharSequence text,
                                    int start, int end) {
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);

            if (c == '<') {
                out.append("&lt;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '&') {
                out.append("&amp;");
            } else if (c >= 0xD800 && c <= 0xDFFF) {
                if (c < 0xDC00 && i + 1 < end) {
                    char d = text.charAt(i + 1);
                    if (d >= 0xDC00 && d <= 0xDFFF) {
                        i++;
                        int codepoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
                        out.append("&#").append(codepoint).append(";");
                    }
                }
            } else if (c > 0x7E || c < ' ') {
                out.append("&#").append((int) c).append(";");
            } else if (c == ' ') {
                while (i + 1 < end && text.charAt(i + 1) == ' ') {
                    out.append("&nbsp;");
                    i++;
                }

                out.append(' ');
            } else {
                out.append(c);
            }
        }
    }
}

class HtmlToSpannedConverter implements ContentHandler {

    private static final float[] HEADER_SIZES = {
            1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f,
    };

    private String mSource;
    private XMLReader mReader;
    private SpannableStringBuilder mSpannableStringBuilder;
    private MyHtml.ImageGetter mImageGetter;
    private Html.TagHandler mTagHandler;
    private NetworkService mNetServ;
    private Channel mChan;

    public HtmlToSpannedConverter(String source, MyHtml.ImageGetter imageGetter, Html.TagHandler tagHandler, org.ccil.cowan.tagsoup.Parser parser, Channel chan, NetworkService netServ) {
        mSource = source;
        mSpannableStringBuilder = new SpannableStringBuilder();
        mImageGetter = imageGetter;
        mTagHandler = tagHandler;
        mReader = parser;
        mNetServ = netServ;
        mChan = chan;
    }

    public Spanned convert() {

        mReader.setContentHandler(this);
        try {
            mReader.parse(new InputSource(new StringReader(mSource)));
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }


        Object[] obj = mSpannableStringBuilder.getSpans(0, mSpannableStringBuilder.length(), ParagraphStyle.class);
        for (int i = 0; i < obj.length; i++) {
            int start = mSpannableStringBuilder.getSpanStart(obj[i]);
            int end = mSpannableStringBuilder.getSpanEnd(obj[i]);


            if (end - 2 >= 0) {
                if (mSpannableStringBuilder.charAt(end - 1) == '\n' &&
                        mSpannableStringBuilder.charAt(end - 2) == '\n') {
                    end--;
                }
            }

            if (end == start) {
                mSpannableStringBuilder.removeSpan(obj[i]);
            } else {
                mSpannableStringBuilder.setSpan(obj[i], start, end, Spannable.SPAN_PARAGRAPH);
            }
        }

        return mSpannableStringBuilder;
    }

    private void handleStartTag(String tag, Attributes attributes) {
        if (tag.equalsIgnoreCase("br")) {


        } else if (tag.equalsIgnoreCase("p")) {
            handleP(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("div")) {
            handleP(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strong")) {
            start(mSpannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("b")) {
            start(mSpannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("em")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("cite")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("dfn")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("i")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("big")) {
            start(mSpannableStringBuilder, new Big());
        } else if (tag.equalsIgnoreCase("small")) {
            start(mSpannableStringBuilder, new Small());
        } else if (tag.equalsIgnoreCase("font")) {
            startFont(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            handleP(mSpannableStringBuilder);
            start(mSpannableStringBuilder, new Blockquote());
        } else if (tag.equalsIgnoreCase("tt")) {
            start(mSpannableStringBuilder, new Monospace());
        } else if (tag.equalsIgnoreCase("a")) {
            handleA(mSpannableStringBuilder, attributes, mChan, mNetServ);
        } else if (tag.equalsIgnoreCase("u")) {
            start(mSpannableStringBuilder, new Underline());
        } else if (tag.equalsIgnoreCase("sup")) {
            start(mSpannableStringBuilder, new Super());
        } else if (tag.equalsIgnoreCase("sub")) {
            start(mSpannableStringBuilder, new Sub());
        } else if (tag.length() == 2 &&
                Character.toLowerCase(tag.charAt(0)) == 'h' &&
                tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            handleP(mSpannableStringBuilder);
            start(mSpannableStringBuilder, new Header(tag.charAt(1) - '1'));
        } else if (tag.equalsIgnoreCase("img")) {
            startImg(mSpannableStringBuilder, attributes, mImageGetter);
        } else if (tag.equalsIgnoreCase("ping")) {
            startPing(mChan, mNetServ);
        } else if (tag.equalsIgnoreCase("background")) {
            startBackground(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("timestamp")) {
            if (mNetServ != null && mNetServ.getSettings().timeStamp) {
                String timestamp = "(" + StringUtilities.timeStamp() + ") ";
                mSpannableStringBuilder.append(timestamp);
            }
        } else if (tag.equalsIgnoreCase("strike")) {
            start(mSpannableStringBuilder, new Strikethrough());
        } else if (mTagHandler != null) {
            mTagHandler.handleTag(true, tag, mSpannableStringBuilder, mReader);
        }
    }

    private void handleEndTag(String tag) {
        if (tag.equalsIgnoreCase("br")) {
            handleBr(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("p")) {
            handleP(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("div")) {
            handleP(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strong")) {
            end(mSpannableStringBuilder, Bold.class, new StyleSpan(Typeface.BOLD));
        } else if (tag.equalsIgnoreCase("b")) {
            end(mSpannableStringBuilder, Bold.class, new StyleSpan(Typeface.BOLD));
        } else if (tag.equalsIgnoreCase("em")) {
            end(mSpannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("cite")) {
            end(mSpannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("dfn")) {
            end(mSpannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("i")) {
            end(mSpannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("big")) {
            end(mSpannableStringBuilder, Big.class, new RelativeSizeSpan(1.25f));
        } else if (tag.equalsIgnoreCase("small")) {
            end(mSpannableStringBuilder, Small.class, new RelativeSizeSpan(0.8f));
        } else if (tag.equalsIgnoreCase("font")) {
            endFont(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            handleP(mSpannableStringBuilder);
            end(mSpannableStringBuilder, Blockquote.class, new QuoteSpan());
        } else if (tag.equalsIgnoreCase("tt")) {
            end(mSpannableStringBuilder, Monospace.class,
                    new TypefaceSpan("monospace"));
        } else if (tag.equalsIgnoreCase("a")) {
            handleAEnd(mSpannableStringBuilder, mChan, mNetServ);
        } else if (tag.equalsIgnoreCase("u")) {
            end(mSpannableStringBuilder, Underline.class, new UnderlineSpan());
        } else if (tag.equalsIgnoreCase("sup")) {
            end(mSpannableStringBuilder, Super.class, new SuperscriptSpan());
        } else if (tag.equalsIgnoreCase("sub")) {
            end(mSpannableStringBuilder, Sub.class, new SubscriptSpan());
        } else if (tag.length() == 2 &&
                Character.toLowerCase(tag.charAt(0)) == 'h' &&
                tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            handleP(mSpannableStringBuilder);
            endHeader(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("tr")) {
            mSpannableStringBuilder.append("\n");
        } else if (tag.equalsIgnoreCase("background")) {
            endBackground(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strike")) {
            end(mSpannableStringBuilder, Strikethrough.class, new StrikethroughSpan());
        } else if (mTagHandler != null) {
            mTagHandler.handleTag(false, tag, mSpannableStringBuilder, mReader);
        }
    }

    private static void handleP(SpannableStringBuilder text) {
        int len = text.length();

        if (len >= 1 && text.charAt(len - 1) == '\n') {
            if (len >= 2 && text.charAt(len - 2) == '\n') {
                return;
            }

            text.append("\n");
            return;
        }

        if (len != 0) {
            text.append("\n\n");
        }
    }

    private static void handleBr(SpannableStringBuilder text) {
        text.append("\n");
    }

    private static Object getLast(Spanned text, Class kind) {

        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private static void start(SpannableStringBuilder text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void end(SpannableStringBuilder text, Class kind,
                            Object repl) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static void startImg(SpannableStringBuilder text, Attributes attributes, MyHtml.ImageGetter img) {
        String src = attributes.getValue("", "src");
        Drawable d = null;

        if (img != null) {
            d = img.getDrawable(src, attributes);
        }

        if (d == null) {
            d = Resources.getSystem().getDrawable(R.drawable.pi_0);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        }

        int len = text.length();
        text.append("\uFFFC");

        text.setSpan(new ImageSpan(d, src), len, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void startFont(SpannableStringBuilder text, Attributes attributes) {
        String color = attributes.getValue("", "color");
        String face = attributes.getValue("", "face");

        int len = text.length();
        text.setSpan(new Font(color, face), len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void endFont(SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Font.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            Font f = (Font) obj;

            if (!TextUtils.isEmpty(f.mColor)) {
                if (f.mColor.startsWith("@")) {
                    Resources res = Resources.getSystem();
                    String name = f.mColor.substring(1);
                    int colorRes = res.getIdentifier(name, "color", "android");
                    if (colorRes != 0) {
                        ColorStateList colors = res.getColorStateList(colorRes);
                        text.setSpan(new TextAppearanceSpan(null, 0, 0, colors, null),
                                where, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    int color = 0;
                    color = MyColor.getHtmlColor(f.mColor);
                    if (color != -1) {
                        text.setSpan(new ForegroundColorSpan(color | 0xFF000000),
                                where, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            if (f.mFace != null) {
                text.setSpan(new TypefaceSpan(f.mFace), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void handleA(SpannableStringBuilder text, Attributes attributes, Channel channel, NetworkService netServ) {
        String href = attributes.getValue("", "href");
        String color = attributes.getValue("", "style");
        int c = -1;
        if (color != null && !color.equals("")) {
            color = color.substring(6);
            try {
                c = MyColor.parseColor(color);
            } catch (IllegalArgumentException e) {
                c = MyColor.BLACK;
            }
        }
        int len = text.length();
        if (href != null && href.startsWith("po:")) {
            href = href.substring(3);
            if (href.startsWith("join/")) {
                href = href.substring(5);
                text.setSpan(new Href(href, HrefType.join, c), len, len, Spannable.SPAN_MARK_MARK);
            } else if (href.startsWith("watch/")) {
                href = href.substring(6);
                text.setSpan(new Href(href, HrefType.watch, c), len, len, Spannable.SPAN_MARK_MARK);
            } else if (href.startsWith("watchplayer/")) {
                href = href.substring(12);
                text.setSpan(new Href(href, HrefType.watchplayer, c), len, len, Spannable.SPAN_MARK_MARK);
            } else if (href.startsWith("pm/")) {
                href = href.substring(3);
                text.setSpan(new Href(href, HrefType.pm, c), len, len, Spannable.SPAN_MARK_MARK);
            } else if (href.startsWith("ignore/")) {
                href = href.substring(7);
                text.setSpan(new Href(href, HrefType.ignore, c), len, len, Spannable.SPAN_MARK_MARK);
            // } else if (href.startsWith("info/")) {
                //href = href.substring(5);
                //text.setSpan(new Href(href, HrefType.info, c), len, len, Spannable.SPAN_MARK_MARK);
            } else if (href.startsWith("send/")) {
                href = href.substring(5);
                text.setSpan(new Href(href, HrefType.send, c), len, len, Spannable.SPAN_MARK_MARK);
            } else if (href.startsWith("setmsg/")) {
                href = href.substring(7);
                text.setSpan(new Href(href, HrefType.setmsg, c), len, len, Spannable.SPAN_MARK_MARK);
            } else if (href.startsWith("appendmsg/")) {
                href = href.substring(10);
                text.setSpan(new Href(href, HrefType.appendmsg, c), len, len, Spannable.SPAN_MARK_MARK);
            }
        } else {
            text.setSpan(new Href(href, HrefType.url), len, len, Spannable.SPAN_MARK_MARK);
        }
    }

    private static void handleAEnd(SpannableStringBuilder text, Channel channel, NetworkService netServ) {
        int len = text.length();
        Object obj = getLast(text, Href.class);
        int where = text.getSpanStart(obj);

        if (where != len) {
            Href h = (Href) obj;

            switch (h.mType) {
                case url: {
                    if (h.mHref != null) {
                        text.setSpan(new URLSpan(h.mHref), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                }case join: {
                    if (h.mHref != null) {
                        text.setSpan(poJoin(h.mHref, netServ), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                }case watch: {
                    if (h.mHref != null) {
                        text.setSpan(poWatch(h.mHref, netServ), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                }case watchplayer: {
                    if (h.mHref != null) {
                        text.setSpan(poWatchPlayer(h.mHref, netServ), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                }case pm: {
                    if (h.mHref != null) {
                        text.setSpan(poPM(h.mHref, netServ), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                }case ignore: {
                    if (h.mHref != null) {
                        text.setSpan(poIgnore(h.mHref, netServ), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                }case info: {

                    break;
                }case send: {
                    if (h.mHref != null) {
                        text.setSpan(poSend(h.mHref, channel, netServ, h.mColor), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                }case setmsg: {
                    if (h.mHref != null) {
                        text.setSpan(poSetMsg(h.mHref, netServ), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                }case appendmsg: {
                    if (h.mHref != null) {
                        text.setSpan(poAppend(h.mHref, netServ), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    break;
                }
            }
        }
    }

    private static void startBackground(SpannableStringBuilder text, Attributes attributes) {
        String color = attributes.getValue("","color");
        int c = -1;
        if (color != null && !color.equals("")) {
            try {
                c = MyColor.parseColor(color);
            } catch (IllegalArgumentException e) {
                c = MyColor.BLACK;
            }
        }
        start(text, new BackgroundColorSpan(c));
    }

    private static void endBackground(SpannableStringBuilder text) {
        Object last = getLast(text, BackgroundColorSpan.class);
        end(text, BackgroundColorSpan.class, last);
    }

    private static ClickableSpan poIgnore(final String idOrName, final NetworkService netServ) {
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (netServ != null) {
                    netServ.poIgnore(idOrName);
                }
            }
        };
    }

    private static ClickableSpan poSend(final String message, Channel channel, final NetworkService netServ, final int color) {
        final int id = channel.id;
        if (color == -1 ) {
            return new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (netServ != null) {
                        Baos b = new Baos();
                        b.write(1);
                        b.write(0);
                        b.putInt(id);
                        b.putString(message);
                        netServ.socket.sendMessage(b, Command.SendMessage);
                    }
                }
            };
        } else {
            return new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (netServ != null) {
                        Baos b = new Baos();
                        b.write(1);
                        b.write(0);
                        b.putInt(id);
                        b.putString(message);
                        netServ.socket.sendMessage(b, Command.SendMessage);
                    }
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(color);
                    ds.setUnderlineText(true);
                }
            };
        }
    }

    private static ClickableSpan poJoin(final String channel, final NetworkService netServ) {
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

    private static ClickableSpan poPM(final String idOrName, final NetworkService netServ) {
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (netServ != null) {
                    netServ.poPM(idOrName);
                }
            }
        };
    }

    private static ClickableSpan poSetMsg(final String message, final NetworkService netServ) {
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (netServ != null && netServ.chatActivity != null) {
                    netServ.chatActivity.chatSetMsg(message);
                }
            }
        };
    }


    private static ClickableSpan poAppend(final String message, final NetworkService netServ) {
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (netServ != null && netServ.chatActivity != null) {
                    netServ.chatActivity.chatAppend(message);
                }
            }
        };
    }

    private static ClickableSpan poWatch(final String s, final NetworkService netServ) {
        final int id = Integer.parseInt(s);
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (netServ != null) {
                    netServ.startWatching(id);
                }
            }
        };
    }

    private static ClickableSpan poWatchPlayer(final String s, final NetworkService netServ) {
        final int id = Integer.parseInt(s);
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (netServ != null) {
                    netServ.poWatchPlayer(s);
                }
            }
        };
    }

    private static void endHeader(SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Header.class);

        int where = text.getSpanStart(obj);

        text.removeSpan(obj);


        while (len > where && text.charAt(len - 1) == '\n') {
            len--;
        }

        if (where != len) {
            Header h = (Header) obj;

            text.setSpan(new RelativeSizeSpan(HEADER_SIZES[h.mLevel]),
                    where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new StyleSpan(Typeface.BOLD),
                    where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static void startPing(Channel chan, NetworkService netServ) {
        if (chan != null) {
            netServ.tryFlashChannel(chan);
        }
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        handleStartTag(localName, attributes);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        handleEndTag(localName);
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char c = ch[i + start];

            if (c == ' ' || c == '\n') {
                char pred;
                int len = sb.length();

                if (len == 0) {
                    len = mSpannableStringBuilder.length();

                    if (len == 0) {
                        pred = '\n';
                    } else {
                        pred = mSpannableStringBuilder.charAt(len - 1);
                    }
                } else {
                    pred = sb.charAt(len - 1);
                }

                if (pred != ' ' && pred != '\n') {
                    sb.append(' ');
                }
            } else {
                sb.append(c);
            }
        }

        mSpannableStringBuilder.append(sb);
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    private static class Bold { }
    private static class Italic { }
    private static class Underline { }
    private static class Big { }
    private static class Small { }
    private static class Monospace { }
    private static class Blockquote { }
    private static class Super { }
    private static class Sub { }
    private static class Strikethrough { }

    private static class Font {
        public String mColor;
        public String mFace;

        public Font(String color, String face) {
            mColor = color;
            mFace = face;
        }
    }

    private static enum HrefType {
        url,
        join,
        watch,
        watchplayer,
        pm,
        ignore,
        info,
        send,
        setmsg,
        appendmsg
    }

    private static class Href {
        String mHref;
        HrefType mType;
        int mColor = -1;

        Href(String href, HrefType type) {
            mHref = href;
            mType = type;
        }

        Href(String href, HrefType type, int color) {
            mHref = href;
            mType = type;
            mColor = color;
        }
    }

    private static class Header {
        private int mLevel;

        Header(int level) {
            mLevel = level;
        }
    }
}
