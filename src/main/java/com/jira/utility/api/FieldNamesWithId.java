package com.jira.utility.api;

import javafx.beans.property.SimpleBooleanProperty;

public class FieldNamesWithId {

	public String id;
    public String name;
    private final SimpleBooleanProperty selected;

	FieldNamesWithId(String id, String Name, Boolean selected ) {
		this.id = id;
		this.name = Name;
		this.selected =  new SimpleBooleanProperty(selected);;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
    public String toString(){
        return name; 
    }
	
	public boolean getSelected() {
		return selected.get();
	}

	public void setSelected(boolean selected) {
		this.selected.set(selected);
	}
	
	public SimpleBooleanProperty selectedProperty() {
		return selected;
	}	

}
