package org.opennms.vaadin.provision.model;

public class Vrf {
	private String m_name; 
	private String m_notifylevel;
	private String m_networklevel;
	private String m_dnsdomain;
	private String m_thresholdlevel;
	private String m_backupprofile;
	private String m_snmpprofile;
	
	public Vrf() {
		
	}

	public Vrf(String name, String notifylevel, String networklevel,
			String dnsdomain, String thresholdlevel, String backupprofile,
			String snmpprofile) {
		super();
		m_name = name;
		m_notifylevel = notifylevel;
		m_networklevel = networklevel;
		m_dnsdomain = dnsdomain;
		m_thresholdlevel = thresholdlevel;
		m_backupprofile = backupprofile;
		m_snmpprofile = snmpprofile;
	}
	public String getName() {
		return m_name;
	}
	public String getNotifylevel() {
		return m_notifylevel;
	}
	public String getNetworklevel() {
		return m_networklevel;
	}
	public String getDnsdomain() {
		return m_dnsdomain;
	}
	public String getThresholdlevel() {
		return m_thresholdlevel;
	}
	public String getBackupprofile() {
		return m_backupprofile;
	}
	public String getSnmpprofile() {
		return m_snmpprofile;
	}
	public void setName(String name) {
		m_name = name;
	}
	public void setNotifylevel(String notifylevel) {
		m_notifylevel = notifylevel;
	}
	public void setNetworklevel(String networklevel) {
		m_networklevel = networklevel;
	}
	public void setDnsdomain(String dnsdomain) {
		m_dnsdomain = dnsdomain;
	}
	public void setThresholdlevel(String thresholdlevel) {
		m_thresholdlevel = thresholdlevel;
	}
	public void setBackupprofile(String backupprofile) {
		m_backupprofile = backupprofile;
	}
	public void setSnmpprofile(String snmpprofile) {
		m_snmpprofile = snmpprofile;
	}
		
}
