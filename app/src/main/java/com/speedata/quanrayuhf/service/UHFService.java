package com.speedata.quanrayuhf.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.bean.SpdReadData;
import com.speedata.libuhf.bean.SpdWriteData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;
import com.speedata.libuhf.interfaces.OnSpdReadListener;
import com.speedata.libuhf.interfaces.OnSpdWriteListener;
import com.speedata.libuhf.utils.SharedXmlUtil;
import com.speedata.libuhf.utils.StringUtils;
import com.speedata.quanrayuhf.utils.BCDUtils;
import com.speedata.quanrayuhf.utils.CrcOperateUtil;
import com.speedata.quanrayuhf.utils.DateUtils;
import com.speedata.quanrayuhf.utils.QuanrayDataUtils;
import com.speedata.quanrayuhf.utils.UHFReader;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by 张明_ on 2017/12/5.
 * Email 741183142@qq.com
 */

public class UHFService extends Service implements OnSpdReadListener, OnSpdInventoryListener, OnSpdWriteListener {
    private static final String TAG = "ZM";
    private ServerSocket serverSocket = null;//创建ServerSocket对象
    private List<Socket> clicksSocketLists = new ArrayList<>();
    private ServerSocketThread mServerSocketThread = null;
    private byte[] buf = new byte[1024];

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        UHFReader.getInstance(this).getReader().setOnReadListener(this);
        UHFReader.getInstance(this).getReader().setOnInventoryListener(this);
        UHFReader.getInstance(this).getReader().setOnWriteListener(this);

        //启动服务器监听线程
        if (mServerSocketThread == null) {
            mServerSocketThread = new ServerSocketThread();
            mServerSocketThread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void getInventoryData(SpdInventoryData var1) {
        String epc = var1.epc;
        byte[] epcBytes = StringUtils.stringToByte(epc);
        byte[] int2bytes = QuanrayDataUtils.int2bytes(epcBytes.length);
        byte[] concatAll = CrcOperateUtil.concatAll(new byte[]{0x00, 0x01}, int2bytes, epcBytes);
        byte[] sendData = QuanrayDataUtils.getSendData(concatAll, QuanrayDataUtils.CMD9);
        sendMsg(sendData);
    }

    @Override
    public void getReadData(SpdReadData var1) {
        int status = var1.getStatus();
        if (status != 0) {
            byte[] sendData = QuanrayDataUtils.getSendData(new byte[]{0x02}, QuanrayDataUtils.CMD10);
            sendMsg(sendData);
        } else {
            byte[] readData = var1.getReadData();
            int len = var1.getDataLen() / 2;
            byte[] int2bytes = QuanrayDataUtils.int2bytes(len);
            byte[] concatAll = CrcOperateUtil.concatAll(new byte[]{0x00}, int2bytes, readData);
            byte[] sendData = QuanrayDataUtils.getSendData(concatAll, QuanrayDataUtils.CMD10);
            sendMsg(sendData);
        }
        UHFReader.getInstance(this).getReader().CloseDev();
    }

    @Override
    public void getWriteData(SpdWriteData var1) {
        int status = var1.getStatus();
        if (status != 0) {
            byte[] sendData = QuanrayDataUtils.getSendData(new byte[]{0x02}, QuanrayDataUtils.CMD11);
            sendMsg(sendData);
        } else {
            byte[] sendData = QuanrayDataUtils.getSendData(new byte[]{0x00}, QuanrayDataUtils.CMD11);
            sendMsg(sendData);
        }
        UHFReader.getInstance(this).getReader().CloseDev();
    }


    /**
     * 服务器监听线程
     */
    private class ServerSocketThread extends Thread {

        @Override
        public void run() {
            try {
                if (serverSocket == null) {
                    serverSocket = new ServerSocket(6117);//监听port端口，这个程序的通信端口就是port了
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!interrupted()) {
                try {
                    //监听连接 ，如果无连接就会处于阻塞状态，一直在这等着
                    Socket clicksSocket = serverSocket.accept();
                    clicksSocketLists.add(clicksSocket);
                    InetAddress inetAddress = clicksSocket.getInetAddress();
                    //启动接收线程
                    String hostAddress = inetAddress.getHostAddress() + "已连接\n";
                    Log.d(TAG, "TCP连接成功：" + hostAddress);
                    ReceiveThread mReceiveThread = new ReceiveThread(clicksSocket);
                    mReceiveThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 接收线程
     */
    private class ReceiveThread extends Thread {
        private Socket clicksSocket;

        ReceiveThread(Socket clicksSocket) {
            this.clicksSocket = clicksSocket;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    InputStream inputstream = clicksSocket.getInputStream();
                    final int len = inputstream.read(buf);
                    if (len < 0) {
                        interrupt();
                        InetAddress inetAddress = clicksSocket.getInetAddress();
                        String hostAddress = inetAddress.getHostAddress() + "已断开\n";
                        Log.d(TAG, "TCP断开连接：" + hostAddress);
                        clicksSocketLists.remove(clicksSocket);
                        clicksSocket.close();
                        continue;
                    }
//                    String receiveStr = new String(buf, 0, len);
//                    Log.d(TAG, "接收16hex: " + StringUtils.byteToHexString(buf, buf.length));
                    //开始盘存
//                    buf = new byte[]{0x5B, 0x5B, 0x00, 0x03, 0x11, 0x01, 0x46, 0x5C, 0x03};
                    //功率读取5B5B0005110142001D861D
//                    buf = new byte[]{0x5B, 0x5B, 0x00, 0x03, 0x11, 0x01, 0x42, 0x1C, (byte) 0x87};
                    //频段读取5B5B000811014400038603A092E9
//                    buf = new byte[]{0x5B, 0x5B, 0x00, 0x03, 0x11, 0x01, 0x49, 0x7C, (byte) 0x41};
                    //读卡 5B5B0007110149000100004AAF
//                    buf = new byte[]{0x5B, 0x5B, 0x00, 0x09,
//                            0x11, 0x01, 0x49, 0x01, 0x12, 0x34, 0x00, 0x00, 0x01, 0x1D, (byte) 0xE3};
                    Log.d(TAG, "接收16hex: " + StringUtils.byteToHexString(buf, buf.length));
                    checkGetData(buf);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void checkGetData(byte[] data) {
        boolean checkData = QuanrayDataUtils.checkData(data);
        if (checkData) {
            byte[] cmd = QuanrayDataUtils.getCMD(data);
            if (Arrays.equals(cmd, QuanrayDataUtils.CMD1)) {
                //链接认证
                boolean haveUHF = SharedXmlUtil.getInstance(this).read("haveUHF", false);
                String yyyyMMddHHmmss = DateUtils.getCurrentTimeMillis("yyyyMMddHHmmss");
                byte[] bytes = BCDUtils.str2Bcd(yyyyMMddHHmmss);
                byte[] concatAll;
                if (haveUHF) {
                    concatAll = CrcOperateUtil.concatAll(new byte[]{0x00}, bytes);
                } else {
                    concatAll = CrcOperateUtil.concatAll(new byte[]{0x01}, bytes);
                }
                byte[] sendData = QuanrayDataUtils.getSendData(concatAll, QuanrayDataUtils.CMD1);
                sendMsg(sendData);
            } else if (Arrays.equals(cmd, QuanrayDataUtils.CMD2)) {
                //链接检测
                String yyyyMMddHHmmss = DateUtils.getCurrentTimeMillis("yyyyMMddHHmmss");
                byte[] bytes = BCDUtils.str2Bcd(yyyyMMddHHmmss);
                byte[] concatAll = CrcOperateUtil.concatAll(new byte[]{0x00}, bytes);
                byte[] sendData = QuanrayDataUtils.getSendData(concatAll, QuanrayDataUtils.CMD2);
                sendMsg(sendData);

            } else if (Arrays.equals(cmd, QuanrayDataUtils.CMD3)) {
                //功率读取
                byte[] power = UHFReader.getInstance(UHFService.this).getPower();
                byte[] sendData = QuanrayDataUtils.getSendData(power, QuanrayDataUtils.CMD3);
                sendMsg(sendData);
            } else if (Arrays.equals(cmd, QuanrayDataUtils.CMD4)) {
                //功率设置
                byte[] bytes = QuanrayDataUtils.getDATA(data);
                byte[] setPower = UHFReader.getInstance(UHFService.this).setPower(bytes);
                byte[] sendData = QuanrayDataUtils.getSendData(setPower, QuanrayDataUtils.CMD4);
                sendMsg(sendData);
            } else if (Arrays.equals(cmd, QuanrayDataUtils.CMD5)) {
                //频段读取
                byte[] region = UHFReader.getInstance(UHFService.this).getRegion();
                byte[] sendData = QuanrayDataUtils.getSendData(region, QuanrayDataUtils.CMD5);
                sendMsg(sendData);
            } else if (Arrays.equals(cmd, QuanrayDataUtils.CMD6)) {
                //频段设置
                byte[] bytes = QuanrayDataUtils.getDATA(data);
                byte[] setRegion = UHFReader.getInstance(UHFService.this).setRegion(bytes);
                byte[] sendData = QuanrayDataUtils.getSendData(setRegion, QuanrayDataUtils.CMD6);
                sendMsg(sendData);
            } else if (Arrays.equals(cmd, QuanrayDataUtils.CMD7)) {
                //启动盘存
                byte[] inventoryStart = UHFReader.getInstance(UHFService.this).inventoryStart();
                byte[] sendData = QuanrayDataUtils.getSendData(inventoryStart, QuanrayDataUtils.CMD7);
                sendMsg(sendData);
            } else if (Arrays.equals(cmd, QuanrayDataUtils.CMD8)) {
                //停止盘存
                byte[] inventoryStop = UHFReader.getInstance(UHFService.this).inventoryStop();
                byte[] sendData = QuanrayDataUtils.getSendData(inventoryStop, QuanrayDataUtils.CMD8);
                sendMsg(sendData);
            } else if (Arrays.equals(cmd, QuanrayDataUtils.CMD10)) {
                //读数据
                byte[] bytes = QuanrayDataUtils.getDATA(data);
                byte[] readArea = UHFReader.getInstance(UHFService.this).readArea(bytes);
                if (!Arrays.equals(readArea, new byte[]{0x00})) {
                    byte[] sendData = QuanrayDataUtils.getSendData(readArea, QuanrayDataUtils.CMD10);
                    sendMsg(sendData);
                }
            } else if (Arrays.equals(cmd, QuanrayDataUtils.CMD11)) {
                //写数据
                byte[] bytes = QuanrayDataUtils.getDATA(data);
                byte[] writeArea = UHFReader.getInstance(UHFService.this).writeArea(bytes);
                if (!Arrays.equals(writeArea, new byte[]{0x00})) {
                    byte[] sendData = QuanrayDataUtils.getSendData(writeArea, QuanrayDataUtils.CMD11);
                    sendMsg(sendData);
                }
            }
        }
    }

    //发送信息
    public void sendMsg(byte[] msg) {
        try {
            //获取输出流
            for (Socket clicksSocket : clicksSocketLists) {
                OutputStream outputStream = clicksSocket.getOutputStream();
                //发送数据
                outputStream.write(msg);
                Log.d(TAG, "sendMsg: " + StringUtils.byteToHexString(msg, msg.length));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        try {
            mServerSocketThread.interrupt();
            mServerSocketThread = null;
            if (clicksSocketLists.size() != 0) {
                for (Socket clicksSocketList : clicksSocketLists) {
                    clicksSocketList.close();
                }
            }
            serverSocket.close();
            clicksSocketLists.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
