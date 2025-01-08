package org.mtcg.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.MyPrintWriter;
import org.mtcg.db.DbAccess;

import java.util.Map;
import java.util.Objects;

public class DELETE_Handler {

    DbAccess dba;

    public DELETE_Handler(DbAccess dba){
        this.dba = dba;
    }

    public void call_request(String auth, String[] path_parts, StringBuilder info, StringBuilder content, MyPrintWriter writer){

        switch (path_parts[1]){
            case "tradings":
                this.tradings(auth, path_parts[2], content, writer);
                break;

            default:
                writer.println(400, "unknown request");
        }
    }
    public void tradings(String auth,String path_part, StringBuilder content, MyPrintWriter writer){
        //todo
    }
}

