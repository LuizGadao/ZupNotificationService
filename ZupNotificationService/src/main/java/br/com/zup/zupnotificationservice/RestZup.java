package br.com.zup.zupnotificationservice;

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
    protected static final String APP_ID = "appId";
    protected static final String HOST = "host";

    protected static final String X_APP_KEY = "X-Application-Key";
    private static final String SUBSCRIPTIONS = "subscriptions";

    private static final String TAG = PushZupNotificationService.class.getSimpleName();
    private static final int TIME_OUT = 10 * 1000;

    public static class Reponse{
        private String mMessage;
        private boolean mSuccess;

        public Reponse() {
        }

        public Reponse(String mMessage, boolean mSuccess) {
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
    }

    private static String getEndpointSubscribe(String host){
        return host + SUBSCRIPTIONS;
    }

    protected static Reponse subscribe(JSONObject jsonParam, String applicationId, String host, boolean debug) throws IOException, JSONException {
        return post(getEndpointSubscribe(host), jsonParam, applicationId, debug);
    }

    private static Reponse post(String urlRequest, JSONObject jsonParam, String applicationId, boolean debug) throws IOException {

        if (debug)
            Log.i(TAG, "subscribe-payload: " + jsonParam.toString());

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
        connection.setRequestMethod("POST");
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

        wr.writeBytes(jsonParam.toString());
        wr.flush();
        wr.close();

        Reponse mReponse = new Reponse();
        String output;
        int httpCode = connection.getResponseCode();
        if(httpCode == HttpURLConnection.HTTP_OK){
            output = getOutputString(new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8")));
            if (TextUtils.isEmpty(output))
                output = SUBSCRIPTIONS + "-success";
            Log.i(TAG, "" + output);
            mReponse.setSuccess(true);
        }else{
            output = getOutputString(new BufferedReader(new InputStreamReader(connection.getErrorStream(),"utf-8")));
            Log.i(TAG, "error: " + httpCode + " - " + output);
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

    public static String bytesToString(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bytesRead = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1)
            byteArrayOutputStream.write(buffer, 0, bytesRead);

        return new String(byteArrayOutputStream.toByteArray(), "UTF-8");
    }
}
