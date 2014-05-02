/**
  * @(#)XMLParserModule.java
  *
  * Guice module for the XML Course parser plugin binding
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
package io.coursescheduler.scheduler.parse.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.coursescheduler.scheduler.parse.ParserRoutine;
import io.coursescheduler.scheduler.parse.ParserRoutineFactory;
import io.coursescheduler.scheduler.parse.course.CourseParserRoutineFactory;
import io.coursescheduler.scheduler.parse.stream.StreamParserRoutineFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;

/**
 * Guice module for the XML Course parser plugin binding
 *
 * @author Mike Reinhold
 *
 */
public class XMLParserModule extends AbstractModule {
	
	/**
	 * Component based logger
	 */
	Logger log = LoggerFactory.getLogger(getClass().getName());
	
	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {		
		configureXMLCourseParserMasterRoutine();
		configureSectionBasedXMLCourseParserHelperRoutine();
	}
	
	/**
	 * Perform all of the configuration steps necessary to properly bind the XMLParserMasterRoutine class
	 *
	 */
	private void configureXMLCourseParserMasterRoutine() {
		/*
		//install a module indicating that XMLParserMasterRoutine can be built from a factory with assisted inject
		log.debug("Installing FactoryModuleBuilder for {} with implementations {}",
				XMLParserMasterRoutineFactory.class,
				XMLParserMasterRoutine.class + " for " + ParserRoutine.class
		);
		install(new FactoryModuleBuilder()
			.implement(ParserRoutine.class, XMLParserMasterRoutine.class)
			.build(XMLParserMasterRoutineFactory.class)
		);
		
		//add a mapped binding from the ParserRouting class to the implementation classes
		log.debug("Creating MapBinder entry for {} to {} at key {}", new Object[] {
				ParserRoutineFactory.class,
				XMLParserMasterRoutineFactory.class,
				XMLParserMasterRoutineFactory.PARSER_ROUTINE_INTERNAL_NAME
		});  
		MapBinder<String, ParserRoutineFactory> parseRoutineBinder = MapBinder.newMapBinder(binder(), String.class, ParserRoutineFactory.class);
		parseRoutineBinder.addBinding(XMLParserMasterRoutineFactory.PARSER_ROUTINE_INTERNAL_NAME).toProvider(getProvider(XMLParserMasterRoutineFactory.class));

		//add a mapped binding for the CourseParserRoutine class to the implementation class
		log.debug("Creating MapBinder entry for {} to {} at key {}", new Object[] {
				CourseParserRoutineFactory.class,
				XMLParserMasterRoutineFactory.class,
				XMLParserMasterRoutineFactory.PARSER_ROUTINE_INTERNAL_NAME
		}); 
		MapBinder<String, CourseParserRoutineFactory> courseParseRoutineBinder = MapBinder.newMapBinder(binder(), String.class, CourseParserRoutineFactory.class);
		courseParseRoutineBinder.addBinding(XMLParserMasterRoutineFactory.PARSER_ROUTINE_INTERNAL_NAME).toProvider(getProvider(XMLParserMasterRoutineFactory.class));
		
		//add a mapped binding for the StreamParserRoutine class to the implementation class
		log.debug("Creating MapBinder entry for {} to {} at key {}", new Object[] {
				StreamParserRoutineFactory.class,
				XMLParserMasterRoutineFactory.class,
				XMLParserMasterRoutineFactory.PARSER_ROUTINE_INTERNAL_NAME
		}); 
		MapBinder<String, StreamParserRoutineFactory> streamParseRoutineBinder = MapBinder.newMapBinder(binder(), String.class, StreamParserRoutineFactory.class);
		streamParseRoutineBinder.addBinding(XMLParserMasterRoutineFactory.PARSER_ROUTINE_INTERNAL_NAME).toProvider(getProvider(XMLParserMasterRoutineFactory.class));
		
		*/
	}
	
	
	/**
	 * Perform all of the configuration steps necessary to properly bind the NodeGroupXMLCourseParserHelperRoutine class
	 *
	 */
	private void configureSectionBasedXMLCourseParserHelperRoutine() {
		//install a module indicating that NodeGroupXMLCourseParserHelperRoutine can be built from a factory with assisted inject
		log.debug("Installing FactoryModuleBuilder for {} with implementations {}, {}",
				NodeGroupXMLCourseParserHelperRoutineFactory.class,
				 NodeGroupXMLCourseParserHelperRoutine.class + " for " + ParserRoutine.class,
				 NodeGroupXMLCourseParserHelperRoutine.class + " for " + XMLParserHelperRoutine.class
		);
		install(new FactoryModuleBuilder()
			.implement(ParserRoutine.class, NodeGroupXMLCourseParserHelperRoutine.class)
			.implement(XMLParserHelperRoutine.class, NodeGroupXMLCourseParserHelperRoutine.class)
			.build(NodeGroupXMLCourseParserHelperRoutineFactory.class)
		);
		
		//add a mapped binding for the ParserRoutine class to the implementation class
		log.debug("Creating MapBinder entry for {} to {} at key {}", new Object[] {
				ParserRoutineFactory.class,
				NodeGroupXMLCourseParserHelperRoutineFactory.class,
				NodeGroupXMLCourseParserHelperRoutineFactory.PARSER_ROUTINE_INTERNAL_NAME
		}); 
		MapBinder<String, ParserRoutineFactory> parseRoutineBinder = MapBinder.newMapBinder(binder(), String.class, ParserRoutineFactory.class);
		parseRoutineBinder.addBinding(NodeGroupXMLCourseParserHelperRoutineFactory.PARSER_ROUTINE_INTERNAL_NAME).toProvider(getProvider(NodeGroupXMLCourseParserHelperRoutineFactory.class));
		
		//add a mapped binding for the XMLParserHelperRoutine class to the implementation class
		log.debug("Creating MapBinder entry for {} to {} at key {}", new Object[] {
				XMLParserHelperRoutine.class,
				NodeGroupXMLCourseParserHelperRoutineFactory.class,
				NodeGroupXMLCourseParserHelperRoutineFactory.PARSER_ROUTINE_INTERNAL_NAME
		}); 
		MapBinder<String, XMLParserHelperRoutineFactory> xmlParseRoutineBinder = MapBinder.newMapBinder(binder(), String.class, XMLParserHelperRoutineFactory.class);
		xmlParseRoutineBinder.addBinding(NodeGroupXMLCourseParserHelperRoutineFactory.PARSER_ROUTINE_INTERNAL_NAME).toProvider(getProvider(NodeGroupXMLCourseParserHelperRoutineFactory.class));
	}	
}
