package io.core9.module.commerce.ogone;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import io.core9.commerce.CommerceDataHandlerHelper;
import io.core9.commerce.cart.lineitem.LineItem;
import io.core9.commerce.checkout.Order;
import io.core9.module.auth.Session;
import io.core9.plugin.server.Server;
import io.core9.plugin.server.request.Request;
import io.core9.plugin.widgets.datahandler.DataHandler;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;
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
	
	private static final List<String> ALLOWED_TYPES = Arrays.asList("PM", "BRAND", "ISSUERID", "ECOM_SHIPTO_DOB", "CIVILITY");
	
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
				if(req.getQueryParams().size() == 0 || !handleReturnedResult(req, config, order)) {
					if(order.getStatus().equals("paying")) {
						order = helper.renewOrderID(req); // New ID is needed for second request
					}
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
		for(Entry<String, Deque<String>> entry : req.getQueryParams().entrySet()) {
			if(entry.getValue() != null && !entry.getValue().getFirst().equals("")) {
				if(entry.getKey().equalsIgnoreCase("SHASIGN")) {
					shaSignature = (String) entry.getValue().getFirst();
				} else {
					ordered.put(entry.getKey().toUpperCase(), (String) entry.getValue().getFirst());
				}
			}
		}
		if(shaSignature == null) {
			return false;
		}
		String signature = generateSignature(config.getShaOutValue(), ordered);
		if(signature.equals(shaSignature)) {
			order.setPaymentData(new HashMap<String,Object>(ordered));
			if(req.getQueryParams().get("STATUS").getFirst().equals("9")){
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
		Session session = helper.getSession(req);
		@SuppressWarnings("unchecked")
		Map<String, String> options = (Map<String,String>) session.getAttribute("paymentmethodoptions");
		if(options != null) {
			for(Map.Entry<String, String> entry : options.entrySet()) {
				if(ALLOWED_TYPES.contains(entry.getKey())) {
					fields.put(entry.getKey(), entry.getValue());
				}
			}
		}
		addInvoiceDataToMap(fields, order);
		addDeliveryDataToMap(fields, order);
		addCartDataToMap(fields, order);
		fields.put("AMOUNT", "" + order.getTotal());
		fields.put("ORDERID", order.getId());
		return fields;
	}
	
	private void addInvoiceDataToMap(TreeMap<String, String> fields, Order order) {
		fields.put("ECOM_BILLTO_POSTAL_NAME_FIRST", cutAt(order.getBilling().getFname(), 35));
		fields.put("ECOM_BILLTO_POSTAL_NAME_LAST", cutAt(order.getBilling().getLname(), 35));
		fields.put("OWNERADDRESS", cutAt(order.getBilling().getStreet(), 35));
		fields.put("ECOM_BILLTO_POSTAL_STREET_NUMBER", cutAt(order.getBilling().getStreet2(), 10));
		fields.put("OWNERZIP", cutAt(order.getBilling().getPostalcode(), 10));
		fields.put("OWNERTOWN", cutAt(order.getBilling().getCity(), 25));
		fields.put("OWNERCTY", cutAt(order.getBilling().getCountry(), 2));
		fields.put("EMAIL", cutAt(order.getBilling().getEmail(), 50));
		fields.put("OWNERTELNO", cutAt(order.getBilling().getTelephone(), 10));
	}
	
	private void addDeliveryDataToMap(TreeMap<String, String> fields, Order order) {
		fields.put("ECOM_SHIPTO_POSTAL_NAME_FIRST", cutAt(order.getBilling().getFname(), 35));
		fields.put("ECOM_SHIPTO_POSTAL_NAME_LAST", cutAt(order.getBilling().getLname(), 35));
		fields.put("ECOM_SHIPTO_POSTAL_STREET_LINE1", cutAt(order.getBilling().getStreet(), 35));
		fields.put("ECOM_SHIPTO_POSTAL_STREET_NUMBER", cutAt(order.getBilling().getStreet2(), 10));
		fields.put("ECOM_SHIPTO_POSTAL_POSTALCODE", cutAt(order.getBilling().getPostalcode(), 10));
		fields.put("ECOM_SHIPTO_POSTAL_CITY", cutAt(order.getBilling().getCity(), 25));
		fields.put("ECOM_SHIPTO_POSTAL_COUNTRYCODE", cutAt(order.getBilling().getCountry(), 2));
	}
	
	private void addCartDataToMap(TreeMap<String, String> fields, Order order) {
		int i = 1;
		for(LineItem item : order.getCart().getItems().values()) {
			addLineItemToMap(fields, item, i);
			i++;
		}
		if(order.getShippingCost() != null) {
			addLineItemToMap(fields, order.getShippingCost(), i);
			i++;
		}
	}
	
	private void addLineItemToMap(TreeMap<String, String> fields, LineItem lineItem, int itemNumber) {
		fields.put("ITEMID" + itemNumber, lineItem.getId());
		fields.put("ITEMNAME" + itemNumber, cutAt(lineItem.getDescription(), 40));
		fields.put("ITEMPRICE" + itemNumber, "" + ((double) lineItem.getPrice() / 100));
		fields.put("ITEMQUANT" + itemNumber, "" + (lineItem.getQuantity() > 0 ? lineItem.getQuantity() : 1));
		fields.put("ITEMVATCODE" + itemNumber, "21%");
		fields.put("TAXINCLUDED" + itemNumber, "1");
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
	
	/**
	 * Cuts a string at a specified index
	 * @param input
	 * @param index
	 * @return
	 */
	public static String cutAt(String input, int index) {
		if(input == null) {
			return "";
		}
		return input.substring(0, Math.min(input.length(), index));
	}
}
