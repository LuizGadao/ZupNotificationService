package br.com.zup.zupnotificationservice;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Luiz on 08/04/16.
 */
public class RestZup {

    protected static final String DEVICE_ID = "deviceId";
    protected static final String USER_ID = "userId";
    protected static final String TOKEN = "token";
    protected static final String PLATFORM = "platform";
    protected static final String PUSH_APP_ID = "appId";
    protected static final String HOST = "host";

    protected static final String X_APP_KEY = "X-Application-Key";
    private static final String SUBSCRIPTIONS = "subscriptions";
    private static final String UPDATE_STATUS = "status";

    private static final String TAG = "ZupNotificationService";
    private static final int TIME_OUT = 10 * 1000;

    public static class Reponse{
        int mCode;
        private String mMessage;
        private boolean mSuccess;

        public Reponse() {
        }

        public Reponse(int code, String mMessage, boolean mSuccess) {
            this.mCode = code;
            this.mMessage = mMessage;
            this.mSuccess = mSuccess;
        }

        public String getMessage() {
            return mMessage;
        }

        public void setMessage(String mMessage) {
            this.mMessage = mMessage;
        }

        public boolean isSuccess() {
            return mSuccess;
        }

        public void setSuccess(boolean mSuccess) {
            this.mSuccess = mSuccess;
        }

        public int getCode() {
            return mCode;
        }

        public void setCode(int mCode) {
            this.mCode = mCode;
        }

        @Override
        public String toString() {
            return String.format("%d - %s", mCode, mMessage);
        }
    }

    private static String getEndpointSubscribe(String host){
        return host + SUBSCRIPTIONS;
    }

    private static String getEndpointUpdateStatus(String host){
        return host + UPDATE_STATUS;
    }

    protected static Reponse subscribe(JSONObject mParams, String mApplicationId, String mHost, boolean mDebug)
            throws IOException, JSONException {
        if (mDebug)
            log("subscribe-payload: " + mParams.toString());

        return getReponse(
                getHttpUrlConnection(getEndpointSubscribe(mHost), mApplicationId, "POST"),
                mParams,
                mDebug);
    }

    protected static Reponse unSubscribe(JSONObject mParams, String mApplicationId, String mHost, boolean mDebug)
            throws IOException, JSONException {
        if (mDebug)
            log("unSubscribe-payload: " + mParams.toString());

        return getReponse(
                getHttpUrlConnection(getEndpointSubscribe(mHost), mApplicationId, "DELETE"),
                mParams,
                mDebug);
    }

    public static Reponse updateStatus(JSONObject mParams, String mApplicationId, String mHost, boolean mDebug) throws IOException {
        if (mDebug)
            log("update-status: " + mParams.toString());

        return getReponse(
                getHttpUrlConnection(getEndpointUpdateStatus(mHost), mApplicationId, "PUT"),
                mParams,
                mDebug);
    }

    @NonNull
    private static Reponse getReponse(HttpURLConnection mConnection, JSONObject mParams, boolean mDebug) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(mConnection.getOutputStream());
        outputStream.writeBytes(mParams.toString());
        outputStream.flush();
        outputStream.close();

        Reponse mReponse = new Reponse();
        String output;
        int httpCode = mConnection.getResponseCode();
        mReponse.setCode(httpCode);
        if (httpCode == HttpURLConnection.HTTP_OK) {
            output = getOutputString(new BufferedReader(new InputStreamReader(mConnection.getInputStream(),"utf-8")));

            if (TextUtils.isEmpty(output))
                output = SUBSCRIPTIONS + "-success";

            if (mDebug) log(output);

            mReponse.setSuccess(true);
        } else {
            output = getOutputString(new BufferedReader(new InputStreamReader(mConnection.getErrorStream(),"utf-8")));

            if (mDebug) log("error: " + httpCode + " - " + output);

            mReponse.setSuccess(false);
        }
        mReponse.setMessage(output);
        return mReponse;
    }

    private static String getOutputString(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        String output = "";
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        output = sb.toString();

        return output;
    }

    private static HttpURLConnection getHttpUrlConnection(String urlRequest, String applicationId, String httpMethod)
            throws IOException {
        URL url = new URL(urlRequest);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setUseCaches(false);
        connection.setConnectTimeout(TIME_OUT);
        connection.setReadTimeout(TIME_OUT);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty(X_APP_KEY, applicationId);
        connection.setUseCaches(false);
        connection.setRequestMethod(httpMethod);

        return connection;
    }

    protected static void log(String mMessage){
        Log.i(TAG, mMessage);
    }

    public static String bytesToString(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bytesRead = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1)
            byteArrayOutputStream.write(buffer, 0, bytesRead);

        return new String(byteArrayOutputStream.toByteArray(), "UTF-8");
    }
}
