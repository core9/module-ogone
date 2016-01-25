package io.core9.module.commerce.ogone;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import io.core9.commerce.CommerceDataHandlerHelper;
import io.core9.commerce.checkout.Order;
import io.core9.plugin.server.request.Request;
import io.core9.plugin.widgets.datahandler.DataHandler;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

/**
 * Ogone Verifier DataHandler
 * Verifies Ogone payments via the widgets flow.
 * 
 * @author mark.wienk@core9.io
 *
 */
@PluginImplementation
public class OgoneVerifierDataHandlerImpl<T extends OgoneVerifierDataHandlerConfig> implements OgoneVerifierDataHandler<T> {
	
	private static final DocumentBuilderFactory XML_FACTORY = DocumentBuilderFactory.newInstance();

	@InjectPlugin
	private CommerceDataHandlerHelper helper;
	
	@Override
	public String getName() {
		return "Payment-Ogone-Verifier";
	}

	@Override
	public Class<? extends DataHandlerFactoryConfig> getConfigClass() {
		return OgoneVerifierDataHandlerConfig.class;
	}

	@Override
	public DataHandler<T> createDataHandler(final DataHandlerFactoryConfig options) {
		@SuppressWarnings("unchecked")
		final T config = (T) options; 
		return new DataHandler<T>() {

			@Override
			public Map<String, Object> handle(Request req) {
				Map<String, Object> result = new HashMap<String, Object>();
				Order order = helper.getOrder(req);
				if(config.getPaymentMethods() != null && config.getPaymentMethods().contains(order.getPaymentmethod())) {
					Map<String, Object> paymentData = getOgoneData(config, order);
					handlePaymentData(order, paymentData);
					helper.saveOrder(req, order);
				}
				result.put("type", "ogone");
				return result;
			}

			@Override
			public T getOptions() {
				return config;
			}
		};
	}
	
	@Override
	public Order handlePaymentData(Order order, Map<String, Object> paymentData) {
		order.setPaymentData(paymentData);
		String strStatus = (String) paymentData.get("STATUS");
		String orderId = (String) paymentData.get("orderID");
		if(!orderId.startsWith(order.getId())) {
			return null;
		}
		if(strStatus.equals("")) {
			order.setStatus("paying");
			return order;
		}
		int status = Integer.parseInt(strStatus);
		
		switch(status) {
		case 5: // Authorised
		case 9: // Payment requested
		case 91: // Payment processing
			order.setStatus("paid");
			break;
		case 51: // Authorisation waiting
		case 52: // Authorisation unknown
		case 92: // Authorisation uncertain
			order.setStatus("uncertain");
			break;
		case 93: // Payment refused
		case 2: // Authorization refused
		case 0:	// Invalid or incomplete
		default:
			order.setStatus("paying");
			break;
		}
		return order;
	}
	
	@Override
	public Map<String, Object> getOgoneData(OgoneVerifierDataHandlerConfig config, Order order) {
		try {
			Map<String,Object> paymentData = getOgoneData(config, order.getPaymentId());
			return paymentData;
		} catch (OgoneResultNotParseableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Map<String,Object> getOgoneData(OgoneVerifierDataHandlerConfig config, String paymentId) throws IOException, OgoneResultNotParseableException {
		Map<String,Object> result = new HashMap<String, Object>();
		URL obj = new URL("https://secure.ogone.com/ncol/" + (config.isTest() ? "test" : "prod") + "/querydirect.asp");
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
 
		con.setRequestMethod("POST");
 		StringBuilder urlParameters = new StringBuilder("PSPID=" + config.getPspid());
 		urlParameters.append("&USERID=" + config.getUsername());
 		urlParameters.append("&PSWD=" + config.getPassword());
 		urlParameters.append("&ORDERID=" + paymentId);
 
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters.toString());
		wr.flush();
		wr.close();
 		
		try {
			DocumentBuilder db = XML_FACTORY.newDocumentBuilder();
			Document dom = db.parse(con.getInputStream());
			NamedNodeMap attributes = dom.getDocumentElement().getAttributes();
			for(int i = 0; i < attributes.getLength(); i++ ) {
				result.put(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new OgoneResultNotParseableException();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new OgoneResultNotParseableException();
		} catch (SAXException e) {
			e.printStackTrace();
			throw new OgoneResultNotParseableException();
		}
		return result;
	}
}
