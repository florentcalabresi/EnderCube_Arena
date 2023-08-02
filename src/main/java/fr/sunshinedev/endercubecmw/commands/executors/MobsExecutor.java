package fr.sunshinedev.endercubecmw.commands.executors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.Utils;
import fr.sunshinedev.endercubecmw.api.CMWArena;
import fr.sunshinedev.endercubecmw.api.CMWKit;
import fr.sunshinedev.endercubecmw.api.CMWMobCustom;
import fr.sunshinedev.endercubecmw.api.CMWMobCustom.TypeMob;
import fr.sunshinedev.endercubecmw.gui.KitGui;
import fr.sunshinedev.endercubecmw.gui.MobGui;
import fr.sunshinedev.endercubecmw.managers.ArenaManager;
import fr.sunshinedev.endercubecmw.managers.KitsManager;
import fr.sunshinedev.endercubecmw.managers.MobsManager;

public class MobsExecutor extends IExecutor {

	public MobsExecutor() {
		super("mobs", "ecmw.admin.mobs", new String[] {});
	}

	public MobsExecutor(String name, String permission, String[] aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void run(CommandExecutor command, Player playerSender, String[] args) {
		super.run(command, playerSender, args);

		if (args.length > 0) {
			String action = args[0];
			if (action.equalsIgnoreCase("create")) {
				if (args.length > 2) {
					String name = args[1];
					String entityName = args[2];

					if(MobsManager.getMobFromName(name).isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Un mob porte déjà ce nom.");
						return;
					}
					
					if(EntityType.fromName(entityName) == null) {
						Utils.sendPlayerMessageError(playerSender, "L'entité %s n'existe pas.".formatted(entityName));
						return;
					}
					
					new MobGui(name, EntityType.fromName(entityName), playerSender);
				}else {
					Utils.sendPlayerMessageError(playerSender, "/cmw mobs create <name> <entityType>");
                }
			} else if (action.equalsIgnoreCase("summon")) {
				if (args.length > 1) {
					String name = args[1];
					Optional<CMWMobCustom> mobCustom = MobsManager.getMobFromName(name);
					if (!mobCustom.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'existe pas");
						return;
					}

					CMWMobCustom mobTarget = mobCustom.get();

					if(mobTarget.getType() == TypeMob.BASIC)
						MobsManager.summonMob(playerSender.getLocation(), mobTarget);
					else if(mobTarget.getType() == TypeMob.ASSEMBLY)
						MobsManager.rideMobs(playerSender.getLocation(), mobTarget);
					
					Utils.sendPlayerMessageSuccess(playerSender, "Le mob %s a été summon.".formatted(mobTarget.getDisplayName()));
				}else {
					Utils.sendPlayerMessageError(playerSender, "/cmw mobs summon <name>");
                }
			} else if (action.equalsIgnoreCase("setPoints")) {
				if (args.length == 3) {
					String name = args[1];
					String points = args[2];

					Optional<CMWMobCustom> optMobs = MobsManager.getMobFromName(name);
					if (!optMobs.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'existe pas");
						return;
					}

					if (!Utils.checkStringIfInt(points)) {
						Utils.sendPlayerMessageError(playerSender, "L'argument des points n'est pas un nombre valide.");
						return;
					}

					if(optMobs.get().getType() != TypeMob.BASIC) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'est pas de type basic. Impossible d'éditer son inventaire.");
						return;
					}

					MobsManager.setPoints(optMobs.get(), Integer.parseInt(points));

					Utils.sendPlayerMessageSuccess(playerSender,"Les points du mob " + name + " sont bien sauvegardés.");

				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw mobs setPoints <name> <points>");
				}
			} else if (action.equalsIgnoreCase("setAge")) {
				if (args.length == 3) {
					String name = args[1];
					String age = args[2];

					Optional<CMWMobCustom> optMobs = MobsManager.getMobFromName(name);
					if (!optMobs.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'existe pas");
						return;
					}

					if(optMobs.get().getType() != TypeMob.BASIC) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'est pas de type basic. Impossible d'éditer son inventaire.");
						return;
					}
					if(Arrays.stream(CMWMobCustom.Age.values()).noneMatch(ageCheck -> ageCheck.getName().equalsIgnoreCase(age))) {
						Utils.sendPlayerMessageError(playerSender, "Le type d'âge du mob n'est pas valide.");
						return;
					}

					MobsManager.setAge(optMobs.get(), CMWMobCustom.Age.valueOf(age));

					Utils.sendPlayerMessageSuccess(playerSender,"L'âge du mob " + name + " a bien été sauvegardés.");

				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw mobs setAge <name> <points>");
				}
			} else if (action.equalsIgnoreCase("summonarena")) {
				if (args.length > 1) {
					String nameMob = args[1];
					String nameArena = args[2];
					Optional<CMWMobCustom> mobCustom = MobsManager.getMobFromName(nameMob);
					Optional<CMWArena> arenaOpt = ArenaManager.getArena(nameArena);
					if (!mobCustom.isPresent()) {
						Utils.sendPlayerMessageError(playerSender,"Ce mob n'existe pas");
						return;
					}

					if (!arenaOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Cette arène n'existe pas");
						return;
					}

					CMWArena arena = arenaOpt.get();

					CMWMobCustom mobTarget = mobCustom.get();

					for (Location loc : arena.getMobsLoc())
						System.out.println(loc);

					for (int i = 0; i < 10; i++) {

						int index = new Random().nextInt(arena.getMobsLoc().size());
						if(mobTarget.getType() == TypeMob.BASIC)
							MobsManager.summonMob(arena.getMobsLoc().get(index), mobTarget);
						else if(mobTarget.getType() == TypeMob.ASSEMBLY)
							MobsManager.rideMobs(arena.getMobsLoc().get(index), mobTarget);
					}

					Utils.sendPlayerMessageSuccess(playerSender, "Le mob %s a été summon dans les localisations preset de l'arène %s"
							.formatted(mobTarget.getDisplayName(), arena.getName()));
				}else {
					Utils.sendPlayerMessageError(playerSender, "/cmw mobs summonarena <name> <arena>");
				}
			} else if (action.equalsIgnoreCase("addEffect")) {
				if (args.length == 4) {
					String name = args[1];
					String effect = args[2];
					String amplif = args[3];

					Optional<CMWMobCustom> optMobs = MobsManager.getMobFromName(name);
					if (!optMobs.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'existe pas");
						return;
					}
					PotionEffectType eff = PotionEffectType.getByName(effect);
					if (eff == null) {
						Utils.sendPlayerMessageError(playerSender, "L'effet %s ne semble pas exister".formatted(effect));
						return;
					}
					CMWMobCustom mob = optMobs.get();
					if(mob.getEffects().containsKey(eff)) {
						Utils.sendPlayerMessageError(playerSender, "L'effet %s est déjà appliqué au mob.".formatted(eff.getName()));
						return;
					}
					if(mob.getType() != TypeMob.BASIC) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'est pas de type basic. Impossible d'éditer son inventaire.");
						return;
					}
					if (!Utils.checkStringIfInt(amplif)) {
						Utils.sendPlayerMessageError(playerSender, "L'argument amplifier n'est pas un nombre valide.");
						return;
					}
					int amplifNumber = Integer.valueOf(amplif);
					if (amplifNumber > 255 || amplifNumber < 0) {
						Utils.sendPlayerMessageError(playerSender, "L'argument amplifier doit être plus grand que 0 et moins grand que 255");
						return;
					}
					
					MobsManager.addEffect(mob, eff, amplifNumber);
					Utils.sendPlayerMessageSuccess(playerSender,"L'effet %s a été appliqué au mob %s".formatted(eff.getName(), mob.getDisplayName()));

				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw mobs addEffect <name> <effect> <amplifier>");
				}
			} else if (action.equalsIgnoreCase("removeEffect")) {
				if (args.length == 3) {
					String name = args[1];
					String effect = args[2];

					Optional<CMWMobCustom> optMobs = MobsManager.getMobFromName(name);
					if (!optMobs.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'existe pas");
						return;
					}
					PotionEffectType eff = PotionEffectType.getByName(effect);
					if (eff == null) {
						Utils.sendPlayerMessageError(playerSender, "L'effet %s ne semble pas exister".formatted(effect));
						return;
					}
					CMWMobCustom mob = optMobs.get();
					if(!mob.getEffects().containsKey(eff)) {
						Utils.sendPlayerMessageError(playerSender, "L'effet %s n'est pas appliqué au mob.".formatted(eff.getName()));
						return;
					}
					if(mob.getType() != TypeMob.BASIC) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'est pas de type basic. Impossible d'éditer son inventaire.");
						return;
					}

					
					MobsManager.removeEffect(mob, eff);
					Utils.sendPlayerMessageSuccess(playerSender,"L'effet %s a été supprimé au mob %s".formatted(eff.getName(), mob.getDisplayName()));

				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw mobs removeEffect <name> <effect>");
				}
			}  else if (action.equalsIgnoreCase("setHealth")) {
				if (args.length == 3) {
					String name = args[1];
					String health = args[2];

					Optional<CMWMobCustom> optMobs = MobsManager.getMobFromName(name);
					if (!optMobs.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'existe pas");
						return;
					}

					if(optMobs.get().getType() != TypeMob.BASIC) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'est pas de type basic. Impossible d'éditer son inventaire.");
						return;
					}
					
					if (!Utils.checkStringIfDouble(health)) {
						Utils.sendPlayerMessageError(playerSender, "L'argument de la vie n'est pas un nombre valide.");
						return;
					}
					
					double healthInt = Double.parseDouble(health);
					
					if (healthInt < 1) {
						Utils.sendPlayerMessageError(playerSender, "L'argument de la vie doit être supérieur à 0.");
						return;
					}
					
					MobsManager.setHealth(optMobs.get(), healthInt);
					
					Utils.sendPlayerMessageSuccess(playerSender,"La vie du mob " + name + " a été sauvegardés.");

				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw mobs setHealth <name> <points>");
				}
			} else if(action.equalsIgnoreCase("editInv")) {
                if(args.length > 1) {
                    String name = args[1];

                    Optional<CMWMobCustom> mobOpt = MobsManager.getMobFromName(name);
                    if(!mobOpt.isPresent()) {
                    	Utils.sendPlayerMessageError(playerSender, "Le mob n'existe pas :'(");
                        return;
                    }
                    
                    CMWMobCustom mob = mobOpt.get();
                    
                    if(mob.getType() != TypeMob.BASIC) {
                    	Utils.sendPlayerMessageError(playerSender, "Ce mob n'est pas de type basic. Impossible d'éditer son inventaire.");
                        return;
                    }
                    
                    new MobGui(mob, playerSender);
                }
            } else if (action.equalsIgnoreCase("ride")) {
            	if (args.length > 2) {
					String name = args[1];
					String mobBelow = args[2];
					String mobAbove = args[3];
					
					Optional<CMWMobCustom> mobBelowOpt = MobsManager.getMobFromName(mobBelow);
                    Optional<CMWMobCustom> mobAboveOpt = MobsManager.getMobFromName(mobAbove);
                    if(!mobBelowOpt.isPresent()) {
                    	Utils.sendPlayerMessageError(playerSender, "Le mob %s n'existe pas :'(".formatted(mobBelow));
                        return;
                    }
                    if(!mobAboveOpt.isPresent()) {
                    	Utils.sendPlayerMessageError(playerSender, "Le mob %s n'existe pas :'(".formatted(mobAbove));
                        return;
                    }
                    
                    CMWMobCustom mobBelowObj = mobBelowOpt.get();
                    CMWMobCustom mobAboveObj = mobAboveOpt.get();
                    
                    if(mobBelowObj.getType() != TypeMob.BASIC || mobAboveObj.getType() != TypeMob.BASIC) {
                    	Utils.sendPlayerMessageError(playerSender, "Les deux mobs doivent être de type basic pour créer une monture.");
                        return;
                    }
                    
                    CMWMobCustom mobCustom = new CMWMobCustom(name);
                    mobCustom.setId(UUID.randomUUID());
                    mobCustom.setType(TypeMob.ASSEMBLY);
                    mobCustom.setBelowMob(mobBelowOpt.get().getId());
                    mobCustom.setAboveMob(mobAboveOpt.get().getId());
                    
                    MobsManager.saveMob(mobCustom, null, false);
                    
                    Utils.sendPlayerMessageSuccess(playerSender, "Le mob monture a bien été créé");
				}else {
					Utils.sendPlayerMessageError(playerSender, "/cmw mobs ride <name> <mobBelow> <mobAbove>");
                }
            } else if (action.equalsIgnoreCase("remove")) {
				if (args.length > 1) {
					String name = args[1];

					Optional<CMWMobCustom> mobOpt = MobsManager.getMobFromName(name);
					if(!mobOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Le mob %s n'existe pas :'(".formatted(name));
						return;
					}

					MobsManager.removeMob(mobOpt.get());

					Utils.sendPlayerMessageSuccess(playerSender, "Le mob a bien été supprimé");
				}else {
					Utils.sendPlayerMessageError(playerSender, "/cmw mobs remove <name>");
				}
			}
		}
	}

	@Override
	public @Nullable List<String> tabCompleter(@NotNull CommandSender sender, @NotNull Command cmd,
			@NotNull String alias, @NotNull String[] args) {
		if (args.length == 1)
			return Arrays.asList("create", "setPoints", "editInv", "summon", "summonarena", "addEffect", "remove", "removeEffect", "setHealth", "ride", "setAge").stream().filter(s -> s.startsWith(args[0])).toList();
		if (args[0].equalsIgnoreCase("create")) {
			if (args.length == 3) {
				return Arrays.asList(EntityType.values()).stream().map(EntityType::name).filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
			}

			return null;
		}else if (args[0].equalsIgnoreCase("summon")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getMobsCustom().stream().map(CMWMobCustom::getDisplayName)
						.filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		}else if (args[0].equalsIgnoreCase("editInv")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getMobsCustom().stream().filter(mob -> mob.getType() == TypeMob.BASIC).map(CMWMobCustom::getDisplayName)
						.filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		} else if (args[0].equalsIgnoreCase("summonarena")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getMobsCustom().stream().map(CMWMobCustom::getDisplayName)
						.filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return EnderCubeCMW.INSTANCE.getArenas().stream().map(CMWArena::getName).filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
			} else if (args.length == 4) {
				return null;
			}
		} else if (args[0].equalsIgnoreCase("setPoints")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getMobsCustom().stream().filter(mob -> mob.getType() == TypeMob.BASIC).map(CMWMobCustom::getDisplayName)
						.filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			}
			return null;
		} else if (args[0].equalsIgnoreCase("addEffect")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getMobsCustom().stream().filter(mob -> mob.getType() == TypeMob.BASIC).map(CMWMobCustom::getDisplayName)
						.filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return Arrays.asList(PotionEffectType.values()).stream().map(PotionEffectType::getName).filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
			}
			return null;
		} else if (args[0].equalsIgnoreCase("removeEffect")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getMobsCustom().stream().filter(mob -> mob.getType() == TypeMob.BASIC).map(CMWMobCustom::getDisplayName)
						.filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return Arrays.asList(PotionEffectType.values()).stream().map(PotionEffectType::getName).filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
			}
			return null;
		} else if (args[0].equalsIgnoreCase("setHealth")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getMobsCustom().stream().filter(mob -> mob.getType() == TypeMob.BASIC).map(CMWMobCustom::getDisplayName)
						.filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			}
			return null;
		} else if (args[0].equalsIgnoreCase("setAge")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getMobsCustom().stream().filter(mob -> mob.getType() == TypeMob.BASIC).map(CMWMobCustom::getDisplayName)
						.filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			}else if (args.length == 3) {
				return Arrays.stream(CMWMobCustom.Age.values()).map(CMWMobCustom.Age::getName).filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
			}
			return null;
		}else if (args[0].equalsIgnoreCase("ride")) {
			if (args.length == 3 || args.length == 4)
				return EnderCubeCMW.INSTANCE.getMobsCustom().stream().filter(mob -> mob.getType() == TypeMob.BASIC).map(CMWMobCustom::getDisplayName)
						.filter(s -> s.startsWith(args[args.length == 3 ? 2 : 3])).collect(Collectors.toList());
			
			return null;
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length == 2)
				return EnderCubeCMW.INSTANCE.getMobsCustom().stream().map(CMWMobCustom::getDisplayName)
						.filter(s -> s.startsWith(args[1])).collect(Collectors.toList());

			return null;
		}

		return List.of("");
	}

}
