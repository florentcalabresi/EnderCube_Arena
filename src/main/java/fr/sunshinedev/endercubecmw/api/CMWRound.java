package fr.sunshinedev.endercubecmw.api;

import fr.sunshinedev.endercubecmw.managers.MobsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CMWRound {

	public UUID id;
    public String name;
	public int maxMobs = -1;
    public List<MobRound> mobsRound = new ArrayList<>();
    
    public CMWRound(String name) {
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
	
	public List<MobRound> getMobsRound() {
		return mobsRound;
	}
	
	public int getMaxMobs() {
		return maxMobs;
	}

	public void setMaxMobs(int i) {
		this.maxMobs = i;
	}

	public static class MobRound {

		public CMWMobCustom mobCustom;
		public UUID id;
		public int number;
		
		public MobRound(UUID id, int number) {
			this.id = id;
			this.number = number;

			Optional<CMWMobCustom> mobOpt = MobsManager.getMobFromId(this.id);
			if(mobOpt.isPresent())	this.mobCustom = mobOpt.get();
		}

		public UUID getId() {
			return id;
		}

		public void setId(UUID id) {
			this.id = id;
		}

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public CMWMobCustom getMobCustom() {
			return mobCustom;
		}
	}
}
