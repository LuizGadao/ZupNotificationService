package br.com.luizgadao.testpushnotificationservices;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }
//
//    public class ZupTest extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... params) {
//            RestZup.setDebug(true);
//            RestZup.setApplicationId("d6b61870c79a0133a3fd021e75abe44c");
//            RestZup.setPushApplicationId("2c9f82e1511a711401511c3f06780000");
//            RestZup.setHost("https://vivo.dev.gateway.zup.me/push/v1/");
//
//            JSONObject mJsonObject = new JSONObject();
//            try {
//                mJsonObject.put("userId", "34988097703");
//                mJsonObject.put("token", "my token here.........");
//                mJsonObject.put("deviceId", "NEHIjio098-jsihfShdIf:fksh4j4Hul98k");
//
//                RestZup.post(mJsonObject);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//    }
}
