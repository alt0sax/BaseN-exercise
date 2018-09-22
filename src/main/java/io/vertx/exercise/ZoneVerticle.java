package io.vertx.exercise;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.nio.file.Paths;
import java.nio.file.Path;

public class ZoneVerticle extends AbstractVerticle {

  private AbstractEntity root;

  @Override
  public void start() {
    setUpInitialData();
    
    Router router = Router.router(vertx);
    
    router.route().handler(BodyHandler.create());
    
    // Binds the "/zones" route to the "view" page of the root zone
    router.get("/zones").handler(routingContext -> {
      routingContext.reroute(HttpMethod.GET, "/zones/" + root.getID());
    });
    
    // The "view" page of a given zone. It displays the given zone's data and sub-zones.
    // Also allows to move the given zone, or to add a new one as a sub-zone of the given zone
    router.get("/zones/:ID").handler(routingContext -> {
      String ID = routingContext.request().getParam("ID");
      HttpServerResponse response = routingContext.response();
    
      if(ID == null) {
        sendError(400, response);
      } else {
        Entity zone = findZoneByID(ID);
      
        if(zone == null) {
          sendError(404, response);
        } else {
          String html =
            "<a href=\"/zones\">ROOT</a>"
            + "<h3>ID: " + zone.getID() + "</h3>";
        
          Set<Entity> subZones = zone.getSubEntities();
        
          if(!subZones.isEmpty()) {
            html +=
              "<h5>Sub-zones :</h5>"
              + "<ul>";
          
            for(Entity currentSubZone : subZones) {
              html += "<li>ID: <a href=\"/zones/" + currentSubZone.getID() + "\">" + currentSubZone.getID() + "</a></li>";
            }
          
            html += "</ul>";
          }
        
          Map<String,String> data = zone.getData();
        
          if(!data.isEmpty()) {
            html += "<table style=\"border: 1px solid black;\"><caption>Data</caption>";
          
            for(Map.Entry<String,String> information : data.entrySet()) {
              html +=
                "<tr>"
                + "<td style=\"border: 1px solid black;\">" + information.getKey() + "</td><td style=\"border: 1px solid black;\">" + information.getValue() + "</td>"
                + "</tr>";
            }
          
            html += "</table>";
          }
          
          String[] path = routingContext.request().path().split("/");
          
          html +=
            "<br>"
            + "<form action=\"/zones\" method=\"post\" style=\"border: 1px solid black;\">"
            + "<input type=\"hidden\" name=\"mode\" value=\"send\">"
            + "<input type=\"hidden\" name=\"ID\" value=\"" + path[path.length - 1] + "\">"
            + "<input type=\"submit\" value=\"Send this zone to the root zone\">"
            + "</form>"
            
            + "<form action=\"/zones\" method=\"post\" style=\"border: 1px solid black;\">"
            + "<input type=\"hidden\" name=\"mode\" value=\"send\">"
            + "<input type=\"hidden\" name=\"ID\" value=\"" + path[path.length - 1] + "\">"
            + "Destination:<br>"
            + "<input type=\"text\" name=\"dest\"><br>"
            + "<input type=\"submit\" value=\"Send this zone\">"
            + "</form>"
            
            + "<form action=\"/zones\" method=\"post\" style=\"border: 1px solid black;\">"
            + "<input type=\"hidden\" name=\"mode\" value=\"add\">"
            + "<input type=\"hidden\" name=\"ID\" value=\"" + path[path.length - 1] + "\">"
            + "ID:<br>"
            + "<input type=\"text\" name=\"newID\"><br>"
            + "Name:<br>"
            + "<input type=\"text\" name=\"name\"><br>"
            + "Population:<br>"
            + "<input type=\"number\" name=\"population\"><br>"
            + "<input type=\"submit\" value=\"Add a sub-zone\">"
            + "</form>";
        
          response.putHeader("content-type", "text/html").end(html);
        }
      }
    });
    
    // The route that allows to move or to add a zone
    router.post("/zones").handler(routingContext -> {
      // Checks the "mode" parameter to see if it is a "move zone" request or an "add zone" request
      String mode = routingContext.request().getParam("mode");
      
      if(mode.equals("send")) { // "move zone" request
        String ID = routingContext.request().getParam("ID");
        String dest = routingContext.request().getParam("dest");
        // If the destination is not given, then makes this zone a sub-zone of the root zone
        if(dest == null) dest = root.getID();
        
        HttpServerResponse response = routingContext.response();
    
        if(ID == null || dest == null) {
          sendError(400, response);
        } else {
          Entity zone = findZoneByID(ID);
          Entity destination = findZoneByID(dest);
      
          if(zone == null || destination == null) {
            sendError(404, response);
          } else {
            // Removes the zone from its super-zone
            findParentByID(ID).getSubEntities().remove(zone);
            // Makes this zone a sub-zone of its destination
            destination.getSubEntities().add(zone);
          }
        }
      } else { // "add zone" request
        String ID = routingContext.request().getParam("ID");
        String newID = routingContext.request().getParam("newID");
        String name = routingContext.request().getParam("name");
        String population = routingContext.request().getParam("population");
        HttpServerResponse response = routingContext.response();
    
        Entity zone = findZoneByID(ID);
        if(zone == null) {
          sendError(404, response);
        } else {
          Entity newZone = new Zone(newID);
          newZone.getData().put("name", name);
          newZone.getData().put("population", population);
          // Makes the new zone a sub-zone of the current zone
          zone.getSubEntities().add(newZone);
        }
      }
      
      routingContext.reroute(HttpMethod.GET, "/zones");
    });
    
    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
  
  private void setUpInitialData() {
    root = new Zone("1");
    root.getData().put("name", "Suomi");
    root.getData().put("population", "5516224");
    
    Entity subZone1 = new Zone("11");
    subZone1.getData().put("name", "Pohjois-Karjala");
    subZone1.getData().put("population", "165866");
    
    Entity subZone2 = new Zone("12");
    subZone2.getData().put("name", "Varsinais-Suomi");
    subZone2.getData().put("population", "465012");
    
    Entity subZone3 = new Zone("111");
    subZone3.getData().put("name", "Juuka");
    subZone3.getData().put("population", "5389");
    
    Entity subZone4 = new Zone("112");
    subZone4.getData().put("name", "Valtimo");
    subZone4.getData().put("population", "2264");
    
    Entity subZone5 = new Zone("121");
    subZone5.getData().put("name", "Aura");
    subZone5.getData().put("population", "3972");
    
    Entity subZone6 = new Zone("122");
    subZone6.getData().put("name", "Vehmaa");
    subZone6.getData().put("population", "2338");
    
    root.getSubEntities().add(subZone1);
    root.getSubEntities().add(subZone2);
    subZone1.getSubEntities().add(subZone3);
    subZone1.getSubEntities().add(subZone4);
    subZone2.getSubEntities().add(subZone5);
    subZone2.getSubEntities().add(subZone6);
  }
  
  private Entity findZoneByID(String ID) {
    return root.findEntityByID(ID);
  }
  
  private Entity findParentByID(String ID) {
    return root.findParentByID(ID);
  }
  
  private void sendError(int statusCode, HttpServerResponse response) {
    response.setStatusCode(statusCode).end();
  }
}
