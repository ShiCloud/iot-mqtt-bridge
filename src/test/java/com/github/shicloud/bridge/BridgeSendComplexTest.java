package com.github.shicloud.bridge;

import java.util.Date;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.QoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.shicloud.mqtt.client.MqttBaseHandler;
import com.github.shicloud.mqtt.client.config.ClientConfig;
import com.github.shicloud.utils.ByteUtil;

/**
 * Created by shifeng on 2018/10/31
 *
 */
public class BridgeSendComplexTest extends MqttBaseHandler {
	private static Logger logger = LoggerFactory.getLogger(BridgeSendComplexTest.class);

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			logger.error("please set config path");
			System.exit(-1);
		}
		BridgeSendComplexTest handler = new BridgeSendComplexTest();
		ClientConfig properties = new ClientConfig(args[0]);
		handler.init(properties, null,"sendComplexTest", false);
		
		logger.info("sendComplexTest inited");
		
		
		byte[] msg = new byte[0];
		msg = ByteUtil.appendBytes(msg, ByteUtil.shortToBytes(Short.valueOf("1001")));// deviceId
		byte[] b = new byte[0];
		for (int i = 0; i < 5; i++) {
			b = ByteUtil.appendBytes(b, ByteUtil.shortToBytes(Short.valueOf(String.valueOf(5+i))));
			b = ByteUtil.appendBytes(b, ("hello").getBytes());
			for (int j = 0; j < i; j++) {
				b = ByteUtil.appendBytes(b, "A".getBytes());
			}
			b = ByteUtil.appendBytes(b, ByteUtil.longToBytes(new Date().getTime()));
		}
		msg = ByteUtil.appendBytes(msg, ByteUtil.intToBytes(b.length));
		msg = ByteUtil.appendBytes(msg, b);
		msg = ByteUtil.appendBytes(msg, ByteUtil.intToBytes(1234));
		handler.send("device_info_topic", msg, QoS.values()[1], false);
		
		Thread.sleep(10000);
	}

	@Override
	public void processInput(UTF8Buffer topic, Buffer payload) {

	}
}
