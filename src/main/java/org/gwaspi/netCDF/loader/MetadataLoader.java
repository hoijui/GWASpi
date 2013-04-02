package org.gwaspi.netCDF.loader;

import java.io.IOException;
import java.util.Map;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;

public interface MetadataLoader {

	Map<MarkerKey, MarkerMetadata> getSortedMarkerSetWithMetaData() throws IOException;
}
