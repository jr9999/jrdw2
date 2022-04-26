package com.cvent.dw.helloworld.resources;

import com.cvent.dw.helloworld.core.Person;
import com.cvent.dw.helloworld.db.PersonDAO;
import com.cvent.dw.helloworld.views.PersonView;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.OptionalLong;

@Path("/people/{personId}")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {

    private final PersonDAO peopleDAO;

    public PersonResource(PersonDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    @GET
    @UnitOfWork
    public Person getPerson(@PathParam("personId") OptionalLong personId) {
        return findSafely(personId.orElseThrow(() -> new BadRequestException("person ID is required")));
    }

    @GET
    @Path("/view_freemarker")
    @UnitOfWork
    @Produces(MediaType.TEXT_HTML)
    public PersonView getPersonViewFreemarker(@PathParam("personId") OptionalLong personId) {
        return new PersonView(PersonView.Template.FREEMARKER, findSafely(personId.orElseThrow(() -> new BadRequestException("person ID is required"))));
    }

    @GET
    @Path("/view_mustache")
    @UnitOfWork
    @Produces(MediaType.TEXT_HTML)
    public PersonView getPersonViewMustache(@PathParam("personId") OptionalLong personId) {
        return new PersonView(PersonView.Template.MUSTACHE, findSafely(personId.orElseThrow(() -> new BadRequestException("person ID is required"))));
    }

    private Person findSafely(long personId) {
        return peopleDAO.findById(personId).orElseThrow(() -> new NotFoundException("No such user."));
    }
}
