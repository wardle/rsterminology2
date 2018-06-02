package com.eldrix.termsearch;

import java.io.BufferedInputStream;
import java.io.IOException;

import com.eldrix.terminology.snomedct.Search;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;

public class ImportCommand extends CommandWithMetadata {

	private static CommandMetadata createMetadata() {
		return CommandMetadata.builder(ImportCommand.class)
				.description("Build index by importing  extended descriptions in delimited protobuf format.")
				.build();
	}

	public ImportCommand() {
		super(createMetadata());
	}
	
	@Override
	public CommandOutcome run(Cli cli) {
		BufferedInputStream bis = new BufferedInputStream(System.in);
		try {
			Search.getInstance(cli.optionString(Application.OPTION_INDEX)).processFromProtobuf(bis);
		} catch (IOException e) {
			return CommandOutcome.failed(1, e);
		}
		return CommandOutcome.succeeded();
	}

}
