package at.mtel.denza.alfresco.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

public class AlfrescoDataReader {
	
	public static final String ALFRESCO_USER = AppPropertyReader.getParameter("alfresco.user");
	public static final String ALFRESCO_PASS = AppPropertyReader.getParameter("alfresco.pass");
	public static final String ATOMPUB_URL = AppPropertyReader.getParameter("alfresco.atompub.url");
	public static final String NAME_QUERY = "SELECT cmis:name FROM cmis:document where cmis:objectId = ?";

	public static Session session;
	
	private void createSession() throws CmisUnauthorizedException {
		SessionFactory factory = SessionFactoryImpl.newInstance();
		
		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(SessionParameter.USER, ALFRESCO_USER);
		parameter.put(SessionParameter.PASSWORD, ALFRESCO_PASS);
		parameter.put(SessionParameter.ATOMPUB_URL, ATOMPUB_URL);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		// get first repository and create session
		Repository repository = factory.getRepositories(parameter).get(0);
		session = repository.createSession();

	}
	
	public Session getSessionForTicket(String ticket) throws CmisUnauthorizedException {
		SessionFactory factory = SessionFactoryImpl.newInstance();

		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(SessionParameter.USER, "ROLE_TICKET");
		parameter.put(SessionParameter.PASSWORD, ticket);
		parameter.put(SessionParameter.ATOMPUB_URL, ATOMPUB_URL);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		// get first repository and create session
		Repository repository = factory.getRepositories(parameter).get(0);
		Session session = repository.createSession();

		return session;
	}
	
	public String getFileNameFromNodeRef(String nodeRef) {
		if(session == null) {
			createSession();
		}
		QueryStatement qs = session.createQueryStatement(NAME_QUERY);
		qs.setString(1, nodeRef);
		ItemIterable<QueryResult> results = session.query(qs.toQueryString(), false);
		if (results.getTotalNumItems() == 0) {
			return "";
		}
		Object value = null;
		for (QueryResult hit : results) {
			for (PropertyData<?> property : hit.getProperties()) {
				value = property.getFirstValue();
			}
		}
		return value == null ? null : (String) value;
	}


}
