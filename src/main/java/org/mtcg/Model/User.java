package org.mtcg.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private final String username;
    private final String password;
    private final String token;
    private final String name;
    private final String bio;
    private final String image;

    // define how to create an instance from JSON
    @JsonCreator
    public User(
            @JsonProperty("Username") String username,   // Maps to the incoming JSON "Username"
            @JsonProperty("Password") String password     // Maps to the incoming JSON "Password"
    ) {
        this.username = username;
        this.password = password;
        this.token = username + "-mtcgToken"; // Generate the token based on username
        this.name = "";
        this.bio = "";
        this.image = "";
    }

    public User(String username, String password, String name, String bio, String image) {
        this.username = username;
        this.password = password;
        this.token = username + "-mtcgToken"; // Generate the token based on username
        this.name = name;
        this.bio = bio;
        this.image = image;
    }

    // GETTER-Methods
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public String toJson(){
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"token\":\"" + token + "\",\"name\":\"" + name + "\",\"bio\":\"" + bio + "\",\"image\":\"" + image + "\"}";
    }
}