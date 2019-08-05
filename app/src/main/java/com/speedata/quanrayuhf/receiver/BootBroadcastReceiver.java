package com.speedata.quanrayuhf.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.speedata.quanrayuhf.MainActivity;
import com.speedata.quanrayuhf.service.UHFService;

/**
 * Created by 张明_ on 2017/8/18.
 * Email 741183142@qq.com
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String action_boot = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action_boot)) {
            Intent ootStartIntent = new Intent(context, UHFService.class);
            ootStartIntent.setPackage("speedata.com.uhfservice");
            context.startService(ootStartIntent);
        }

    }
}
