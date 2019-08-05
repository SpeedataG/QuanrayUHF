package com.speedata.quanrayuhf.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.utils.SharedXmlUtil;

import java.util.Arrays;

/**
 * Created by 张明_ on 2017/12/5.
 * Email 741183142@qq.com
 */

public class UHFReader {
    private static final Object LOCK = new Object();
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static UHFReader uhfReader;
    private IUHFService iuhfService;
    private byte[] successBytes = {0x00};
    private byte[] failedBytes = {0x01};

    public static UHFReader getInstance(Context context) {
        mContext = context;
        if (uhfReader == null) {
            synchronized (LOCK) {
                if (uhfReader == null) {
                    uhfReader = new UHFReader();
                }
            }
        }
        return uhfReader;
    }

    public synchronized IUHFService getReader() {
        try {
            if (iuhfService == null) {
                iuhfService = UHFManager.getUHFService(mContext);
                SharedXmlUtil.getInstance(mContext).write("haveUHF", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "模块不识别", Toast.LENGTH_SHORT).show();
            SharedXmlUtil.getInstance(mContext).write("haveUHF", false);
        }
        return iuhfService;
    }

    /**
     * 开始盘存
     *
     * @return
     */
    public byte[] inventoryStart() {
        int openDev = getReader().OpenDev();
        if (openDev == 0) {
            getReader().newInventoryStart();
            return successBytes;
        }
        return failedBytes;
    }

    /**
     * 开始盘存
     *
     * @return
     */
    public byte[] inventoryStop() {
        getReader().newInventoryStop();
        getReader().CloseDev();
        return successBytes;
    }

    /**
     * 获取功率
     *
     * @return
     */
    public byte[] getPower() {
        int openDev = getReader().OpenDev();
        if (openDev != 0) {
            return failedBytes;
        }
        int antennaPower = getReader().get_antenna_power();
        getReader().CloseDev();
        if (antennaPower != -1) {
            byte[] bytes = QuanrayDataUtils.int2bytes(antennaPower);
            return CrcOperateUtil.concatAll(successBytes, bytes);
        } else {
            return failedBytes;
        }
    }

    /**
     * 设置功率
     *
     * @return
     */
    public byte[] setPower(byte[] data) {
        int openDev = getReader().OpenDev();
        if (openDev != 0) {
            return failedBytes;
        }
        int anInt = QuanrayDataUtils.bytes2int(data);
        int setAntennaPower = getReader().set_antenna_power(anInt);
        getReader().CloseDev();
        if (setAntennaPower == 0) {
            return successBytes;
        } else {
            return failedBytes;
        }
    }


    /**
     * 获取频段
     *
     * @return
     */
    public byte[] getRegion() {
        int openDev = getReader().OpenDev();
        if (openDev != 0) {
            return failedBytes;
        }
        int freqRegion = getReader().get_freq_region();
        getReader().CloseDev();
        if (freqRegion == -1) {
            return failedBytes;
        } else {
            if (freqRegion == 0) {
                return CrcOperateUtil.concatAll(
                        successBytes, QuanrayDataUtils.F840, QuanrayDataUtils.F845);
            } else if (freqRegion == 1) {
                return CrcOperateUtil.concatAll(
                        successBytes, QuanrayDataUtils.F920, QuanrayDataUtils.F925);
            } else if (freqRegion == 2) {
                return CrcOperateUtil.concatAll(
                        successBytes, QuanrayDataUtils.F902, QuanrayDataUtils.F928);
            } else if (freqRegion == 3) {
                return CrcOperateUtil.concatAll(
                        successBytes, QuanrayDataUtils.F865, QuanrayDataUtils.F868);
            } else {
                return failedBytes;
            }
        }
    }

    /**
     * 频段设置
     *
     * @param data
     * @return
     */
    public byte[] setRegion(byte[] data) {
        int openDev = getReader().OpenDev();
        if (openDev != 0) {
            return failedBytes;
        }
        int set = 0;
        byte[] start = CrcOperateUtil.getBytesByindex(data, 0, 1);
        byte[] end = CrcOperateUtil.getBytesByindex(data, 2, 3);
        if (Arrays.equals(start, QuanrayDataUtils.F840)
                && Arrays.equals(end, QuanrayDataUtils.F845)) {
            set = 0;
        } else if (Arrays.equals(start, QuanrayDataUtils.F920)
                && Arrays.equals(end, QuanrayDataUtils.F925)) {
            set = 1;
        } else if (Arrays.equals(start, QuanrayDataUtils.F902)
                && Arrays.equals(end, QuanrayDataUtils.F928)) {
            set = 2;
        } else if (Arrays.equals(start, QuanrayDataUtils.F865)
                && Arrays.equals(end, QuanrayDataUtils.F868)) {
            set = 3;
        } else {
            return failedBytes;
        }

        int setFreqRegion = getReader().set_freq_region(set);
        getReader().CloseDev();
        if (setFreqRegion != 0) {
            return failedBytes;
        } else {
            return successBytes;
        }
    }

    /**
     * 读卡
     *
     * @param data
     * @return
     */
    public byte[] readArea(byte[] data) {
        int openDev = getReader().OpenDev();
        if (openDev != 0) {
            return failedBytes;
        }
        byte[] bank = CrcOperateUtil.getBytesByindex(data, 0, 0);
        byte[] selectData = CrcOperateUtil.getBytesByindex(data, 1, data.length - 4);
        byte[] area = CrcOperateUtil.getBytesByindex(data, data.length - 3,
                data.length - 3);
        byte[] addr = CrcOperateUtil.getBytesByindex(data, data.length - 2,
                data.length - 2);
        byte[] count = CrcOperateUtil.getBytesByindex(data, data.length - 1,
                data.length - 1);
        int bankInt = QuanrayDataUtils.bytes2int(bank);
        int selectCard = getReader().select_card(bankInt, selectData, true);
        if (selectCard != 0) {
            return failedBytes;
        }
        int areaInt = QuanrayDataUtils.bytes2int(area);
        int addrInt = QuanrayDataUtils.bytes2int(addr);
        int countInt = QuanrayDataUtils.bytes2int(count);
        int readArea = getReader().newReadArea(areaInt, addrInt, countInt, "00000000");
        if (readArea != 0) {
            return failedBytes;
        } else {
            return successBytes;
        }
    }


    /**
     * 写卡
     * @param data
     * @return
     */
    public byte[] writeArea(byte[] data) {
        int openDev = getReader().OpenDev();
        if (openDev != 0) {
            return failedBytes;
        }
        byte[] bank = CrcOperateUtil.getBytesByindex(data, 0, 0);
        byte[] count = CrcOperateUtil.getBytesByindex(data, data.length - 1,
                data.length - 1);
        int countInt = QuanrayDataUtils.bytes2int(count);

        byte[] selectData = CrcOperateUtil.getBytesByindex(data, 1, data.length - 4 - countInt);
        byte[] area = CrcOperateUtil.getBytesByindex(data, data.length - 3 - countInt,
                data.length - 3 - countInt);
        byte[] addr = CrcOperateUtil.getBytesByindex(data, data.length - 2 - countInt,
                data.length - 2 - countInt);
        byte[] writeData = CrcOperateUtil.getBytesByindex(data, data.length - 1 - countInt,
                data.length - 2);
        int bankInt = QuanrayDataUtils.bytes2int(bank);
        int selectCard = getReader().select_card(bankInt, selectData, true);
        if (selectCard != 0) {
            return failedBytes;
        }
        int areaInt = QuanrayDataUtils.bytes2int(area);
        int addrInt = QuanrayDataUtils.bytes2int(addr);

        int readArea = getReader().newWriteArea(areaInt, addrInt, countInt, "00000000",writeData);
        if (readArea != 0) {
            return failedBytes;
        } else {
            return successBytes;
        }
    }
}
