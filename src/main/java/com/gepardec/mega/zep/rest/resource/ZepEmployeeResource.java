package com.gepardec.mega.zep.rest.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.service.ZepEmployeeRestService;
import io.quarkus.restclient.runtime.QuarkusRestClientBuilder;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Set;

@ApplicationScoped
public class ZepEmployeeResource {

    @RestClient
    ZepEmployeeRestService zepEmployeeRestService;
//
//    public ZepEmployeeResource(){
//        zepEmployeeRestService = QuarkusRestClientBuilder.newBuilder()
//
//                .build(ExtensionsService.class);
//    }

    public ZepEmployee username(String username) {
        try (Response resp = zepEmployeeRestService.getByUsername(username)) {
            String output = resp.readEntity(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(output);
            return objectMapper.treeToValue(jsonNode.get("data"), ZepEmployee.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
