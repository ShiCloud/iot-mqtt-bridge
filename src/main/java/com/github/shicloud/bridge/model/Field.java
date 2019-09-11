package com.github.shicloud.bridge.model;

public class Field {
	
	private String name;
	
	private String type;
	
	private Integer divide;
	
	private Integer index;
	
	private Integer lenght;
	
	private Integer offset;
	
	private Integer dependsOn;
	
	private Boolean isTransient;
	
	private String idType;
	
	private Boolean isLittleEnd;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getDivide() {
		return divide;
	}
	public void setDivide(Integer divide) {
		this.divide = divide;
	}
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	public Integer getLenght() {
		return lenght;
	}
	public void setLenght(Integer lenght) {
		this.lenght = lenght;
	}
	public Integer getOffset() {
		return offset;
	}
	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	public Integer getDependsOn() {
		return dependsOn;
	}
	public void setDependsOn(Integer dependsOn) {
		this.dependsOn = dependsOn;
	}
	public Boolean getIsTransient() {
		return isTransient;
	}
	public void setIsTransient(Boolean isTransient) {
		this.isTransient = isTransient;
	}
	public String getIdType() {
		return idType;
	}
	public void setIdType(String idType) {
		this.idType = idType;
	}
	public Boolean getIsLittleEnd() {
		return isLittleEnd;
	}
	public void setIsLittleEnd(Boolean isLittleEnd) {
		this.isLittleEnd = isLittleEnd;
	}
}
