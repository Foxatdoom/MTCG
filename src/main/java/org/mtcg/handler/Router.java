package org.mtcg.handler;

import org.mtcg.db.DbAccess;

import java.sql.SQLException;

public class Router {

    POST_Handler post;
    GET_Handler get;
    PUT_Handler put;
    DELETE_Handler del;

    DbAccess dba;

    public Router() throws SQLException {
        dba = new DbAccess();

        post = new POST_Handler(dba);
        //get = new GET_Handler(dba);
        //put = new PUT_Handler(dba);
        //del = new DELETE_Handler(dba);
    }


    public String call_handler(StringBuilder info, StringBuilder content){

        //System.out.println("calling handler");

        String response_to_client = "";

        String spezific_request = info.substring(0, info.indexOf(" ")); // post,get,...

        switch (spezific_request){
            case "POST":
                response_to_client = post.call_request(info, content);
                break;


            case "GET":
                //get.call_request(info, content);
                break;



            case "PUT":
                //put.call_request(info, content);
                break;



            case "DELETE":
                //del.call_request(info, content);
                break;
        }
        return response_to_client;
    }
}
