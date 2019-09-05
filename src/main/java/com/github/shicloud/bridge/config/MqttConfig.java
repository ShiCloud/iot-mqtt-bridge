package com.github.shicloud.bridge.config;

import java.util.List;

public class MqttConfig{
	String url;
	String username;
	String password;
	Short keepAlive;
	Boolean retained;
	Integer reconnectAttemptsMax;
	Integer reconnectDelay;
	List<String> models;

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Short getKeepAlive() {
		return keepAlive;
	}
	public void setKeepAlive(Short keepAlive) {
		this.keepAlive = keepAlive;
	}
	public Boolean getRetained() {
		return retained;
	}
	public void setRetained(Boolean retained) {
		this.retained = retained;
	}
	public Integer getReconnectAttemptsMax() {
		return reconnectAttemptsMax;
	}
	public void setReconnectAttemptsMax(Integer reconnectAttemptsMax) {
		this.reconnectAttemptsMax = reconnectAttemptsMax;
	}
	public Integer getReconnectDelay() {
		return reconnectDelay;
	}
	public void setReconnectDelay(Integer reconnectDelay) {
		this.reconnectDelay = reconnectDelay;
	}
	public List<String> getModels() {
		return models;
	}
	public void setModels(List<String> models) {
		this.models = models;
	}
	
}
