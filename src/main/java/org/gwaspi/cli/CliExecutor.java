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
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;

/**
 * Parses command line parameters and executes them.
 */
public class CliExecutor {

	private static final Map<String, ScriptCommand> SCRIPT_COMMANDS;
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

		SCRIPT_COMMANDS = Collections.unmodifiableMap(tmpScriptCommands);
	}
	private static void addScriptCommand(Map<String, ScriptCommand> commands, ScriptCommand scriptCommand) {
		commands.put(scriptCommand.getCommandName(), scriptCommand);
	}

	private final File scriptFile;

	public CliExecutor(File scriptFile) {

		this.scriptFile = scriptFile;
	}

	public void execute() throws IOException, ScriptExecutionException {

		final List<Map<String, String>> scripts = ScriptUtils.readScriptsFromFile(scriptFile);

		System.out.println();
		System.out.println("Scripts in queue: " + scripts.size());

		// parse and execute all scripts, one after an other, in order, in a single thread
		for (int scriptIndex = 0; scriptIndex < scripts.size(); scriptIndex++) {
			final Map<String, String> args = scripts.get(scriptIndex);
			// GET COMMAND NAME OF CURRENT SCRIPT
			final String command = args.values().iterator().next(); // HACK this will not always work, as we now support named arguments in any order

			System.out.println("Script " + scriptIndex + ": " + command);

			final ScriptCommand scriptCommand = SCRIPT_COMMANDS.get(command);

			if (scriptCommand == null) {
				throw new ScriptExecutionException(
						"Not a valid script command name: \"" + command + "\"");
			} else {
				scriptCommand.execute(args);
			}
		}
		System.out.println("done. (all scripts executed)");
		System.out.println();
		System.out.println();
	}
}
