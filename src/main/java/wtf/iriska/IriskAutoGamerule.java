package wtf.iriska;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.ArrayList;

public class IriskAutoGamerule extends JavaPlugin {

    private List<String> specialWorlds;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        getLogger().info("IriskAutoGamerule запущен! Проверяем геймрулы...");

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, this::checkAndDisableGamerules, 20L);
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();

        // Настройки по умолчанию
        List<String> defaultWorlds = new ArrayList<>();
        defaultWorlds.add("spawn");
        defaultWorlds.add("boss");
        defaultWorlds.add("duel1");
        defaultWorlds.add("duel2");
        defaultWorlds.add("duel3");
        defaultWorlds.add("duel4");
        defaultWorlds.add("osada");

        config.addDefault("special-worlds", defaultWorlds);
        config.options().copyDefaults(true);
        saveConfig();

        specialWorlds = config.getStringList("special-worlds");
    }

    private void checkAndDisableGamerules() {
        for (World world : Bukkit.getWorlds()) {
            // Общие геймрулы для ВСЕХ миров
            checkAndDisableGamerule(world, GameRule.MOB_GRIEFING, false);
            checkAndDisableGamerule(world, GameRule.DO_FIRE_TICK, false);
            checkAndDisableGamerule(world, GameRule.ANNOUNCE_ADVANCEMENTS, false);
            checkAndDisableGamerule(world, GameRule.SHOW_DEATH_MESSAGES, false);
            checkAndDisableGamerule(world, GameRule.DO_INSOMNIA, false);
            checkAndDisableGamerule(world, GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
            checkAndDisableGamerule(world, GameRule.DO_IMMEDIATE_RESPAWN, true); // Моментальный респавн

            // Проверяем, является ли мир специальным
            boolean isSpecialWorld = specialWorlds.stream()
                    .anyMatch(worldName -> world.getName().equalsIgnoreCase(worldName));

            if (isSpecialWorld) {
                applySpecialWorldRules(world);
            }
        }

        getLogger().info("Проверка геймрулов завершена!");
    }

    private void applySpecialWorldRules(World world) {
        // Отключаем урон
        checkAndDisableGamerule(world, GameRule.DROWNING_DAMAGE, false);
        checkAndDisableGamerule(world, GameRule.FALL_DAMAGE, false);
        checkAndDisableGamerule(world, GameRule.FIRE_DAMAGE, false);

        // Отключаем спавн мобов
        checkAndDisableGamerule(world, GameRule.DO_MOB_SPAWNING, false);
        checkAndDisableGamerule(world, GameRule.DO_PATROL_SPAWNING, false);
        checkAndDisableGamerule(world, GameRule.DO_TRADER_SPAWNING, false);

        // Отключаем физику и циклы мира
        checkAndDisableGamerule(world, GameRule.RANDOM_TICK_SPEED, 0);
        checkAndDisableGamerule(world, GameRule.DO_DAYLIGHT_CYCLE, false);
        checkAndDisableGamerule(world, GameRule.DO_WEATHER_CYCLE, false);
    }

    private <T> void checkAndDisableGamerule(World world, GameRule<T> gamerule, T desiredValue) {
        try {
            T currentValue = world.getGameRuleValue(gamerule);

            if (currentValue != null && !currentValue.equals(desiredValue)) {
                world.setGameRule(gamerule, desiredValue);
                getLogger().info(String.format("Изменен геймрул %s в мире %s с %s на %s",
                        gamerule.getName(),
                        world.getName(),
                        currentValue,
                        desiredValue));
            }
        } catch (Exception e) {
            getLogger().warning("Не удалось изменить геймрул " + gamerule.getName() +
                    " для мира " + world.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("IriskAutoGamerule выключен");
    }
}
