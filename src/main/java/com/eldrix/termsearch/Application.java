package com.eldrix.termsearch;

import com.google.inject.Binder;
import com.google.inject.Module;

import io.bootique.BQCoreModule;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.meta.application.OptionMetadata;

/**
 * Hello world!
 *
 */
public class Application implements Module {
	public static final String OPTION_INDEX="index";
	
	public static void main( String[] args ) {
		Bootique
		.app(args)
		.module(Application.class)
		.autoLoadModules()
		.exec()
		.exit();    
	}

	@Override
	public void configure(Binder binder) {
		OptionMetadata option = OptionMetadata
			    .builder(OPTION_INDEX, "Location for index")
			    .valueRequired("path").build();

		BQCoreModule.extend(binder).addOption(option);
        BQCoreModule.extend(binder).addCommand(ImportCommand.class);  
        JerseyModule.extend(binder).addResource(SearchResource.class);
	}
}
