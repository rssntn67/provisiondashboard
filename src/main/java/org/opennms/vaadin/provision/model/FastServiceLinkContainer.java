package org.opennms.vaadin.provision.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class FastServiceLinkContainer extends SQLContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6920237072015173649L;

	public FastServiceLinkContainer(QueryDelegate delegate)
			throws SQLException {
		super(delegate);
	}
	
    @SuppressWarnings("unchecked")
	public synchronized List<FastServiceLink> getFastServiceLinks() {
    	List<FastServiceLink> links = new ArrayList<FastServiceLink>();
		for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			Item fastservicelinktableRow = getItem(i.next());
			links.add(new FastServiceLink(
					fastservicelinktableRow.getItemProperty("order_code"), 
					fastservicelinktableRow.getItemProperty("tariff"),
					fastservicelinktableRow.getItemProperty("link_type"),
					fastservicelinktableRow.getItemProperty("pcv_1_name"),
					fastservicelinktableRow.getItemProperty("pcv_2_name"),
					fastservicelinktableRow.getItemProperty("td"),
					fastservicelinktableRow.getItemProperty("delivery_device_network_side"),
					fastservicelinktableRow.getItemProperty("delivery_device_client_side"),
					fastservicelinktableRow.getItemProperty("delivery_interface"),
					fastservicelinktableRow.getItemProperty("interface_description"),
					fastservicelinktableRow.getItemProperty("vrf"),
					fastservicelinktableRow.getItemProperty("delivery_code")));
		}
    	
    	return links;
    }


}
