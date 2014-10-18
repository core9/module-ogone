package io.core9.module.commerce.ogone;

import io.core9.commerce.CommerceDataHandlerHelper;
import io.core9.commerce.checkout.Order;
import io.core9.module.auth.AuthenticationPlugin;
import io.core9.module.auth.Session;
import io.core9.plugin.server.Cookie;
import io.core9.plugin.server.Server;
import io.core9.plugin.server.request.Request;
import io.core9.plugin.widgets.datahandler.DataHandler;
import io.core9.plugin.widgets.datahandler.DataHandlerFactoryConfig;
import io.core9.plugin.widgets.widget.WidgetFactory;

import java.util.HashMap;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

import org.apache.log4j.Logger;

@PluginImplementation
public class OgoneFeedbackDataHandlerImpl<T extends OgoneFeedbackDataHandlerConfig> implements OgoneFeedbackDataHandler<T> {
	
	private static final Logger LOG = Logger.getLogger(OgoneFeedbackDataHandlerImpl.class);
	
	@InjectPlugin
	private WidgetFactory widgets;
	
	@InjectPlugin
	private CommerceDataHandlerHelper helper;
	
	@InjectPlugin
	private AuthenticationPlugin auth;
	
	@InjectPlugin
	private Server server;

	@Override
	public String getName() {
		return "Payment-Ogone-Feedback";
	}

	@Override
	public Class<? extends DataHandlerFactoryConfig> getConfigClass() {
		return OgoneFeedbackDataHandlerConfig.class;
	}

	@Override
	public DataHandler<T> createDataHandler(DataHandlerFactoryConfig options) {
		
		@SuppressWarnings("unchecked")
		final T config = (T) options;
		return new DataHandler<T>() {

			@Override
			public Map<String, Object> handle(Request req) {
				Map<String, Object> result = new HashMap<String, Object>();
				parseComplusSession(req);
				try {
					result.put("data", widgets.getRegistry(req.getVirtualHost())
							.get(config.getCheckoutFinalWidgetID())
							.getDataHandler()
							.handle(req));
				} catch (NullPointerException e) {
					LOG.error(config.getCheckoutFinalWidgetID() + " does not exist or has no datahandler");
				}
				return result;
			}

			@Override
			public T getOptions() {
				return config;
			}
		};
	}
	
	/**
	 * Parses the COMPLUS parameter to a SESSION and puts the order on the request context (via helper)
	 * @param req
	 */
	private void parseComplusSession(Request req) {
		if(req.getParams().get("COMPLUS") != null && req.getCookie("CORE9SESSIONID") == null) {
			Cookie cookie = server.newCookie("CORE9SESSIONID");
			cookie.setValue((String) req.getParams().get("COMPLUS"));
			Session session = auth.getUser(req, cookie).getSession();
			Order order = helper.getOrder(req, session);
			LOG.info("Put order " + order.getId() + " on request context");
		}
	}
}
