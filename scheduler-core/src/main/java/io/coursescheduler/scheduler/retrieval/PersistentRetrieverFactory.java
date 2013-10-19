/**
  * @(#)PersistentRetrieverFactory.java
  *
  * Factory interface for building Persistent Retriever instances
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
package io.coursescheduler.scheduler.retrieval;

import io.coursescheduler.util.variable.SubstitutionVariableSource;

import java.util.prefs.Preferences;

import com.google.inject.assistedinject.Assisted;

/**
 * Factory interface for building Persistent Retriever instances
 *
 * @author Mike Reinhold
 *
 */
public interface PersistentRetrieverFactory extends RetrieverFactory {
	
	/* (non-Javadoc)
	 * @see io.coursescheduler.scheduler.retrieval.RetrieverFactory#getRetriever(java.util.prefs.Preferences, io.coursescheduler.util.variable.SubstitutionVariableSource)
	 */
	public Retriever getRetriever(@Assisted("config") Preferences config, @Assisted("localVars") SubstitutionVariableSource replacements);
}
