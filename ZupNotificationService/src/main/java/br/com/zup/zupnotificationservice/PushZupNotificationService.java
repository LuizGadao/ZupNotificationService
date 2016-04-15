package br.com.zup.zupnotificationservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Luiz on 11/04/16.
 */
public class PushZupNotificationService {

    private ResponseCallback mResponseCallback;
    private static Context mContext;

    public PushZupNotificationService(Context mContext) {
        this.mContext = mContext;
    }

    protected String mHost;
    protected String mApplicationId;
    protected String mUserId;
    protected String mToken;
    protected String mZupApplicationId;
    protected boolean mDebug;

    /**
     *
     * @param mUrl host ex: https://vivo.dev.gateway.zup.me/push/v1/
     */
    public PushZupNotificationService setHost(String mUrl) {
        mHost = mUrl;
        return this;
    }

    /**
     *
     * @param mApplicationId for identify your platform application
     */
    public PushZupNotificationService setApplicationId(String mApplicationId) {
        this.mApplicationId = mApplicationId;
        return this;
    }

    /**
     *
     * @param mZupApplicationId for identify your PUSH APPLICATION
     */
    public PushZupNotificationService setPushApplicationId(String mZupApplicationId) {
        this.mZupApplicationId = mZupApplicationId;
        return this;
    }

    /**
     *
     * @param mDebug
     */
    public PushZupNotificationService setDebug(Boolean mDebug) {
        this.mDebug = mDebug;
        return this;
    }

    /**
     *
     * @param userId for identify who is user your APP
     * @param deviceId for identify the device
     * @param token token that your receive from GooglePlayService reference: https://developers.google.com/cloud-messaging/android/start
     */
    public void subscribe(@NonNull String userId, @NonNull String deviceId,
                          @NonNull String token, @NonNull ResponseCallback callback) {
        // TODO: 14/04/16 valid fields
        mResponseCallback = callback;
        mUserId = userId;
        mToken = token;
        try {
            JSONObject mParams = new JSONObject();
            mParams.put(RestZup.APP_ID, mZupApplicationId);
            mParams.put(RestZup.USER_ID, userId);
            mParams.put(RestZup.DEVICE_ID, deviceId);
            mParams.put(RestZup.TOKEN, token);
            mParams.put(RestZup.PLATFORM, "ANDROID");
            new ZupTask().execute(mParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void unSubscribe(){
        Log.i("ZNS", "unsubscribe");

    }

    protected class ZupTask extends AsyncTask<JSONObject, Void, RestZup.Reponse>{

        @Override
        protected RestZup.Reponse doInBackground(JSONObject... params) {
            JSONObject mParams = params[0];
            try {
                RestZup.Reponse mResponse = RestZup.subscribe(mParams, mApplicationId, mHost, mDebug);
                return mResponse;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(RestZup.Reponse reponse) {
            super.onPostExecute(reponse);
            ZNSPreferences mZnsPreferences = ZNSPreferences.newInstance(mContext);
            mZnsPreferences.save(RestZup.APP_ID, mZupApplicationId);
            mZnsPreferences.save(RestZup.X_APP_KEY, mApplicationId);
            mZnsPreferences.save(RestZup.USER_ID, mUserId);
            mZnsPreferences.save(RestZup.TOKEN, mToken);
            mZnsPreferences.save(RestZup.HOST, mHost);
            mResponseCallback.callback(reponse);
        }
    }


    //****************************************** CALLBACK ******************************************
    public interface ResponseCallback{
        void callback(RestZup.Reponse mResponse);
    }

    protected static class ZNSPreferences{
        private SharedPreferences mSharedPreferences;
        private static ZNSPreferences mInstance;

        private ZNSPreferences(Context mContext) {
            this.mSharedPreferences = mContext.getSharedPreferences(ZNSPreferences.class.getSimpleName(), Context.MODE_PRIVATE);
        }

        public static ZNSPreferences newInstance(Context mContext){
            if (mInstance == null)
                mInstance = new ZNSPreferences(mContext);

            return mInstance;
        }

        public void save(String key, String value){
            Log.i("ZNSPreferences", String.format("key:%s value:%s", key, value));
            mSharedPreferences
                    .edit()
                    .putString(key, value)
                    .apply();
        }
    }

}
