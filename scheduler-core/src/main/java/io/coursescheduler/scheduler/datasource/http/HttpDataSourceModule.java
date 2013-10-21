/**
  * @(#)HttpDataSourceModule.java
  *
  * Guice module for binding the HttpDataSource and its Factory
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
package io.coursescheduler.scheduler.datasource.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.coursescheduler.scheduler.datasource.DataSource;
import io.coursescheduler.scheduler.datasource.DataSourceFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;

/**
 * Guice module for binding the HttpDataSource and its Factory
 *
 * @author Mike Reinhold
 *
 */
public class HttpDataSourceModule extends AbstractModule {
	
	/**
	 * Component based logger
	 */
	private Logger log = LoggerFactory.getLogger(getClass().getName());
	
	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {

		//install a module indicating that HttpDataSource can be built from a factory with assisted inject
		log.debug("Installing FactoryModuleBuilder for {} with implementations {}",
			HttpDataSourceFactory.class,
			HttpDataSource.class + " for " + DataSource.class
		);
		install(new FactoryModuleBuilder()
			.implement(DataSource.class, HttpDataSource.class)
			.build(HttpDataSourceFactory.class)
		);
		
		//add a mapped binding from the DataSource class to the implementation classes
		log.debug("Creating MapBinder entry for {} to {} at key {}", new Object[] {
				DataSourceFactory.class,
				HttpDataSourceFactory.class,
				HttpDataSourceFactory.DATA_SOURCE_INTERNAL_NAME
		});
		MapBinder<String, DataSourceFactory> dataSourceBinder = MapBinder.newMapBinder(binder(), String.class, DataSourceFactory.class );
		dataSourceBinder.addBinding(HttpDataSourceFactory.DATA_SOURCE_INTERNAL_NAME).toProvider(getProvider(HttpDataSourceFactory.class));
	}
	
}
