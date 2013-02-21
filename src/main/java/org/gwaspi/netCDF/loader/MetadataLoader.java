package org.gwaspi.netCDF.loader;

import java.io.IOException;
import java.util.Map;
import org.gwaspi.model.MarkerKey;

public interface MetadataLoader {

	Map<MarkerKey, Object> getSortedMarkerSetWithMetaData() throws IOException;
}
