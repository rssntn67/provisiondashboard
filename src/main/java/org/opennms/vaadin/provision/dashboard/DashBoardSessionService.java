package org.opennms.vaadin.provision.dashboard;

import static org.opennms.vaadin.provision.core.DashBoardUtils.valid;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.web.svclayer.model.SnmpInfo;
import org.opennms.vaadin.provision.config.DashBoardConfig;
import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.dao.BackupProfileDao;
import org.opennms.vaadin.provision.dao.DnsDomainDao;
import org.opennms.vaadin.provision.dao.FastServiceDeviceDao;
import org.opennms.vaadin.provision.dao.FastServiceLinkDao;
import org.opennms.vaadin.provision.dao.IpSnmpProfileDao;
import org.opennms.vaadin.provision.dao.JobDao;
import org.opennms.vaadin.provision.dao.JobLogDao;
import org.opennms.vaadin.provision.dao.KettleDao;
import org.opennms.vaadin.provision.dao.OnmsDao;
import org.opennms.vaadin.provision.dao.SnmpProfileDao;
import org.opennms.vaadin.provision.dao.CategoriaDao;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.FastServiceDevice;
import org.opennms.vaadin.provision.model.FastServiceLink;
import org.opennms.vaadin.provision.model.IpSnmpProfile;
import org.opennms.vaadin.provision.model.MediaGatewayNode;
import org.opennms.vaadin.provision.model.SistemiInformativiNode;
import org.opennms.vaadin.provision.model.TrentinoNetworkNode;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.SnmpProfile;
import org.opennms.vaadin.provision.model.Categoria;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.model.OnmsIpInterface;

import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import com.vaadin.server.VaadinSession;

public class DashBoardSessionService extends VaadinSession implements Serializable {

	public DashBoardSessionService(DashBoardService service) {
		super(service);
		m_onmsDao = new OnmsDao();
	}

	/**
	 * 
	 */
	private IpSnmpProfileDao     m_ipsnmpprofilecontainer;
	private SnmpProfileDao       m_snmpprofilecontainer;
	private BackupProfileDao     m_backupprofilecontainer;
	private CategoriaDao         m_catcontainer;
	private DnsDomainDao         m_dnsdomaincontainer;
	private FastServiceDeviceDao m_fastservicedevicecontainer;
	private FastServiceLinkDao   m_fastservicelinkcontainer;
	private JobDao               m_jobcontainer;
	private JobLogDao            m_joblogcontainer;
	private KettleDao            m_kettleDao;
	
	private static final long serialVersionUID = 508580392774265535L;
	private final static Logger logger = Logger.getLogger(DashBoardSessionService.class.getName());	
	
	final private OnmsDao m_onmsDao;
	private JDBCConnectionPool m_pool; 
	private DashBoardConfig m_config;

	private String m_user;
	private String m_url;
	
	public void setPool(JDBCConnectionPool pool) {
		m_pool=pool;
	}
	
	public void setConfig(DashBoardConfig config) {
		m_config = config;
	}
	
	public DashBoardConfig getConfig() {
		return m_config;
	}

	public IpSnmpProfileDao getIpSnmpProfileContainer() {
		return m_ipsnmpprofilecontainer;
	}

	public SnmpProfileDao getSnmpProfileContainer() {
		return m_snmpprofilecontainer;
	}

	public BackupProfileDao getBackupProfileContainer() {
		return m_backupprofilecontainer;
	}

	public CategoriaDao getCatContainer() {
    	return m_catcontainer;
    }

	public FastServiceDeviceDao getFastServiceDeviceContainer() {
		return m_fastservicedevicecontainer;
    }
    
	public FastServiceLinkDao getFastServiceLinkContainer() {
		return m_fastservicelinkcontainer;
    }

	public DnsDomainDao getDnsDomainContainer() {
		return m_dnsdomaincontainer;
	}

	public JobDao getJobContainer() {
		return m_jobcontainer;
	}

	public JobLogDao getJobLogContainer() {
		return m_joblogcontainer;
	}
	
	public KettleDao getKettleDao() {
		return m_kettleDao;
	}
	
    public OnmsDao getOnmsDao() {
		return m_onmsDao;
	}
    
    public JDBCConnectionPool getPool() {
    	return m_pool;
    }
    
	public void logout() {
		m_onmsDao.getJerseyClient().destroy();
		logger.info("logged out: user: " + m_user + " url: " + m_url);
	}
	
	public void login(String url, String username, String password) {
		logger.info("loggin user: " + username + "@" + url);
		m_onmsDao.setJerseyClient(
				new JerseyClientImpl(url,username,password));
		m_onmsDao.getSnmpInfo("127.0.0.1");
		logger.info("logged in user: " + username + "@" + url);
		m_user = username;
		m_url = url;
	}
		
	public void init() throws SQLException {
		TableQuery ipsnmptq = new TableQuery("ipsnmpprofile", m_pool);
		ipsnmptq.setVersionColumn("versionid");
        m_ipsnmpprofilecontainer = new IpSnmpProfileDao(ipsnmptq);

		TableQuery snmptq = new TableQuery("snmpprofiles", m_pool);
		snmptq.setVersionColumn("versionid");
        m_snmpprofilecontainer = new SnmpProfileDao(snmptq);
        
        TableQuery bcktq = new TableQuery("backupprofiles", m_pool);
		bcktq.setVersionColumn("versionid");
		m_backupprofilecontainer = new BackupProfileDao(bcktq);

		TableQuery cattq = new TableQuery("vrf", m_pool);
		cattq.setVersionColumn("versionid");
	    m_catcontainer =  new CategoriaDao(cattq);	
	    
	    TableQuery dnstq = new TableQuery("dnsdomains", m_pool);
	    dnstq.setVersionColumn("versionid");
	    m_dnsdomaincontainer =  new DnsDomainDao(dnstq);	
	    
	    m_fastservicedevicecontainer = new FastServiceDeviceDao
	    		(new FreeformQuery("select * from fastservicedevices", m_pool));

		m_fastservicelinkcontainer = new FastServiceLinkDao
				(new FreeformQuery("select * from fastservicelink", m_pool));

	    TableQuery jtq = new TableQuery("jobs", m_pool);
	    jtq.setVersionColumn("versionid");
		m_jobcontainer = new JobDao(jtq);
		
		TableQuery jltq = new TableQuery("joblogs", m_pool);
	    jltq.setVersionColumn("versionid");
		m_joblogcontainer = new JobLogDao(jltq);
		
		m_kettleDao = new KettleDao(new JerseyClientImpl(m_config.getKettleUrl(), 
				m_config.getKettleUsername(), 
				m_config.getKettlePassword()));
		
	}

	public void deleteSnmpProfile(String primary) throws SQLException{
		if (primary == null)
			return;
		m_ipsnmpprofilecontainer.remove(primary);
		m_ipsnmpprofilecontainer.commit();
	}

	public void saveSnmpProfile(String primary, String snmpprofile) throws SQLException{
		if (primary == null || snmpprofile == null)
			return;
		Map<String,IpSnmpProfile>ipSnmpMap = m_ipsnmpprofilecontainer.getIpSnmpProfileMap();

		IpSnmpProfile ipSnmpProfile= new IpSnmpProfile(primary,snmpprofile); 
		if (ipSnmpMap.containsKey(primary)) {
			if (ipSnmpMap.get(primary).getSnmprofile().equals(snmpprofile))
				return;
			logger.info("syncSnmpProfile: update primary ip/snmpprofile: " + primary+"/"+snmpprofile );
			m_ipsnmpprofilecontainer.update(ipSnmpProfile);
		}  else {
			logger.info("syncSnmpProfile: adding primary ip/snmpprofile: " + primary+"/"+snmpprofile );
			m_ipsnmpprofilecontainer.add(ipSnmpProfile);				
		}
		m_ipsnmpprofilecontainer.commit();
	}
	
	public void syncSnmpProfile(Set<String> primaries) throws SQLException {
		Map<String,IpSnmpProfile>ipSnmpMap = m_ipsnmpprofilecontainer.getIpSnmpProfileMap();

		for (String primary: primaries) {
			String snmpprofile = getSnmpProfileName(primary);
			logger.info("syncSnmpProfile: found primary ip/snmpprofile: " + primary+"/"+snmpprofile );
			if (snmpprofile == null) 
				continue;			
			IpSnmpProfile ipSnmpProfile= new IpSnmpProfile(primary,snmpprofile); 
			if (ipSnmpMap.containsKey(primary)) {
				if (ipSnmpMap.get(primary).getSnmprofile().equals(snmpprofile))
					continue;
				logger.info("syncSnmpProfile: update primary ip/snmpprofile: " + primary+"/"+snmpprofile );
				m_ipsnmpprofilecontainer.update(ipSnmpProfile);
			}  else {
				logger.info("syncSnmpProfile: adding primary ip/snmpprofile: " + primary+"/"+snmpprofile );
				m_ipsnmpprofilecontainer.add(ipSnmpProfile);				
			}
			ipSnmpMap.put(primary, ipSnmpProfile);
		}
		m_ipsnmpprofilecontainer.commit();
	}

	@SuppressWarnings("deprecation")
	public BeanContainer<String, SistemiInformativiNode> getSIContainer() {
		BeanContainer<String, SistemiInformativiNode> requisitionContainer = new BeanContainer<String, SistemiInformativiNode>(SistemiInformativiNode.class);
		requisitionContainer.setBeanIdProperty(DashBoardUtils.LABEL);
		List<String> domains = getDnsDomainContainer().getDomains();
		Map<String,IpSnmpProfile>ipSnmpMap = m_ipsnmpprofilecontainer.getIpSnmpProfileMap();

		for (RequisitionNode node : m_onmsDao.getRequisition(DashBoardUtils.SI_REQU_NAME).getNodes()) {
			String foreignId = node.getForeignId();
			String nodelabel = node.getNodeLabel();

			String vrf = null;
			String hostname = null;
			for (String tncat: domains) {
				if (nodelabel.endsWith("."+tncat)) {
					vrf = tncat;
					hostname = nodelabel.substring(0,nodelabel.indexOf(vrf)-1);
					break;
				}
			}

			if (vrf == null) {
				hostname = nodelabel;
			}
			String primary = null;
			String descr = null;
			Map<String, Set<String>> serviceMap = new HashMap<String, Set<String>>();
			for (RequisitionInterface ip: node.getInterfaces()) {
				logger.info("parsing foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel() + "ip address:" + ip.getIpAddr());
				if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
					primary = ip.getIpAddr();
					descr = ip.getDescr();
				} 
				serviceMap.put(ip.getIpAddr(),new HashSet<String>());
				for (RequisitionMonitoredService service: ip.getMonitoredServices()) {
					logger.info("adding secondary: " + ip.getIpAddr() 
							+ ":" + service.getServiceName()+ " foreignid: "
							+ node.getForeignId() + ", nodelabel: " + node.getNodeLabel());
					serviceMap.get(ip.getIpAddr()).add(service.getServiceName());
				}
			}

			String[] serverLevelCategory = null;
			for (String[] levels : DashBoardUtils.m_server_levels) {
				if (node.getCategory(levels[0]) != null && node.getCategory(levels[1]) != null) {
					serverLevelCategory = levels;
					break;
				}
			}

			String managedByCategory = null;
			for (String managedby: DashBoardUtils.m_server_managedby) {
				if (node.getCategory(managedby) != null) {
					managedByCategory = managedby;
					break;
				}				
			}
			String notifCategory = null;
			for (String notif: DashBoardUtils.m_server_notif) {
				if (node.getCategory(notif) != null) {
					notifCategory = notif;
					break;
				}				
			}

			Set<String> optionalCategory = new HashSet<String>();
			for (String option: DashBoardUtils.m_server_optional) {
				if (node.getCategory(option) != null) {
					optionalCategory.add(option);
				}				
			}

			String prodCategory = null;
			for (String prod: DashBoardUtils.m_server_prod) {
				if (node.getCategory(prod) != null) {
					prodCategory = prod;
					break;
				}				
			}

			String tnCategory = null;
			if (node.getCategory(DashBoardUtils.TN_REQU_NAME) == null) {
				logger.info("no TrentinoNetwork category on SI node: foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel());
			} else {
				tnCategory = DashBoardUtils.TN_REQU_NAME;
			}

			String city = node.getCity();
			String building = node.getBuilding();
			
			String address1 = null;
			if (node.getAsset(DashBoardUtils.ADDRESS1) != null)
				address1 = node.getAsset(DashBoardUtils.ADDRESS1).getValue();

			String description = null;
			if (node.getAsset(DashBoardUtils.DESCRIPTION) != null)
				description = node.getAsset(DashBoardUtils.DESCRIPTION).getValue();

			String leaseExpires = "";
			if (node.getAsset(DashBoardUtils.LEASEEXPIRES) != null)
				leaseExpires = node.getAsset(DashBoardUtils.LEASEEXPIRES).getValue();

			String lease = null;
			if (node.getAsset(DashBoardUtils.LEASE) != null)
				lease = node.getAsset(DashBoardUtils.LEASE).getValue();
			
			String vendorPhone = null;
			if (node.getAsset(DashBoardUtils.VENDORPHONE) != null)
				vendorPhone = node.getAsset(DashBoardUtils.VENDORPHONE).getValue();

			String vendor = null;
			if (node.getAsset(DashBoardUtils.VENDOR) != null)
				vendor = node.getAsset(DashBoardUtils.VENDOR).getValue();

			String slot = null;
			if (node.getAsset(DashBoardUtils.SLOT) != null)
				slot = node.getAsset(DashBoardUtils.SLOT).getValue();

			String rack = null;
			if (node.getAsset(DashBoardUtils.RACK) != null)
				rack = node.getAsset(DashBoardUtils.RACK).getValue();

			String room = null;
			if (node.getAsset(DashBoardUtils.ROOM) != null)
				room = node.getAsset(DashBoardUtils.ROOM).getValue();

			String operatingSystem = null;
			if (node.getAsset(DashBoardUtils.OPERATINGSYSTEM) != null)
				operatingSystem = node.getAsset(DashBoardUtils.OPERATINGSYSTEM).getValue();

			String dateInstalled = null;
			if (node.getAsset(DashBoardUtils.DATEINSTALLED) != null)
				dateInstalled = node.getAsset(DashBoardUtils.DATEINSTALLED).getValue();

			String assetNumber = null;
			if (node.getAsset(DashBoardUtils.ASSETNUMBER) != null)
				assetNumber = node.getAsset(DashBoardUtils.ASSETNUMBER).getValue();

			String serialNumber = null;
			if (node.getAsset(DashBoardUtils.SERIALNUMBER) != null)
				serialNumber = node.getAsset(DashBoardUtils.SERIALNUMBER).getValue();

			String category = null;
			if (node.getAsset(DashBoardUtils.CATEGORY) != null)
				category = node.getAsset(DashBoardUtils.CATEGORY).getValue();

			String modelNumber = null;
			if (node.getAsset(DashBoardUtils.MODELNUMBER) != null)
				modelNumber = node.getAsset(DashBoardUtils.MODELNUMBER).getValue();

			String manufacturer = null;
			if (node.getAsset(DashBoardUtils.MANUFACTURER) != null)
				manufacturer = node.getAsset(DashBoardUtils.MANUFACTURER).getValue();
			
			String snmpProfile = null;
			if (primary != null && ipSnmpMap.containsKey(primary))
				snmpProfile= ipSnmpMap.get(primary).getSnmprofile();

			
			SistemiInformativiNode sinode = new SistemiInformativiNode(serviceMap, descr, hostname, 
					null,null,snmpProfile,
					vrf, primary, 
					serverLevelCategory, managedByCategory, notifCategory, optionalCategory, 
					prodCategory, tnCategory, 
					city, address1, description, building, 
					leaseExpires, lease, vendorPhone, vendor, 
					slot, rack, room, 
					operatingSystem, dateInstalled, 
					assetNumber, serialNumber, category, 
					modelNumber, manufacturer, 
					foreignId,DashBoardUtils.SI_REQU_NAME);
			sinode.setValid(isValid(sinode));
			requisitionContainer.addBean(sinode);
		}

		return requisitionContainer;
	}

	@SuppressWarnings("deprecation")
	public BeanContainer<String, TrentinoNetworkNode> getTNContainer() {
		BeanContainer<String, TrentinoNetworkNode> requisitionContainer = new BeanContainer<String, TrentinoNetworkNode>(TrentinoNetworkNode.class);
		
		Map<String, BackupProfile> backupprofilemap = getBackupProfileContainer().getBackupProfileMap();
		requisitionContainer.setBeanIdProperty(DashBoardUtils.LABEL);
		
		Map<String,String> foreignIdNodeLabelMap = new HashMap<String, String>();
		Map<String,String> nodeLabelForeignIdMap = new HashMap<String, String>();
		Map<String,Set<String>> primaryipforeignidmap = new HashMap<String,Set<String>>();

		Collection<Categoria> cats = getCatContainer().getCatMap().values();
		List<String> domains = getDnsDomainContainer().getDomains();
		Map<String,IpSnmpProfile>ipSnmpMap = m_ipsnmpprofilecontainer.getIpSnmpProfileMap();

		Requisition req = m_onmsDao.getRequisition(DashBoardUtils.TN_REQU_NAME);

		for (RequisitionNode node : req.getNodes()) {
			foreignIdNodeLabelMap.put(node.getForeignId(),node.getNodeLabel());
			nodeLabelForeignIdMap.put(node.getNodeLabel(),node.getForeignId());
			for (RequisitionInterface ip: node.getInterfaces()) {
				if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
					if (!primaryipforeignidmap.containsKey(ip.getIpAddr()))
						primaryipforeignidmap.put(ip.getIpAddr(),new HashSet<String>());
					primaryipforeignidmap.get(ip.getIpAddr()).add(node.getForeignId());
				}
			}
		}
		
		for (RequisitionNode node : req.getNodes()) {
			if (node.getCategory(DashBoardUtils.MEDIAGATEWAY_CATEGORY) != null) {
				logger.info("skipping media gateway: foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel());
				continue;
			}
			String foreignId = node.getForeignId();
			String nodelabel = node.getNodeLabel();

			String vrf = null;
			String hostname = null;
			for (String tncat: domains) {
				if (nodelabel.endsWith("."+tncat)) {
					vrf = tncat;
					hostname = nodelabel.substring(0,nodelabel.indexOf(vrf)-1);
					break;
				}
			}

			if (vrf == null) 
				hostname = nodelabel;
			
			String primary = null;
			String descr = null;
			Map<String,Set<String>> serviceMap = new HashMap<String,Set<String>>();
			for (RequisitionInterface ip: node.getInterfaces()) {
				logger.info("parsing foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel() + "ip address:" + ip.getIpAddr());
				if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
					primary = ip.getIpAddr();
					descr = ip.getDescr();
				} 
				serviceMap.put(ip.getIpAddr(),new HashSet<String>());
				for (RequisitionMonitoredService service: ip.getMonitoredServices()) {
					logger.info("adding secondary: " + ip.getIpAddr() 
							+ ":" + service.getServiceName()+ " foreignid: "
							+ node.getForeignId() + ", nodelabel: " + node.getNodeLabel());
					serviceMap.get(ip.getIpAddr()).add(service.getServiceName());
				}
			}
				
			Categoria networkCategory = null;
			for (Categoria lcat: cats) {
				if (node.getCategory(lcat.getName()) != null && node.getCategory(lcat.getNetworklevel()) != null) {
					networkCategory = lcat;
					break;
				}
			}

			String notifCategory=null;
			for (String fcat: DashBoardUtils.m_notify_levels) {
				if (node.getCategory(fcat) != null ) {
					notifCategory = fcat;
					break;
				}
			}
			
			String threshCategory = null;
			for (String tcat: DashBoardUtils.m_threshold_levels) {
				if (node.getCategory(tcat) != null ) {
					threshCategory = tcat;
					break;
				}
			}

			String slaCategory = null;
			for (String slacat: DashBoardUtils.m_sla_levels) {
				if (node.getCategory(slacat) != null ) {
					slaCategory = slacat;
					break;
				}
			}

			Set<String> optionalCategory = new HashSet<String>();
			for (String option: DashBoardUtils.m_server_optional) {
				if (node.getCategory(option) != null) {
					optionalCategory.add(option);
				}				
			}


			String city = null;
			if (node.getCity() != null)
				city = node.getCity();

			String address1 = null;
			if (node.getAsset(DashBoardUtils.ADDRESS1) != null)
				address1 = node.getAsset(DashBoardUtils.ADDRESS1).getValue();
			
			String parent = null;
			String parentId = null;
			if (node.getParentForeignId() != null) {
				parentId=node.getParentForeignId();
				parent =foreignIdNodeLabelMap.get(node.getParentForeignId());
			}
			
			String building = null;
			if (node.getAsset(DashBoardUtils.BUILDING) != null)
				building = node.getAsset(DashBoardUtils.BUILDING).getValue();
			
			String circuitId = null;
			if (node.getAsset(DashBoardUtils.CIRCUITID) != null)
				circuitId = node.getAsset(DashBoardUtils.CIRCUITID).getValue();
			else if (node.getBuilding() != null) 
				circuitId = node.getBuilding();

			String snmpProfile = null;
			if (primary != null && ipSnmpMap.containsKey(primary))
				snmpProfile= ipSnmpMap.get(primary).getSnmprofile();
			
			TrentinoNetworkNode tnnode = new TrentinoNetworkNode(
					serviceMap,
					descr, 
					hostname, 
					vrf, 
					primary, 
					parent, 
					parentId,
					networkCategory, 
					notifCategory, 
					threshCategory, 
					snmpProfile, 
					DashBoardUtils.getBackupProfile(node, backupprofilemap), 
					city, 
					address1, 
					foreignId, 
					slaCategory,
					building,
					circuitId,
					optionalCategory,
					DashBoardUtils.TN_REQU_NAME);
			tnnode.setValid(isValid(tnnode));
			requisitionContainer.addBean(tnnode);
		}
		return requisitionContainer;
	}

	@SuppressWarnings("deprecation")
	public BeanContainer<String, MediaGatewayNode> getMediaGatewayContainer() {
		BeanContainer<String, MediaGatewayNode> requisitionContainer = new BeanContainer<String, MediaGatewayNode>(MediaGatewayNode.class);
		requisitionContainer.setBeanIdProperty(DashBoardUtils.LABEL);
		
		Map<String,String> foreignIdNodeLabelMap = new HashMap<String, String>();
		Map<String,String> nodeLabelForeignIdMap = new HashMap<String, String>();
		Map<String,Set<String>> primaryipforeignidmap = new HashMap<String,Set<String>>();

		Map<String, BackupProfile> backupprofilemap = getBackupProfileContainer().getBackupProfileMap();
		List<String> domains = getDnsDomainContainer().getDomains();
		Map<String,IpSnmpProfile>ipSnmpMap = m_ipsnmpprofilecontainer.getIpSnmpProfileMap();
		
		Requisition req = m_onmsDao.getRequisition(DashBoardUtils.TN_REQU_NAME);
		
		for (RequisitionNode node : req.getNodes()) {
			foreignIdNodeLabelMap.put(node.getForeignId(),node.getNodeLabel());
			nodeLabelForeignIdMap.put(node.getNodeLabel(),node.getForeignId());
			for (RequisitionInterface ip: node.getInterfaces()) {
				if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
					if (!primaryipforeignidmap.containsKey(ip.getIpAddr()))
						primaryipforeignidmap.put(ip.getIpAddr(),new HashSet<String>());
					primaryipforeignidmap.get(ip.getIpAddr()).add(node.getForeignId());
				}
			}
		}
		
		for (RequisitionNode node : req.getNodes()) {
			if (node.getCategory(DashBoardUtils.MEDIAGATEWAY_CATEGORY) == null) {
				continue;
			}
			String foreignId = node.getForeignId();
			String nodelabel = node.getNodeLabel();

			String vrf = null;
			String hostname = null;
			for (String tncat: domains) {
				if (nodelabel.endsWith("."+tncat)) {
					vrf = tncat;
					hostname = nodelabel.substring(0,nodelabel.indexOf(vrf)-1);
					break;
				}
			}

			if (vrf == null) 
				hostname = nodelabel;
			
			String primary = null;
			String descr = null;
			Map<String,Set<String>> serviceMap = new HashMap<String, Set<String>>();
			for (RequisitionInterface ip: node.getInterfaces()) {
				logger.info("parsing foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel() + "ip address:" + ip.getIpAddr());
				if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
					primary = ip.getIpAddr();
					descr = ip.getDescr();
				} 
				serviceMap.put(ip.getIpAddr(),new HashSet<String>());
				for (RequisitionMonitoredService service: ip.getMonitoredServices()) {
					logger.info("adding secondary: " + ip.getIpAddr() 
							+ ":" + service.getServiceName()+ " foreignid: "
							+ node.getForeignId() + ", nodelabel: " + node.getNodeLabel());
					serviceMap.get(ip.getIpAddr()).add(service.getServiceName());
				}

			}

			String networkCategory = null;
			if (node.getCategory(DashBoardUtils.MEDIAGATEWAY_NETWORK_CATEGORY) != null) {
				networkCategory = DashBoardUtils.MEDIAGATEWAY_NETWORK_CATEGORY;
			}

			String city = null;
			if (node.getCity() != null)
				city = node.getCity();

			String address1 = null;
			if (node.getAsset(DashBoardUtils.ADDRESS1) != null)
				address1 = node.getAsset(DashBoardUtils.ADDRESS1).getValue();
			
			String building = null;
			if (node.getAsset(DashBoardUtils.BUILDING) != null)
				building = node.getAsset(DashBoardUtils.BUILDING).getValue();

			String parent = null;
			String parentId = null;
			if (node.getParentForeignId() != null) {
				parentId = node.getParentForeignId();
				parent =foreignIdNodeLabelMap.get(node.getParentForeignId());
			}
			
			String snmpProfile = null;
			if (primary != null && ipSnmpMap.containsKey(primary))
				snmpProfile= ipSnmpMap.get(primary).getSnmprofile();

			MediaGatewayNode tnnode = new MediaGatewayNode(
					serviceMap,
					descr, 
					hostname, 
					vrf, 
					primary, 
					parent, 
					parentId,
					networkCategory, 
					snmpProfile, 
					DashBoardUtils.getBackupProfile(node, backupprofilemap), 
					city, 
					address1,
					building,
					foreignId,
					DashBoardUtils.TN_REQU_NAME);
			tnnode.setValid(isValid(tnnode));
			requisitionContainer.addBean(tnnode);
		}
		return requisitionContainer;	}

	public void createRequisition(String foreignSource) {
		logger.info("creating requisition: " + foreignSource );
		m_onmsDao.createRequisition(foreignSource);
	}

	public String getSnmpProfileName(String ip) throws SQLException {
		SnmpInfo info = m_onmsDao.getSnmpInfo(ip);
		for (Entry<String, SnmpProfile> snmpprofileentry : getSnmpProfileContainer().getSnmpProfileMap().entrySet()) {
			if (info.getReadCommunity().equals(snmpprofileentry.getValue().getCommunity()) &&
				info.getVersion().equals(snmpprofileentry.getValue().getVersion()) &&	
				info.getTimeout().intValue() == Integer.parseInt(snmpprofileentry.getValue().getTimeout()) &&
				info.getMaxVarsPerPdu().intValue() == Integer.parseInt(snmpprofileentry.getValue().getMaxvarsperpdu()) &&
				info.getRetries().intValue() == Integer.parseInt(snmpprofileentry.getValue().getRetries()) 
				)
				return snmpprofileentry.getKey();
		}
		return null;
	}

	public void delete(String foreignSource, RequisitionNode node) {
		if (node == null)
			return;
		for (RequisitionInterface riface: node.getInterfaces())
			m_onmsDao.deletePolicy(foreignSource, DashBoardUtils.getPolicyName(riface.getIpAddr()));
		m_onmsDao.deleteRequisitionNode(foreignSource, node.getForeignId());
	}
	
	public void delete(String foreignSource,String foreignId, String ipaddr) {
		logger.info("Deleting policy for interface: " + ipaddr);
		m_onmsDao.deletePolicy(foreignSource, DashBoardUtils.getPolicyName(ipaddr));
		logger.info("Deleting interface" + ipaddr+" with foreignId: " + foreignId );
		m_onmsDao.deleteRequisitionInterface(foreignSource, foreignId, ipaddr);
	}
	
	public void delete(SistemiInformativiNode sinode) {
		logger.info("Deleting SI node with foreignId: " + sinode.getForeignId() + " primary: " + sinode.getPrimary());
		m_onmsDao.deleteRequisitionNode(DashBoardUtils.SI_REQU_NAME, sinode.getForeignId());
	}
	
	public void delete(TrentinoNetworkNode tnnode) {
		logger.info("Deleting TN node with foreignId: " + tnnode.getForeignId() + " primary: " + tnnode.getPrimary());
		if (tnnode.getPrimary() != null)
			m_onmsDao.deletePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyName(tnnode.getPrimary()));
		if (tnnode.getServiceMap() != null) {
			for (String iface: tnnode.getServiceMap().keySet())
				m_onmsDao.deletePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyName(iface));
		}
		m_onmsDao.deleteRequisitionNode(DashBoardUtils.TN_REQU_NAME, tnnode.getForeignId());
	}

	public void delete(MediaGatewayNode tnnode) {
		logger.info("Deleting media gateway node with foreignId: " + tnnode.getForeignId() + " primary: " + tnnode.getPrimary());
		if (tnnode.getPrimary() != null)
			m_onmsDao.deletePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyName(tnnode.getPrimary()));
		m_onmsDao.deleteRequisitionNode(DashBoardUtils.TN_REQU_NAME, tnnode.getForeignId());		

		RequisitionNode mediagateway = getMediaGateway();
		if (mediagateway == null)
			return;
		m_onmsDao.deleteRequisitionInterface(DashBoardUtils.SIVN_REQU_NAME, mediagateway.getForeignId(), tnnode.getPrimary());
	}
	
	public List<String> getIpAddresses(String foreignSource,String nodelabel) {
	   	List<String> ipaddresses = new ArrayList<String>();
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
    	queryParams.add("label", nodelabel);
    	queryParams.add("foreignSource", foreignSource);

		OnmsNodeList nodes = m_onmsDao.findNodes(queryParams);
		if (nodes.isEmpty() || nodes.size() >1)
			return ipaddresses; 
        for (OnmsIpInterface ipinterface: m_onmsDao.getNodeIpInterfaces(nodes.getObjects().iterator().next().getId()))
        	ipaddresses.add(ipinterface.getIpAddress().getHostAddress());        
        return ipaddresses;
	}


	public RequisitionNode getMediaGateway() {
		for (RequisitionNode reqnode: m_onmsDao.getRequisition(DashBoardUtils.SIVN_REQU_NAME).getNodes()) {
			if (reqnode.getNodeLabel().equals("mediagateway")) {
				return reqnode;
			}
		}
		return null;
	}
	
	public String createMediaGateway() {
		RequisitionNode requisitionNode = new RequisitionNode();
		requisitionNode.setForeignId("mediagateway");
		requisitionNode.setNodeLabel("mediagateway");
		requisitionNode.putCategory(new RequisitionCategory(DashBoardUtils.MEDIAGATEWAY_CATEGORY));
		m_onmsDao.addRequisitionNode(DashBoardUtils.SIVN_REQU_NAME, requisitionNode);
		return requisitionNode.getNodeLabel();
	}

	public void add(MediaGatewayNode node) throws SQLException {
		RequisitionNode requisitionNode = new RequisitionNode();
		
		requisitionNode.setForeignId(node.getForeignId());
		
		requisitionNode.setNodeLabel(node.getNodeLabel());
		
		if (node.getParent() != null) {
			requisitionNode.setParentForeignId(node.getParentId());
		}
		
		requisitionNode.setCity(node.getCity());
		
		RequisitionInterface iface = new RequisitionInterface();
		iface.setSnmpPrimary(PrimaryType.PRIMARY);
		iface.setIpAddr(node.getPrimary());
		iface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
		iface.setDescr(node.getDescr());
		
		requisitionNode.putInterface(iface);

		requisitionNode.putCategory(new RequisitionCategory(DashBoardUtils.MEDIAGATEWAY_CATEGORY));
		if (node.getNetworkCategory() != null) 
			requisitionNode.putCategory(new RequisitionCategory(node.getNetworkCategory()));

				
		if (node.getCity() != null && node.getAddress1() != null)
			requisitionNode.putAsset(new RequisitionAsset("description", node.getCity() + " - " + node.getAddress1()));
		
		if (node.getAddress1()  != null)
			requisitionNode.putAsset(new RequisitionAsset("address1", node.getAddress1() ));
		
		for ( RequisitionAsset asset : getBackupProfileContainer().getBackupProfile(node.getBackupProfile()).getRequisitionAssets().getAssets()) {
			requisitionNode.putAsset(asset);
		}
		m_onmsDao.setSnmpInfo(node.getPrimary(), getSnmpProfileContainer().getSnmpProfile(node.getSnmpProfile()).getSnmpInfo());
		logger.info("Adding node with foreignId: " + node.getForeignId() + " primary: " + node.getPrimary());
		m_onmsDao.addRequisitionNode(DashBoardUtils.TN_REQU_NAME, requisitionNode);
		logger.info("Adding policy for interface: " + node.getPrimary());
		m_onmsDao.addOrReplacePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyWrapper(node.getPrimary()));
		
		node.clear();
		
		RequisitionNode mediagateway = getMediaGateway();
		if (mediagateway == null )
			return;
		RequisitionInterface mgiface = new RequisitionInterface();
		mgiface.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
		mgiface.setIpAddr(node.getPrimary());
		mgiface.putMonitoredService(new RequisitionMonitoredService("PattonSIPCalls"));
		mgiface.setDescr(node.getDescr());
		
		m_onmsDao.addRequisitionInterface(DashBoardUtils.SIVN_REQU_NAME, mediagateway.getForeignId(),mgiface);
			
	}

	public void add(SistemiInformativiNode node) {
		RequisitionNode requisitionNode = new RequisitionNode();
		
		requisitionNode.setForeignId(node.getForeignId());
		
		requisitionNode.setNodeLabel(node.getNodeLabel());
				
		requisitionNode.setCity(node.getCity());
		requisitionNode.setBuilding(node.getBuilding());
		
		RequisitionInterface iface = new RequisitionInterface();
		iface.setSnmpPrimary(PrimaryType.PRIMARY);
		iface.setIpAddr(node.getPrimary());
		iface.setDescr(node.getDescr());
		iface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
		requisitionNode.putInterface(iface);

		for (String ip : node.getServiceMap().keySet()) {
			if (ip.equals(node.getPrimary()))
				continue;
			RequisitionInterface face = new RequisitionInterface();
			face.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
			face.setIpAddr(ip);
			face.setDescr(node.getDescr());
			for (String service: node.getServiceMap().get(ip)) {
				face.putMonitoredService(new RequisitionMonitoredService(service));
			}
			requisitionNode.putInterface(face);
		}
		
		if (node.getServerLevelCategory() != null) {
			if (node.getServerLevelCategory()[0] != null)
				requisitionNode.putCategory(new RequisitionCategory(node.getServerLevelCategory()[0]));
			if (node.getServerLevelCategory()[1] != null)
				requisitionNode.putCategory(new RequisitionCategory(node.getServerLevelCategory()[1]));
		}
		
		requisitionNode.putCategory(new RequisitionCategory(DashBoardUtils.TN_REQU_NAME));
		
		if (node.getNotifCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getNotifCategory()));
		
		if (node.getProdCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getProdCategory()));

		if (node.getManagedByCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getManagedByCategory()));

		if (node.getOptionalCategory() != null) {
			for (String optionCat: node.getOptionalCategory())
				requisitionNode.putCategory(new RequisitionCategory(optionCat));
		}		
		if (node.getAddress1()  != null)
			requisitionNode.putAsset(new RequisitionAsset("address1", node.getAddress1() ));

		if (node.getLeaseExpires() != null)
			requisitionNode.putAsset(new RequisitionAsset("leaseExpires", node.getLeaseExpires() ));

		if (node.getLease() != null)
			requisitionNode.putAsset(new RequisitionAsset("lease", node.getLease() ));

		if (node.getVendorPhone() != null)
			requisitionNode.putAsset(new RequisitionAsset("vendorPhone", node.getVendorPhone() ));

		if (node.getVendor() != null)
			requisitionNode.putAsset(new RequisitionAsset("vendor", node.getVendor() ));

		if (node.getSlot() != null)
			requisitionNode.putAsset(new RequisitionAsset("slot", node.getSlot() ));

		if (node.getRack() != null)
			requisitionNode.putAsset(new RequisitionAsset("rack", node.getRack() ));

		if (node.getRoom() != null)
			requisitionNode.putAsset(new RequisitionAsset("room", node.getRoom() ));

		if (node.getOperatingSystem() != null)
			requisitionNode.putAsset(new RequisitionAsset("operatingSystem", node.getOperatingSystem() ));

		if (node.getDateInstalled() != null)
			requisitionNode.putAsset(new RequisitionAsset("dateInstalled", node.getDateInstalled() ));

		if (node.getAssetNumber() != null)
			requisitionNode.putAsset(new RequisitionAsset("assetNumber", node.getAssetNumber() ));

		if (node.getSerialNumber() != null)
			requisitionNode.putAsset(new RequisitionAsset("serialNumber", node.getSerialNumber() ));

		if (node.getCategory() != null)
			requisitionNode.putAsset(new RequisitionAsset("category", node.getCategory() ));

		if (node.getModelNumber() != null)
			requisitionNode.putAsset(new RequisitionAsset("modelNumber", node.getModelNumber() ));

		if (node.getManufacturer() != null)
			requisitionNode.putAsset(new RequisitionAsset("manufacturer", node.getManufacturer() ));

		if (node.getDescription() != null)
			requisitionNode.putAsset(new RequisitionAsset("description", node.getDescription() ));

		m_onmsDao.setSnmpInfo(node.getPrimary(), getSnmpProfileContainer().getSnmpProfile(node.getSnmpProfile()).getSnmpInfo());
		logger.info("Adding node with foreignId: " + node.getForeignId() + " primary: " + node.getPrimary());
		m_onmsDao.addRequisitionNode(DashBoardUtils.SI_REQU_NAME, requisitionNode);
		
		node.clear();
	}
	
	public void update(SistemiInformativiNode node) {
		Map<String, String> update = new HashMap<String, String>();
		
		if ((node.getUpdatemap().contains(DashBoardUtils.SNMP_PROFILE) ||
				node.getUpdatemap().contains(DashBoardUtils.PRIMARY))
				&& node.getPrimary() != null) {
			update.put(DashBoardUtils.SNMP_PROFILE, node.getSnmpProfile());
			update.put(DashBoardUtils.PRIMARY, node.getPrimary());
		}
		if (node.getUpdatemap().contains(DashBoardUtils.HOST)
				|| node.getUpdatemap().contains(DashBoardUtils.CAT))
			update.put(DashBoardUtils.LABEL, node.getNodeLabel());
		if (node.getUpdatemap().contains(DashBoardUtils.CITY))
			update.put(DashBoardUtils.CITY, node.getCity());
		if (node.getUpdatemap().contains(DashBoardUtils.ADDRESS1))
			update.put(DashBoardUtils.ADDRESS1, node.getAddress1());
		if (node.getUpdatemap().contains(DashBoardUtils.BUILDING))
			update.put(DashBoardUtils.BUILDING_SCALAR, node.getBuilding());
		if (node.getUpdatemap().contains(DashBoardUtils.LEASEEXPIRES))
			update.put(DashBoardUtils.LEASEEXPIRES, node.getLeaseExpires());
		if (node.getUpdatemap().contains(DashBoardUtils.LEASE))
			update.put(DashBoardUtils.LEASE, node.getLease());
		if (node.getUpdatemap().contains(DashBoardUtils.VENDORPHONE))
			update.put(DashBoardUtils.VENDORPHONE, node.getVendorPhone());
		if (node.getUpdatemap().contains(DashBoardUtils.VENDOR))
			update.put(DashBoardUtils.VENDOR, node.getVendor());
		if (node.getUpdatemap().contains(DashBoardUtils.SLOT))
			update.put(DashBoardUtils.SLOT, node.getSlot());
		if (node.getUpdatemap().contains(DashBoardUtils.RACK))
			update.put(DashBoardUtils.RACK, node.getRack());
		if (node.getUpdatemap().contains(DashBoardUtils.ROOM))
			update.put(DashBoardUtils.ROOM, node.getRoom());
		if (node.getUpdatemap().contains(DashBoardUtils.OPERATINGSYSTEM))
			update.put(DashBoardUtils.OPERATINGSYSTEM, node.getOperatingSystem());
		if (node.getUpdatemap().contains(DashBoardUtils.DATEINSTALLED))
			update.put(DashBoardUtils.DATEINSTALLED, node.getDateInstalled());
		if (node.getUpdatemap().contains(DashBoardUtils.ASSETNUMBER))
			update.put(DashBoardUtils.ASSETNUMBER, node.getAssetNumber());
		if (node.getUpdatemap().contains(DashBoardUtils.SERIALNUMBER))
			update.put(DashBoardUtils.SERIALNUMBER, node.getSerialNumber());
		if (node.getUpdatemap().contains(DashBoardUtils.CATEGORY))
			update.put(DashBoardUtils.CATEGORY, node.getCategory());
		if (node.getUpdatemap().contains(DashBoardUtils.MODELNUMBER))
			update.put(DashBoardUtils.MODELNUMBER, node.getModelNumber());
		if (node.getUpdatemap().contains(DashBoardUtils.MANUFACTURER))
			update.put(DashBoardUtils.MANUFACTURER, node.getManufacturer());
		if (node.getUpdatemap().contains(DashBoardUtils.DESCRIPTION))
			update.put(DashBoardUtils.DESCRIPTION, node.getDescription());
		
		updateNode(node.getServiceMap(),DashBoardUtils.SI_REQU_NAME, node.getForeignId(), node.getPrimary(),
				node.getDescr(), update, node.getInterfToDel(),
				node.getInterfToAdd(), node.getCategoriesToDel(),
				node.getCategoriesToAdd(),node.getServiceToDel(),node.getServiceToAdd());
		node.clear();
	}

	public void update(MediaGatewayNode node) {
		Map<String, String> update = new HashMap<String, String>();

		if ((node.getUpdatemap().contains(DashBoardUtils.SNMP_PROFILE) ||
				node.getUpdatemap().contains(DashBoardUtils.PRIMARY))
				&& node.getPrimary() != null) {
			update.put(DashBoardUtils.SNMP_PROFILE, node.getSnmpProfile());
			update.put(DashBoardUtils.PRIMARY, node.getPrimary());
		}
		if (node.getUpdatemap().contains(DashBoardUtils.PARENT))
			update.put(DashBoardUtils.PARENT,node.getParentId());
		if (node.getUpdatemap().contains(DashBoardUtils.HOST)
				|| node.getUpdatemap().contains(DashBoardUtils.CAT))
			update.put(DashBoardUtils.LABEL, node.getNodeLabel());
		if (node.getUpdatemap().contains(DashBoardUtils.CITY))
			update.put(DashBoardUtils.CITY, node.getCity());
		if (node.getUpdatemap().contains(DashBoardUtils.ADDRESS1))
			update.put(DashBoardUtils.ADDRESS1, node.getAddress1());
		if (node.getUpdatemap().contains(DashBoardUtils.CITY)
				|| node.getUpdatemap().contains(DashBoardUtils.ADDRESS1))
			update.put(DashBoardUtils.DESCRIPTION, node.getCity() + " - "
					+ node.getAddress1());
		BackupProfile bck = null;
		if (node.getUpdatemap().contains(DashBoardUtils.BACKUP_PROFILE)
				&& node.getBackupProfile() != null)
			bck = getBackupProfileContainer().getBackupProfile(node.getBackupProfile());
		updateNode(node.getServiceMap(),DashBoardUtils.TN_REQU_NAME, node.getForeignId(), node.getPrimary(),
				node.getDescr(), update, node.getInterfToDel(),
				node.getInterfToAdd(), node.getCategoriesToDel(),
				node.getCategoriesToAdd(),node.getServiceToDel(),
				node.getServiceToAdd(),bck);
		node.clear();
		
		RequisitionNode mediagateway = getMediaGateway();
		if (mediagateway == null) {
			return;
		}
		for (String ipaddr : node.getInterfToDel()) 
			m_onmsDao.deleteRequisitionInterface(DashBoardUtils.SI_REQU_NAME, mediagateway.getForeignId(), ipaddr);
		for (String ipaddr : node.getInterfToAdd()) {
			RequisitionInterface mgiface = new RequisitionInterface();
			mgiface.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
			mgiface.setIpAddr(ipaddr);
			mgiface.putMonitoredService(new RequisitionMonitoredService("PattonSIPCalls"));
			mgiface.setDescr(node.getDescr());
			m_onmsDao.addRequisitionInterface(DashBoardUtils.SIVN_REQU_NAME, mediagateway.getForeignId(),mgiface);
		}
	}

	public void add(TrentinoNetworkNode node) throws SQLException {
		RequisitionNode requisitionNode = new RequisitionNode();
		
		requisitionNode.setForeignId(node.getForeignId());
		
		requisitionNode.setNodeLabel(node.getNodeLabel());
		
		if (node.getParent() != null) {
			requisitionNode.setParentForeignId(node.getParentId());
		}
		
		requisitionNode.setCity(node.getCity());
		
		RequisitionInterface iface = new RequisitionInterface();
		iface.setSnmpPrimary(PrimaryType.PRIMARY);
		iface.setIpAddr(node.getPrimary());
		iface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
		iface.setDescr(node.getDescr());
		
		requisitionNode.putInterface(iface);

		if (node.getNetworkCategory() != null) {
			requisitionNode.putCategory(new RequisitionCategory(node.getNetworkCategory().getName()));
			requisitionNode.putCategory(new RequisitionCategory(node.getNetworkCategory().getNetworklevel()));
		}
		
		if (node.getNotifCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getNotifCategory()));
		
		if (node.getThreshCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getThreshCategory()));

		if (node.getSlaCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getSlaCategory()));
		if (node.getOptionalCategory() != null) {
			for (String optionCat: node.getOptionalCategory())
				requisitionNode.putCategory(new RequisitionCategory(optionCat));
		}		

		if (node.getCity() != null && node.getAddress1() != null)
			requisitionNode.putAsset(new RequisitionAsset("description", node.getCity() + " - " + node.getAddress1()));
		
		if (node.getAddress1()  != null)
			requisitionNode.putAsset(new RequisitionAsset("address1", node.getAddress1() ));
		
		if (node.getCircuitId() != null) {
			requisitionNode.putAsset(new RequisitionAsset("circuitId", node.getCircuitId() ));
			requisitionNode.setBuilding(node.getCircuitId());
		}
		
		if (node.getBuilding() != null)
			requisitionNode.putAsset(new RequisitionAsset("building", node.getBuilding() ));
		for ( RequisitionAsset asset : getBackupProfileContainer().getBackupProfile(node.getBackupProfile()).getRequisitionAssets().getAssets()) {
			requisitionNode.putAsset(asset);
		}
		m_onmsDao.setSnmpInfo(node.getPrimary(), getSnmpProfileContainer().getSnmpProfile(node.getSnmpProfile()).getSnmpInfo());
		logger.info("Adding node with foreignId: " + node.getForeignId() + " primary: " + node.getPrimary());
		m_onmsDao.addRequisitionNode(DashBoardUtils.TN_REQU_NAME, requisitionNode);
		logger.info("Adding policy for interface: " + node.getPrimary());
		m_onmsDao.addOrReplacePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyWrapper(node.getPrimary()));		
		node.clear();			
	}

	public void update(TrentinoNetworkNode node) {
		Map<String, String> update = new HashMap<String, String>();
		
		if ((node.getUpdatemap().contains(DashBoardUtils.SNMP_PROFILE) ||
				node.getUpdatemap().contains(DashBoardUtils.PRIMARY))
				&& node.getPrimary() != null) {
			update.put(DashBoardUtils.SNMP_PROFILE, node.getSnmpProfile());
			update.put(DashBoardUtils.PRIMARY, node.getPrimary());
		}
		if (node.getUpdatemap().contains(DashBoardUtils.PARENT)) {
			logger.info("Updating parent: " + node.getParent());
			update.put(DashBoardUtils.PARENT,node.getParentId());
		}
		if (node.getUpdatemap().contains(DashBoardUtils.HOST)
				|| node.getUpdatemap().contains(DashBoardUtils.CAT))
			update.put(DashBoardUtils.LABEL, node.getNodeLabel());
		if (node.getUpdatemap().contains(DashBoardUtils.CITY))
			update.put(DashBoardUtils.CITY, node.getCity());
		if (node.getUpdatemap().contains(DashBoardUtils.BUILDING))
			update.put(DashBoardUtils.BUILDING, node.getBuilding());
		if (node.getUpdatemap().contains(DashBoardUtils.CIRCUITID)) {
			update.put(DashBoardUtils.CIRCUITID, node.getCircuitId());
			update.put(DashBoardUtils.BUILDING_SCALAR, node.getCircuitId());
		}
		if (node.getUpdatemap().contains(DashBoardUtils.ADDRESS1))
			update.put(DashBoardUtils.ADDRESS1, node.getAddress1());
		if (node.getUpdatemap().contains(DashBoardUtils.CITY)
				|| node.getUpdatemap().contains(DashBoardUtils.ADDRESS1))
			update.put(DashBoardUtils.DESCRIPTION, node.getCity() + " - "
					+ node.getAddress1());
		BackupProfile bck = null;
		if (node.getUpdatemap().contains(DashBoardUtils.BACKUP_PROFILE)
				&& node.getBackupProfile() != null) {
			bck = getBackupProfileContainer().getBackupProfile(node.getBackupProfile());
		}		
		updateNode(node.getServiceMap(),DashBoardUtils.TN_REQU_NAME, node.getForeignId(), node.getPrimary(),
				node.getDescr(), update, node.getInterfToDel(),
				node.getInterfToAdd(), node.getCategoriesToDel(),
				node.getCategoriesToAdd(),node.getServiceToDel(),
				node.getServiceToAdd(),bck);
		node.clear();
	}

	public boolean updateNonFastNode(RequisitionNode rnode,Set<String> ipaddresses) {
		List<String> iptoAdd = new ArrayList<String>();
		List<String> ipToDel = new ArrayList<String>();
		Map<String,Set<String>> servicetoAdd = new HashMap<String, Set<String>>();
		for (String ip : ipaddresses) {
			if (rnode.getInterface(ip) == null) {
				iptoAdd.add(ip);
				servicetoAdd.put(ip, new HashSet<String>());
				servicetoAdd.get(ip).add("ICMP");
			}
		}
		for (RequisitionInterface riface: rnode.getInterfaces()) {
			if (riface.getDescr().contains("FAST") && !ipaddresses.contains(riface.getIpAddr()))
				ipToDel.add(riface.getIpAddr());
		}
		
		if (iptoAdd.isEmpty() && ipToDel.isEmpty())
			return false;
		updateNode(null,DashBoardUtils.TN_REQU_NAME,rnode.getForeignId(),null,DashBoardUtils.DESCR_FAST,
				new HashMap<String, String>(),ipToDel,iptoAdd,new ArrayList<String>(),new ArrayList<String>(),new HashMap<String, Set<String>>(),servicetoAdd,null);
		return true;
	}
	
	public boolean updateFastNode(String nodelabel, FastServiceLink reflink,
			RequisitionNode rnode, FastServiceDevice refdevice, Categoria cat,
			BackupProfile bck, Set<String> ipaddresses,
			boolean updateSnmpProfile) {
		Map<String,String> update = new HashMap<String, String>();
		
		if (updateSnmpProfile)
			update.put(DashBoardUtils.SNMP_PROFILE, refdevice.getSnmpprofile());
		
		if (!rnode.getNodeLabel().equals(nodelabel))
			update.put(DashBoardUtils.LABEL, nodelabel);

		if (refdevice.getCity() != null && !refdevice.getCity().equals(rnode.getCity()))
			update.put(DashBoardUtils.CITY, refdevice.getCity());
		else if (refdevice.getCity() == null && rnode.getCity() != null)
			update.put(DashBoardUtils.CITY, "");

		StringBuffer address1 = new StringBuffer();
		if (refdevice.getAddressDescr() != null)
			address1.append(refdevice.getAddressDescr());
		if (refdevice.getAddressName() != null) {
			if (address1.length() > 0)
				address1.append(" ");
			address1.append(refdevice.getAddressName());
		}
		if (refdevice.getAddressNumber() != null) {
			if (address1.length() > 0)
				address1.append(" ");
			address1.append(refdevice.getAddressNumber());
		}

		if (address1.length() > 0 && rnode.getAsset("address1") == null)
			update.put(DashBoardUtils.ADDRESS1, address1.toString());
		else if (address1.length() > 0 && !address1.toString().equals(rnode.getAsset("address1").getValue()))
			update.put(DashBoardUtils.ADDRESS1, address1.toString());
		else if (address1.length() == 0 && rnode.getAsset("address1") != null && rnode.getAsset("address1").getValue() != null && !"".equals(rnode.getAsset("address1").getValue()))
			update.put(DashBoardUtils.ADDRESS1, "");
		
		if (update.containsKey(DashBoardUtils.CITY) || update.containsKey(DashBoardUtils.ADDRESS1))
			update.put(DashBoardUtils.DESCRIPTION, refdevice.getCity() + " - " + address1.toString());
		
		if (reflink.getDeliveryCode() != null && rnode.getAsset("circuitId") == null) {
			update.put(DashBoardUtils.CIRCUITID, reflink.getDeliveryCode());
			update.put(DashBoardUtils.BUILDING_SCALAR, reflink.getDeliveryCode());
		} else if (reflink.getDeliveryCode() != null &&  !reflink.getDeliveryCode().equals(rnode.getAsset("circuitId").getValue())) {
			update.put(DashBoardUtils.CIRCUITID, reflink.getDeliveryCode());
			update.put(DashBoardUtils.BUILDING_SCALAR, reflink.getDeliveryCode());
		} else if (reflink.getDeliveryCode() == null && rnode.getAsset("circuitId") != null && rnode.getAsset("circuitId").getValue() != null && !"".equals(rnode.getAsset("circuitId").getValue()) ) {
			update.put(DashBoardUtils.CIRCUITID, "");
			update.put(DashBoardUtils.BUILDING_SCALAR, "");
		}					
		if (refdevice.getIstat() != null && reflink.getSiteCode() !=  null && rnode.getAsset("building") == null)
			update.put(DashBoardUtils.BUILDING, refdevice.getIstat()+"-"+reflink.getSiteCode());
		else if (refdevice.getIstat() != null && reflink.getSiteCode() !=  null 
				&& !rnode.getAsset("building").getValue().equals(refdevice.getIstat()+"-"+reflink.getSiteCode()))
			update.put(DashBoardUtils.BUILDING, refdevice.getIstat()+"-"+reflink.getSiteCode());
		else if ((refdevice.getIstat() == null || reflink.getSiteCode() ==  null) && rnode.getAsset("building") != null)
			update.put(DashBoardUtils.BUILDING, "");
		
		List<String> categorytoAdd = new ArrayList<String>();
		List<String> categoryToDel = new ArrayList<String>();
		List<String> all = new ArrayList<String>();
		
		if (rnode.getCategory(cat.getNetworklevel()) == null)
			categorytoAdd.add(cat.getNetworklevel());
		if (rnode.getCategory(cat.getName()) == null)
			categorytoAdd.add(cat.getName());
		String notiyCategory = cat.getNotifylevel();
		if (refdevice.getNotifyCategory() != null && !refdevice.getNotifyCategory().equals(DashBoardUtils.m_fast_default_notify))
			notiyCategory = refdevice.getNotifyCategory();
		if (rnode.getCategory(notiyCategory) == null)
			categorytoAdd.add(notiyCategory);
		if (rnode.getCategory(cat.getThresholdlevel()) == null)
			categorytoAdd.add(cat.getThresholdlevel());
		
		all.add(cat.getNetworklevel());
		all.add(cat.getName());
		all.add(notiyCategory);
		all.add(cat.getThresholdlevel());
		
		for (RequisitionCategory category: rnode.getCategories()) {
			if (!all.contains(category.getName()))
				categoryToDel.add(category.getName());
		}
		
		List<String> iptoAdd = new ArrayList<String>();
		List<String> ipToDel = new ArrayList<String>();
		Map<String,Set<String>> servicetoAdd = new HashMap<String, Set<String>>();
		for (String ip : ipaddresses) {
			if (rnode.getInterface(ip) == null) {
				iptoAdd.add(ip);
				servicetoAdd.put(ip, new HashSet<String>());
				servicetoAdd.get(ip).add("ICMP");
			}
		}
		for (RequisitionInterface riface: rnode.getInterfaces()) {
			if (!ipaddresses.contains(riface.getIpAddr()))
				ipToDel.add(riface.getIpAddr());
		}
		if (update.isEmpty() && categorytoAdd.isEmpty() && categoryToDel.isEmpty() && iptoAdd.isEmpty() && ipToDel.isEmpty())
			return false;
		updateNode(null,DashBoardUtils.TN_REQU_NAME,rnode.getForeignId(),
				refdevice.getIpaddr(),
				DashBoardUtils.DESCR_FAST,
				update,ipToDel,iptoAdd,categoryToDel,categorytoAdd,
				new HashMap<String, Set<String>>(),servicetoAdd,
				bck);
		return true;
	}

	private void updateNode(Map<String,Set<String>> serviceMap,String foreignSource, String foreignId,
			String primary, String descr, Map<String, String> update,
			List<String> interfaceToDel, List<String> interfaceToAdd,
			List<String> categoriesToDel, List<String> categoriesToAdd, 
			Map<String,Set<String>> serviceToDel, Map<String,Set<String>> serviceToAdd,			
			BackupProfile bck) {

		MultivaluedMap<String, String> updatemap = new MultivaluedMapImpl();
		List<RequisitionAsset> assetsToPut = new ArrayList<RequisitionAsset>();

		if (update.containsKey(DashBoardUtils.PARENT)) {
			logger.info("UpdateMap: parent-node-id: " + update.get(DashBoardUtils.PARENT));
			updatemap.add("parent-foreign-id", update.get(DashBoardUtils.PARENT));
		}
		if (update.containsKey(DashBoardUtils.BUILDING_SCALAR))
			updatemap.add("building", update.get(DashBoardUtils.BUILDING_SCALAR));
		if (update.containsKey(DashBoardUtils.LABEL))
			updatemap.add("node-label", update.get(DashBoardUtils.LABEL));
		if (update.containsKey(DashBoardUtils.CITY))
			updatemap.add("city", update.get(DashBoardUtils.CITY));
		if (update.containsKey(DashBoardUtils.ADDRESS1))
			assetsToPut.add(new RequisitionAsset("address1", update
					.get(DashBoardUtils.ADDRESS1)));
		if (update.containsKey(DashBoardUtils.DESCRIPTION))
			assetsToPut.add(new RequisitionAsset("description", update
					.get(DashBoardUtils.DESCRIPTION)));
		if (update.containsKey(DashBoardUtils.BUILDING))
			assetsToPut.add(new RequisitionAsset("building", update
					.get(DashBoardUtils.BUILDING)));
		if (update.containsKey(DashBoardUtils.CIRCUITID))
			assetsToPut.add(new RequisitionAsset("circuitId", update
					.get(DashBoardUtils.CIRCUITID)));
		if (update.containsKey(DashBoardUtils.LEASEEXPIRES))
			assetsToPut.add(new RequisitionAsset("leaseExpires", update
					.get(DashBoardUtils.LEASEEXPIRES)));
		if (update.containsKey(DashBoardUtils.LEASE))
			assetsToPut.add(new RequisitionAsset("lease", update
					.get(DashBoardUtils.LEASE)));
		if (update.containsKey(DashBoardUtils.VENDORPHONE))
			assetsToPut.add(new RequisitionAsset("vendorPhone", update
					.get(DashBoardUtils.VENDORPHONE)));
		if (update.containsKey(DashBoardUtils.VENDOR))
			assetsToPut.add(new RequisitionAsset("vendor", update
					.get(DashBoardUtils.VENDOR)));
		if (update.containsKey(DashBoardUtils.SLOT))
			assetsToPut.add(new RequisitionAsset("slot", update
					.get(DashBoardUtils.SLOT)));
		if (update.containsKey(DashBoardUtils.RACK))
			assetsToPut.add(new RequisitionAsset("rack", update
					.get(DashBoardUtils.RACK)));
		if (update.containsKey(DashBoardUtils.ROOM))
			assetsToPut.add(new RequisitionAsset("room", update
					.get(DashBoardUtils.ROOM)));
		if (update.containsKey(DashBoardUtils.OPERATINGSYSTEM))
			assetsToPut.add(new RequisitionAsset("operatingSystem", update
					.get(DashBoardUtils.OPERATINGSYSTEM)));
		if (update.containsKey(DashBoardUtils.DATEINSTALLED))
			assetsToPut.add(new RequisitionAsset("dateInstalled", update
					.get(DashBoardUtils.DATEINSTALLED)));
		if (update.containsKey(DashBoardUtils.ASSETNUMBER))
			assetsToPut.add(new RequisitionAsset("assetNumber", update
					.get(DashBoardUtils.ASSETNUMBER)));
		if (update.containsKey(DashBoardUtils.SERIALNUMBER))
			assetsToPut.add(new RequisitionAsset("serialNumber", update
					.get(DashBoardUtils.SERIALNUMBER)));
		if (update.containsKey(DashBoardUtils.CATEGORY))
			assetsToPut.add(new RequisitionAsset("category", update
					.get(DashBoardUtils.CATEGORY)));
		if (update.containsKey(DashBoardUtils.MODELNUMBER))
			assetsToPut.add(new RequisitionAsset("modelNumber", update
					.get(DashBoardUtils.MODELNUMBER)));
		if (update.containsKey(DashBoardUtils.MANUFACTURER))
			assetsToPut.add(new RequisitionAsset("manufacturer", update
					.get(DashBoardUtils.MANUFACTURER)));
		if (bck != null) {
			assetsToPut.addAll(bck.getRequisitionAssets().getAssets());
		}
		Set<String> ipaddress = new HashSet<String>();
		if (update.containsKey(DashBoardUtils.PRIMARY)) {
			ipaddress = serviceMap.keySet();
		}

		updateonms(foreignSource, 
				foreignId, primary, true, descr, 
				updatemap, interfaceToDel, interfaceToAdd, 
				categoriesToDel, categoriesToAdd, serviceToDel, 
				serviceToAdd, assetsToPut, ipaddress, 
				update.get(DashBoardUtils.SNMP_PROFILE));

	}

	private void updateNode(Map<String,Set<String>> serviceMap,String foreignSource, String foreignId,
			String primary, String descr, Map<String, String> update,
			List<String> interfaceToDel,List<String> interfaceToAdd,
			List<String> categoriesToDel, List<String> categoriesToAdd, Map<String,Set<String>> serviceToDel, Map<String,Set<String>> serviceToAdd) {
		
		MultivaluedMap<String, String> updatemap = new MultivaluedMapImpl();
		List<RequisitionAsset> assetsToPut = new ArrayList<RequisitionAsset>();

		if (update.containsKey(DashBoardUtils.PARENT)) {
			logger.info("UpdateMap: parent-node-id: " + update.get(DashBoardUtils.PARENT));
			updatemap.add("parent-foreign-id", update.get(DashBoardUtils.PARENT));
		}
		if (update.containsKey(DashBoardUtils.BUILDING_SCALAR))
			updatemap.add("building", update.get(DashBoardUtils.BUILDING_SCALAR));
		if (update.containsKey(DashBoardUtils.LABEL))
			updatemap.add("node-label", update.get(DashBoardUtils.LABEL));
		if (update.containsKey(DashBoardUtils.CITY))
			updatemap.add("city", update.get(DashBoardUtils.CITY));

		if (update.containsKey(DashBoardUtils.ADDRESS1))
			assetsToPut.add(new RequisitionAsset("address1", update
					.get(DashBoardUtils.ADDRESS1)));
		if (update.containsKey(DashBoardUtils.DESCRIPTION))
			assetsToPut.add(new RequisitionAsset("description", update
					.get(DashBoardUtils.DESCRIPTION)));
		if (update.containsKey(DashBoardUtils.BUILDING))
			assetsToPut.add(new RequisitionAsset("building", update
					.get(DashBoardUtils.BUILDING)));
		if (update.containsKey(DashBoardUtils.CIRCUITID))
			assetsToPut.add(new RequisitionAsset("circuitId", update
					.get(DashBoardUtils.CIRCUITID)));
		if (update.containsKey(DashBoardUtils.LEASEEXPIRES))
			assetsToPut.add(new RequisitionAsset("leaseExpires", update
					.get(DashBoardUtils.LEASEEXPIRES)));
		if (update.containsKey(DashBoardUtils.LEASE))
			assetsToPut.add(new RequisitionAsset("lease", update
					.get(DashBoardUtils.LEASE)));
		if (update.containsKey(DashBoardUtils.VENDORPHONE))
			assetsToPut.add(new RequisitionAsset("vendorPhone", update
					.get(DashBoardUtils.VENDORPHONE)));
		if (update.containsKey(DashBoardUtils.VENDOR))
			assetsToPut.add(new RequisitionAsset("vendor", update
					.get(DashBoardUtils.VENDOR)));
		if (update.containsKey(DashBoardUtils.SLOT))
			assetsToPut.add(new RequisitionAsset("slot", update
					.get(DashBoardUtils.SLOT)));
		if (update.containsKey(DashBoardUtils.RACK))
			assetsToPut.add(new RequisitionAsset("rack", update
					.get(DashBoardUtils.RACK)));
		if (update.containsKey(DashBoardUtils.ROOM))
			assetsToPut.add(new RequisitionAsset("room", update
					.get(DashBoardUtils.ROOM)));
		if (update.containsKey(DashBoardUtils.OPERATINGSYSTEM))
			assetsToPut.add(new RequisitionAsset("operatingSystem", update
					.get(DashBoardUtils.OPERATINGSYSTEM)));
		if (update.containsKey(DashBoardUtils.DATEINSTALLED))
			assetsToPut.add(new RequisitionAsset("dateInstalled", update
					.get(DashBoardUtils.DATEINSTALLED)));
		if (update.containsKey(DashBoardUtils.ASSETNUMBER))
			assetsToPut.add(new RequisitionAsset("assetNumber", update
					.get(DashBoardUtils.ASSETNUMBER)));
		if (update.containsKey(DashBoardUtils.SERIALNUMBER))
			assetsToPut.add(new RequisitionAsset("serialNumber", update
					.get(DashBoardUtils.SERIALNUMBER)));
		if (update.containsKey(DashBoardUtils.CATEGORY))
			assetsToPut.add(new RequisitionAsset("category", update
					.get(DashBoardUtils.CATEGORY)));
		if (update.containsKey(DashBoardUtils.MODELNUMBER))
			assetsToPut.add(new RequisitionAsset("modelNumber", update
					.get(DashBoardUtils.MODELNUMBER)));
		if (update.containsKey(DashBoardUtils.MANUFACTURER))
			assetsToPut.add(new RequisitionAsset("manufacturer", update
					.get(DashBoardUtils.MANUFACTURER)));
		Set<String> ipaddress = new HashSet<String>();
		if (update.containsKey(DashBoardUtils.PRIMARY)) {
			ipaddress = serviceMap.keySet();
		}
		
		updateonms(foreignSource, foreignId, 
				primary, false, descr, updatemap, 
				interfaceToDel, interfaceToAdd, 
				categoriesToDel, categoriesToAdd, 
				serviceToDel, serviceToAdd, 
				assetsToPut, ipaddress,update.get(DashBoardUtils.SNMP_PROFILE));

		
	}

	private void updateonms(String foreignSource, 
			String foreignId, String primary, boolean addPolicy, String descr,
			MultivaluedMap<String, String> updatemap,
			List<String> interfaceToDel,List<String> interfaceToAdd,
			List<String> categoriesToDel, List<String> categoriesToAdd,
			Map<String,Set<String>> serviceToDel, Map<String,Set<String>> serviceToAdd,
			List<RequisitionAsset> assetsToPut,Set<String> ipsetN,String snmpProfile) {
				
		if (snmpProfile != null && primary != null)
			m_onmsDao.setSnmpInfo(primary, getSnmpProfileContainer()
					.getSnmpProfile(snmpProfile)
					.getSnmpInfo());

		if (!updatemap.isEmpty())
			m_onmsDao
					.updateRequisitionNode(foreignSource, foreignId, updatemap);

		for (String ip : interfaceToDel) {
			if (addPolicy)
				m_onmsDao.deletePolicy(foreignSource,
						DashBoardUtils.getPolicyName(ip));
			m_onmsDao.deleteRequisitionInterface(foreignSource, foreignId, ip);		
		}

		for (String ip: serviceToDel.keySet()) {
			if (interfaceToDel.contains(ip))
				continue;
			for (String service: serviceToDel.get(ip)) {
				m_onmsDao.deleteRequisitionservice(foreignSource, foreignId, ip, service);
			}
		}
		
		for (String ip : interfaceToAdd) {
			RequisitionInterface iface = new RequisitionInterface();
			iface.setIpAddr(ip);
			iface.setDescr(descr);
			if ( primary!= null && primary.equals(ip)) {
				iface.setSnmpPrimary(PrimaryType.PRIMARY);
			} else {
				iface.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
			}
			m_onmsDao.addRequisitionInterface(foreignSource, foreignId, iface);
			if (addPolicy)
				m_onmsDao.addOrReplacePolicy(foreignSource,
					DashBoardUtils.getPolicyWrapper(ip));
		}

		for (String ip: serviceToAdd.keySet()) {
			for (String service: serviceToAdd.get(ip)) {
				m_onmsDao.addRequisitionservice(foreignSource, foreignId, ip, new RequisitionMonitoredService(service));
			}
		}

		for (RequisitionAsset asset : assetsToPut) {
			m_onmsDao.addRequisitionAsset(foreignSource, foreignId, asset);
		}

		for (String category : categoriesToDel) {
			m_onmsDao.deleteRequisitionCategory(foreignSource, foreignId,
					category);
		}

		for (String category : categoriesToAdd) {
			m_onmsDao.addRequisitionCategory(foreignSource, foreignId,
					new RequisitionCategory(category));
		}
		
		for (String ip: ipsetN) {
			if (ip.equals(primary))
				continue;
			MultivaluedMap< String, String> form = new MultivaluedMapImpl();
			form.add("snmp-primary", "N");
			m_onmsDao.updateRequisitionInterface(foreignSource, foreignId, ip, form);
		}
				

	}
	
	public void addFastNode(String foreignId, FastServiceDevice node, FastServiceLink link, Categoria cat, Set<String> secondary) {
		RequisitionNode requisitionNode = new RequisitionNode();
		
		requisitionNode.setForeignId(foreignId);
		requisitionNode.setNodeLabel(foreignId+"."+cat.getDnsdomain());
		
		if (node.getCity() != null)
			requisitionNode.setCity(node.getCity());
		if (link.getDeliveryCode() != null)
			requisitionNode.setBuilding(link.getDeliveryCode());
		
		RequisitionInterface iface = new RequisitionInterface();
		iface.setSnmpPrimary(PrimaryType.PRIMARY);
		iface.setIpAddr(node.getIpaddr());
		iface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
		iface.setDescr(DashBoardUtils.DESCR_FAST);
		requisitionNode.putInterface(iface);

		for (String ip: secondary) {
			RequisitionInterface ifacesecondary = new RequisitionInterface();
			ifacesecondary.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
			ifacesecondary.setIpAddr(ip);
			ifacesecondary.putMonitoredService(new RequisitionMonitoredService("ICMP"));
			ifacesecondary.setDescr(DashBoardUtils.DESCR_FAST);
			requisitionNode.putInterface(ifacesecondary);
		}

		requisitionNode.putCategory(new RequisitionCategory(cat.getNetworklevel()));
		requisitionNode.putCategory(new RequisitionCategory(cat.getName()));
		
		if (node.getNotifyCategory().equals(DashBoardUtils.m_fast_default_notify))
			requisitionNode.putCategory(new RequisitionCategory(cat.getNotifylevel()));
		else
			requisitionNode.putCategory(new RequisitionCategory(node.getNotifyCategory()));
			
		
		requisitionNode.putCategory(new RequisitionCategory(cat.getThresholdlevel()));
		
		StringBuffer address1 = new StringBuffer();
		if (node.getAddressDescr() != null)
			address1.append(node.getAddressDescr());
		if (node.getAddressName() != null) {
			if (address1.length() > 0)
				address1.append(" ");
			address1.append(node.getAddressName());
		}
		if (node.getAddressNumber() != null) {
			if (address1.length() > 0)
				address1.append(" ");
			address1.append(node.getAddressNumber());
		}
		
		if (node.getCity() != null &&  address1.length() > 0)
			requisitionNode.putAsset(new RequisitionAsset("description", node.getCity() + " - " + address1.toString()));
		
		if (address1.length() > 0 )
			requisitionNode.putAsset(new RequisitionAsset("address1", address1.toString()));
		
		if (link.getDeliveryCode() != null)
			requisitionNode.putAsset(new RequisitionAsset("circuitId", link.getDeliveryCode()));
		
		if (node.getIstat() != null && link.getSiteCode() !=  null )
			requisitionNode.putAsset(new RequisitionAsset("building", node.getIstat()+"-"+link.getSiteCode()));

		for ( RequisitionAsset asset : getBackupProfileContainer().getBackupProfile(node.getBackupprofile()).getRequisitionAssets().getAssets()) {
			requisitionNode.putAsset(asset);
		}
		m_onmsDao.setSnmpInfo(node.getIpaddr(), getSnmpProfileContainer().getSnmpProfile(node.getSnmpprofile()).getSnmpInfo());
			
		logger.info("Adding node with foreignId: " + foreignId + " primary: " + node.getIpaddr());
		m_onmsDao.addRequisitionNode(DashBoardUtils.TN_REQU_NAME, requisitionNode);
		logger.info("Adding policy for interface: " + node.getIpaddr());
		m_onmsDao.addOrReplacePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyWrapper(node.getIpaddr()));
		for (String ip: secondary) {
			logger.info("Adding policy for interface: " + ip);
			m_onmsDao.addOrReplacePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyWrapper(ip));			
		}

	}
	public String getUser() {
		return m_user;
	}

	public void setUser(String user) {
		m_user = user;
	}
		
    public boolean isFastRunning() {
    	return getJobContainer().isFastRunning();
    }
    
    public void synctrue(String foregnSource) {
    	m_onmsDao.sync(foregnSource);
    }

    public void syncdbonly(String foregnSource) {
    	m_onmsDao.syncDbOnly(foregnSource);
    }

    public void syncfalse(String foregnSource) {
    	m_onmsDao.syncRescanExistingFalse(foregnSource);
    }
    
	public String getUrl() {
		return m_url;
	}

	public void setUrl(String url) {
		m_url = url;
	}
	
	public boolean isValid(TrentinoNetworkNode node) {
		return valid(node,getDnsDomainContainer().getDomains());
	}

	public boolean isValid(SistemiInformativiNode node) {
		return valid(node,getDnsDomainContainer().getDomains());
	}

	public boolean isValid(MediaGatewayNode node) {
		return valid(node,getDnsDomainContainer().getDomains());
	}

	public boolean isValid(BasicNode node) {
		if (node instanceof TrentinoNetworkNode ) 
			return isValid((TrentinoNetworkNode)node);
		else if (node instanceof MediaGatewayNode)
			return isValid((MediaGatewayNode)node);
		else if (node instanceof SistemiInformativiNode)	
			return isValid((SistemiInformativiNode)node);
		return false;
	}

	public void update(BasicNode node) {
		if (node instanceof TrentinoNetworkNode ) {
			update((TrentinoNetworkNode)node);
		} else if (node instanceof MediaGatewayNode) {
			update((MediaGatewayNode)node);
		} else if (node instanceof SistemiInformativiNode) {	
			update((SistemiInformativiNode)node);
		}
	}
	
	public void add(BasicNode node) throws SQLException {
		if (node instanceof TrentinoNetworkNode ) {
			add((TrentinoNetworkNode)node);
		} else if (node instanceof MediaGatewayNode) {
			add((MediaGatewayNode)node);
		} else if (node instanceof SistemiInformativiNode) {	
			add((SistemiInformativiNode)node);
		}
	}

	public void delete(BasicNode node) {
		if (node instanceof TrentinoNetworkNode ) {
			delete((TrentinoNetworkNode)node);
		} else if (node instanceof MediaGatewayNode) {
			delete((MediaGatewayNode)node);
		} else if (node instanceof SistemiInformativiNode) {
			delete((SistemiInformativiNode)node);
		}
	}

}