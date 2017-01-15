package com.podevs.android.poAndroid.registry;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.Timestamp;

/**
 * Custom Unhandled exception handler.
 *
 * example upload.php
 * <body>
 <p>< ?php</p>
 <p>$timestamp = isset($_POST['time']) ? $_POST['time'] : "";</p>
 <p>$error = isset($_POST['error']) ? $_POST['error'] : "";</p>
 <p>if ($timestamp != "" && $error != "") {</p>
 <p>$subError = isset($_POST['causeError']) ? $_POST['causeError'] : "";</p>
 <p> $stackTrace = isset($_POST['stackTrace']) ? $_POST['stackTrace'] : "";</p>
 <p>if ($subError != "" && $stackTrace != "") {</p>
 <p>$logCat = isset($_POST['logCat']) ? $_POST['logCat'] : "";</p>
 <p>$versionNumber = isset($_POST['versionNumber']) ? $_POST['versionNumber'] : "";</p>
 <p>$versionName = isset($_POST['versionNumber']) ? $_POST['versionNumber'] : "";</p>

 <p>// Write to file</p>
 <p>$handle = fopen("[filename]", "w+");</p>
 <p>fwrite($handle, "[text to write]");</p>
 <p>fclose($handle);</p>

 <p> // OR</p>
 <p>file_put_contents("[filename]", "[message]"."\n", FILE_APPEND);</p>

 <p> // Email</p>
 <p> mail("to what email address", "subject", "text", "who it is from: foobar@domainhostingthis");</p>
 <p> }</p>
 <p> }</p>
 <p> ?></p>
 </body>
 */

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    /**Log Tag*/
    private final String TAG = "Exception Handler";

    /**Whether or not it dumps to file*/
    public static boolean shouldWrite = false;

    /**The system's default handler*/
    private Thread.UncaughtExceptionHandler defaultHandler;

    private Integer versionCode = 50;

    private String versionName = "2.5.3.8";

    /**URL link to upload.php*/
    private String url = "meow";

    /**
     * Default Constructor
     *
     * @param context Context
     */
    public CustomExceptionHandler(Context context) {
        shouldWrite = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("shouldWrite", false);
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
            versionName = packageInfo.versionName;
        } catch (Exception e) {
            Log.e(TAG, "Error getting meta data", e);
        }
        mContext = context;
    }

    /**
     * Handling of error
     *
     * @param t Thread with error
     * @param e Error thrown
     */

    private Context mContext;

    public void uncaughtException(Thread t, final Throwable e) {
        if (!shouldWrite) {
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Toast toast = Toast.makeText(mContext, e.getClass().getName() + "\n" + Log.getStackTraceString(e), Toast.LENGTH_LONG);
                    ((TextView) ((ViewGroup) toast.getView()).getChildAt(0)).setTextSize(10);
                    toast.show();
                    Looper.loop();
                }
            }.start();

            try {
                Thread.sleep(10000);
            } catch (InterruptedException dontcare) {
                // don't care
            }
        }

        if (shouldWrite) {
            try {
                String timestamp = "default";
                String stackTrace = "default";
                String finalLogcat = "default";

                try {
                    timestamp = (new Timestamp(System.currentTimeMillis())).toString();
                } catch (Exception exception) {
                    Log.e(TAG, "Error in producing timestamp", exception);
                }

                try {
                    /*
                    final Writer result = new StringWriter();
                    final PrintWriter printWriter = new PrintWriter(result);
                    e.printStackTrace(printWriter);
                    stackTrace = result.toString();
                    printWriter.close();
                    */
                    stackTrace = Log.getStackTraceString(e);
                } catch (Exception exception) {
                    Log.e(TAG, "Error printing stack trace", exception);
                }

                try {
                    String[] cmd = new String[]{"logcat", "-d", "-s", "*:w"};

                    Process logcat = Runtime.getRuntime().exec(cmd);

                    BufferedReader br = new BufferedReader(new InputStreamReader(logcat.getInputStream()));

                    String line;
                    StringBuilder log = new StringBuilder();
                    String separator = System.getProperty("line.separator");
                    while ((line = br.readLine()) != null) {
                        log.append(line);
                        log.append(separator);
                    }

                    finalLogcat = log.toString();
                } catch (Exception exception) {
                    Log.e(TAG, "Error getting log from runtime", exception);
                }

                String[] info = new String[7];
                info[0] = timestamp;
                info[1] = e.getClass().getName();
                if (e.getCause() != null && e.getCause().getClass() != null) {
                    info[2] = (e.getCause().getClass().getName() != null ? e.getCause().getClass().getName() : "N/A");
                } else info[2] = "N/A";
                info[3] = stackTrace;
                info[4] = finalLogcat;
                info[5] = versionCode.toString();
                info[6] = versionName;

                writeToFile(info);

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        defaultHandler.uncaughtException(t, e);
    }

    /**
     * Uploads information to website
     *
     *
     * "time" info[0]
     * "error" info[1]
     * "causeError" info[2]
     * "stackTrace" info[3]
     * "logcat" info[4]
     * "versionNumber" info[5]
     * "versionName" info[6]
     *
     * @param info Array of info
     */

    /*
    private void upload(final String[] info) {
        final List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("time", info[0]));
        list.add(new BasicNameValuePair("error", info[1]));
        list.add(new BasicNameValuePair("causeError", info[2]));
        list.add(new BasicNameValuePair("stackTrace", info[3]));
        list.add(new BasicNameValuePair("logcat", info[4]));
        list.add(new BasicNameValuePair("versionNumber", info[5]));
        list.add(new BasicNameValuePair("versionName", info[6]));

        new Thread() {
            @Override
            public void run() {
                try {
                    DefaultHttpClient httpClient = new DefaultHttpClient();

                    HttpPost httpPost = new HttpPost(url);

                    httpPost.setEntity(new UrlEncodedFormEntity(list, HTTP.UTF_8));

                    httpClient.execute(httpPost);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    */

    /**
     * Writes info to file. Filename = POError" +info[0].replace(" ", "@").replace(":", ".") + ".txt"
     *
     * @param info Array of info
     */

    private void writeToFile(String[] info) {
        try {
            final String separator = System.getProperty("line.separator");
            String[] tags = new String[]{"Timestamp: ", "Main Error: ", "Cause Error: ", "Stack Trace: ", "Log Cat: ", "Version Number: ", "Version Name: "};
            BufferedWriter bos = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() + "/POError" +info[0].replace(" ", "@").replace(":", ".") + ".txt"));
            bos.write(tags[0] + info[0] + separator + tags[5] + info[5] + separator + tags[6] + info[6] + separator + tags[1] + info[1] + separator + tags[2] + info[2] + separator + tags[3] + info[3] + separator + separator + tags[4] + info[4]);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            Log.e(TAG, "Error writing to file", e);
        }
    }
}
