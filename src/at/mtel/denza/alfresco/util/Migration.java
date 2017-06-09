package at.mtel.denza.alfresco.util;

import java.util.List;

import javax.persistence.RollbackException;
import javax.ws.rs.core.Response;

import at.mtel.denza.alfresco.jpa.Customer;
import at.mtel.denza.alfresco.jpa.EntityManagerSingleton;
import at.mtel.denza.alfresco.jpa.Metadata;
import at.mtel.denza.alfresco.jpa.Subscriber;
import at.mtel.denza.alfresco.ws.rest.FunctionIntegrator;

public class Migration {
	private Customer c;
	private Subscriber s;

	public static void main(String[] args) {
		Migration m = new Migration();
		m.migrateSubscriberToCustomer("9012", "1012618528");
	}

	public void migrateSubscriberToCustomer(String customer, String subscriber) {
		List<Customer> customerList = FunctionIntegrator.getCustomer(customer);
		if (customerList.isEmpty()) {
			// create customer
			c = new Customer();
			c.setCustomerId(customer);
			addSubscriber(c, subscriber);

		} else {
			c = customerList.get(0);
			System.out.println(c.getId());
			addSubscriber(c, subscriber);
			addSubscriberToMetadata(c);
			// System.out.println(c.getMetadata().get(0).getSubscriber());
		}
		try {
			EntityManagerSingleton.getEntityManager().getTransaction().begin();
			EntityManagerSingleton.getEntityManager().persist(c);
			EntityManagerSingleton.getEntityManager().getTransaction().commit();
			System.out.println("saved!");
			// logger.warn("Customer created: " + customerId);
		} catch (Exception re) {
			if (EntityManagerSingleton.getEntityManager().getTransaction().isActive()) {
				EntityManagerSingleton.getEntityManager().getTransaction().rollback();
			}
			System.err.println("failed! " +re.getMessage());
		}

	}

	private void addSubscriber(Customer cust, String subscriberId) {
		List<Subscriber> subsList = FunctionIntegrator.getSubscriber(subscriberId);

		if (subsList.isEmpty()) {
			Subscriber subscr = new Subscriber();
			subscr.setSubscriberId(subscriberId);
			subscr.setCustomer(cust);
			subsList.add(subscr);
			cust.setSubscribers(subsList);
		} else {
			Subscriber s = subsList.get(0);
			s.setCustomer(cust);
			cust.setSubscribers(subsList);
		}
	}

	private void addSubscriberToMetadata(Customer cust) {
		List<Metadata> metaList = cust.getMetadata();
		if (!metaList.isEmpty()) {
			for (Metadata m : metaList) {
				m.setSubscriber(cust.getSubscribers().get(0));
			}
			c.setMetadata(metaList);

		}
	}

}
