package io.core9.module.commerce.ogone;

import io.core9.core.plugin.Core9Plugin;
import io.core9.plugin.widgets.datahandler.DataHandlerFactory;

public interface OgoneDataHandler<T extends OgoneDataHandlerConfig> extends DataHandlerFactory<T>, Core9Plugin {

}
