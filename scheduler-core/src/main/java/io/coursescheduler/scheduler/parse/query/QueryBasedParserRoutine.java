/**
 * @(#)QueryBasedParserRoutine.java
 *
 * Abstract parser routine for query based parsing
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
package io.coursescheduler.scheduler.parse.query;

import static io.coursescheduler.scheduler.parse.ParseConstants.PARSER_IMPLEMENTATION_KEY;
import static io.coursescheduler.scheduler.parse.ParseConstants.SCRIPT_IMPLEMENTATION_KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RecursiveAction;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import io.coursescheduler.scheduler.parse.ParseActionBatch;
import io.coursescheduler.scheduler.parse.ParseException;
import io.coursescheduler.scheduler.parse.ParserRoutine;
import io.coursescheduler.util.script.engine.ScriptEngine;
import io.coursescheduler.util.script.engine.ScriptEngineMap;

/**
 * Provide an interface for and default implementation of a query based parser routine. This is for 
 * parsers that operate on documents or structures that can be queried against. Generally the parser 
 * will query for a group of elements that are related based on some data element and then build
 * background tasks for querying data out of those elements.
 *
 * @author Mike Reinhold
 *
 */
public abstract class QueryBasedParserRoutine<N> extends ParserRoutine {
	
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Preferences child node containing the query strings to use when retrieving data from the 
	 * queryable document
	 * 
	 * Value: {@value}
	 */
	public static final String QUERY_PREFERENCES_NODE = "_query";

	/**
	 * Component based logger
	 */
	private transient Logger log = LoggerFactory.getLogger(getClass().getName());
	
	/**
	 * The {@link java.util.prefs.Preferences} node containing the configuration for this ParserRoutine
	 */
	private transient Preferences profile;

	/**
	 * The Parser Tool that will be used to assist with querying data out of the source data
	 */
	private QueryBasedParserTool<N> parser;
	
	/**
	 * Script engine that will perform post processing on data element
	 */
	private ScriptEngine script;

	/**
	 * Create a new QueryBasedParserRoutine based on the configuration profile specified
	 *
	 * @param toolMap the map of query based tools which could be used for parsing
	 * @param scriptMap the map of script engines which could be used for processing
	 * @param profile the {@link java.util.prefs.Preferences} node containing the parser profile
	 */
	@AssistedInject
	public QueryBasedParserRoutine(QueryBasedParserToolMap toolMap, ScriptEngineMap scriptMap, @Assisted("profile") Preferences profile) {
		super();
		
		this.profile = profile;
		
		String parserKey = profile.get(PARSER_IMPLEMENTATION_KEY, "");	//TODO specific preferences properties
		String scriptKey = profile.get(SCRIPT_IMPLEMENTATION_KEY, "");	//TODO specific preferences properties
		
		this.parser = toolMap.getQueryBasedParserTool(parserKey);
		this.script = scriptMap.getScriptEngine(scriptKey, profile); 
	}
	//ANALYZE alternate constructor/factory that allows the implementation to specify the parserKey and/or scriptKey directly
	
	/* (non-Javadoc)
	 * @see java.util.concurrent.RecursiveAction#compute()
	 */
	@Override
	protected void compute() {
		log.info("Preparing to query the input for data elements");
		long start = System.currentTimeMillis();
		
		try {
			N queryable = prepareInput();
			
			Set<String> groups = queryGroups(queryable, profile);
			log.info("Found {} groups of elements to process", groups.size());
			
			List<RecursiveAction> batches = buildBatches(profile, queryable, groups);		
			waitForBatches(batches);
		}catch(Exception e) {
			log.error("Exception querying input for data elements", e);
		}

		long end = System.currentTimeMillis();
		log.info("Retrieved data for {} elements in {} milliseconds", getDataSets().size(), (end - start));
	}


	/**
	 * Prepare the input document for processing. QueryBasedParser implementations must define the input preparation
	 * based on the needs of the implementation.
	 * 
	 * return the top level node or element representing the input that can be used to start querying the input for data
	 * @throws Exception if there is an issue preparing the input. Many different types of errors could occur.
	 */
	public abstract N prepareInput() throws Exception;
	
	/**
	 * TODO Describe this method
	 *
	 * @param settings
	 * @param queryable
	 * @param groups
	 * @return
	 */
	protected List<RecursiveAction> buildBatches(Preferences settings, N queryable, Set<String> groups){
		long start = System.currentTimeMillis();
		log.info("Preparing to build background tasks for {} groups", groups.size());
		
		int batchSize = profile.getInt(BATCH_SIZE_PROPERTY, Integer.MAX_VALUE);
		log.info("Using batch size of {}", batchSize);
		
		List<RecursiveAction> batches = new ArrayList<>();
		List<RecursiveAction> elementsBatch = new ArrayList<>();
		int elementsBatched = 0;
		
		for(String group: groups) {
			RecursiveAction task;
			try {
				List<N> elements = queryGroup(queryable, settings, group);
				task = createBackgroundTask(group, elements, profile);
				elementsBatch.add(task);
				log.info("Finished creating background task for processing group {}", group);
			} catch (Exception e) {
				log.error("Unable to create background task for processing group {}", group);
			}

			//increment this separately from the task creation in case there is an issue with that step
			//this needs to be incremented for each course so that we can be sure to initiate the last 
			//batch in the instance that courses.size() % batchSize != 0 (which will be often)
			elementsBatched++;	
			
			//check if batch size is met or if all
			if(elementsBatch.size() == batchSize || elementsBatched == groups.size()) {
				log.debug("{} tasks ready to be batched ({} of {})", new Object[] {elementsBatch.size(), elementsBatched, groups.size()});
				RecursiveAction batch = new ParseActionBatch(elementsBatch);
				batches.add(batch);
				log.info("Forking background task batch {}", batch);
				batch.fork();
				elementsBatch = new ArrayList<>();
			}
		}
		long end = System.currentTimeMillis();
		log.info("Completed building background tasks for {} groups in {} batches in {} ms", new Object[] {groups.size(), batches.size(), end - start});
		
		return batches;
	}
	
	/**
	 * TODO Describe this method
	 *
	 * @param batches
	 */
	protected void waitForBatches(List<RecursiveAction> batches) {
		long start = System.currentTimeMillis();
		long end;
		log.info("Waiting for {} batches to finish", batches.size());
		for(RecursiveAction action: batches) {
			log.debug("Waiting for batch {} to finish processing", action);
			action.join();
			end = System.currentTimeMillis();
			log.debug("Batch {} finished after {} ms", action, end - start);
		}
		end = System.currentTimeMillis();
		log.info("All batches finished processing in {} ms", end - start);
	}
	
	/**
	 * TODO Describe this method
	 *
	 * @param group
	 * @param elements
	 * @param profile
	 * @return
	 */
	protected RecursiveAction createBackgroundTask(String group, List<N> elements, Preferences profile) {
		ConcurrentMap<String, String> data = new ConcurrentHashMap<>();
		getDataSets().put(group, data);
		
		RecursiveAction action = createBackgroundTaskImpl(group, elements, data, profile);
		
		return action;
	}
	
	/**
	 * TODO Describe this method
	 *
	 * @param group
	 * @param elements
	 * @param data
	 * @param profile
	 * @return
	 */
	protected abstract RecursiveAction createBackgroundTaskImpl(String group, List<N> elements, ConcurrentMap<String, String> data, Preferences profile);
		
	/**
	 * TODO Describe this method
	 *
	 * @param queryable
	 * @param settings
	 * @return
	 * @throws ParseException
	 */
	protected Set<String> queryGroups(N queryable, Preferences settings) throws ParseException {
		long start = System.currentTimeMillis();
		log.info("Retrieving element identifiers from source data set");
		Set<String> elements = new TreeSet<String>();
		
		List<N> groupList = parser.query(queryable, settings, "" /* TODO all groups query */);
		
		for(N element: groupList) {
			String groupName = parser.asString(element);
			groupName = script.executeScript(groupName, settings, "" /* TODO group list property script */ );
			elements.add(groupName);
			log.debug("Found row belonging to {}", groupName);
		}

		long end = System.currentTimeMillis();
		log.debug("Finished retrieving element identifiers from source data in {} ms", end - start);
		return elements;
	}
	
	/**
	 * TODO Describe this method
	 *
	 * @param queryable
	 * @param settings
	 * @param group
	 * @return
	 */
	protected List<N> queryGroup(N queryable, Preferences settings, String group){
		Map<String, String> replacements = new HashMap<String, String>();
		replacements.put("" /* TODO variable name */, group);

		List<N> groupElements;
		
		try{
			groupElements = parser.query(queryable, settings, "" /* TODO group elements query */, replacements);
		}catch(ParseException e) {
			log.error("Erorr querying group elements for {} using configuration {}", group, settings);
			log.error("Error during parser query",e);
			groupElements = Collections.emptyList();
			log.warn("Using empty list for group elements");
		}

		log.info("Found {} elements for group {}", groupElements.size(), group);
				
		return groupElements;
	}
}