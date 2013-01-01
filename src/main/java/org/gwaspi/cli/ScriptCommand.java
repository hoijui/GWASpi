package org.gwaspi.cli;

import java.io.IOException;
import java.util.List;

/**
 * Parses, prepares and executes one command read from a script file.
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
interface ScriptCommand {

	String getCommandName();

	boolean execute(List<String> args) throws IOException;
}
