package io.vertx.exercise;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public abstract class AbstractEntity implements Entity {
  protected String ID;
  private Set<Entity> subEntities = new HashSet<Entity>();
  private Map<String,String> data = new HashMap<String,String>();
  
  // Returns a unique identifier
  public String getID() {
    return ID;
  }
  
  // Returns the sub-entities of this entity
  public Set<Entity> getSubEntities() {
    return subEntities;
  }
  
  // Returns a set of key-value data belonging to this entity
  public Map<String,String> getData() {
    return data;
  }
  
  // Looks for the entity identified by the given ID in the children of the current entity plus this entity
  // Returns the found entity or null if it is not found
  public Entity findEntityByID(String ID) {
    if(getID().equals(ID)) {
      return this;
    }
    
    if(getSubEntities().isEmpty()) {
      return null;
    }
    
    for(Entity currentSubEntity : getSubEntities()) {
      Entity foundEntity = ((AbstractEntity) currentSubEntity).findEntityByID(ID);
      
      if(foundEntity != null) {
        return foundEntity;
      }
    }
    
    return null;
  }
  
  // Looks for the parent of the entity identified by the given ID in the children of the current entity plus this entity
  // Returns the found entity or null if it is not found
  public Entity findParentByID(String ID) {
    if(getID().equals(ID)) {
      return null;
    }
    
    if(getSubEntities().isEmpty()) {
      return null;
    }
    
    for(Entity currentSubEntity : getSubEntities()) {
      if(currentSubEntity.getID().equals(ID)) {
        return this;
      }
    }
    
    for(Entity currentSubEntity : getSubEntities()) {
      Entity foundParent = ((AbstractEntity) currentSubEntity).findParentByID(ID);
      
      if(foundParent != null) {
        return foundParent;
      }
    }
    
    return null;
  }
}
