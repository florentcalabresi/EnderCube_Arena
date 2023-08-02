package fr.sunshinedev.endercubecmw.api;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.commands.executors.*;

public class ECMWRegisterExecutor {

    public static void register() {
        EnderCubeCMW.INSTANCE.getMapCommandECMWExecutor().add(new TeamExecutor());
        EnderCubeCMW.INSTANCE.getMapCommandECMWExecutor().add(new KitsExecutor());
        EnderCubeCMW.INSTANCE.getMapCommandECMWExecutor().add(new GameExecutor());
        EnderCubeCMW.INSTANCE.getMapCommandECMWExecutor().add(new MobsExecutor());
        EnderCubeCMW.INSTANCE.getMapCommandECMWExecutor().add(new ArenaExecutor());
        EnderCubeCMW.INSTANCE.getMapCommandECMWExecutor().add(new RoundExecutor());
        EnderCubeCMW.INSTANCE.getMapCommandECMWExecutor().add(new GamePresetExecutor());
        EnderCubeCMW.INSTANCE.getMapCommandECMWExecutor().add(new ReloadExecutor());
    }
}
