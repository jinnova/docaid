package com.jinnova.docaid;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Service {
	
	public String id;
	
	public String name;
	
	public int price;
	
	public boolean priceAdjustable = false;
	
	public static LinkedHashMap<String, Service> allServices;

	public static Service serviceDefault;
	
	public static void loadServices() throws SQLException {
		if (allServices != null) {
			return;
		}
		allServices = new LinkedHashMap<>();
		//HashMap<String, String> map = DBManager.getSettingByPrefix("services_");
		//HashMap<String, String> map = new HashMap<String, String>(SettingName.allSettings);
		HashMap<String, String> map = SettingName.allSettings;
		String serviceDefaultName = map.get(SettingName.services_default.name());
		String idPrefix = SettingName.services_id.name() + "_";
		for (String key : map.keySet()) {
			if (!key.startsWith(idPrefix) ||
					!key.endsWith("_name")) {
				continue;
			}
			String id = key.substring(idPrefix.length(), key.indexOf("_name"));
			if (id.isEmpty() || id.contains("_")) {
				continue;
			}
			Service serv = new Service();
			serv.id = id;
			allServices.put(id, serv);
			if (id.equals(serviceDefaultName)) {
				serviceDefault = serv;
			}
		}

		for (Service serv : allServices.values()) {
			serv.name = map.get(idPrefix + serv.id + "_name");
			String s = map.get(idPrefix + serv.id + "_price");
			try {
				if (s != null) {
					serv.price = Integer.parseInt(s);
				}
			} catch (NumberFormatException ne) {
				//continue;
			}
			s = map.get(idPrefix + serv.id + "_priceAdjustable");
			if (s != null) {
				serv.priceAdjustable = Boolean.parseBoolean(s);
			}
		}
	}
}
