package at.mtel.denza.alfresco.ws.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.RollbackException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import at.mtel.denza.alfresco.jpa.Customer;
import at.mtel.denza.alfresco.jpa.Document;
import at.mtel.denza.alfresco.jpa.EntityManagerSingleton;
import at.mtel.denza.alfresco.jpa.Metadata;
import at.mtel.denza.alfresco.jpa.Subscriber;
import at.mtel.denza.alfresco.util.AlfrescoDataReader;
import at.mtel.denza.alfresco.util.DateUtil;

@Path("/alfresco/metadatas")
public class MetadataWebServices {

	private AlfrescoDataReader alfrescoDataReader = new AlfrescoDataReader();

	// Metadata za sve dokumente
	/*
	 * @GET
	 * 
	 * @Produces({ MediaType.APPLICATION_JSON })
	 * 
	 * @Path("/") public Response getAllIds() { List<Metadata> x =
	 * FunctionIntegrator.getAllMetadata(); GenericEntity<List<Metadata>> list = new
	 * GenericEntity<List<Metadata>>(x) { }; return Response.ok(list).build(); }
	 */

	// Metadata za sve dokumente za poslati ID klijenta
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/customer/{customer}")
	public Response getAllDocuments4Customer(@PathParam("customer") String customerId) {
		List<Customer> customerList = FunctionIntegrator.getCustomer(customerId);
		if (customerList.size() == 0)
			return Response.status(Response.Status.NOT_FOUND).build();

		List<Metadata> x = FunctionIntegrator.getMetadata(customerId);
		GenericEntity<List<Metadata>> list = new GenericEntity<List<Metadata>>(x) {
		};
		return Response.ok(list).build();
	}

	// Metadata za sve dokumente za poslate ID klijenta i tip dokumenta
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/customer/{customer}/doctype/{doctype}")
	public Response getMetadata4CustomerDocType(@PathParam("customer") String customerId,
			@PathParam("doctype") int doctype) {
		List<Customer> customerList = FunctionIntegrator.getCustomer(customerId);
		if (customerList.size() == 0)
			return Response.status(Response.Status.NOT_FOUND).build();

		List<Metadata> x = FunctionIntegrator.getMetadata(customerId, doctype);
		GenericEntity<List<Metadata>> list = new GenericEntity<List<Metadata>>(x) {
		};
		return Response.status(201).entity(list).build();
	}

	// Metadata za sve dokumente za poslate clientID, doctype i period
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/customer/{customer}/doctype/{doctype}/from/{from}/to/{to}")
	public Response getMetadata4CustomerDocTypePeriod(@PathParam("customer") String customerId,
			@PathParam("doctype") int doctype, @PathParam("from") String from, @PathParam("to") String to) {
		List<Customer> customerList = FunctionIntegrator.getCustomer(customerId);
		if (customerList.size() == 0)
			return Response.status(Response.Status.NOT_FOUND).build();
		try {
			List<Metadata> metadataList = FunctionIntegrator.getMetadata(customerId, doctype, from, to);
			for (Metadata m : metadataList) {
				// if filename is not present, call Alfresco to retrieve it
				if (m.getFilename() == null || m.getFilename().length() == 0) {
					String filename = alfrescoDataReader.getFileNameFromNodeRef(m.getNoderef());
					if (filename != null && filename.length() > 0) {
						m.setFilename(filename);
						try {
							EntityManagerSingleton.getEntityManager().getTransaction().begin();
							EntityManagerSingleton.getEntityManager().persist(m);
							EntityManagerSingleton.getEntityManager().getTransaction().commit();
						} catch (RollbackException re) {
							System.out.println("Retreived filename not persisted: " + filename);
						}
					}
				}

			}
			GenericEntity<List<Metadata>> list = new GenericEntity<List<Metadata>>(metadataList) {
			};
			return Response.status(201).entity(list).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.NOT_IMPLEMENTED).build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/subscriber/{subscriber}")
	public Response getAllDocuments4Subscriber(@PathParam("subscriber") String subscriberId) {
		List<Subscriber> subscriberList = FunctionIntegrator.getSubscriber(subscriberId);
		if (subscriberList.size() == 0)
			return Response.status(Response.Status.NOT_FOUND).build();

		List<Metadata> x = FunctionIntegrator.getSubscriberMetadata(subscriberId);
		GenericEntity<List<Metadata>> list = new GenericEntity<List<Metadata>>(x) {
		};
		return Response.ok(list).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/subscriber/{subscriber}/doctype/{doctype}")
	public Response getMetadata4SubscriberDocType(@PathParam("subscriber") String subscriberId,
			@PathParam("doctype") int doctype) {
		List<Subscriber> subscriberList = FunctionIntegrator.getSubscriber(subscriberId);
		if (subscriberList.size() == 0)
			return Response.status(Response.Status.NOT_FOUND).build();

		List<Metadata> x = FunctionIntegrator.getSubscriberMetadata(subscriberId, doctype);
		GenericEntity<List<Metadata>> list = new GenericEntity<List<Metadata>>(x) {
		};
		return Response.status(201).entity(list).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/subscriber/{subscriber}/doctype/{doctype}/from/{from}/to/{to}")
	public Response getMetadata4SubscriberDocTypePeriod(@PathParam("subscriber") String subscriberId,
			@PathParam("doctype") int doctype, @PathParam("from") String from, @PathParam("to") String to) {
		List<Subscriber> subscriberList = FunctionIntegrator.getSubscriber(subscriberId);
		if (subscriberList.size() == 0)
			return Response.status(Response.Status.NOT_FOUND).build();

		try {
			List<Metadata> x = FunctionIntegrator.getSubscriberMetadata(subscriberId, doctype, from, to);
			GenericEntity<List<Metadata>> list = new GenericEntity<List<Metadata>>(x) {
			};
			return Response.status(201).entity(list).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.NOT_IMPLEMENTED).build();
		}
	}

	// Upisi metadata
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/insert")
	public Response insertMetadata(@QueryParam("customerId") String customerId,
			@QueryParam("subscriberId") String subscriberId, @QueryParam("fileName") String fileName,
			@QueryParam("nodeRef") String nodeRef, @QueryParam("period") int period,
			@QueryParam("documentType") int doctype, @QueryParam("user") int user) {

		if (customerId == null || customerId.length() == 0 || nodeRef == null || nodeRef.length() == 0) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		boolean shouldPersist = false;
		Metadata m = new Metadata();
		Customer cust = null;
		Subscriber sub = null;
		List<Customer> customers = FunctionIntegrator.getCustomer(customerId);
		// if customer doesn't exist, create it
		if (customers.size() == 0) {
			cust = new Customer();
			cust.setCustomerId(customerId);
			cust.setMsisdn(0L);
			shouldPersist = true;
		} else {
			// customer exist
			cust = FunctionIntegrator.getCustomer(customerId).get(0);
		}
		// if subscriber doesn't exist, create it
		if (subscriberId != null && subscriberId.length() > 0) {
			List<Subscriber> subscribers = FunctionIntegrator.getSubscriber(subscriberId);
			if (subscribers.size() == 0) {
				sub = new Subscriber();
				sub.setSubscriberId(subscriberId);
				sub.setCustomer(cust);
				subscribers.add(sub);
				cust.setSubscribers(subscribers);
				shouldPersist = true;
			} else {
				sub = subscribers.get(0);
			}
		}
		if (shouldPersist) {
			try {
				EntityManagerSingleton.getEntityManager().getTransaction().begin();
				EntityManagerSingleton.getEntityManager().persist(cust);
				EntityManagerSingleton.getEntityManager().getTransaction().commit();
			} catch (RollbackException re) {
				return Response.serverError().build();
			}
		}
		Document d = FunctionIntegrator.getDocument(doctype);
		m.setCustomer(cust);
		m.setSubscriber(sub);
		m.setDocument(d);
		m.setNoderef(nodeRef);
		m.setPeriod(DateUtil.getDateFromString(String.valueOf(period)));
		try {
			EntityManagerSingleton.getEntityManager().getTransaction().begin();
			EntityManagerSingleton.getEntityManager().persist(m);
			EntityManagerSingleton.getEntityManager().getTransaction().commit();
		} catch (RollbackException re) {
			for (Throwable t = re.getCause(); t != null; t = t.getCause()) {
				if (t instanceof org.postgresql.util.PSQLException)
					return Response.status(Response.Status.CONFLICT).build();
			}
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		return Response.ok(String.valueOf(m.getId())).build();
	}
}