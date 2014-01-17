/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.cli;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;

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
		addScriptCommand(tmpScriptCommands, new TestScriptCommand(OPType.ALLELICTEST));
		addScriptCommand(tmpScriptCommands, new TestScriptCommand(OPType.GENOTYPICTEST));
		addScriptCommand(tmpScriptCommands, new CombiTestScriptCommand());
		addScriptCommand(tmpScriptCommands, new GenotypeFrequencyHardyWeinbergScriptCommand());
		addScriptCommand(tmpScriptCommands, new ExportMatrixScriptCommand());
		addScriptCommand(tmpScriptCommands, new TestScriptCommand(OPType.TRENDTEST));
		addScriptCommand(tmpScriptCommands, new UpdateSampleInfoScriptCommand());

		scriptCommands = Collections.unmodifiableMap(tmpScriptCommands);
	}
	private static void addScriptCommand(Map<String, ScriptCommand> commands, ScriptCommand scriptCommand) {
		commands.put(scriptCommand.getCommandName(), scriptCommand);
	}

	private final File scriptFile;

	public CliExecutor(File scriptFile) {

		this.scriptFile = scriptFile;
	}

	public boolean execute() throws IOException {

		boolean success = false;

		// GET ALL SCRIPTS CONTAINED IN FILE
		List<Map<String, String>> scripts = ScriptUtils.readArgsFromScript(scriptFile);

		System.out.println("\nScripts in queue: " + scripts.size());

		// ITERATE THROUGH SCRIPTS AND LAUNCH THREAD FOR EACH
		for (int i = 0; i < scripts.size(); i++) {
			// TRY TO GARBAGE COLLECT BEFORE ANY OTHER THING
			System.gc();

			// GET ARGS FOR CURRENT SCRIPT
			Map<String, String> args = scripts.get(i);
			// GET COMMAND NAME OF CURRENT SCRIPT
			String command = args.values().iterator().next();

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
