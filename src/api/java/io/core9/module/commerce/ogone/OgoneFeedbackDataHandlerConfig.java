package io.core9.module.commerce.ogone;

import io.core9.plugin.widgets.datahandler.DataHandlerDefaultConfig;

public class OgoneFeedbackDataHandlerConfig extends DataHandlerDefaultConfig {
	
	private String checkoutFinalWidgetID;

	public String getCheckoutFinalWidgetID() {
		return checkoutFinalWidgetID;
	}

	public void setCheckoutFinalWidgetID(String checkoutFinalWidgetID) {
		this.checkoutFinalWidgetID = checkoutFinalWidgetID;
	}

}
