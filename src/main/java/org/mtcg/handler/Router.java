package org.mtcg.handler;

import org.mtcg.MyPrintWriter;
import org.mtcg.db.DbAccess;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router {

    POST_Handler post;
    GET_Handler get;
    PUT_Handler put;
    DELETE_Handler del;

    DbAccess dba;

    public Router() throws SQLException {
        dba = new DbAccess();

        post = new POST_Handler(dba);
        get = new GET_Handler(dba);
        put = new PUT_Handler(dba);
        //del = new DELETE_Handler(dba);
    }


    public void call_handler(StringBuilder info, StringBuilder content, MyPrintWriter writer){

        String spezific_request = "";
        String auth = "";
        String paths = "";
        String[] path_parts = null; // the first one is always null because every path starts with / !!!!!!

        // Extract request
        spezific_request = info.substring(0, info.indexOf(" ")); // post,get,...

        // Extract path (starts with / and ends at whitespace)
        Pattern pathPattern = Pattern.compile("\\s(/\\S+)");
        Matcher pathMatcher = pathPattern.matcher(info.toString());
        if (pathMatcher.find()) {
            paths = pathMatcher.group(1);
            path_parts = paths.split("[/?]"); //splits by / or ?
        }

        // Extract token
        Pattern authTokenPattern = Pattern.compile("Authorization: Bearer (\\S+)");
        Matcher authTokenMatcher = authTokenPattern.matcher(info.toString());
        if (authTokenMatcher.find()) {
            auth = authTokenMatcher.group(1);
        }

        //System.out.println("infos: " + auth + " " + paths + " " + path_parts[1]);

        switch (spezific_request){
            case "POST":
                post.call_request(auth, path_parts, info, content, writer);
                break;


            case "GET":
                get.call_request(auth, path_parts, info, content, writer);
                break;



            case "PUT":
                put.call_request(auth, path_parts, info, content, writer);
                break;



            case "DELETE":
                //del.call_request(info, content, writer);
                break;
        }
    }
}
