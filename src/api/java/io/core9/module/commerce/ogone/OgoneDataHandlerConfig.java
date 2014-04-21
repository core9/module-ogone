package io.core9.module.commerce.ogone;

import io.core9.plugin.widgets.datahandler.DataHandlerDefaultConfig;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;

public class OgoneDataHandlerConfig extends DataHandlerDefaultConfig implements DataHandlerFactoryConfig {
	
	private String pspid;
	private boolean test;
	private String acceptUrl;
	private String currency;
	private String language;
	private String shaInValue;

	public String getAcceptUrl() {
		return acceptUrl;
	}

	public void setAcceptUrl(String acceptUrl) {
		this.acceptUrl = acceptUrl;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getPspid() {
		return pspid;
	}

	public void setPspid(String pspid) {
		this.pspid = pspid;
	}

	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}

	public String getShaInValue() {
		return shaInValue;
	}

	public void setShaInValue(String shaInValue) {
		this.shaInValue = shaInValue;
	}

}
