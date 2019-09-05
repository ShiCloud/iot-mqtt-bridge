package com.github.shicloud.bridge.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bridge", ignoreUnknownFields = false)
public class BridgeConfig{
	
	String modelPath;
	List<MqttConfig> mqtts;

	public String getModelPath() {
		return modelPath;
	}

	public void setModelPath(String modelPath) {
		this.modelPath = modelPath;
	}

	public List<MqttConfig> getMqtts() {
		return mqtts;
	}

	public void setMqtts(List<MqttConfig> mqtts) {
		this.mqtts = mqtts;
	}
	
}
