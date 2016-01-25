package io.core9.module.commerce.ogone;

import java.util.List;

import io.core9.plugin.widgets.datahandler.DataHandlerDefaultConfig;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;

public class OgoneVerifierDataHandlerConfig extends DataHandlerDefaultConfig implements DataHandlerFactoryConfig {

	private List<String> paymentMethods;
	private String pspid;
	private String username;
	private String password;
	private boolean test;

	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}

	public List<String> getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(List<String> paymentMethods) {
		this.paymentMethods = paymentMethods;
	}

	public String getPspid() {
		return pspid;
	}

	public void setPspid(String pspid) {
		this.pspid = pspid;
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

}
