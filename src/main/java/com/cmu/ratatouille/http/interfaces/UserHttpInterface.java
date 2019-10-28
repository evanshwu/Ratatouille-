package com.cmu.ratatouille.http.interfaces;

import com.cmu.ratatouille.http.utils.StringUtil;
import com.cmu.ratatouille.managers.BookManager;
import com.cmu.ratatouille.managers.RecipeManager;
import com.cmu.ratatouille.models.Recipe;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.cmu.ratatouille.http.exceptions.HttpBadRequestException;
import com.cmu.ratatouille.http.responses.AppResponse;
import com.cmu.ratatouille.http.utils.PATCH;
import com.cmu.ratatouille.managers.UserManager;
import com.cmu.ratatouille.models.User;
import com.cmu.ratatouille.utils.*;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

@Path("/users")

 public class UserHttpInterface extends HttpInterface {

    private ObjectWriter ow;
    private MongoCollection<Document> userCollection = null;

    public UserHttpInterface() {
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public AppResponse postUsers(Object request) {
        try {
            JSONObject json = null;
            json = new JSONObject(ow.writeValueAsString(request));
            User newuser = new User(
                    null,
                    json.getString("username"),
                    json.getString("email"),
                    json.getDouble("height"),
                    json.getDouble("weight"),
                    json.getInt("age"),
                    json.getString("gender")
            );
            UserManager.getInstance().createUser(newuser);
            return new AppResponse("Insert Successful");

        } catch (Exception e) {
            throw handleException("POST recipes", e);
        }
    }

    /*@GET
    @Produces({MediaType.APPLICATION_JSON})
    public AppResponse getUsers(@Context HttpHeaders headers) {

        try {
            AppLogger.info("Got an API call");
            ArrayList<User> users = UserManager.getInstance().getUserList();

            if (users != null)
                return new AppResponse(users);
            else
                throw new HttpBadRequestException(0, "Problem with getting users");
        } catch (Exception e) {
            throw handleException("GET /users", e);
        }


    }*/

    @GET
    @Path("/{username}")
    @Produces({MediaType.APPLICATION_JSON})
    public AppResponse getSingleUser(@Context HttpHeaders headers, @PathParam("username") String username) {

        try {
            AppLogger.info("Got an API call");
            ArrayList<User> users = UserManager.getInstance().getUserById(username);

            if (users != null)
                return new AppResponse(users);
            else
                throw new HttpBadRequestException(0, "Problem with getting users");
        } catch (Exception e) {
            throw handleException("GET /users/{username}", e);
        }


    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public AppResponse getsortUser(@Context HttpHeaders headers,
                                   @QueryParam("sortby") String sortBy,
                                   @QueryParam("pagesize") Integer pageSize,
                                   @QueryParam("page") Integer page) {
        AppLogger.info("[getsortUser]" + sortBy);
        if (sortBy == null && pageSize == null && page == null) {
            try {
                AppLogger.info("Got an API call");
                ArrayList<User> users = UserManager.getInstance().getUserList();

                if (users != null)
                    return new AppResponse(users);
                else
                    throw new HttpBadRequestException(0, "Problem with getting users");
            } catch (Exception e) {
                throw handleException("GET /users", e);
            }
        } else if (sortBy != null && page == null && pageSize == null) {

            try {
                AppLogger.info("hello");
                FindIterable<Document> userDocs = MongoPool.getInstance().getCollection("users").find().sort(Sorts.ascending(sortBy));
                //userCollection.find().sort(Sorts.ascending("username"));
                AppLogger.info("size=" + userDocs.toString());
                ArrayList<User> userList = new ArrayList<>();
                for (Document userDoc : userDocs) {
                    AppLogger.info("Got a doc");
                    //if (userDoc.getString("username").equals(username)) {
                    User user = new User(
                            userDoc.getObjectId("_id").toString(),
                            userDoc.getString("username"),
                            userDoc.getString("email"),
                            userDoc.getDouble("height"),
                            userDoc.getDouble("weight"),
                            userDoc.getInteger("age"),
                            userDoc.getString("gender")
                    );
                    userList.add(user);

                    //}
                }
                if (userList != null)
                    return new AppResponse(userList);
                else
                    throw new HttpBadRequestException(0, "Problem with getting users");

            } catch (Exception e) {
                throw handleException("GET /users?sortby={username}", e);
            }
        } else{
            try {
                AppLogger.info("hello");
                FindIterable<Document> userDocs = MongoPool.getInstance().getCollection("users").find().skip(pageSize * (page - 1)).limit(pageSize);
                AppLogger.info("size=" + userDocs.toString());
                ArrayList<User> userList = new ArrayList<>();
                for (Document userDoc : userDocs) {
                    AppLogger.info("Got a doc");
                    User user = new User(
                            userDoc.getObjectId("_id").toString(),
                            userDoc.getString("username"),
                            userDoc.getString("email"),
                            userDoc.getDouble("height"),
                            userDoc.getDouble("weight"),
                            userDoc.getInteger("age"),
                            userDoc.getString("gender")
                    );
                    userList.add(user);
                }
                if (userList != null)
                    return new AppResponse(userList);
                else
                    throw new HttpBadRequestException(0, "Problem with getting users");

            } catch (Exception e) {
                throw handleException("GET /users?sortby={username}", e);
            }
        }
    }


 @PATCH
 @Path("/{username}")
 @Consumes({ MediaType.APPLICATION_JSON})
 @Produces({ MediaType.APPLICATION_JSON})
 public AppResponse patchUsers(Object request, @PathParam("username") String username){
    JSONObject json = null;
    try{
        json = new JSONObject(ow.writeValueAsString(request));
            User user = new User(
                    null,
                    json.getString("username"),
                    json.getString("email"),
                    json.getDouble("height"),
                    json.getDouble("weight"),
                    json.getInt("age"),
                    json.getString("gender")
        );

    UserManager.getInstance().updateUser(user);

 }catch (Exception e){
 throw handleException("PATCH users/{username}", e);
 }

 return new AppResponse("Update Successful");
 }




 @DELETE
 @Path("/{username}")
 @Consumes({ MediaType.APPLICATION_JSON })
 @Produces({ MediaType.APPLICATION_JSON })
 public AppResponse deleteUsers(@PathParam("username") String username){

    try{
        UserManager.getInstance().deleteUser( username);
        return new AppResponse("Delete Successful");
        }catch (Exception e){

        throw handleException("DELETE users/{username}", e);
 }

 }


 }