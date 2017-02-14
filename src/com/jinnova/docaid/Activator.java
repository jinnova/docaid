package com.jinnova.docaid;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.mysql.jdbc.Driver;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public static Connection dbcon;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		DriverManager.registerDriver(new Driver());
		initDbCon();
		SettingName.loadAllSettings();
		Service.loadServices();
		WaitingList.startBackgroundLoading();
	}
	
	public static void initDbCon() throws SQLException {
		String[] args = Platform.getApplicationArgs();
		//dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/docaid", "root", "");
		String jdbcUrl = "jdbc:mysql://localhost:3306/docaid";
		String dbuser = "root";
		String dbpass = "";
		for (String s : args) {
			if (s.startsWith("-jdbcUrl=")) {
				jdbcUrl = s.substring("-jdbcUrl=".length());
			} else if (s.startsWith("-dbuser=")) {
				dbuser = s.substring("-dbuser=".length());
			} else if (s.startsWith("-dbpass=")) {
				dbpass = s.substring("-dbpass=".length());
			}
		}
		dbcon = DriverManager.getConnection(jdbcUrl, dbuser, dbpass);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
