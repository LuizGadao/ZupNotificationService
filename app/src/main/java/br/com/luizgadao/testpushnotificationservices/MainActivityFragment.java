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
import br.com.zup.zupnotificationservice.exception.ZupNotificationServiceException;

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
        Button btUpdateStatus = (Button) view.findViewById(R.id.btUpdateStatus);

        btSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe();
            }
        });

        btUnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unSubscribe();
            }
        });

        btUpdateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatus(PushZupNotificationService.PushStatus.READ);
            }
        });
    }

    private void unSubscribe() {
        PushZupNotificationService pushZupNotificationService = new PushZupNotificationService(getContext())
                .setDebug(true);

        try {
            pushZupNotificationService.unSubscribe(new PushZupNotificationService.ResponseCallback() {
                @Override
                public void callback(RestZup.Reponse mResponse) {
                    if (mResponse.isSuccess())
                        Log.i(TAG, "UNSUBSCRIBE-SUCCESS");
                    else
                        Log.i(TAG, "UNSUBSCRIBE-ERROR: " + mResponse.toString());
                }
            });
        } catch (ZupNotificationServiceException e) {
            e.printStackTrace();
        }
    }

    private void subscribe() {
        PushZupNotificationService mZNS = new PushZupNotificationService(getContext())
                .setApplicationId("f4dc9060e7be0133a5a1021e75abe44c")
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

        try {
            mZNS.subscribe(userId, deviceid, token, mZNSCallback);
        } catch (ZupNotificationServiceException e) {
            e.printStackTrace();
        }
    }


    private void updateStatus(PushZupNotificationService.PushStatus status){
        PushZupNotificationService pushZupNotificationService = new PushZupNotificationService(getContext()).setDebug(true);

        PushZupNotificationService.ResponseCallback mZNSCallback = new PushZupNotificationService.ResponseCallback() {
            @Override
            public void callback(RestZup.Reponse mResponse) {
                if (mResponse.isSuccess())
                    Log.i(TAG, "UPDATE-STATUS-SUCCESS");
            }
        };

        try {
            pushZupNotificationService.updateStatePush(status, "cHVzaF92aXZvX2RlZmF1bHRfMjAxNjA0fEFWUGhraTFIN1EtZUhId2JTVjdE", mZNSCallback);
        } catch (ZupNotificationServiceException e) {
            e.printStackTrace();
        }
    }

}
