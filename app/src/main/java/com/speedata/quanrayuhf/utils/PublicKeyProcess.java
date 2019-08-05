package com.speedata.quanrayuhf.utils;

public abstract class PublicKeyProcess
{
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public static byte charToByte(char c)
	{
		byte b = 0;
		switch (c)
		{
			case '0':
				b = 0;
				break;
			case '1':
				b = 1;
				break;
			case '2':
				b = 2;
				break;
			case '3':
				b = 3;
				break;
			case '4':
				b = 4;
				break;
			case '5':
				b = 5;
				break;
			case '6':
				b = 6;
				break;
			case '7':
				b = 7;
				break;
			case '8':
				b = 8;
				break;
			case '9':
				b = 9;
				break;
			case 'A':
			case 'a':
				b = 10;
				break;
			case 'B':
			case 'b':
				b = 11;
				break;
			case 'C':
			case 'c':
				b = 12;
				break;
			case 'D':
			case 'd':
				b = 13;
				break;
			case 'E':
			case 'e':
				b = 14;
				break;
			case 'F':
			case 'f':
				b = 15;
				break;
			default:
				b = 0;
				break;
		}
		return b;
	}
	
	/**
	 * 
	 * @param b
	 * @return
	 */
	public static char byteToChar(byte b)
	{
		char c = '0';
		switch (b)
		{
			case 0:
				c = '0';
				break;
			case 1:
				c = '1';
				break;
			case 2:
				c = '2';
				break;
			case 3:
				c = '3';
				break;
			case 4:
				c = '4';
				break;
			case 5:
				c = '5';
				break;
			case 6:
				c = '6';
				break;
			case 7:
				c = '7';
				break;
			case 8:
				c = '8';
				break;
			case 9:
				c = '9';
				break;
			case 10:
				c = 'a';
				break;
			case 11:
				c = 'b';
				break;
			case 12:
				c = 'c';
				break;
			case 13:
				c = 'd';
				break;
			case 14:
				c = 'e';
				break;
			case 15:
				c = 'f';
				break;
			default:
				c = '0';
				break;
		}
		return c;
	}
}
