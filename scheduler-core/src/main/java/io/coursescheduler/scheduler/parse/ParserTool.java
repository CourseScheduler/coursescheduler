/**
  * @(#)ParserTool.java
  *
  * Interface for all parser types and modules to provide common ancestor and functionality
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

/**
 * Interface for all parser types and modules to provide common ancestor and functionality
 *
 * @author Mike Reinhold
 *
 */
public interface ParserTool {
	
	/**
	 * The internal name for this ParserTool module for using this ParserTool in configuration
	 *
	 * @return the internal name
	 */
	public String getInternalName();
	
	/**
	 * The external, user friendly name for this ParserTool module for seleccting this ParserTool
	 * in the user interface
	 *
	 * @return the user friendly name
	 */
	public String getUserFriendlyName();
	
	/**
	 * Provide a short description of the ParserTool module
	 *
	 * @return a short description
	 */
	public String getShortDescription();
	
	/**
	 * Provide a longer description of the ParserTool module and its intended use case
	 *
	 * @return a long description
	 */
	public String getLongDescription();
}