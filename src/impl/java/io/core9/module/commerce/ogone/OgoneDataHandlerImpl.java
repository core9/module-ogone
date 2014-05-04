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
import java.util.TreeMap;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

/**
 * Ogone DataHandler
 * Handles Ogone payments via the widgets flow.
 * 
 * TODO: Handle Ogone return URL (success/failure)
 * 
 * @author mark.wienk@core9.io
 *
 */
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
				TreeMap<String, String> values = addOrderContent(config.retrieveFields(), order);
				generateSignature(req, config, values);
								
				Map<String, Object> result = new HashMap<String, Object>();
				result.put("link", config.isTest() ? " https://secure.ogone.com/ncol/test/orderstandard.asp" : " https://secure.ogone.com/ncol/prod/orderstandard.asp");
				result.put("amount", order.getCart().getTotal());
				result.put("orderid", order.getId());
				result.put("ogoneconfig", values);
				
				return result;
			}

			@Override
			public OgoneDataHandlerConfig getOptions() {
				return (OgoneDataHandlerConfig) options;
			}
		};
	}
	
	private TreeMap<String,String> addOrderContent(TreeMap<String,String> fields, Order order) {
		fields.put("AMOUNT", "" + order.getCart().getTotal());
		fields.put("ORDERID", order.getId());
		return fields;
	}

	/**
	 * Generate a SHA-1 Signature for ogone
	 * TODO Make more dynamic, to allow additional fields (use CommerceEncryptionPlugin)
	 * @param req
	 * @param config
	 * @param order
	 * @return
	 */
	private String generateSignature(Request req, OgoneDataHandlerConfig config, TreeMap<String,String> fields) {
		String shaInValue = config.getShaInValue();
		String input = "";
		for(Map.Entry<String,String> entry : fields.entrySet()) {
			input += entry.getKey() + "=" + entry.getValue() + shaInValue;
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
