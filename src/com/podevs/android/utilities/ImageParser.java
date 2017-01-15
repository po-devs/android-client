package com.podevs.android.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Base64;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * Custom Html.ImageGetter
 */

public class ImageParser implements MyHtml.ImageGetter {
    Context context;
    final static Pattern urlPattern =  Pattern.compile("^(http|https)\\:\\/\\/.*\\S\\.(jpg|png|bmp)$");

    public ImageParser(Context context) {
        // this.view = view;
        this.context = context;
    }

    @Override
    public Drawable getDrawable(String src, Attributes attributes) {
        Draw drawable = new Draw();
        // Handle external resources
        if (urlPattern.matcher(src).matches()) {
            GetURLImage task = new GetURLImage(drawable);
            task.execute(src);
            try {
                drawable = task.get(15L, TimeUnit.SECONDS); // Wait 15 Seconds for call.
            } catch (InterruptedException|TimeoutException|ExecutionException e) {}
        } else {
            // Handle local resources
            drawable = (new ResourceParser(context)).parseText(src);
        }
        if (drawable != null) {
            try {
                String width = attributes.getValue("width");
                String height = attributes.getValue("height");
                if (width != null && height != null) {
                    int iWidth = Integer.parseInt(width);
                    int iHeight = Integer.parseInt(height);
//                    drawable.drawable = new ScaleDrawable(drawable, 0, iWidth, iHeight).getDrawable();
                    drawable.drawable.setBounds(0, 0, iWidth, iHeight);
                    String background = attributes.getValue("background");
                    if (background != null) {
                        drawable.drawable.setColorFilter(Color.parseColor(background), PorterDuff.Mode.DST_OVER);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return drawable;
    }

    private class ResourceParser {
        final static String pkgName = "com.podevs.android.poAndroid";
        private Resources resources;

        private ResourceParser(Context context1) {
            resources = context1.getResources();
        }

        private Draw parseText(String src) {
            Draw draw = new Draw();
            Drawable drawable = getResource(src);
            draw.setBounds(drawable.getBounds());
            draw.drawable = drawable;
            return draw;
        }

        private Drawable getResource(String src) {
            String source = getLocalName(src);
            if (source.indexOf("base64:") == 0) {
                try {
                    source = source.replace("base64:", "");
                    byte[] decode = Base64.decode(source, Base64.DEFAULT);
                    Drawable drawable = new BitmapDrawable(resources, BitmapFactory.decodeByteArray(decode, 0, decode.length));
                    // drawable = new ScaleDrawable(drawable, 0, 55, 55).getDrawable();
                    drawable.setBounds(0,0, 50, 50);
                    return drawable;
                } catch (IllegalArgumentException e) {}
            }
            int Identifier = resources.getIdentifier(source, "drawable", pkgName);
            if (Identifier == 0) {
                return getResource("pi_0");
            } else {
                Drawable drawable = resources.getDrawable(Identifier);
                drawable.setBounds(0,0,drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                return drawable;
            }
        }

        private String getLocalName(String src) {
            try {
                if (src.indexOf("pokemon:") == 0) {
                    try {
                        src = src.replace("pokemon:", "");
                        if (src.indexOf("num=") == 0) {
                            src = src.replace("num=", "");
                            if (src.contains("&")) {
                                int i = Integer.parseInt(src.substring(0, src.indexOf("&")));
                                int j = i >> 16;
                                i = i - (j << 16);
                                return "p" + i + (j == 0 ? "" : "_" + j) + "_front" + (src.contains("shiny=true") ? "s" : "");
                            } else {
                                int i = Integer.parseInt(src);
                                return "p" + i + "_front";
                            }
                        } else {
                            int i = Integer.parseInt(src);
                            return "p" + i + "_front";
                        }
                    } catch (NumberFormatException e) {}
                } else if (src.indexOf("icon:") == 0) {
                    int i = Integer.parseInt(src.replace("icon:", ""));
                    int j = i >> 16;
                    i = i - (j << 16);
                    return "pi_" + i + (j == 0 ? "" : "_" + j);
                } else if (src.indexOf("trainer:") == 0) {
                    int i = Integer.parseInt(src.replace("trainer:", ""));
                    return "t" + i;
                } else if (src.indexOf("item:") == 0) {
                    int i = Integer.parseInt(src.replace("item:", ""));
                    return "i" + i;
                } else if (src.indexOf("data:image") == 0) {
                    src = src.replace("data:image/" , "").replace("x-png;base64,", "").replace("gif;base64,", "").replace("png;base64,", "");
                    src = "base64:" + src;
                }
            } catch (StringIndexOutOfBoundsException e) {}
            return src;
        }
    }

    private class GetURLImage extends AsyncTask<String, Void, Draw> {
        Draw mDrawable;

        public GetURLImage(Draw draw) {
            mDrawable = draw;
        }

        // protected void onPreExecute() {}

        @Override
        protected Draw doInBackground(String... params) {
            String src = params[0];
            Drawable drawable = getDrawable(src);
            mDrawable.setBounds(drawable.getBounds());
            mDrawable.drawable = drawable;
            return mDrawable;
        }

        // protected void onProgressUpdate(Void... progress) {}

        // protected void onPostExecute(Drawable result) {}

        private Drawable getDrawable(String url) {
            try {
                Drawable drawable = download(url);

                if (drawable != null) {
                    drawable.setBounds(0,0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                }
                return drawable;
            } catch (IOException e) {
                return null;
            }
        }

        private Drawable download(String url) throws IOException{
            Drawable ret = null;

            URL urlObj = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
            try {
                ret = Drawable.createFromStream(urlConnection.getInputStream(), "src");
            } finally {
                urlConnection.disconnect();
            }

            return ret;
        }

    }
}


/*
    Different approach. This would async write html images to the chat rather than having
    the main thread wait for a result from an async download then write to chat on the main thread
    Stranger but might work better if finished

    loadImages(chan);
    loadImages.execute(message);
	private class loadImages extends AsyncTask<CharSequence, ImageSpan, SpannableStringBuilder> {
		DisplayMetrics dms = new DisplayMetrics();
		Channel mChan;
		SpannableStringBuilder mSpannableStringBuilder;

		public loadImages(Channel chan) {
			mChan = chan;
		}

		protected void onPreExecute() {}

		@Override
		protected SpannableStringBuilder doInBackground(CharSequence... params) {
			CharSequence charSequence = params[0];
			Spanned spanned = Html.fromHtml((String) charSequence);
			if (spanned instanceof SpannableStringBuilder) {
				mSpannableStringBuilder = (SpannableStringBuilder) spanned;
			} else {
				mSpannableStringBuilder = new SpannableStringBuilder(spanned);
			}
			for (ImageSpan img : mSpannableStringBuilder.getSpans(0, mSpannableStringBuilder.length(), ImageSpan.class)) {
				publishProgress(img);
			}
			return mSpannableStringBuilder;
		}

		protected void onProgressUpdate(ImageSpan... progress) {
			ImageSpan img = progress[0];
			String src = img.getSource();
			if (src.contains("pokemon:")) {
				int test = src.indexOf("pokemon:");
				updateImage(img, new ImageSpan(resize(getResource("p2_front")), src));
			} else if (src.contains("icon:")) {
				updateImage(img, new ImageSpan(resize(getResource("pi_2")), src));
			}
			String text = "";
		}

		@Override
		protected void onPostExecute(SpannableStringBuilder result) {
			mChan.write(result);
		}

		private void updateImage(ImageSpan oldImage, ImageSpan newImage) {
			int start = mSpannableStringBuilder.getSpanStart(oldImage);
			int end = mSpannableStringBuilder.getSpanEnd(oldImage);
			mSpannableStringBuilder.removeSpan(oldImage);
			mSpannableStringBuilder.setSpan(newImage, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		private Drawable getResource(String localName) {
			int Identifier = getResources().getIdentifier(localName, "drawable", pkgName);
			if (Identifier == 0) {
				return getResource("pi_0");
			} else {
				return getResources().getDrawable(Identifier);
			}
		}

		private Drawable resize(Drawable d) {
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			return d;
		}
	}
 */