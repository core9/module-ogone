package io.core9.module.commerce.ogone;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.core9.plugin.widgets.datahandler.DataHandlerDefaultConfig;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;

public class OgoneDataHandlerConfig extends DataHandlerDefaultConfig implements DataHandlerFactoryConfig {
	
	private Map<String,String> values;
	private List<KeyValueEntry> rest;
	private String shaInValue;
	private String shaOutValue;
	private boolean test;
	

	public String getCurrency() {
		return values.get("CURRENCY");
	}

	public void setCurrency(String currency) {
		this.values.put("CURRENCY", currency);
	}

	public String getLanguage() {
		return this.values.get("LANGUAGE");
	}

	public void setLanguage(String language) {
		this.values.put("LANGUAGE", language);
	}

	public String getPspid() {
		return values.get("PSPID");
	}

	public void setPspid(String pspid) {
		this.values.put("PSPID", pspid);
	}

	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}

	public String getShaInValue() {
		return this.shaInValue;
	}

	public void setShaInValue(String shaInValue) {
		this.shaInValue = shaInValue;
	}
	
	public String getShaOutValue() {
		return this.shaOutValue;
	}

	public void setShaOutValue(String shaOutValue) {
		this.shaOutValue = shaOutValue;
	}

	public List<KeyValueEntry> getRest() {
		return rest;
	}

	public void setRest(List<KeyValueEntry> rest) {
		this.rest = rest;
	}
	
	public OgoneDataHandlerConfig() {
		this.values = new HashMap<String, String>();
	}
	
	public TreeMap<String,String> retrieveFields() {
		TreeMap<String,String> result = new TreeMap<String, String>(values);
		for(KeyValueEntry entry : rest) {
			result.put(entry.getKey().toUpperCase(), entry.getValue());
		}
		return result;
	}

}
