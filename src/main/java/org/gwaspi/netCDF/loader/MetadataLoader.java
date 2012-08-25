package org.gwaspi.netCDF.loader;

import java.io.IOException;
import java.util.Map;

public interface MetadataLoader {

	Map<String, Object> getSortedMarkerSetWithMetaData() throws IOException;
}
