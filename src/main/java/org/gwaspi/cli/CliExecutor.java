package org.gwaspi.cli;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses command line parameters and executes them.
 */
public class CliExecutor {

	private static final Map<String, ScriptCommand> scriptCommands;
	static {
		Map<String, ScriptCommand> tmpScriptCommands = new HashMap<String, ScriptCommand>();

		addScriptCommand(tmpScriptCommands, new LoadGenotypesScriptCommand());
		addScriptCommand(tmpScriptCommands, new LoadGenotypesDoGwasInOneGoScriptCommand());
		addScriptCommand(tmpScriptCommands, new GwasInOneGoScriptCommand());
		addScriptCommand(tmpScriptCommands, new AssociationScriptCommand(true));
		addScriptCommand(tmpScriptCommands, new AssociationScriptCommand(false));
		addScriptCommand(tmpScriptCommands, new GenotypeFrequencyHardyWeinbergScriptCommand());
		addScriptCommand(tmpScriptCommands, new ExportMatrixScriptCommand());
		addScriptCommand(tmpScriptCommands, new TrendTestScriptCommand());
		addScriptCommand(tmpScriptCommands, new UpdateSampleInfoScriptCommand());

		scriptCommands = Collections.unmodifiableMap(tmpScriptCommands);
	}
	private static void addScriptCommand(Map<String, ScriptCommand> commands, ScriptCommand scriptCommand) {
		commands.put(scriptCommand.getCommandName(), scriptCommand);
	}

	private File scriptFile;

	public CliExecutor(File scriptFile) {

		this.scriptFile = scriptFile;
	}

	public boolean execute() throws IOException {

		boolean success = false;

		// GET ALL SCRIPTS CONTAINED IN FILE
		List<List<String>> scriptsAL = org.gwaspi.cli.Utils.readArgsFromScript(scriptFile);

		System.out.println("\nScripts in queue: " + scriptsAL.size());

		// ITERATE THROUGH SCRIPTS AND LAUNCH THREAD FOR EACH
		for (int i = 0; i < scriptsAL.size(); i++) {

			// TRY TO GARBAGE COLLECT BEFORE ANY OTHER THING
			System.gc();

			// GET ARGS FOR CURRENT SCRIPT
			List<String> args = scriptsAL.get(i);
			// GET COMMAND LINE OF CURRENT SCRIPT
			String command = args.get(0).toString();

			System.out.println("Script " + i + ": " + command);

			ScriptCommand scriptCommand = scriptCommands.get(command);

			if (scriptCommand == null) {
				throw new IOException("Not a valid script command name: \"" + command + "\"");
			} else {
				success = scriptCommand.execute(args);
			}
		}
		System.out.println("\n");

		return success;
	}
}
