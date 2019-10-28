package com.cmu.ratatouille.managers;

import com.cmu.ratatouille.http.utils.StringUtil;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.cmu.ratatouille.exceptions.AppException;
import com.cmu.ratatouille.exceptions.AppInternalServerException;
import com.cmu.ratatouille.models.User;
import com.cmu.ratatouille.utils.MongoPool;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserManager extends Manager {
    public static UserManager _self;
    private MongoCollection<Document> userCollection;


    public UserManager() {
        this.userCollection = MongoPool.getInstance().getCollection("users");
    }

    public static UserManager getInstance(){
        if (_self == null)
            _self = new UserManager();
        return _self;
    }


    public void createUser(User user) throws AppException {

        try{
            JSONObject json = new JSONObject(user);

            Document newDoc = new Document()
                    .append("username", user.getUsername())
                    .append("email",user.getEmail())
                    .append("height", user.getHeight())
                    .append("weight", user.getWeight())
                    .append("age", user.getAge())
                    .append("password", user.getGender());
            if (newDoc != null)
                userCollection.insertOne(newDoc);
            else
                throw new AppInternalServerException(0, "Failed to create new user");

        }catch(Exception e){
            throw handleException("Create User", e);
        }

    }

    public void updateUser( User user) throws AppException {
        try {
            Bson filter = new Document("username", user.getUsername());
            Bson newValue = new Document()
                    .append("username", user.getUsername())
                    .append("email",user.getEmail())
                    .append("height", user.getHeight())
                    .append("weight", user.getWeight())
                    .append("age", user.getAge())
                    .append("password", user.getGender());
            Bson updateOperationDocument = new Document("$set", newValue);

            if (newValue != null)
                userCollection.updateOne(filter, updateOperationDocument);
            else
                throw new AppInternalServerException(0, "Failed to update user details");

        } catch(Exception e) {
            throw handleException("Update User", e);
        }
    }

    public void deleteUser(String username) throws AppException {
        try {
            Bson filter = new Document("username", username);
            userCollection.deleteOne(filter);
        }catch (Exception e){
            throw handleException("Delete User", e);
        }
    }

    public ArrayList<User> getUserList() throws AppException {
        try{
            ArrayList<User> userList = new ArrayList<>();
            FindIterable<Document> userDocs = userCollection.find();
            for(Document userDoc: userDocs) {
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
            return new ArrayList<>(userList);
        } catch(Exception e){
            throw handleException("Get User List", e);
        }
    }

    public ArrayList<User> getUserById(String username) throws AppException {
        try{
            ArrayList<User> userList = new ArrayList<>();
            FindIterable<Document> userDocs = userCollection.find();
            for(Document userDoc: userDocs) {
                if(userDoc.getString("username").equals(username)) {
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
            }
            return new ArrayList<>(userList);
        } catch(Exception e){
            throw handleException("Get User List", e);
        }
    }


}
