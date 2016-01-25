package io.core9.module.commerce.ogone;

import java.io.IOException;
import java.util.Map;

import io.core9.commerce.checkout.Order;
import io.core9.core.plugin.Core9Plugin;
import io.core9.plugin.widgets.datahandler.DataHandlerFactory;

public interface OgoneVerifierDataHandler<T extends OgoneVerifierDataHandlerConfig> extends DataHandlerFactory<T>, Core9Plugin {

	/**
	 * Handle the retrieved Payment data from Ogone
	 * @param order
	 * @param paymentData
	 * @return
	 */
	Order handlePaymentData(Order order, Map<String, Object> paymentData);

	/**
	 * Retrieve the data from ogone
	 * @param config
	 * @param order
	 * @return
	 */
	Map<String, Object> getOgoneData(OgoneVerifierDataHandlerConfig config, Order order);
	
	/**
	 * Retrieve the data from ogone
	 * @param config
	 * @param order
	 * @return
	 */
	Map<String, Object> getOgoneData(OgoneVerifierDataHandlerConfig config, String paymentId) throws IOException, OgoneResultNotParseableException;

}
