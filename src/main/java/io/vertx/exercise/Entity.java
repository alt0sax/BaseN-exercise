package io.vertx.exercise;

import java.util.Set;
import java.util.Map;

interface Entity {
  // Returns a unique identifier
  String getID();
  
  // Returns the sub-entities of this entity
  Set<Entity> getSubEntities();
  
  // Returns a set of key-value data belonging to this entity
  Map<String,String> getData();
}
