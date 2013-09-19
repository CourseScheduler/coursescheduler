/**
  * @(#)ParserRoutine.java
  *
  * Interface for describing Parse routines and the methods tthat must be supported
  * by said parse routines.
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
package io.coursescheduler.scheduler.parse.routines;

import java.util.concurrent.RecursiveAction;

/**
 * Interface for describing Parser routines and the methods that must be supported
 * by said parse routines.
 *
 * @author Mike Reinhold
 *
 */
public abstract class ParserRoutine extends RecursiveAction {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * General configuration node for ParserRoutine classes
	 * 
	 * Value: {@value}
	 */
	public static final String GENERAL_SETTINGS_NODE = "general";

	/**
	 * Batch process size configuration property name
	 * 
	 * Value: {@value}
	 */
	public static final String BATCH_SIZE_PROPERTY = "batch.size";
	
}
