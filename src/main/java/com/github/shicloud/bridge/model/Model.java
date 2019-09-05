package com.github.shicloud.bridge.model;

import java.util.List;

public class Model {
	
	private String name;
	private String topic;
	private String clientId;
	private Integer qos;
	private Boolean cleanSession;
	private String storeType;
	private String tableName;
	private List<Field> fields;
	
	private Class<?> clazz;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Field> getFields() {
		return fields;
	}
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public Integer getQos() {
		return qos;
	}
	public void setQos(Integer qos) {
		this.qos = qos;
	}
	public Boolean getCleanSession() {
		return cleanSession;
	}
	public void setCleanSession(Boolean cleanSession) {
		this.cleanSession = cleanSession;
	}
	public String getStoreType() {
		return storeType;
	}
	public void setStoreType(String storeType) {
		this.storeType = storeType;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public Class<?> getClazz() {
		return clazz;
	}
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
}
