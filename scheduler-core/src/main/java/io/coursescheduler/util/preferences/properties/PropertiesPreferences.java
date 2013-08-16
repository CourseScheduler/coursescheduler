/**
  * @(#)PropertiesFilePreferences.java
  *
  * Provide a properties file backed Preferences implementation
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
package io.coursescheduler.util.preferences.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Provide a properties file backed Preferences implementation
 *
 * @author Mike Reinhold
 *
 */
public abstract class PropertiesPreferences extends AbstractPreferences {

	/**
	 * The name of the backing store file for the user root Preference node
	 * 
	 * Value: {@value}
	 */
	private static final String ROOT_FILE_NAME_USER = "user-root";	
	
	/**
	 * The name of the backing store file for the system root Preference node
	 * 
	 * Value: {@value}
	 */
	private static final String ROOT_FILE_NAME_SYSTEM = "system-root";
		
	/**
	 * The system properties entry for specifying the user root filesystem path that will
	 * be used for the backing store files of the user preferences tree.
	 * 
	 * Value: {@value}
	 */
	public static final String PROPERTY_PATH_USER = "io.coursescheduler.util.preferences.path.user";
	
	/**
	 * The system properties entry for specifying the system root filesystem path that will
	 * be used for the backing store files of the system preferences tree.
	 * 
	 * Value: {@value}
	 */
	public static final String PROPERTY_PATH_SYSTEM = "io.coursescheduler.util.preferences.path.system";
	
	/**
	 * The default filesystem path for the user preferences tree if the 
	 * io.coursescheduler.util.preferences.path.user system property is not set.
	 * 
	 * Value: {@value}
	 */
	private static final String DEFAULT_PATH_USER = ".";
	
	/**
	 * The default filesystem path for the system preferences tree if the 
	 * io.coursescheduler.util.preferences.path.system system property is not set.
	 * 
	 * Value: {@value}
	 */
	private static final String DEFAULT_PATH_SYSTEM = ".";
	
	/**
	 * The Properties object that stores the Preferences elements for this node
	 */
	private Properties properties;
		
	/**
	 * The node name for this Preferences node
	 */
	private String nodeName;
	
	/**
	 * A boolean specifying that this is a root Preferences node. If true, this 
	 * node is a root node. If false this node is a child node.
	 */
	private boolean rootNode;
	
	/**
	 * A boolean specifying that this is a system Preferences node. If true,
	 * this node is part of the system Preferences tree. If false, this node
	 * is part of the user Preferences tree.
	 */
	private boolean systemNode;

	/**
	 * The Preferences node that is a parent to this node. The Preferences API does
	 * not appear to really allow for multiple active Preferences implementations,
	 * which is good because we need specific info from the parent node in order 
	 * to properly handle the file names and locations.
	 */
	private PropertiesPreferences propertiesFileParent;
	
	/**
	 * A map of the child node names to the child node objects.
	 */
	private Map<String, PropertiesPreferences> children;
	
	/**
	 * Create a new Preferences instance as a child of the specified instance using the
	 * specified node name.
	 *
	 * @param parent the parent Preferences node
	 * @param name the preferences node name
	 */
	PropertiesPreferences(AbstractPreferences parent, String name) {
		super(parent, name);
		
		properties = new Properties();
		children = new HashMap<>();
		
		propertiesFileParent = (PropertiesPreferences)parent;
		nodeName = name;
		rootNode = false;
		
		initializeProperties();
	}
	
	/**
	 * Create a new root Preferences instance using the specified node name. If isUserRoot
	 * is specified then a user root Preferences instance will be created, otherwise a 
	 * system root Preferences instance will be created.
	 *
	 * @param name the preferences node name
	 * @param isUserNode true if a user root instance, false if a system root
	 */
	PropertiesPreferences(String name, boolean isUserNode){
		super(null, name);
		
		properties = new Properties();
		children = new HashMap<>();
		
		propertiesFileParent = null;
		nodeName = name;
		rootNode = true;
		systemNode = !isUserNode;
		
		initializeProperties();
	}

	/**
	 * Initialize the in-memory properties by sync'ing against the backing file
	 * on disk. If the Preferences node is new (no backing file exists), flush
	 * the properties instance back to disk if the "create immediately" setting
	 * is set.	 *
	 */
	private void initializeProperties(){
		try{
			sync();
		} catch (BackingStoreException e) {
			// TODO CATCH STUB
			e.printStackTrace();
		}
		
		//TODO check for a "create immediately" setting, and then flush
		try {
			flush();
		} catch (BackingStoreException e) {
			// TODO CATCH STUB
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.util.prefs.AbstractPreferences#putSpi(java.lang.String, java.lang.String)
	 */
	@Override
	protected void putSpi(String key, String value) {
		properties.put(key, value);
		
		try {
			flush();
		} catch (BackingStoreException e) {
			// TODO CATCH STUB
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see java.util.prefs.AbstractPreferences#getSpi(java.lang.String)
	 */
	@Override
	protected String getSpi(String key) {
		return properties.getProperty(key);
	}

	/* (non-Javadoc)
	 * @see java.util.prefs.AbstractPreferences#removeSpi(java.lang.String)
	 */
	@Override
	protected void removeSpi(String key) {
		properties.remove(key);
		
		try {
			flush();
		} catch (BackingStoreException e) {
			// TODO CATCH STUB
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see java.util.prefs.AbstractPreferences#removeNodeSpi()
	 */
	@Override
	protected synchronized void removeNodeSpi() throws BackingStoreException {
		synchronized(lock){
			File file = new File(getFullFilePathAndExtension());
			
			file.delete();	//TODO is this ok?
		}

	}

	/* (non-Javadoc)
	 * @see java.util.prefs.AbstractPreferences#keysSpi()
	 */
	@Override
	protected String[] keysSpi() throws BackingStoreException {
		return properties.keySet().toArray(new String[properties.keySet().size()]);
	}

	/* (non-Javadoc)
	 * @see java.util.prefs.AbstractPreferences#childrenNamesSpi()
	 */
	@Override
	protected String[] childrenNamesSpi() throws BackingStoreException {
		return children.keySet().toArray(new String[children.keySet().size()]);
	}

	/* (non-Javadoc)
	 * @see java.util.prefs.AbstractPreferences#childSpi(java.lang.String)
	 */
	@Override
	protected synchronized AbstractPreferences childSpi(String name) {
		PropertiesPreferences node = children.get(name);
		
		if(node == null){
			node = createChildNode(this, name);
			children.put(name, node);
		}
		return node;
	}

	/* (non-Javadoc)
	 * @see java.util.prefs.AbstractPreferences#syncSpi()
	 */
	@Override
	protected void syncSpi() throws BackingStoreException {
		synchronized (lock) {
			File file = getAndCreateFilePaths();
			
			if(!newNode){
				try (FileInputStream fileInStream = new FileInputStream(file)){
					load(properties, fileInStream);
				} catch (IOException e) {
					// TODO CATCH STUB
					e.printStackTrace();
					
					throw new BackingStoreException(e);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.util.prefs.AbstractPreferences#flushSpi()
	 */
	@Override
	protected void flushSpi() throws BackingStoreException {
		synchronized(lock){
			File file = getAndCreateFilePaths();
			
			try(FileOutputStream fileOutStream = new FileOutputStream(file)){
				store(properties, fileOutStream);
			} catch (IOException e) {
				// TODO CATCH STUB
				e.printStackTrace();
				
				throw new BackingStoreException(e);
			}
		}
	}

	/**
	 * Retrieve the backing store File. Verify whether the path to the specified
	 * backing file exists and if not, create it. Check if the backing file currently
	 * exists.
	 *
	 * @return the backing File for the current node
	 */
	private File getAndCreateFilePaths() {
		File file = new File(getFullFilePathAndExtension());
		File path = file.getParentFile();
		
		if(path != null && !path.exists()){
			path.mkdirs();
		}
		
		newNode = !file.exists();
		
		return file;
	}
	
	/**
	 * Retrieve the filesystem path for the current node. This is where the backing 
	 * properties file is stored.
	 * 
	 * @return the filesystem path for the backing file of the current node
	 */
	protected String getFilePath() {
		String filePath;
		
		if(isRootNode()){
			filePath = isSystemNode() ? getRootSystemNodeFilesystemPath() : getRootUserNodeFilesystemPath();
		}else{
			filePath = propertiesFileParent.getFilePath();
		}
		
		return filePath;
	}
	

	/**
	 * Return the user root node filesystem path for the backing file. This is the 
	 * current working directory by default. It can also be specified by setting the
	 * system property "io.coursescheduler.util.preferences.path.user" to the desired
	 * path.
	 *
	 * @return the user root node backing filesystem path 
	 */
	private String getRootUserNodeFilesystemPath(){
		String path = System.getProperty(PROPERTY_PATH_USER);
		
		if(path == null){
			path = DEFAULT_PATH_USER;
		}
		
		return path;
	}
	
	/**
	 * Return the system root node filesystem path for the backing file. This is the 
	 * current working directory by default. It can also be specified by setting the
	 * system property "io.coursescheduler.util.preferences.path.system" to the desired
	 * path.
	 *
	 * @return the system root node backing filesystem path 
	 */
	private String getRootSystemNodeFilesystemPath(){
		String path = System.getProperty(PROPERTY_PATH_SYSTEM);
		
		if(path == null){
			path = DEFAULT_PATH_SYSTEM;
		}
		
		return path;
	}

	/**
	 * Retrieve the filepath, name, and extension of the file that provides the backing
	 * store for the Preferences node. This may be relative to the current working
	 * directory or absolute, depending on the filepath of the root node
	 *
	 * @return the filepath, name, and extension of the backing file
	 */
	protected String getFullFilePathAndExtension() {
		String filePath = getFilePath();
		String fullPathAndExtension;
		
		fullPathAndExtension = filePath + "/" + getFullNodeName() + "." + getFileExtension();
		
		return fullPathAndExtension;
	}
	
	/**
	 * Return the Preferences node's path relative to the root node
	 *
	 * @return the node's absolute path name
	 */
	protected String getFullNodeName(){
		String fullNodeName;
		
		if(propertiesFileParent == null){
			fullNodeName = isSystemNode() ? ROOT_FILE_NAME_SYSTEM : ROOT_FILE_NAME_USER;
		}else{
			String parentAbsolutePath = propertiesFileParent.absolutePath();
			
			fullNodeName = parentAbsolutePath + "/" + getNodeName();
		}
		
		return fullNodeName;
	}
	
	/**
	 * Return this Preferences node's specific node name
	 * 
	 * @return the nodeName
	 */
	protected String getNodeName() {
		return nodeName;
	}
	
	/**
	 * Check if this Preferences node is a root node (path is "" or "/")
	 * 
	 * @return true if this is a root node, false if this is a child node
	 */
	protected boolean isRootNode() {
		return rootNode;
	}

	/**
	 * Check if this Preferences node is a system node
	 * 
	 * @return true if this is a system node, false for a user node
	 */
	private boolean isSystemNode() {
		return systemNode;
	}
	
	/**
	 * Return the backing implementation specific file extension
	 *
	 * @return the file extension (without the ".") which will be used when writing the file 
	 * (eg. properties or xml)
	 */
	protected abstract String getFileExtension();

	/**
	 * Load the PropertiesPreferences from the specified File Input Stream into the Properties instance
	 *
	 * @param properties the target Properties instance for loading from the input file
	 * @param fis the input filestream from which to load the properties
	 * @throws IOException if there is some kind of IO error reading the input file
	 */
	protected abstract void load(Properties properties, FileInputStream fis) throws IOException;
	
	/**
	 * Store the PropertiesPreferences from the Properties instance to the specified File Output Stream
	 *
	 * @param properties the source Properties instance which will be flushed to the output file
	 * @param fos the output filestream to which the properties should be flushed
	 * @throws IOException  if there is some kind of error writing the output file
	 */
	protected abstract void store(Properties properties, FileOutputStream fos) throws IOException;
	
	/**
	 * Create a new PropertiesPreferences instance as a child to the specified abstractPrefernces
	 * instance using the specified node name.
	 *
	 * @param abstractPreferences the Preferences node which will be the new node's parent
	 * @param name the node name under which to register the Preferences node
	 * @return a new preferences node with the specified name as a child to the specified node
	 */
	protected abstract PropertiesPreferences createChildNode(AbstractPreferences abstractPreferences, String name);
}