package com.speedata.quanrayuhf.utils;

import java.nio.ByteBuffer;

/**
 * 
 * 
 * @author zhoulei
 */
public class CompressWrapPublic
{
	/**
	 * 
	 * @param value
	 * @param length
	 * @return
	 */
	protected static byte[] packCompress(String value, int length)
	{
		ByteBuffer tempBuffer = ByteBuffer.allocate(length);
		
		int lenTwice = length + length;
		
		byte tByte = 0;
		byte f;
		for (int i = 0; i < lenTwice; i++)
		{
			f = PublicKeyProcess.charToByte(value.charAt(i));
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
		
		return tempBuffer.array();
	}
	
	/**
	 * 
	 * @param length
	 * @return
	 */
	protected static ByteBuffer allocTempBuffer(int length)
	{
		ByteBuffer tempBuffer = ByteBuffer.allocate(length);
		return tempBuffer;
	}
	
}
