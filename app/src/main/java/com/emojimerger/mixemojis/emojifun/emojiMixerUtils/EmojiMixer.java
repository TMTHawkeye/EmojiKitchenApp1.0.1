package com.emojimerger.mixemojis.emojifun.emojiMixerUtils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.emojimerger.mixemojis.emojifun.R;

import java.net.HttpURLConnection;
import java.net.URL;

public class EmojiMixer implements Runnable {

    private final String emoji_1;
    private final String emoji_2;
    private final String creation_date;
    private final Activity mContext;
    private String LOG="";
    public String API ;
    public EmojiListener listener;
    private String finalURL;
    private String failure_reason;
    private boolean isTaskSuccessful = false;
    private boolean shouldAbortTask = false;
    private String fileName;


    public EmojiMixer(String emoji1, String emoji2, String date, Activity context, EmojiListener emojiListener) {
        this.listener = emojiListener;
        mContext = context;
        emoji_1 = emoji1;
        emoji_2 = emoji2;
        creation_date = date;
        API=context.getString(R.string.apiUrl);
        LOG=context.getString(R.string.apiLOG);
    }

    @Override
    public void run() {
        Log.d(LOG, "Emojis checker started with the following data:\nEmojis 1: " + emoji_1 + "\nEmoji 2: " + emoji_2 + "\nDate: " + creation_date);
        if (isConnected()) {
            checkIfImageEmojiInServer(emoji_1, emoji_2, creation_date);
        }
        mContext.runOnUiThread(() -> {
            if (isTaskSuccessful) {
                if (listener != null) {
                    listener.onSuccess(finalURL,fileName);
                }
            } else {
                String finalFailureUrl="";
                if (listener != null) {
                    finalFailureUrl = emoji_1 + "/" + emoji_1 + "_" + emoji_2 + ".png";
                    Log.d(LOG, "final url for failure is " + finalFailureUrl);
                    fileName=finalFailureUrl;

                    listener.onFailure( finalFailureUrl, fileName);
                }
            }
        });
    }


    public void checkIfImageEmojiInServer(String emoji1, String emoji2, String date) {
        if (!shouldAbortTask) {
            String Combination = "/" + emoji1 + "/" + emoji1 + "_" + emoji2 + ".png";
            finalURL = API + date + Combination;
            Log.d(LOG, "Checking final url: " + finalURL);
            if (checkImage(finalURL)) {
                isTaskSuccessful = true;
                Log.d(LOG, "Found a combination at:  " + finalURL);
                fileName=emoji1+"_"+emoji2;
            } else {
                Log.d(LOG, "Couldn't find a combination in the regular order, swap emojis then recheck...");
                checkReversedEmojis(emoji2, emoji1, date);

            }
        }
    }

    public void checkReversedEmojis(String emoji1, String emoji2, String date) {
        if (!shouldAbortTask) {
            String Combination = "/" + emoji1 + "/" + emoji1 + "_" + emoji2 + ".png";
            finalURL = API + date + Combination;
            Log.d(LOG, "Checking reversed url: " + finalURL);
            if (checkImage(finalURL)) {
                isTaskSuccessful = true;
                Log.d(LOG, "Found a combination at:  " + finalURL);
                fileName=emoji1+"_"+emoji2;

            } else {
                Log.d(LOG, "Couldn't find a combination in the reversed order, task failed.");
                failure_reason = "No combination found for selected emojis.";
                isTaskSuccessful = false;
            }
        }
    }

    public boolean checkImage(String url) {
        if (isConnected()) {
            try {
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod("HEAD");
                return con.getResponseCode() == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return true;
        } else {
            Log.d(LOG, "Device is not connected.");
            failure_reason = "Your device is not connected to the internet.";
            isTaskSuccessful = false;
            shouldAbortTask = true;
        }
        return false;
    }

    public void setListener(EmojiListener listener) {
        this.listener = listener;
    }


    public interface EmojiListener {
        void onSuccess(String emojiUrl,String fileName);

        void onFailure(String emojiUrl,String fileName);
    }
}
