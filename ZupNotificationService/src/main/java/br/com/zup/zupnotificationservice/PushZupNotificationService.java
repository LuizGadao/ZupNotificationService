package br.com.zup.zupnotificationservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    protected String mHost;
    protected String mApplicationId;
    protected String mPushApplicationId;
    protected String mUserId;
    protected String mDeviceId;
    protected String mToken;
    protected boolean mDebug;

    public PushZupNotificationService(Context mContext) {
        this.mContext = mContext;
    }

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
     * @param callback
     * @throws ZupNotificationServiceException
     */
    public void subscribe(@NonNull String userId, @NonNull String deviceId,
                          @NonNull String token, @NonNull ResponseCallback callback) throws ZupNotificationServiceException {
        checkInternetEnable();

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
            new ZupTask(new Subscribe()).execute(mParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkInternetEnable() throws ZupNotificationServiceException {
        if (! isNetworkAvailable(mContext))
            throw new ZupNotificationServiceException("internet disable in device");
    }

    /**
     * UNSUBSCRIBE
     * @param callback
     * @throws ZupNotificationServiceException
     */
    public void unSubscribe(@NonNull ResponseCallback callback) throws ZupNotificationServiceException {
        ZNSPreferences mZnsPreferences = ZNSPreferences.newInstance(mContext);
        if (mZnsPreferences.get(RestZup.PUSH_APP_ID).equals(""))
            throw new ZupNotificationServiceException("é necessário fazer o subscribe antes de tentar fazer o unsubscribe");

        checkInternetEnable();

        mResponseCallback = callback;
        restoreParams(mZnsPreferences);

        try {
            JSONObject mParams = new JSONObject();
            mParams.put(RestZup.PUSH_APP_ID, mPushApplicationId);
            mParams.put(RestZup.DEVICE_ID, mDeviceId);
            mParams.put(RestZup.USER_ID, mUserId);
            mParams.put(RestZup.PLATFORM, "ANDROID");
            new ZupTask(new UnSubscribe()).execute(mParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * UPDATE STAUS PUSH NOTIFICATION
     * @param status
     * @param pushMessageId
     * @param callback
     * @throws ZupNotificationServiceException
     */
    public void updateStatePush(@NonNull PushStatus status, @NonNull String pushMessageId, @NonNull ResponseCallback callback) throws ZupNotificationServiceException {
        ZNSPreferences mZnsPreferences = ZNSPreferences.newInstance(mContext);
        if (mZnsPreferences.get(RestZup.PUSH_APP_ID).equals(""))
            throw new ZupNotificationServiceException("é necessário fazer o subscribe antes de tentar fazer o update do status do push");

        checkInternetEnable();
        mResponseCallback = callback;
        restoreParams(mZnsPreferences);

        try {
            JSONObject mParams = new JSONObject();
            mParams.put("status", status);
            mParams.put("pushMessageId", pushMessageId);
            new ZupTask(new UpdateStatus()).execute(mParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void restoreParams(ZNSPreferences mZnsPreferences) {
        mApplicationId = mZnsPreferences.get(RestZup.X_APP_KEY);
        mPushApplicationId = mZnsPreferences.get(RestZup.PUSH_APP_ID);
        mDeviceId = mZnsPreferences.get(RestZup.DEVICE_ID);
        mUserId = mZnsPreferences.get(RestZup.USER_ID);
        mHost = mZnsPreferences.get(RestZup.HOST);
    }

    private interface ZNSRequest {
        RestZup.Reponse request(JSONObject mParams);
        void afterRequest();
    }

    private class Subscribe implements ZNSRequest {
        @Override
        public RestZup.Reponse request(JSONObject mParams) {
            try {
                return RestZup.subscribe(mParams, mApplicationId, mHost, mDebug);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void afterRequest() {
            saveParams();
        }
    }

    private class UnSubscribe implements ZNSRequest {
        @Override
        public RestZup.Reponse request(JSONObject mParams) {
            try {
                return RestZup.unSubscribe(mParams, mApplicationId, mHost, mDebug);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void afterRequest() {
            deleteParams();
        }
    }

    private class UpdateStatus implements ZNSRequest{
        @Override
        public RestZup.Reponse request(JSONObject mParams) {
            try {
                return RestZup.updateStatus(mParams, mApplicationId, mHost, mDebug);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void afterRequest() {

        }
    }

    protected class ZupTask extends AsyncTask<JSONObject, Void, RestZup.Reponse>{

        private ZNSRequest mRequest;
        public ZupTask(ZNSRequest request) {
            this.mRequest = request;
        }

        @Override
        protected RestZup.Reponse doInBackground(JSONObject... params) {
            JSONObject mParams = params[0];
            RestZup.Reponse mResponse = mRequest.request(mParams);
            return mResponse;
        }

        @Override
        protected void onPostExecute(RestZup.Reponse reponse) {
            super.onPostExecute(reponse);
            mRequest.afterRequest();
            mResponseCallback.callback(reponse);
        }
    }

    //****************************************** CALLBACK ******************************************
    public interface ResponseCallback{
        void callback(RestZup.Reponse mResponse);
    }

    //****************************************** PREFERENCES ******************************************
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

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public enum PushStatus{
        RECEIVED,
        READ
    }
}
