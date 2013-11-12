/**
  * @(#)XMLParserMasterRoutine.java
  *
  * A general XML parsing routine for extracting course data from XML formatted documents. This
  * is the master XML parsing routine that performs the work of scheduling additional helper
  * routines for individual courses
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
package io.coursescheduler.scheduler.parse.routines.xml;


import io.coursescheduler.scheduler.parse.ParseActionBatch;
import io.coursescheduler.scheduler.parse.ParseException;
import io.coursescheduler.scheduler.parse.routines.ParserRoutine;
import io.coursescheduler.scheduler.parse.tools.xml.DocumentBuilderProvider;
import io.coursescheduler.scheduler.parse.tools.xml.XMLParserTool;
import io.coursescheduler.scheduler.parse.tools.xml.XMLParserToolMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RecursiveAction;
import java.util.prefs.Preferences;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * A general XML parsing routine for extracting course data from XML formatted documents. This
 * is the master XML parsing routine that performs the work of scheduling additional helper
 * routines for individual courses
 *
 * @author Mike Reinhold
 *
 */
public class XMLParserMasterRoutine extends ParserRoutine {
	
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Component based logger
	 */
	private transient Logger log = LoggerFactory.getLogger(getClass().getName());
	
	/**
	 * XML Document for this XMLParserMasterRoutine to process
	 */
	private transient Document doc;

	/**
	 * The {@link java.util.prefs.Preferences} node containing the configuration for this XMLParserMasterRoutine
	 */
	private transient Preferences profile;
	
	/**
	 * The XML Parser Tool that will be used to process the XML document to extract nodes
	 */
	private transient XMLParserTool parser;
	
	/**
	 * Parser Helper Routine Factory for creating parser routines
	 */
	private transient XMLParserHelperRoutineFactory parserHelperFactory;
	
	/**
	 * Create a new XMLParserMasterRoutine instance using the specified input stream and the preferences node
	 * containing the configuration necessary to process the data from the XML document represented by
	 * the input stream
	 * 
	 * @param helperMap the XMLParserHelperRoutine mapping instance to use for retrieving the helper parser routine
	 * @param toolMap the ParserTool mapping instance to use for retrieving a ParserTool
	 * @param builderProvider a provider for getting a DocumentBuilder instance which is used to create the XML document from the input stream
	 * @param input the input stream from which the XML document can be obtained
	 * @param profile the Preferences node that contains the configuration necessary to parse the xml document
	 *
	 * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
	 * @throws SAXException if any parse error occurs
	 * @throws IOException if any io error occurs
	 */
	@AssistedInject
	public XMLParserMasterRoutine(XMLParserHelperMap helperMap, XMLParserToolMap toolMap, DocumentBuilderProvider builderProvider, @Assisted("source") InputStream input, @Assisted("profile") Preferences profile) throws ParserConfigurationException, SAXException, IOException{
		super();
		
		doc = builderProvider.get().parse(input);
		this.profile = profile;
		this.parser = toolMap.getXMLParserTool(
			profile.get(XMLParserConstants.PARSER_TOOL_PROPERTY, null)
		);
		parserHelperFactory = helperMap.getXMLCourseParserHelperRoutineFactory(
			profile.get(XMLParserConstants.PARSER_HELPER_PROPERTY, null)
		); 
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.RecursiveAction#compute()
	 */
	@Override
	protected void compute() {
		log.info("Starting to parse the XML input");
		long start = System.currentTimeMillis();
		try {
			executeBatches(profile, getElementIDs(profile));
		} catch (Exception e) {
			log.error("Exception retrieving element identifiers", e);
		}
		
		long end = System.currentTimeMillis();
		log.info("Retrieved data for {} elements in {} milliseconds", getDataSets().size(), (end - start));
	}
	
	
	/**
	 * Retrieve the set of element identifiers found within the XML document
	 *
	 * @param settings the preferences node containing the parser routine configuration
	 * @return a set containing the elements ids retrieved from the XML document
	 * @throws ParseException if there is an issue parsing the element IDs from the document
	 */
	private Set<String> getElementIDs(Preferences settings) throws ParseException{
		log.info("Retrieving element identifiers from source data set");
		Set<String> elements = new TreeSet<String>();
		NodeList list = parser.retrieveNodeList(doc, settings, XMLParserConstants.GROUP_LIST_PROPERTY);
		
		for(int item = 0; item < list.getLength(); item++){
			Node node = list.item(item).cloneNode(true);
			String courseID = node.getTextContent();
			elements.add(courseID);
			log.debug("Found row belonging to {}", courseID);
		}

		log.debug("Finished retrieving element identifiers from source data set");
		return elements;
	}
	
	/**
	 * Create a new sub-task for processing the specified element identifier using the specified preferences node
	 *
	 * @param settings the preferences node containing the parser routine configuration
	 * @param elementID the element ID for which this task will retrieve data
	 * @return the sub-task which will process the element data 
	 * @throws ParseException if there is an issue retrieving the list of nodes
	 */
	private XMLParserHelperRoutine createElementTask(Preferences settings, String elementID) throws ParseException{
		Map<String, String> replacements = new HashMap<String, String>();
		replacements.put(XMLParserConstants.ELEMENT_ID_VARIABLE, elementID);

		NodeList list = parser.retrieveNodeList(doc, settings, XMLParserConstants.GROUP_ELEMENT_PROPERTY, replacements);
		
		log.info("Found {} rows for {}", list.getLength(), elementID);
		
		Node node;
		ConcurrentMap<String, String> data = new ConcurrentHashMap<>();
		List<Node> nodeList = new ArrayList<Node>();
		
		getDataSets().put(elementID, data);
		for(int item = 0; item < list.getLength(); item++){
			node = list.item(item).cloneNode(true);
			nodeList.add(node);
		}
		
		return parserHelperFactory.createParserRoutine(nodeList, settings, elementID, "", data);
	}
	
	/**
	 * Build and execute a sub-task per course ID, batching the sub-tasks into task groups based on 
	 * the configured batch size.
	 * 
	 * This method blocks until all batches have completed processing (in any completion state, including failure)
	 *
	 * @param settings the preferences node containing the parser routine configuration
	 * @param elements the set of element IDs in the document that should be processed in batch
	 */
	private void executeBatches(Preferences settings, Set<String> elements) {
		int batchSize = settings.getInt(BATCH_SIZE_PROPERTY, Integer.MAX_VALUE);
		log.info("Using batch size of {}", batchSize);

		List<RecursiveAction> batches = new ArrayList<>();
		List<RecursiveAction> elementsBatch = new ArrayList<>();
		int coursesBatched = 0;
		
		for(String elementID: elements) {
			RecursiveAction task;
			try {
				task = createElementTask(settings, elementID);
				elementsBatch.add(task);
			} catch (Exception e) {
				log.error("Unable to create background task for processing element {}", elementID);
			}

			//increment this separately from the task creation in case there is an issue with that step
			//this needs to be incremented for each course so that we can be sure to initiate the last 
			//batch in the instance that courses.size() % batchSize != 0 (which will be often)
			coursesBatched++;	
			
			//check if batch size is met or if all
			if(elementsBatch.size() == batchSize || coursesBatched == elements.size()) {
				RecursiveAction batch = new ParseActionBatch(elementsBatch);
				batches.add(batch);
				batch.fork();
				elementsBatch = new ArrayList<>();
			}
		}
		
		//wait for the batches to finish processing
		log.info("Waiting for batches to finish");
		for(RecursiveAction action: batches) {
			action.join();
		}
		log.info("All batches finished");
	}
}