package com.paradopolis.realisticmovierecommender.managers;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The webservice manager runs a simple threadpool, and will execute the 
 * runnables from the other Web service Call classes.
 * @author Paradopolis
 *
 */
public class WebServiceManager {
	
	 private static final int  CORE_POOL_SIZE  =    1;
	 private static final int  MAX_POOL_SIZE   =   1;
	 private static final long TIME_TO_LIVE = 20000;
	 private static ThreadPoolExecutor pool;
	 private static boolean initialized;
	
	 /**
	 * We use an initialization flag to confirm that the configuration manager is configured.
	 * This may be necessary in concurrent programming, and can also be used to check that
	 * initialization was at least run.
	 */
	public static boolean isInitialized() {
		return initialized;
	}

	/**
	 * Internal function to set initialized to true. Once initialized, can not uninitialize.
	 */
	protected static void setInitialized() {
		initialized = true;
	}
 
	 
	public static boolean initialize(){
		pool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, TIME_TO_LIVE, TimeUnit.MILLISECONDS,  new LinkedBlockingQueue<Runnable>());
		setInitialized();
		return true;
	}
	
	public static void handleWebserviceRequest(Runnable r){
		if (!isInitialized()){
			initialize();
		}
		pool.execute(r);
	}

}
