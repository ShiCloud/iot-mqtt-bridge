package com.github.shicloud.bridge.mysql;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.persistence.Table;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.github.shicloud.bridge.annotation.RefValue;
import com.github.shicloud.bridge.annotation.StaticValue;
import com.github.shicloud.bridge.config.MqttConfig;
import com.github.shicloud.bridge.model.Model;
import com.github.shicloud.bytes.ByteParser;
import com.github.shicloud.jdbc.templete.JdbcTemplateTool;
import com.github.shicloud.mqtt.client.MqttBaseHandler;
import com.github.shicloud.mqtt.client.config.ClientConfig;
import com.github.shicloud.utils.ByteUtil;
import com.github.shicloud.utils.ReflectUtil;

/**
 * Created by shifeng on 2018/10/31
 *
 */
public class MsgHandler extends MqttBaseHandler {

	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

	private static final Logger log = LoggerFactory.getLogger(MsgHandler.class);

	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
			AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS*2, 60, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(AVAILABLE_PROCESSORS*50), Executors.defaultThreadFactory());

	private Model model;
	private JdbcTemplateTool jtt;

	public MsgHandler(Model model, MqttConfig mqttConfig, JdbcTemplateTool jtt) {
		this.model = model;
		this.jtt = jtt;
		ClientConfig properties = new ClientConfig();
		BeanUtils.copyProperties(mqttConfig, properties);

		Topic[] topics = new Topic[] { new Topic(model.getTopic(), QoS.values()[model.getQos()]) };
		this.init(properties, topics, model.getClientId(), model.getCleanSession());
		log.info("model {} mqtt client inited", model.getName());
	}

	@Override
	public void processInput(UTF8Buffer topic, Buffer payload) {
		String topicStr = ByteUtil.byteToStr(ByteUtil.hexStrToBytes(topic.hex()));
		byte[] msg = ByteUtil.hexStrToBytes(payload.hex());
		ByteUtil.printBytes(msg);
		Object object = ByteParser.toObject(msg, model.getClazz());
		if (object == null) {
			return;
		}
		long start = System.currentTimeMillis();
		boolean rs = false;
		try {
			rs = executor.submit(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					processObject(object);
					return true;
				}
			}).get(10000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			log.warn("{} processInput error : ", topicStr);
		}
		if (!rs) {
			log.warn("{} processInput is interrupted topic name : ", topicStr);
		}
		long cost = System.currentTimeMillis() - start;
		log.debug("{} processInput cost time:{}", topicStr, cost);
		
	}

	private void processObject(Object object) {
		Map<String, Object> staticValues = new HashMap<>();
		List<?> list = null;
		Field[] fields = object.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			try {
				StaticValue staticValue = field.getAnnotation(StaticValue.class);
				if (staticValue != null) {
					Method getter = ReflectUtil.getGetter(object, field.getName());
					staticValues.put(staticValue.name(), getter.invoke(object));
				}
				if (field.getType() == List.class) {
					Method getter = ReflectUtil.getGetter(object, field.getName());
					list = (List<?>) getter.invoke(object);
				}
			} catch (Exception e) {
				log.info(field.getName() + " get value failure ", e);
			}
		}

		save(object);

		if (list != null) {
			for (Object subObj : list) {
				Field[] subFields = subObj.getClass().getDeclaredFields();
				for (int i = 0; i < subFields.length; i++) {
					Field subField = subFields[i];
					try {
						RefValue refValue = subField.getAnnotation(RefValue.class);
						if (refValue != null) {
							Method setter = ReflectUtil.getSetter(subObj, subField.getName());
							setter.invoke(subObj, staticValues.get(refValue.target()));
						}
					} catch (Exception e) {
						log.info(subField.getName() + " set value failure ", e);
					}
				}
				save(subObj);
			}
		}
	}

	private void save(Object obj) {
		if (obj.getClass().getAnnotation(Table.class) != null) {
			try {
				jtt.insert(obj);
			} catch (Exception e) {
				log.info("{} insert mysql db error ", obj.getClass().getName(), e);
			}
		}
	}
}
