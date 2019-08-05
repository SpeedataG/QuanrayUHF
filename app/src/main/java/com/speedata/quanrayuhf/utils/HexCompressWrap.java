package com.speedata.quanrayuhf.utils;

import java.nio.ByteBuffer;

/**
 * 十六进制压缩字符串
 * 
 * @author zhoulei
 * 
 */
public class HexCompressWrap extends CompressWrapPublic
{
	/**
	 * 组包十六进制压缩字符串
	 * 
	 * @param buffer
	 * @param value
	 * @param key
	 */
	public static byte[] packHexCompress(String value, int length)
	{
		ByteBuffer tempBuffer;
		if (length == 0)
		{
			// 支持变长处理
			tempBuffer = allocTempBuffer(2 + value.length() / 2);
			tempBuffer.putShort((short) (value.length() / 2));
			
			int lenTwice = value.length();
			byte tByte = 0;
			byte f;
			for (int i = 0; i < lenTwice; i++)
			{
				if (i < value.length())
				{
					f = PublicKeyProcess.charToByte(value.charAt(i));
				}
				else
				{
					f = PublicKeyProcess.charToByte('f');
				}
				if ((i + 1) % 2 != 0)
				{
					tByte = (byte) (((f & 0x0F) << 4) & 0xF0);
				}
				else
				{
					tByte = (byte) (tByte + (f & 0x0F));
					tempBuffer.put(tByte);
				}
			}
		}
		else
		{
			// 定长处理
			tempBuffer = ByteBuffer.allocate(length);
			
			// packCompress(buffer, value, key);
			
			// 得到占用字节长度属性值
			// int length = Integer.parseInt(key.getAttribute("length"));
			
			int lenTwice = length + length;
			
			byte tByte = 0;
			byte f;
			for (int i = 0; i < lenTwice; i++)
			{
				if (i < value.length())
				{
					f = PublicKeyProcess.charToByte(value.charAt(i));
				}
				else
				{
					f = PublicKeyProcess.charToByte('f');
				}
				if ((i + 1) % 2 != 0)
				{
					tByte = (byte) (((f & 0x0F) << 4) & 0xF0);
				}
				else
				{
					tByte = (byte) (tByte + (f & 0x0F));
					tempBuffer.put(tByte);
				}
			}
		}
		
		return tempBuffer.array();
	}
	
	/**
	 * 解包十进制压缩字符串
	 * 
	 * @param buffer
	 * @param key
	 * @return
	 */
	public static String unpackHexCompress(ByteBuffer buffer, int length)
	{
		
		StringBuffer buf = new StringBuffer("");
		if (length == 0)
		{
			// 变长处理
			short len = buffer.getShort(); // 获取值所占的长度
			int lenTwice = len + len;
			
			byte tByte = 0;
			
			for (int i = 0; i < lenTwice; i++)
			{
				if (i % 2 == 0)
				{
					tByte = buffer.get();
					// 右移四位
					buf.append(PublicKeyProcess.byteToChar((byte) ((tByte & 0xF0) >> 4)));
				}
				else
				{
					buf.append(PublicKeyProcess.byteToChar((byte) (tByte & 0x0F)));
				}
			}
			
		}
		else
		{
			// 定长处理
			int lenTwice = length + length;
			
			byte tByte = 0;
			for (int i = 0; i < lenTwice; i++)
			{
				if (i % 2 == 0)
				{
					tByte = buffer.get();
					buf.append(PublicKeyProcess.byteToChar((byte) ((tByte & 0xF0) >> 4)));
				}
				else
				{
					buf.append(PublicKeyProcess.byteToChar((byte) (tByte & 0x0F)));
				}
			}
		}
		
		return buf.toString().trim();
	}
	
	/**
	 * 获取数字
	 * 
	 * @param str
	 * @return
	 */
	public static String getNumABFromString(String str)
	{
		String number = "";
		if (str != null && !"".equals(str))
		{
			for (int i = 0; i < str.length(); i++)
			{
				if ((str.charAt(i) >= 48 && str.charAt(i) <= 57) || (str.charAt(i) == 'a') || (str.charAt(i) == 'b'))
				{
					number += str.charAt(i);
				}
			}
		}
		return number;
	}
}
