package org.mtcg.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Users {
    private String username;
    private String password;
    private String token; // Do I need it for the first hand-in????

    // Use @JsonCreator to define how to create an instance from JSON
    @JsonCreator
    public Users(
            @JsonProperty("Username") String username,   // Maps to the incoming JSON "Username"
            @JsonProperty("Password") String password     // Maps to the incoming JSON "Password"
    ) {
        this.username = username;
        this.password = password;
        this.token = username + "-mtcgToken"; // Generate the token based on username
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
}
