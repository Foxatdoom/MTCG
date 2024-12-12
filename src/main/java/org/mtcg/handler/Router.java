package org.mtcg.handler;

import org.mtcg.db.DbAccess;

import java.io.PrintWriter;
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


    public void call_handler(StringBuilder info, StringBuilder content, PrintWriter writer){

        //System.out.println("calling handler");

        String spezific_request = info.substring(0, info.indexOf(" ")); // post,get,...

        switch (spezific_request){
            case "POST":
                post.call_request(info, content, writer);
                break;


            case "GET":
                //get.call_request(info, content, writer);
                break;



            case "PUT":
                //put.call_request(info, content, writer);
                break;



            case "DELETE":
                //del.call_request(info, content, writer);
                break;
        }
    }
}
