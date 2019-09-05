package com.github.shicloud.bridge.mysql;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.github.shicloud.bridge.config.MqttConfig;
import com.github.shicloud.bridge.model.Model;
import com.github.shicloud.bytes.ByteParser;
import com.github.shicloud.jdbc.templete.JdbcTemplateTool;
import com.github.shicloud.mqtt.client.MqttBaseHandler;
import com.github.shicloud.mqtt.client.config.ClientConfig;
import com.github.shicloud.utils.ByteUtil;

/**
 * Created by shifeng on 2018/10/31
 *
 */
public class MysqlHandler extends MqttBaseHandler {
	
	private static final Logger log = LoggerFactory.getLogger(MysqlHandler.class);
	
	private Model model;
	private JdbcTemplateTool jtt;
	
	public MysqlHandler(Model model, MqttConfig mqttConfig, JdbcTemplateTool jtt) {
		this.model = model;
		this.jtt = jtt;
		ClientConfig properties = new ClientConfig();
		BeanUtils.copyProperties(mqttConfig, properties);
		
		Topic[] topics = new Topic[] { new Topic(model.getTopic(), QoS.values()[model.getQos()])};
		this.init(properties, topics, model.getClientId(), model.getCleanSession());
		log.info("model {} mqtt client inited",model.getName());
	}


	@Override
	public void processInput(UTF8Buffer topic, Buffer payload) {
		Object object = ByteParser.toObject(ByteUtil.hexStrToBytes(payload.hex()), model.getClazz());
		if(object == null) {
			return;
		}
		try {
			jtt.insert(object);
		} catch (Exception e) {
			log.info("model {} insert mysql db error ",model.getName(),e);
		}
	}
}
