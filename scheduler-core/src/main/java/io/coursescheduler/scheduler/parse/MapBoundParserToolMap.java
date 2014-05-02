/**
  * @(#)MapBoundParserToolMap.java
  *
  * Default ParserTool mapping class for retrieving registered ParserTool instances based on
  * the implementation key. This implementation uses the MapBinding characteristics of the ParserTools and  
  * their providers in order to provide the keyed retrieval methods.
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
package io.coursescheduler.scheduler.parse;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;


/**
 * Default ParserTool mapping class for retrieving registered ParserTool instances based on
 * the implementation key. This implementation uses the MapBinding characteristics of the ParserTools and  
 * their providers in order to provide the keyed retrieval methods.
 *
 * @author Mike Reinhold
 *
 */
public class MapBoundParserToolMap implements ParserToolMap {

	/**
	 * Map of ParseTool internal names to the Guice Providers used to create instances of the ParserTool
	 */
	private Map<String, Provider<ParserTool>> parserProviders;
	
	/**
	 * Create a new MapBoundParserToolMap instance containing maps of the ParserTool internal names
	 * to the Guice Providers that create those ParserTools
	 *
	 * @param parserProviders the map of ParserTool internal names to ParserTool Providers
	 * @param xmlParserProviders the map of ParserTool internal names to XMLParserTool Providers
	 */
	@Inject
	public MapBoundParserToolMap(Map<String, Provider<ParserTool>> parserProviders) {
		this.parserProviders = parserProviders;
	}
	
	/* (non-Javadoc)
	 * @see io.coursescheduler.scheduler.parse.tools.ParserToolMap#getParserTool(java.lang.String)
	 */
	@Override
	public ParserTool getParserTool(String key) {
		try {
			return parserProviders.get(key).get();
		}catch(NullPointerException e) {
			return null;
		}
	}
	
}