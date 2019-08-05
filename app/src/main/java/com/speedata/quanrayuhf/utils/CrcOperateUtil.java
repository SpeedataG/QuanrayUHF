/*      						
 * Copyright 2010 Beijing Xinwei, Inc. All rights reserved.
 * 
 * History:
 * ------------------------------------------------------------------------------
 * Date    	|  Who  		|  What  
 * 2013-2-1	| chenlong 	| 	create the file                       
 */

package com.speedata.quanrayuhf.utils;

import java.util.Arrays;

/**
 * CRC数组处理
 * <p>
 * <p>
 * 类详细描述
 * </p>
 *
 * @author chenlong
 */

public class CrcOperateUtil {
    /**
     * 为Byte数组添加两位CRC校验
     *
     * @param buf
     * @return
     */
    public static byte[] setParamCRC(byte[] buf) {
        int MASK = 0x0001, CRCSEED = 0x0810;
        int remain = 0;

        byte val;
        for (int i = 0; i < buf.length; i++) {
            val = buf[i];
            for (int j = 0; j < 8; j++) {
                if (((val ^ remain) & MASK) != 0) {
                    remain ^= CRCSEED;
                    remain >>= 1;
                    remain |= 0x8000;
                } else {
                    remain >>= 1;
                }
                val >>= 1;
            }
        }

        byte[] crcByte = new byte[2];
        crcByte[0] = (byte) ((remain >> 8) & 0xff);
        crcByte[1] = (byte) (remain & 0xff);

        // 将新生成的byte数组添加到原数据结尾并返回
        return concatAll(buf, crcByte);
    }

    /**
     * 根据起始和结束下标截取byte数组
     *
     * @param bytes
     * @param start
     * @param end
     * @return
     */
    public static byte[] getBytesByindex(byte[] bytes, int start, int end) {
        byte[] returnBytes = new byte[end - start + 1];
        for (int i = 0; i < returnBytes.length; i++) {
            returnBytes[i] = bytes[start + i];
        }
        return returnBytes;
    }

    /**
     * 对buf中offset以前crcLen长度的字节作crc校验，返回校验结果
     *
     * @param buf    byte[]
     * @param offset int
     * @param crcLen int　crc校验的长度
     * @return int　crc结果
     */
    public static int calcCRC(byte[] buf, int offset, int crcLen) {
        int MASK = 0x0001, CRCSEED = 0x0810;
        int start = offset;
        int end = offset + crcLen;
        int remain = 0;

        byte val;
        for (int i = start; i < end; i++) {
            val = buf[i];
            for (int j = 0; j < 8; j++) {
                if (((val ^ remain) & MASK) != 0) {
                    remain ^= CRCSEED;
                    remain >>= 1;
                    remain |= 0x8000;
                } else {
                    remain >>= 1;
                }
                val >>= 1;
            }
        }
        return remain;
    }

    /***
     * CRC校验是否通过
     *
     * @param srcByte
     * @param length
     * @return
     */
    public static boolean isPassCRC(byte[] srcByte, int length) {
        // 取出除crc校验位的其他数组，进行计算，得到CRC校验结果
        int calcCRC = calcCRC(srcByte, 0, srcByte.length - length);

        // 取出CRC校验位，进行计算
        int receive = toInt(getBytesByindex(srcByte, srcByte.length - length, srcByte.length - 1));

        // 比较
        return calcCRC == receive;
    }


    /***
     * 坤瑞CRC校验是否通过
     *
     * @param srcByte
     * @return
     */
    public static boolean isQuanrayPassCRC(byte[] srcByte) {
        int length = toInt(getBytesByindex(srcByte, 2, 3));
        // 取出除crc校验位的其他数组，进行计算，得到CRC校验结果
        byte[] countCrc = getBytesByindex(srcByte, 4, 4 + length - 1);
        byte[] crc16Byte = getCHKBytes(countCrc);

        // 取出CRC校验位
        byte[] crc = getBytesByindex(srcByte, length + 6 - 2, length + 6 - 1);

        // 比较
        return Arrays.equals(crc16Byte, crc);
    }

    /**
     * 获取校验位
     */
    public static byte[] getCHKBytes(byte[] countCrc) {
        return getCRC16Byte(CRCXModeOFF((char) 0xFFFF, countCrc));
    }

    /**
     * 多个数组合并
     *
     * @param first
     * @param rest
     * @return
     */
    public static byte[] concatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * Byte转换为Int
     *
     * @param b
     * @return
     */
    public static int toInt(byte[] b) {
        return toInt(b, 0, 4);
    }

    /**
     * Byte转换为Int
     *
     * @param b
     * @param off
     * @param len
     * @return
     */
    public static int toInt(byte[] b, int off, int len) {
        int st = 0;
        if (off < 0) {
            off = 0;
        }
        if (len > 4) {
            len = 4;
        }
        for (int i = 0; i < len && (i + off) < b.length; i++) {
            st <<= 8;
            st += (int) b[i + off] & 0xff;
        }
        return st;
    }


    private static char[] crc_ta = {0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5,
            0x60c6, 0x70e7, 0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad,
            0xe1ce, 0xf1ef};

    /**
     * 初始值0
     *
     * @param buffer 需要校验的数据
     * @return 校验int结果
     */
    public static int CRCXMode(byte[] buffer) {
        short da;
        char CRC_16_Data = 0xffff;
        for (int i = 0; i < buffer.length; i++) {
            da = (short) (CRC_16_Data >> 12);
            CRC_16_Data <<= 4;
            CRC_16_Data ^= crc_ta[da ^ ((short) (char) (buffer[i] & 0xff) / 16)];
            da = (short) (CRC_16_Data >> 12);
            CRC_16_Data <<= 4;
            CRC_16_Data ^= crc_ta[da
                    ^ ((short) (char) (buffer[i] & 0xff) & 0x0f)];
        }
        return CRC_16_Data;
    }

    /**
     * @param start  初始值
     * @param buffer 需要校验的数据
     * @return 校验结果（结果取非） int
     */
    public static int CRCXModeOFF(char start, byte[] buffer) {
        short da;
        char CRC_16_Data = start;
        for (int i = 0; i < buffer.length; i++) {
            da = (short) (CRC_16_Data >> 12);
            CRC_16_Data <<= 4;
            CRC_16_Data ^= crc_ta[da ^ ((short) (char) (buffer[i] & 0xff) / 16)];
            da = (short) (CRC_16_Data >> 12);
            CRC_16_Data <<= 4;
            CRC_16_Data ^= crc_ta[da
                    ^ ((short) (char) (buffer[i] & 0xff) & 0x0f)];
        }
        //返回取非结果
        return ~CRC_16_Data;
    }

    /**
     * @param CRC
     * @return 返回2byte CRC校验结果
     */
    public static byte[] getCRC16Byte(int CRC) {
        byte[] result = new byte[2];
        result[0] = (byte) ((CRC >> 8) & 0xFF);
        result[1] = (byte) (CRC & 0xFF);
        return result;
    }
}
