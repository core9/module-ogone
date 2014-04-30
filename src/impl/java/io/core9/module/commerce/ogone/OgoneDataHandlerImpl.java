package io.core9.module.commerce.ogone;

import io.core9.commerce.checkout.Order;
import io.core9.module.auth.AuthenticationPlugin;
import io.core9.plugin.server.request.Request;
import io.core9.plugin.widgets.datahandler.DataHandler;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

@PluginImplementation
public class OgoneDataHandlerImpl implements OgoneDataHandler {
	
	@InjectPlugin
	private AuthenticationPlugin auth;

	@Override
	public String getName() {
		return "Payment-Ogone";
	}

	@Override
	public Class<? extends DataHandlerFactoryConfig> getConfigClass() {
		return OgoneDataHandlerConfig.class;
	}

	@Override
	public DataHandler<OgoneDataHandlerConfig> createDataHandler(final DataHandlerFactoryConfig options) {
		final OgoneDataHandlerConfig config = (OgoneDataHandlerConfig) options; 
		return new DataHandler<OgoneDataHandlerConfig>() {

			@Override
			public Map<String, Object> handle(Request req) {
				Order order = (Order) auth.getUser(req).getSession().getAttribute("order");
				Map<String, Object> result = new HashMap<String, Object>();
				result.put("link", config.isTest() ? " https://secure.ogone.com/ncol/test/orderstandard.asp" : " https://secure.ogone.com/ncol/prod/orderstandard.asp");
				//TODO Make more dynamic, to allow additional fields
				result.put("amount", order.getCart().getTotal());
				result.put("pspid", ((OgoneDataHandlerConfig) options).getPspid());
				result.put("acceptUrl", config.getAcceptUrl());
				result.put("orderid", order.getId());
				result.put("currency", config.getCurrency());
				result.put("language", config.getLanguage());
				result.put("shasign", generateSignature(req, config, order));
				return result;
			}

			@Override
			public OgoneDataHandlerConfig getOptions() {
				return (OgoneDataHandlerConfig) options;
			}
		};
	}

	/**
	 * Generate a SHA-1 Signature for ogone
	 * TODO Make more dynamic, to allow additional fields (use CommerceEncryptionPlugin)
	 * @param req
	 * @param config
	 * @param order
	 * @return
	 */
	private String generateSignature(Request req, OgoneDataHandlerConfig config, Order order) {
		String shaInValue = config.getShaInValue();
		String input = "";
		input += "ACCEPTURL=" + config.getAcceptUrl() + shaInValue;
		input += "AMOUNT=" + order.getCart().getTotal() + shaInValue;
		input += "CURRENCY=" + config.getCurrency() + shaInValue;
		input += "LANGUAGE=" + config.getLanguage() + shaInValue;
		input += "ORDERID=" + order.getId() + shaInValue;
		input += "PSPID=" + config.getPspid() + shaInValue;
		
		byte[] bytes = input.getBytes();

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return bytesToHex(md.digest(bytes)).toUpperCase();
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
