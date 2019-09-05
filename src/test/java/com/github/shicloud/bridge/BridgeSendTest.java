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
public class BridgeSendTest extends MqttBaseHandler {
	private static Logger logger = LoggerFactory.getLogger(BridgeSendTest.class);

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			logger.error("please set config path");
			System.exit(-1);
		}
		BridgeSendTest handler = new BridgeSendTest();
		ClientConfig properties = new ClientConfig(args[0]);
		handler.init(properties, null,"user_sendTest", false);
		
		logger.info("user_sendTest inited");
		
		for (int i = 0; i < 10000; i++) {
			byte[] msg = new byte[0];
			msg = ByteUtil.appendBytes(msg, ByteUtil.shortToBytes(Short.valueOf("1")));// offset
			msg = ByteUtil.appendBytes(msg, ByteUtil.longToBytes(Long.valueOf(i+1)));
			msg = ByteUtil.appendBytes(msg, ByteUtil.shortToBytes(Short.valueOf("36")));
			msg = ByteUtil.appendBytes(msg, ByteUtil.floatToBytes(49.8f));
			msg = ByteUtil.appendBytes(msg, ByteUtil.doubleToBytes(149.8));
			int j = (i+1)%5;
			msg = ByteUtil.appendBytes(msg, ByteUtil.shortToBytes(Short.valueOf(""+j)));
			byte[] b = new byte[j];
			for (int k = 0; k < j; k++) {
				b[k] = 65;
			}
			msg = ByteUtil.appendBytes(msg, b);	
			msg = ByteUtil.appendBytes(msg, ByteUtil.longToBytes(new Date().getTime()));
			msg = ByteUtil.appendBytes(msg, new byte[] { ByteUtil.boolTobyte(1)});
			msg = ByteUtil.appendBytes(msg, new byte[] { ByteUtil.boolTobyte(0)});
			
			handler.send("user_topic", msg, QoS.values()[1], false);
			Thread.sleep(100);
		}
		Thread.sleep(10000);
	}

	@Override
	public void processInput(UTF8Buffer topic, Buffer payload) {

	}
}
