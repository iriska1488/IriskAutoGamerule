package wtf.iriska;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class IriskAutoGamerule extends JavaPlugin {

    private String spawnWorldName;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        spawnWorldName = getConfig().getString("spawn-world", "spawn");

        getLogger().info("IriskAutoGamerule запущен! Проверяем геймрулы...");
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, this::checkAndDisableGamerules, 20L);
    }

    private void checkAndDisableGamerules() {
        for (World world : Bukkit.getWorlds()) {
            // Общие геймрулы для всех миров
            checkAndDisableGamerule(world, GameRule.MOB_GRIEFING, false);
            checkAndDisableGamerule(world, GameRule.DO_FIRE_TICK, false);
            checkAndDisableGamerule(world, GameRule.ANNOUNCE_ADVANCEMENTS, false);
            checkAndDisableGamerule(world, GameRule.SHOW_DEATH_MESSAGES, false);

            // Специальные настройки для мира спавна
            if (world.getName().equalsIgnoreCase(spawnWorldName)) {
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
        }
        
        getLogger().info("Проверка геймрулов завершена!");
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
