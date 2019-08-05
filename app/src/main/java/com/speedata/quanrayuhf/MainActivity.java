package com.speedata.quanrayuhf;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.speedata.libuhf.utils.StringUtils;
import com.speedata.quanrayuhf.service.UHFService;
import com.speedata.quanrayuhf.utils.BCDUtils;
import com.speedata.quanrayuhf.utils.CrcOperateUtil;
import com.speedata.quanrayuhf.utils.DateUtils;
import com.speedata.quanrayuhf.utils.QuanrayDataUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        boolean quanrayPassCRC = CrcOperateUtil.isQuanrayPassCRC(
//                new byte[]{0x5B, 0x5B, 0x00, 0x03, 0x11, 0x01, 0x46, 0x5C, 0x03});
        Intent ootStartIntent = new Intent(this, UHFService.class);
        ootStartIntent.setPackage("speedata.com.uhfservice");
        startService(ootStartIntent);

//        String yyyyMMddHHmmss = DateUtils.getCurrentTimeMillis("yyyyMMddHHmmss");
//        byte[] bytes = BCDUtils.str2Bcd(yyyyMMddHHmmss);
//        String bcd2Str = BCDUtils.bcd2Str(bytes);

//        byte[] chkBytes = CrcOperateUtil.getCHKBytes(new byte[]{0x11, 0x01, 0x49, 0x01, 0x12, 0x34,0x00,0x00,0x01});
//        String toHexString = StringUtils.byteToHexString(chkBytes, chkBytes.length);
//        Toast.makeText(this, toHexString, Toast.LENGTH_LONG).show();

    }
}
