package org.gwaspi.cli;

import java.io.IOException;
import java.util.List;

/**
 * Parses, prepares and executes one command read from a script file.
 */
interface ScriptCommand {

	String getCommandName();

	boolean execute(List<String> args) throws IOException;
}
