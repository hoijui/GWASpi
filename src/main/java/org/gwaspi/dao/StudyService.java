package org.gwaspi.dao;

import java.io.IOException;
import java.util.List;
import org.gwaspi.model.Study;

public interface StudyService {

	Study getById(int studyId) throws IOException;

	List<Study> getAll() throws IOException;

	Object[][] getAllAsTable() throws IOException;
}
