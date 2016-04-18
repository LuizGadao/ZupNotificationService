package br.com.zup.zupnotificationservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import br.com.zup.zupnotificationservice.exception.ZupNotificationServiceException;

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
    protected String mPushApplicationId;
    protected String mUserId;
    protected String mDeviceId;
    protected String mToken;
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
        this.mPushApplicationId = mZupApplicationId;
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
        mResponseCallback = callback;
        mUserId = userId;
        mDeviceId = deviceId;
        mToken = token;
        try {
            JSONObject mParams = new JSONObject();
            mParams.put(RestZup.PUSH_APP_ID, mPushApplicationId);
            mParams.put(RestZup.USER_ID, userId);
            mParams.put(RestZup.DEVICE_ID, deviceId);
            mParams.put(RestZup.TOKEN, token);
            mParams.put(RestZup.PLATFORM, "ANDROID");
            new ZupTask(true).execute(mParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void unSubscribe(@NonNull ResponseCallback callback) throws ZupNotificationServiceException {
        ZNSPreferences mZnsPreferences = ZNSPreferences.newInstance(mContext);
        if (mZnsPreferences.get(RestZup.PUSH_APP_ID).equals(""))
            throw new ZupNotificationServiceException("é necessário fazer o subscribe antes de tentar fazer o unsubscribe");

        mResponseCallback = callback;
        mApplicationId = mZnsPreferences.get(RestZup.X_APP_KEY);
        mPushApplicationId = mZnsPreferences.get(RestZup.PUSH_APP_ID);
        mDeviceId = mZnsPreferences.get(RestZup.DEVICE_ID);
        mUserId = mZnsPreferences.get(RestZup.USER_ID);
        mHost = mZnsPreferences.get(RestZup.HOST);

        try {
            JSONObject mParams = new JSONObject();
            mParams.put(RestZup.PUSH_APP_ID, mPushApplicationId);
            mParams.put(RestZup.DEVICE_ID, mDeviceId);
            mParams.put(RestZup.USER_ID, mUserId);
            mParams.put(RestZup.PLATFORM, "ANDROID");
            new ZupTask(false).execute(mParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected class ZupTask extends AsyncTask<JSONObject, Void, RestZup.Reponse>{

        boolean mIsSubscribe;

        public ZupTask(boolean mIsSubscribe) {
            this.mIsSubscribe = mIsSubscribe;
        }

        @Override
        protected RestZup.Reponse doInBackground(JSONObject... params) {
            JSONObject mParams = params[0];
            try {
                RestZup.Reponse mResponse = null;

                if (mIsSubscribe)
                    mResponse = RestZup.subscribe(mParams, mApplicationId, mHost, mDebug);
                else
                    mResponse = RestZup.unSubscribe(mParams, mApplicationId, mHost, mDebug);

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

            if (mIsSubscribe == true)
                saveParams();
            else
                deleteParams();

            mResponseCallback.callback(reponse);
        }
    }

    private void saveParams(){
        ZNSPreferences mZnsPreferences = ZNSPreferences.newInstance(mContext);
        mZnsPreferences.save(RestZup.PUSH_APP_ID, mPushApplicationId);
        mZnsPreferences.save(RestZup.X_APP_KEY, mApplicationId);
        mZnsPreferences.save(RestZup.USER_ID, mUserId);
        mZnsPreferences.save(RestZup.DEVICE_ID, mDeviceId);
        mZnsPreferences.save(RestZup.TOKEN, mToken);
        mZnsPreferences.save(RestZup.HOST, mHost);
    }

    private void deleteParams(){
        ZNSPreferences mZnsPreferences = ZNSPreferences.newInstance(mContext);
        mZnsPreferences.delete(RestZup.PUSH_APP_ID);
        mZnsPreferences.delete(RestZup.X_APP_KEY);
        mZnsPreferences.delete(RestZup.USER_ID);
        mZnsPreferences.delete(RestZup.DEVICE_ID);
        mZnsPreferences.delete(RestZup.TOKEN);
        mZnsPreferences.delete(RestZup.HOST);
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

        public void save(String mKey, String mValue){
            //RestZup.log("ZNSPreferences " + String.format("key:%s value:%s", mKey, mValue));
            mSharedPreferences
                    .edit()
                    .putString(mKey, mValue)
                    .apply();
        }

        public void delete(String mKey){
            if (mSharedPreferences.contains(mKey))
                mSharedPreferences.edit().remove(mKey).apply();
        }

        public String get(String mKey){
            return mSharedPreferences.getString(mKey, "");
        }
    }
}
