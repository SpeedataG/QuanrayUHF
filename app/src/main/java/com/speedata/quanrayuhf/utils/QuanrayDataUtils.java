package com.speedata.quanrayuhf.utils;

import com.speedata.libuhf.utils.StringUtils;

import java.util.Arrays;

/**
 * Created by 张明_ on 2017/12/5.
 * Email 741183142@qq.com
 */

public class QuanrayDataUtils {
    private static final byte[] HEAD = {0x5B, 0x5B};
    public static final byte[] VER = {0x11};
    private static int sn = 0;
    //链接认证
    public static final byte[] CMD1 = {0x40};
    //链接检测
    public static final byte[] CMD2 = {0x41};
    //功率读取
    public static final byte[] CMD3 = {0x42};
    //功率设置
    public static final byte[] CMD4 = {0x43};
    //频段读取
    public static final byte[] CMD5 = {0x44};
    //频段设置
    public static final byte[] CMD6 = {0x45};
    //启动盘存
    public static final byte[] CMD7 = {0x46};
    //停止盘存
    public static final byte[] CMD8 = {0x47};
    //盘存数据响应
    public static final byte[] CMD9 = {0x48};
    //读数据
    public static final byte[] CMD10 = {0x49};
    //写数据
    public static final byte[] CMD11 = {0x4A};

    public static final byte[] F840 = {0x03, 0x48};
    public static final byte[] F845 = {0x03, 0x4D};
    public static final byte[] F920 = {0x03, (byte) 0x98};
    public static final byte[] F925 = {0x03, (byte) 0x9D};
    public static final byte[] F902 = {0x03, (byte) 0x86};
    public static final byte[] F928 = {0x03, (byte) 0xA0};
    public static final byte[] F865 = {0x03, 0x61};
    public static final byte[] F868 = {0x03, 0x64};

    /**
     * 检验数据是否符合要求
     *
     * @param data 数据
     * @return
     */
    public static boolean checkData(byte[] data) {
        byte[] head = CrcOperateUtil.getBytesByindex(data, 0, 1);
        return Arrays.equals(head, HEAD) && CrcOperateUtil.isQuanrayPassCRC(data);
    }

    /**
     * 得到命令字
     *
     * @param data 数据
     * @return
     */
    public static byte[] getCMD(byte[] data) {
        return CrcOperateUtil.getBytesByindex(data, 6, 6);
    }


    /**
     * 得到数据字段
     *
     * @param data 数据
     * @return
     */
    public static byte[] getDATA(byte[] data) {
        int length = CrcOperateUtil.toInt(CrcOperateUtil.getBytesByindex(data, 2, 3)) + 6;
        return CrcOperateUtil.getBytesByindex(data, 7, length - 3);
    }


    public static byte[] getSendData(byte[] data, byte[] cmd) {
        sn++;
        if (sn > 255) {
            sn = 0;
        }
        byte[] snBytes = int2bytes(sn);
        byte[] lenBytes = getLen(data.length);
        byte[] concatAll = CrcOperateUtil.concatAll(VER, snBytes, cmd, data);
        byte[] chkBytes = CrcOperateUtil.getCHKBytes(concatAll);
        return CrcOperateUtil.concatAll(HEAD, lenBytes, concatAll, chkBytes);
    }

    public static byte[] int2bytes(int int10) {
        String hexString = Integer.toHexString(int10);
        if (hexString.length() == 1) {
            hexString = "0" + hexString;
        }
        return StringUtils.stringToByte(hexString);
    }

    public static int bytes2int(byte[] bytes) {
        String hexString = StringUtils.byteToHexString(bytes, bytes.length);
        return Integer.parseInt(hexString, 16);
    }

    /**
     * 得到数据长度byte
     *
     * @param dataLen
     * @return
     */
    private static byte[] getLen(int dataLen) {
        int len = 3 + dataLen;
        byte[] bytes = int2bytes(len);
        if (bytes.length == 1) {
            return CrcOperateUtil.concatAll(new byte[]{0x00}, bytes);
        } else {
            return bytes;
        }
    }
}
