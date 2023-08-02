package fr.sunshinedev.endercubecmw.api;

import java.util.UUID;

import org.json.JSONArray;

public class CMWKit {

	public UUID id;
    public String name;
    public JSONArray inventory;
    public CMWKit(String name) {
        this.name = name;
    }

    public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
        return name;
    }

    public JSONArray getInventory() {
        return inventory;
    }

    public void setInventory(JSONArray inventory) {
        this.inventory = inventory;
    }
}
