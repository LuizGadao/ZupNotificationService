package br.com.luizgadao.testpushnotificationservices;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import br.com.zup.zupnotificationservice.PushZupNotificationService;
import br.com.zup.zupnotificationservice.RestZup;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = MainActivityFragment.class.getSimpleName();

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btSubscribe = (Button) view.findViewById(R.id.btSubscribe);
        Button btUnSubscribe = (Button) view.findViewById(R.id.btUnSubscribe);

        btSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe();
            }
        });

        btUnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PushZupNotificationService(getContext()).unSubscribe();
            }
        });
    }

    private void subscribe() {
        PushZupNotificationService mZNS = new PushZupNotificationService(getContext())
                .setApplicationId("d6b61870c79a0133a3fd021e75abe44c")
                .setPushApplicationId("2c9f82e1511a711401511c3f06780000")
                .setHost("https://vivo.dev.gateway.zup.me/push/v1/")
                .setDebug(true);

        String userId = "34988097703";
        String deviceid = "NEHIjio098-jsihfShdIf:fksh4j4Hul98k";
        String token = "my token here.........";

        PushZupNotificationService.ResponseCallback mZNSCallback = new PushZupNotificationService.ResponseCallback() {
            @Override
            public void callback(RestZup.Reponse mResponse) {
                if (mResponse.isSuccess())
                    Log.i(TAG, "SUBSCRIBE-SUCCESS");
            }
        };

        mZNS.subscribe(userId, deviceid, token, mZNSCallback);
    }
}
