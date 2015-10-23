package com.intel.director.api.ui;

public class ImageCountPieChart {
	String label;
	int value;
	String color;
	String pie_label;
	
	public String getPie_label() {
		return pie_label;
	}
	public void setPie_label(String pie_label) {
		this.pie_label = pie_label;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	
	@Override
	public String toString() {
		return "{label=" + label + ",value=" + value + ",color=" + color + "}";
	}

}
