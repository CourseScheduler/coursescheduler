package Scheduler;

import io.coursescheduler.scheduler.parse.SectionBasedXMLParser;
import io.coursescheduler.util.guice.Guicer;
import io.coursescheduler.util.preferences.PreferencesFactory;

import java.awt.Component;

import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.XTabComponent;
import javax.swing.UIManager.LookAndFeelInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Properties;


public class Main {
	
	/********************************************************
	 * UPDATE SERIAL VERSION IN VERSION WHEN THIS FILE CHANGES
	********************************************************/
	protected static final long versionID = 2013010921152L;//object id
	protected static final long buildNumber = 1510L;//build number
	protected static final String version = new String("4.12");
	
	protected static final String author = new String("Mike Reinhold");
	protected static final String maintain = new String("Mike Reinhold");
	protected static final String email = new String("kuscheduler@gmail.com");
	protected static final String contributers = new String(
		"Phil DeMonaco, Alex Thomson, Ryan Murphy, Vlad Patryshev, Rob MacGrogan");
	
	protected static final String folderName = new String(System.getProperty("user.home") + "/Scheduler");
	protected static final String dataName = new String("Data");
	protected static final String dataPath = new String(folderName + "/" + dataName);
	protected static final String dataFolder = new String(dataPath + "/");
	protected static final String databaseExt = new String(".sdb");
	protected static final String scheduleExt = new String(".ssf");
	protected static final String preferencesExt = new String(".spf");
	protected static final String smLogo = new String("Images/logo-small.png");
	protected static final String logo = new String("Images/logo.png");
	protected static final String xIconStr = new String("Images/xIcon.png");
	protected static final String xIconPStr = new String("Images/xIconP.png");
	protected static final String xIconRStr = new String("Images/xIconR.png");
	protected static final String rmp = new String("Profs.txt");
	protected static final String fixRMP = new String(dataFolder + rmp);
	protected static final String jarFixRMP = new String(dataName + "/" + rmp);
	protected static InputStream fixRMPFile;
	

	/**
	 * System property for specifying the java.util.pref.Preferences backend store implementation
	 * that should be used by the application
	 */
	protected static final String preferencesFactoryProperty = "java.util.prefs.PreferencesFactory";
	
	/**
	 * Default preferred java.util.pref.Preferences backend
	 */
	protected static final String defaultPreferencesBackend = "io.coursescheduler.util.preferences.properties.xml.XMLPropertiesFilePreferencesFactory";
	
	/**
	 * System property for specifying the filesystem path of the user preferences root node
	 */
	protected static final String userFilePathProperty = "io.coursescheduler.util.preferences.path.user";
	
	/**
	 * Default filesystem path for the user preferences node
	 */
	protected static final String defaultUserFilePath = "config/user";
	
	/**
	 * System property for specifying the filesystem path of the system preferences root node
	 */
	protected static final String systemFilePathProperty = "io.coursescheduler.util.preferences.path.system";
	
	/**
	 * Default filesystem path for the system preferences node
	 */
	protected static final String defaultSystemFilePath = "config/system";
	
	/**
	 * System property for specifying the user preferences root node path within the user root filesystem path
	 */
	protected static final String userRootPathProperty = "io.coursescheduler.util.preferences.root.user";
	
	/**
	 * Default user preferences root node within the user tree
	 */
	protected static final String defaultUserRootPath = "io.coursescheduler";
	
	/**
	 * System property for specifying the system preferences root node path within the system root filesystem path
	 */
	protected static final String systemRootPathProperty = "io.coursescheduler.util.preferences.root.system";
	
	/**
	 * Default system preferences root node within the system tree
	 */
	protected static final String defaultSystemRootPath = "io.coursescheduler"; 
	
	private static final int buffers = 1;
	
	protected static String defURL = new String("https://jweb.kettering.edu/cku1/bwckschd.p_get_crse_unsec");
	protected static String defGradCampURL = new String("https://jweb.kettering.edu/cku1/xhwschedule_grad07.P_ViewSchedule");
	protected static String defGradDistURL = new String("https://jweb.kettering.edu/cku1/xhwschedule_grad08.P_ViewSchedule");
	protected static String defSID = new String("366");
	
	protected static ClassLoader loader;
	
	protected static Preferences prefs;
	protected static MainFrame master;
	protected static ScheduledThreadPoolExecutor threadExec;
	protected static TreeMap<String, Database> terms;
	protected static boolean termChanged = false;
	protected static ImageIcon icon;// = new ImageIcon(smLogo);
	protected static ImageIcon xlIcon;// = new ImageIcon(logo);
	protected static ImageIcon xIcon;
	protected static ImageIcon xIconP;
	protected static ImageIcon xIconR;
		
	protected static int availProcs = 0;
	protected static String windowSystem;
	protected static String os;
	protected static String jvm;
	
	protected static SingleInstanceService sis;
	protected static SISListener sisL;
	
	protected static boolean nimbus = false;
	protected static boolean conflictDebugEnabled = false;
	
	/**
	 * Guice injector that will be used to access and build dependencies
	 */
	private static Injector injector;
	
	/**
	 * A basic logger for this class to monitor the startup routine
	 */
	private static Logger log = LoggerFactory.getLogger(Main.class.getName());
	
	public static void main(String[] args){
		XMLTest();
		
		initializeGuice();
		
		try {
			try {
				sis = (SingleInstanceService) ServiceManager
						.lookup("javax.jnlp.SingleInstanceService");
				sisL = new SISListener();
				sis.addSingleInstanceListener(sisL);
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						try {
							sis.removeSingleInstanceListener(sisL);
						} catch (Exception e) {
						}
					}
				});
			} catch (UnavailableServiceException e) {
				sis = null;
			}
		} catch (NoClassDefFoundError e) {
			// TODO: handle exception
		}
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            nimbus = true;
		            break;
		        }
		    }
		} 
		catch (UnsupportedLookAndFeelException e) {} 
		catch (ClassNotFoundException e) {} 
		catch (InstantiationException e) {} 
		catch (IllegalAccessException e) {}
		
		retrieveSystemSpecifications();
				
		threadExec = new ScheduledThreadPoolExecutor(32 * availProcs);
		threadExec.setKeepAliveTime(1500, TimeUnit.MILLISECONDS);
		threadExec.allowCoreThreadTimeOut(true);
		
		loader = Main.class.getClassLoader();
		
		initializePreferences();
		
		String current = prefs.getCurrentTerm();
		
		terms = new TreeMap<String, Database>();
		terms.put(current, Database.load(current));

		try {
			fixRMPFile = loader.getResourceAsStream(jarFixRMP);
						
			icon = new ImageIcon(loader.getResource(smLogo));
			xlIcon = new ImageIcon(loader.getResource(logo));
			xIcon = new ImageIcon(loader.getResource(xIconStr));
			xIconP = new ImageIcon(loader.getResource(xIconPStr));
			xIconR = new ImageIcon(loader.getResource(xIconRStr));
			
			if(icon == null){
				icon = new ImageIcon(smLogo);
			}
			if(xlIcon == null){
				xlIcon = new ImageIcon(logo);
			}
			if(xIcon == null){
				xIcon = new ImageIcon(xIconStr);
			}
			if(xIconP == null){
				xIconP = new ImageIcon(xIconPStr);
			}
			if(xIconR == null){
				xIconR = new ImageIcon(xIconRStr);
			}
		} 
		catch (Exception e){
			icon = new ImageIcon(smLogo);
			xlIcon = new ImageIcon(logo);
			xIcon = new ImageIcon(xIconStr);
			xIconP = new ImageIcon(xIconPStr);
			xIconR = new ImageIcon(xIconRStr);
		}
		
		XTabComponent.setIcons(xIcon, xIconP, xIconR);
		
		master = new MainFrame();
		
		Database database = terms.get(current);
		
		master.setVisible(true);
		master.createBufferStrategy(buffers);
		
		if (database == null){
			int hresult = JOptionPane.showConfirmDialog(Main.master,
					"You have not yet downloaded course information for " + 
					Term.getTermString(current) + ". Do you want to do so now?", "Download Course Information", 
					JOptionPane.YES_NO_OPTION);
			if (hresult == JOptionPane.YES_OPTION){
				updateDatabase(current);
			}
			else{
				master.mainMenu.newScheduleMenu.setEnabled(false);
			}
		}
		
		for(String item: args){
			if(item.endsWith(Main.scheduleExt)){
				ScheduleWrap found = ScheduleWrap.load(item);
				
				Main.master.mainMenu.addMadeSchedule(found, new File(item).getName());
			}
		}		
	}
	
	private static void XMLTest(){
		try{
			SectionBasedXMLParser test = new SectionBasedXMLParser(new FileInputStream("Data/ku_scheduler_2.xml"));
			test.parse();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialize the Guice injector with the 
	 *
	 */
	private static void initializeGuice(){
		log.info("Preparing to initialize Guice subsystem");
		Guicer guicer= new Guicer(configureDefaultModules());
						
		injector = guicer.initialize();
		log.info("Guice subsystem initialized");
	}
	
	/**
	 * Build the Map of application components to default module implementations
	 *
	 * @return the map of default implementations for application components
	 */
	private static Map<String, String> configureDefaultModules(){
		Map<String, String> modules = new HashMap<String, String>();
		
		//default component module definitions
		modules.put("preferences", "io.coursescheduler.util.preferences.properties.PropertiesPreferencesModule");
		
		return modules;
	}
	
	/**
	 * Initialize the configuration storage subsystem to allow for storage and retrieval
	 * of configuration settings
	 *
	 */
	private static void initializePreferences(){
		log.info("Preparing to initialize application Preferences");
		Main.prefs = Preferences.load();
		
		if (Main.prefs == null){
			new File(folderName).mkdir();
			new File(dataPath).mkdir();
			Main.prefs = new Preferences();
		}
		
		configurePreferencesProperties();
		injector.injectMembers(Main.prefs);
		prefs.migrate();
		log.info("Preferences initialization complete");
	}
	
	/**
	 * Check for command line specified configurations for the preferences storage.
	 * If command line configurations have not been specified, use the default values
	 *
	 */
	private static void configurePreferencesProperties(){		
		Properties systemProps = System.getProperties();	

		//check for a desired Preferences Factory class
		log.debug("Checking for Preferences backend specified in {}", preferencesFactoryProperty);
		if(systemProps.get(preferencesFactoryProperty) == null){
			systemProps.put(preferencesFactoryProperty, defaultPreferencesBackend);
			log.debug("No backend found in {}. Using default backend: {}", preferencesFactoryProperty, defaultPreferencesBackend);
		}
		
		//check for desired filesystem path for the user root
		log.debug("Checking for user root file path specified in {}", userFilePathProperty);
		if(systemProps.get(userFilePathProperty) == null){
			systemProps.put(userFilePathProperty, defaultUserFilePath);
			log.debug("No user root file path found in {}. Using default backend: {}", userFilePathProperty, defaultUserFilePath);
		}
		
		//check for desired filesystem path for the system root
		log.debug("Checking for system root file path specified in {}", systemFilePathProperty);
		if(systemProps.get(systemFilePathProperty) == null){
			systemProps.put(systemFilePathProperty, defaultSystemFilePath);
			log.debug("No system root file path found in {}. Using default backend: {}", systemFilePathProperty, defaultSystemFilePath);
		}
		
		//check for the desired root node path for the user root
		log.debug("Checking for user root node path specified in {}", userRootPathProperty);
		if(systemProps.get(userRootPathProperty) == null){
			systemProps.put(userRootPathProperty, defaultUserRootPath);
			log.debug("No user root node path found in {}. Using default backend: {}", userRootPathProperty, defaultUserRootPath);
		}

		//check for the desired root node path for the user root
		log.debug("Checking for system root node file path specified in {}", systemRootPathProperty);
		if(systemProps.get(systemRootPathProperty) == null){
			systemProps.put(systemRootPathProperty, defaultSystemRootPath);
			log.debug("No system root node path found in {}. Using default backend: {}", systemRootPathProperty, defaultSystemRootPath);
		}
	}
	
	/**
	 * Retrieve system specifications from the JVM Properties and Runtime
	 *
	 */
	private static void retrieveSystemSpecifications(){
		Properties systemProps = System.getProperties();
		
		windowSystem = systemProps.getProperty("sun.desktop");
		os = systemProps.getProperty("os.name");
		availProcs = Runtime.getRuntime().availableProcessors();
		jvm = System.getProperty("java.vendor") + " " + System.getProperty("java.version");
	}
	
	public static void printZeroRatedProfs(){
		ArrayList<Prof> profs = new ArrayList<Prof>();
		
		Database current = terms.get(prefs.getCurrentTerm());
		
		for(String key: current.getCourseList(CourseType.all)){
			Course course = current.getCourse(key);
			
			for(Section item: course.getSectionsLl()){
				Prof prof = item.getInstructor();
				
				if(!profs.contains(prof) && prof.getRating() == 0.0){
					profs.add(prof);
				}
			}
		}
		
		for(Prof prof: profs){
			System.out.println(prof.getName() + ": " + prof.getRating());
		}		
	}
	
	
	/********************************************************
	 * @purpose Update the current database
	*********************************************************/
	public static void updateDatabase(String term){
		ParseThread worker = new ParseThread();		//create new parse thread
		worker.setEngine(Parser.ku);				//set the parsing engine
		worker.setTerm(term);						//set term to download
		worker.setUrl(Main.prefs.getURL());			//set the url to download from
				
		Main.threadExec.execute(worker);							//run the thread
	}
	
	
	/********************************************************
	 * @purpose Update the term display
	*********************************************************/
	public static void displayTerm(){
		Main.master.mainMenu.currentTerm.setText(//set current term text
				"Current Term: " + Term.getTermString(
				Main.prefs.getCurrentTerm()) +	MainMenu.mainMenuPad);
	}
	
	
	/********************************************************
	 * @purpose Update the downloaded date display
	*********************************************************/
	public static void displayDate(){
		try{
			Main.master.mainMenu.downloadDate.setText("Downloaded: " + Main.terms.get(
					Main.prefs.getCurrentTerm()).getCreation().getTime().toString());
		}
		catch(NullPointerException ex){
			Main.master.mainMenu.downloadDate.setText("Not Yet Downloaded");
		}
		
	}
	
	
	/********************************************************
	 * @purpose Update all of the make schedule databases
	*********************************************************/
	public static void updateAllForRMP(){
		boolean anyUpdate = false;
		
		for(String item: terms.keySet()){
			if(Integer.parseInt(prefs.getCurrentTerm()) - Integer.parseInt(item) < 4){
				if(!terms.get(item).getProfs().hasRatings()){
					anyUpdate = true;
					updateDatabase(item);
				}
			}
		}
		
		reRateAll();
		displayDate();
		repairDates();
		
		if(!anyUpdate){
			Main.master.setEnabled(true);
		}
	}
	
	
	/********************************************************
	 * @purpose Update all of the make schedule databases
	*********************************************************/
	public static void updateAll(){
		for(String item: terms.keySet()){
			if(Integer.parseInt(prefs.getCurrentTerm()) - Integer.parseInt(item) < 4){
				updateDatabase(item);
			}
		}
		
		reRateAll();
		displayDate();
		repairDates();
	}
	
		
	public static void repairDates(){
		for(int pos = 0; pos < Main.master.tabControl.getTabCount(); pos++){//for each tab
			Component comp = Main.master.tabControl.getComponentAt(pos);//get the component there
			
			try{
				Tab item = (Tab)comp;
				
				item.setDate();
			}
			catch(ClassCastException ex){}
		}
		
		for(int pos = 0; pos < Main.master.detached.size(); pos++){//for each tab
			Popup comp = (Popup)Main.master.detached.get(pos);//get the component there
			
			try{
				Tab item = (Tab)comp.getComponent();
				
				item.setDate();
			}
			catch(ClassCastException ex){}
		}
	}
	
	
	/********************************************************
	 * @purpose Rerate all of the stored databases and update schedules
	*********************************************************/
	public static void reRateAll(){
		for(String item: terms.keySet()){
			terms.get(item).reRate();
		}
		
		for(int pos = 0; pos < Main.master.tabControl.getTabCount(); pos++){//for each tab
			Component comp = Main.master.tabControl.getComponentAt(pos);//get the component there
			
			try{
				Tab item = (Tab)comp;
				
				item.setDatabase(terms.get(item.getDatabase().getTerm()), true, true);
			}
			catch(ClassCastException ex){}
		}
		
		for(int pos = 0; pos < Main.master.detached.size(); pos++){//for each tab
			Popup comp = (Popup)Main.master.detached.get(pos);//get the component there
			
			try{
				Tab item = (Tab)comp.getComponent();
				
				item.setDatabase(terms.get(item.getDatabase().getTerm()), true, true);
			}
			catch(ClassCastException ex){}
		}
	}
}
