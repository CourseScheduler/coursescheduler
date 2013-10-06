/**
  * @(#)DataSource.java
  *
  * Base class for data source implementations to access data for processing
  *
  * @author Mike Reinhold
  * 
  * @license GNU General Public License version 3 (GPLv3)
  *
  * This file is part of Course Scheduler, an open source, cross platform
  * course scheduling tool, configurable for most universities.
  *
  * Copyright (C) 2010-2013 Mike Reinhold
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
  * 
  */
package io.coursescheduler.scheduler.datasource;

import io.coursescheduler.util.text.StrSubstitutionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.RecursiveAction;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Base class for data source implementations to access data for processing
 *
 * @author Mike Reinhold
 *
 */
public abstract class DataSource extends RecursiveAction {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Component based logger
	 */
	private Logger log = LoggerFactory.getLogger(getClass().getName());
	
	/**
	 * Preferences node containing the data source configuration
	 */
	private Preferences settings;
	
	/**
	 * TODO Describe this field
	 */
	private StrSubstitutor replacer;
	
	/**
	 * Create a new DataSource using the specified Preferences node and map of placeholders
	 * and replacement values
	 *
	 * @param settings the Preferences node containing the configuration for the data source access
	 * @param substitutionFactory factory instance for creating StrSubstitution instances
	 * @param replacements map of substitution placeholders to values
	 */
	@AssistedInject
	public DataSource(Preferences settings, StrSubstitutionFactory substitutionFactory, @Assisted("localVars") Map<String, String> replacements) {
		super();
		
		this.settings = settings;
		this.replacer = substitutionFactory.createSubstitutor(replacements);
	}
	
	/**
	 * Return the input stream that is a result of processing the data source.
	 * 
	 * Calling this method prior to execution of the DataSource may result in invalid results.
	 *
	 * @return the input stream resulting from processing the input stream
	 * @throws IOException if there is an error performing IO on the data source
	 */
	public abstract InputStream getDataSourceAsInputStream() throws IOException;
	
	/**
	 * Perform the string replacements and return the resultant value
	 *
	 * @param input the string upon which to perform the replacements
	 * @return the updated string value
	 */
	public String performReplacements(String input) {
		String value = input;
		log.trace("Performing placeholder substitution on string: {}", input);
		value = replacer.replace(value);		
		log.debug("Substituted string is: {}", value);
		return value;
	}
	
	/**
	 * Return the Preferences node corresponding to the DataSource configuration
	 *
	 * @return the configuration node
	 */
	protected Preferences getSettings() {
		return settings;
	}
}
