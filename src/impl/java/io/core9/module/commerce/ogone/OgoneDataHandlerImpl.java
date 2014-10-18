package io.core9.module.commerce.ogone;

import io.core9.commerce.CommerceDataHandlerHelper;
import io.core9.commerce.checkout.Order;
import io.core9.plugin.server.Server;
import io.core9.plugin.server.request.Request;
import io.core9.plugin.widgets.datahandler.DataHandler;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

/**
 * Ogone DataHandler
 * Handles Ogone payments via the widgets flow.
 * 
 * @author mark.wienk@core9.io
 *
 */
@PluginImplementation
public class OgoneDataHandlerImpl<T extends OgoneDataHandlerConfig> implements OgoneDataHandler<T> {
	
	@InjectPlugin
	private CommerceDataHandlerHelper helper;
	
	@InjectPlugin
	private Server server;

	@Override
	public String getName() {
		return "Payment-Ogone";
	}

	@Override
	public Class<? extends DataHandlerFactoryConfig> getConfigClass() {
		return OgoneDataHandlerConfig.class;
	}

	@Override
	public DataHandler<T> createDataHandler(final DataHandlerFactoryConfig options) {
		@SuppressWarnings("unchecked")
		final T config = (T) options; 
		return new DataHandler<T>() {

			@Override
			public Map<String, Object> handle(Request req) {
				Order order = helper.getOrder(req);
				Map<String, Object> result = new HashMap<String, Object>();
				if(req.getParams().size() == 0 || !handleReturnedResult(req, config, order)) {
					if(order.getPaymentData() != null && order.getPaymentData().get("STATUS") != null) {
						result.put("status", returnStatusMessage(order, (String) order.getPaymentData().get("STATUS")));
					}
					TreeMap<String, String> values = addOrderContent(config.retrieveFields(), order, req);
					generateSignature(config.getShaInValue(), values);
					result.put("link", config.isTest() ? " https://secure.ogone.com/ncol/test/orderstandard.asp" : " https://secure.ogone.com/ncol/prod/orderstandard.asp");
					result.put("amount", order.getTotal());
					result.put("orderid", order.getId());
					result.put("ogoneconfig", values);
				}
				helper.saveOrder(req, order);
				return result;
			}

			@Override
			public T getOptions() {
				return config;
			}
		};
	}
	
	/**
	 * Handle the returned request from Ogone
	 * @param req
	 * @param order 
	 * @return
	 */
	private boolean handleReturnedResult(Request req, T config, Order order) {
		TreeMap<String,String> ordered = new TreeMap<String,String>();
		String shaSignature = null;
		for(Map.Entry<String, Object> entry : req.getParams().entrySet()) {
			if(entry.getValue() != null && !entry.getValue().equals("")) {
				if(entry.getKey().equalsIgnoreCase("SHASIGN")) {
					shaSignature = (String) entry.getValue();
				} else {
					ordered.put(entry.getKey().toUpperCase(), (String) entry.getValue());
				}
			}
		}
		if(shaSignature == null) {
			return false;
		}
		String signature = generateSignature(config.getShaOutValue(), ordered);
		if(signature.equals(shaSignature)) {
			order.setPaymentData(new HashMap<String,Object>(ordered));
			if(req.getParams().get("STATUS").equals("9")){
				order.setStatus("paid");
				return true;
			};
		}
		return false;
	}

	/**
	 * return the ogone status message
	 * @param order 
	 * @param status
	 * @return
	 */
	private String returnStatusMessage(Order order, String status) {
		switch(status) {
		case "1":
			order.setId(null);
			return "Order canceled by customer";
		case "2":
			order.setId(null);
			return "Order not authorized, please use another payment method.";
		case "9":
			return null;
		default:
			order.setId(null);
			return "Something went wrong, please try another payment method.";
		}
	}
	
	private TreeMap<String,String> addOrderContent(TreeMap<String,String> fields, Order order, Request req) {
		fields.put("AMOUNT", "" + order.getTotal());
		fields.put("ORDERID", order.getId());
		fields.put("COMPLUS", req.getCookie("CORE9SESSIONID").getValue());
		return fields;
	}

	/**
	 * Generate a SHA-1 Signature for ogone
	 * @param req
	 * @param key
	 * @param order
	 * @return
	 */
	private String generateSignature(String key, TreeMap<String,String> fields) {
		String input = "";
		for(Map.Entry<String,String> entry : fields.entrySet()) {
			input += entry.getKey() + "=" + entry.getValue() + key;
		}
		
		byte[] bytes = input.getBytes();

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		String result = bytesToHex(md.digest(bytes)).toUpperCase();
		fields.put("SHASIGN", result);
		return result;
	}
	
	private String bytesToHex(byte[] bytes) {
		char[] hexArray = "0123456789ABCDEF".toCharArray();
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
