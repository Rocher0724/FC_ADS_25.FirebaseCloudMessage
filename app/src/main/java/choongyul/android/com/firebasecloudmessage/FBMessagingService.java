package choongyul.android.com.firebasecloudmessage;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FBMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FBMessagingService";

    public FBMessagingService() {
    }

    // 메시지를 받았을때 동작
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "하하하하하하하하");
        Log.e(TAG, "하하From: " + remoteMessage.getFrom());
        Log.e(TAG, "하하Message Notification Body: " + remoteMessage.getNotification().getBody());


    }
}
