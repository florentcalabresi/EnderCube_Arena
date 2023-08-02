package fr.sunshinedev.endercubecmw;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import fr.sunshinedev.endercubecmw.api.*;
import fr.sunshinedev.endercubecmw.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.ScoreboardManager;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.sunshinedev.endercubecmw.api.CMWMobCustom.TypeMob;
import fr.sunshinedev.endercubecmw.api.CMWRound.MobRound;
import fr.sunshinedev.endercubecmw.commands.ECMWExecutor;
import fr.sunshinedev.endercubecmw.commands.executors.IExecutor;
import fr.sunshinedev.endercubecmw.listeners.GameListener;
import fr.sunshinedev.endercubecmw.listeners.MobsListeners;
import net.md_5.bungee.api.ChatColor;

public final class EnderCubeCMW extends JavaPlugin {

    public static EnderCubeCMW INSTANCE;
    public Map<String, CMWGame> gamesPresetList = new HashMap<String, CMWGame>();
    public ArrayList<CMWKit> kitsList = new ArrayList<CMWKit>();
    public ArrayList<CMWMobCustom> mobsList = new ArrayList<CMWMobCustom>();
    public ArrayList<CMWArena> arenasList = new ArrayList<CMWArena>();
    public ArrayList<CMWRound> roundsList = new ArrayList<CMWRound>();
    public ArrayList<CMWTeam> teamsConfigList = new ArrayList<CMWTeam>();
    public ArrayList<String> cooldownPowerItem = new ArrayList<>();
    public CMWGame game = null;
    private final List<IExecutor> mapCommandECBExecutor = new ArrayList<>();
    public String prefixChat = ChatColor.AQUA + "[" + ChatColor.BLUE + "EnderCube Arena Event" + ChatColor.AQUA + "] ";

    public File kitsFile = null;
    public File mobsFile = null;
    public File arenasFile = null;
    public File roundsFile = null;
    public File teamsFile = null;

    public ScoreboardManager scoreBoardManager;

    @Override
    public void onEnable() {
        INSTANCE = this;
        kitsFile = new File(getDataFolder(), "kits.json");
        mobsFile = new File(getDataFolder(), "mobs.json");
        arenasFile = new File(getDataFolder(), "arenas.json");
        roundsFile = new File(getDataFolder(), "rounds.json");
        teamsFile = new File(getDataFolder(), "teams.json");

        if (!getDataFolder().exists())
            getDataFolder().mkdirs();

        createFileJArray(kitsFile);
        createFileJArray(mobsFile);
        createFileJArray(arenasFile);
        createFileJArray(roundsFile);
        createFileJArray(teamsFile);

        initPlugin(false);
    }

    public void createFileJArray(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();

                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(new JSONArray().toString());
                fileWriter.close();

            } catch (IOException e) {
                Bukkit.getPluginManager().disablePlugin(this);
                throw new RuntimeException(e);
            }
        }
    }

    public void initPlugin(boolean reload) {
        ECMWRegisterExecutor.register();

        if (!reload) {
            Objects.requireNonNull(this.getCommand("cmw")).setExecutor(new ECMWExecutor());
            Bukkit.getPluginManager().registerEvents(new GameListener(), INSTANCE);
            Bukkit.getPluginManager().registerEvents(new MobsListeners(), INSTANCE);
            scoreBoardManager = Bukkit.getScoreboardManager();
        }

        reloadKits();
        reloadMobs();
        reloadArenas();
        reloadRounds();
        reloadTeams();
    }

    public void reloadKits() {
        kitsList.clear();
        JSONArray kits = KitsManager.configKit();
        for (int i = 0; i < kits.length(); i++) {
            JSONObject kit = (JSONObject) kits.get(i);
            CMWKit newKit = new CMWKit(kit.getString("name"));
            newKit.setId(UUID.fromString(kit.getString("id")));
            newKit.setInventory(kit.getJSONArray("inventory"));
            kitsList.add(newKit);
        }
        getLogger().info(kitsList.size() + " kits loaded.");
    }

    public void reloadMobs() {
        mobsList.clear();
        JSONArray mobs = MobsManager.configMobs();
        for (int i = 0; i < mobs.length(); i++) {
            JSONObject mobJson = (JSONObject) mobs.get(i);

            CMWMobCustom newMob = new CMWMobCustom(mobJson.getString("displayName"));
            newMob.setId(UUID.fromString(mobJson.getString("id")));
            newMob.setType(TypeMob.valueOf(mobJson.getString("type")));
            switch (newMob.getType()) {
                case BASIC:
                    JSONArray effectsMob = mobJson.getJSONArray("effects");
                    newMob.setPoints(mobJson.getInt("points"));
                    newMob.setHealth(mobJson.getDouble("health"));
                    newMob.setArmorInventory(mobJson.getJSONArray("armorInventory"));
                    newMob.setEntity(EntityType.fromName(mobJson.getString("entity")));
                    if(mobJson.has("age")) {
                        newMob.setAgeMob(CMWMobCustom.Age.valueOf(mobJson.getString("age")));
                    }
                    // Load Mobs Effect
                    for (int j = 0; j < effectsMob.length(); j++) {
                        JSONObject effectMob = effectsMob.getJSONObject(j);
                        newMob.getEffects().put(PotionEffectType.getByName(effectMob.getString("name")),
                                effectMob.getInt("amplifier"));
                    }
                    break;
                case ASSEMBLY:
                    newMob.setBelowMob(UUID.fromString(mobJson.getString("belowEntityID")));
                    newMob.setAboveMob(UUID.fromString(mobJson.getString("aboveEntityID")));
                    break;
                default:
                    break;
            }


            mobsList.add(newMob);
        }
        getLogger().info(mobsList.size() + " mobs loaded.");
    }

    public void reloadArenas() {
        arenasList.clear();
        JSONArray arenas = ArenaManager.config();
        for (int i = 0; i < arenas.length(); i++) {
            JSONObject arenaJson = (JSONObject) arenas.get(i);
            JSONArray sml = arenaJson.getJSONArray("spawnMonstersLocations");
            JSONObject playersSpawnJson = arenaJson.getJSONObject("playersSpawn");
            CMWArena newArena = new CMWArena(arenaJson.getString("name"));
            World worldArena = Bukkit.getWorld(arenaJson.getString("world"));
            newArena.setWorld(worldArena);
            newArena.setRegion(WorldGuardManager.getRegion(worldArena, arenaJson.getString("region")));
            if (!playersSpawnJson.toString().equalsIgnoreCase(new JSONObject().toString())) {
                newArena.setPlayersSpawn(new Location(worldArena, playersSpawnJson.getFloat("x"),
                        playersSpawnJson.getFloat("y"), playersSpawnJson.getFloat("z")));
            }

            // Load Mobs Spawn Locations
            for (int j = 0; j < sml.length(); j++) {
                JSONObject locMob = sml.getJSONObject(j);
                newArena.getMobsLoc().add(new Location(Bukkit.getWorld(arenaJson.getString("world")),
                        locMob.getFloat("x"), locMob.getFloat("y"), locMob.getFloat("z")));
            }

            arenasList.add(newArena);
        }
        getLogger().info(arenasList.size() + " arena loaded.");
    }

    public void reloadRounds() {
        roundsList.clear();
        JSONArray rounds = RoundsManager.config();
        for (int i = 0; i < rounds.length(); i++) {
            JSONObject roundJson = (JSONObject) rounds.get(i);
            JSONArray rmobs = roundJson.getJSONArray("mobs");
            CMWRound newRound = new CMWRound(roundJson.getString("name"));
            newRound.setId(UUID.fromString(roundJson.getString("id")));

            for (int j = 0; j < rmobs.length(); j++) {
                JSONObject roundMob = rmobs.getJSONObject(j);
                newRound.getMobsRound().add(new MobRound(UUID.fromString(roundMob.getString("id")), roundMob.getInt("number")));
            }

            roundsList.add(newRound);
        }
        getLogger().info(roundsList.size() + " rounds loaded.");
    }


    public void reloadTeams() {
        teamsConfigList.clear();
        JSONArray teams = TeamsManager.config();
        for (int i = 0; i < teams.length(); i++) {
            JSONObject teamJson = (JSONObject) teams.get(i);
            CMWTeam team = new CMWTeam(teamJson.getString("name"));
            team.setId(UUID.fromString(teamJson.getString("id")));
            if (!teamJson.getString("kit").isEmpty()) {
                Optional<CMWKit> kit = KitsManager.getKitFromID(UUID.fromString(teamJson.getString("kit")));
                team.attributeKit(kit.isPresent() ? kit.get() : null);
            }
            team.setColor(teamJson.getString("color"));
            teamsConfigList.add(team);
        }
        getLogger().info(teamsConfigList.size() + " teams loaded.");
    }

    @Override
    public void onDisable() {

    }

    public List<IExecutor> getMapCommandECMWExecutor() {
        return mapCommandECBExecutor;
    }

    public List<String> getMapCommandECMWAction() {
        return mapCommandECBExecutor.stream().map(IExecutor::getName).collect(Collectors.toList());
    }

    public ArrayList<CMWKit> getKits() {
        return kitsList;
    }

    public ArrayList<CMWMobCustom> getMobsCustom() {
        return mobsList;
    }

    public ArrayList<CMWArena> getArenas() {
        return arenasList;
    }

    public ArrayList<CMWRound> getRounds() {
        return roundsList;
    }

    public CMWGame getGame() {
        return game;
    }

    public void setGame(CMWGame ngame) {
        game = ngame;
    }

    public ScoreboardManager getScoreBoardManager() {
        return scoreBoardManager;
    }

    public String getPrefixChat() {
        return prefixChat;
    }

    public Map<String, CMWGame> getGamesPresetList() {
        return gamesPresetList;
    }

    public ArrayList<CMWTeam> getTeamsConfigList() {
        return teamsConfigList;
    }

    public ArrayList<String> getCooldownPowerItem() {
        return cooldownPowerItem;
    }
}
