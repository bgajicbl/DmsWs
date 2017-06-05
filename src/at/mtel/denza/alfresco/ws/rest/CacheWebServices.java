package at.mtel.denza.alfresco.ws.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import at.mtel.denza.alfresco.jpa.EntityManagerSingleton;
import at.mtel.denza.alfresco.util.Migration;

@Path("/alfresco")
public class CacheWebServices {

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/cache/refresh")
	public Response cacheRefresh() {

		EntityManagerSingleton.getEntityManager().clear();

		return Response.ok("Cleared!").build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/migration")
	public Response migration(@QueryParam("path") String file) {
		if (file != null && file.length() > 3) {
			java.nio.file.Path path = Paths.get(file);
			try {
				Migration m = new Migration();
				List<String> list = Files.readAllLines(path);
				for (String s : list) {
					String[] spl = s.split(";");
					if (spl.length == 2) {
						m.migrateSubscriberToCustomer(spl[0], spl[1]);
					}
				}
				return Response.ok("migrated: " + list.size()).build();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return Response.notModified().build();
	}

}
