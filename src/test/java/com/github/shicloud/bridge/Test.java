package com.github.shicloud.bridge;

import com.github.shicloud.utils.ByteUtil;

/**
 * Created by shifeng on 2018/10/31
 *
 */
public class Test  {

	public static void main(String[] args) throws Exception {
		byte[] msg = new byte[0];
		msg = ByteUtil.appendBytes(msg, ByteUtil.shortToBytes(Short.valueOf("1001")));// offset
		msg = ByteUtil.appendBytes(msg, ByteUtil.intToBytes(54321));
		msg = ByteUtil.appendBytes(msg, ByteUtil.floatToBytes(123.45f));
		msg = ByteUtil.appendBytes(msg, ByteUtil.intToBytes(1234));
		ByteUtil.printBytes(msg);
	}

}
