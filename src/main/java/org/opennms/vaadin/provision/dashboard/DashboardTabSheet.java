package org.opennms.vaadin.provision.dashboard;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

public class DashboardTabSheet extends CustomComponent implements
		SelectedTabChangeListener {

	protected final static String[] FOREIGN_SOURCE_LIST = new String[] {"TrentinoNetwork"};
	//,
	//	"SI","SIVirtualNodes"};

	/**
	 * 
	 */
	private static final long serialVersionUID = -4835992723502900986L;
	TabSheet m_tabsheet; ;
    LoginBox m_loginbox;
    
    DashboardTabSheet(DashBoardService service) {
    	m_tabsheet = new TabSheet();
		setCompositionRoot(m_tabsheet);
    	
		m_loginbox = new LoginBox(m_tabsheet,service);		
		m_tabsheet.addSelectedTabChangeListener(this);
        m_tabsheet.addTab(m_loginbox, "Login Box", new ThemeResource("icons/16/user.png"));
    	
        TrentinoNetworkTab tab=new TrentinoNetworkTab(service);
    	m_tabsheet.addTab(tab, "rete TN", new ThemeResource("icons/16/users.png"));
    	m_tabsheet.getTab(tab).setEnabled(false);

        VrfTab vrfTab=new VrfTab(service);
    	m_tabsheet.addTab(vrfTab, "Vrf", new ThemeResource("icons/16/users.png"));
    	m_tabsheet.getTab(vrfTab).setEnabled(false);
    	
    	SnmpProfileTab snmpTab = new SnmpProfileTab(service);
        m_tabsheet.addTab(snmpTab, "Profili Snmp",new ThemeResource("icons/16/users.png"));
    	m_tabsheet.getTab(snmpTab).setEnabled(false);
    	
    	BackupProfileTab backupTab = new BackupProfileTab(service);
        m_tabsheet.addTab(backupTab, "Profili Backup",new ThemeResource("icons/16/users.png"));
        m_tabsheet.getTab(backupTab).setEnabled(false);
        
        FastTab fastTab= new FastTab(service);
        m_tabsheet.addTab(fastTab, "Fast",new ThemeResource("icons/16/users.png"));
        m_tabsheet.getTab(fastTab).setEnabled(false);
    	
	}
	
	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		final TabSheet source = (TabSheet) event.getSource();
		if (source == m_tabsheet) {
			if (source.getSelectedTab() != m_loginbox) {
				((DashboardTab)source.getSelectedTab()).load();
			}
		}
	}

}
