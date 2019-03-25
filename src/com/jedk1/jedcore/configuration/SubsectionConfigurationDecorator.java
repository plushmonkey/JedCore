package com.jedk1.jedcore.configuration;

import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;

// ConfigurationSection decorator that tries to fetch with a prefix prepended to the path.
// If that section doesn't exist, then it falls back to the parent.
public class SubsectionConfigurationDecorator implements ConfigurationSection {
    private ConfigurationSection parent;
    private String prefix;

    public SubsectionConfigurationDecorator(ConfigurationSection parent, String prefix) {
        this.parent = parent;
        this.prefix = prefix;

        if (!this.prefix.endsWith(".")) {
            this.prefix += ".";
        }
    }

    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public Set<String> getKeys(boolean deep) {
        return parent.getKeys(deep);
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        return parent.getValues(deep);
    }

    @Override
    public boolean contains(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return true;
        }

        return parent.contains(path);
    }

    @Override
    public boolean contains(String path, boolean ignoreDefault) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath, ignoreDefault)) {
            return true;
        }

        return parent.contains(path, ignoreDefault);
    }

    @Override
    public boolean isSet(String path) {
        String newPath = getPrefix() + path;

        if (parent.isSet(newPath)) {
            return true;
        }

        return parent.isSet(path);
    }

    @Override
    public String getCurrentPath() {
        return parent.getCurrentPath();
    }

    @Override
    public String getName() {
        return parent.getName();
    }

    @Override
    public Configuration getRoot() {
        return parent.getRoot();
    }

    @Override
    public ConfigurationSection getParent() {
        return parent.getParent();
    }

    @Override
    public Object get(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.get(newPath);
        }

        return parent.get(path);
    }

    @Override
    public Object get(String path, Object def) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.get(newPath, def);
        }

        return parent.get(path, def);
    }

    @Override
    public void set(String path, Object value) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            parent.set(newPath, value);
            return;
        }

        parent.set(path, value);
    }

    @Override
    public ConfigurationSection createSection(String path) {
        return parent.createSection(getPrefix() + path);
    }

    @Override
    public ConfigurationSection createSection(String path, Map<?, ?> map) {
        return parent.createSection(getPrefix() + path, map);
    }

    @Override
    public String getString(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getString(newPath);
        }

        return parent.getString(path);
    }

    @Override
    public String getString(String path, String def) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getString(newPath, def);
        }

        return parent.getString(path, def);
    }

    @Override
    public boolean isString(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.isString(newPath);
        }

        return parent.isString(path);
    }

    @Override
    public int getInt(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getInt(newPath);
        }

        return parent.getInt(path);
    }

    @Override
    public int getInt(String path, int def) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getInt(newPath, def);
        }

        return parent.getInt(path, def);
    }

    @Override
    public boolean isInt(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.isInt(newPath);
        }

        return parent.isInt(path);
    }

    @Override
    public boolean getBoolean(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getBoolean(newPath);
        }

        return parent.getBoolean(path);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getBoolean(newPath, def);
        }

        return parent.getBoolean(path, def);
    }

    @Override
    public boolean isBoolean(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.isBoolean(newPath);
        }

        return parent.isBoolean(path);
    }

    @Override
    public double getDouble(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getDouble(newPath);
        }

        return parent.getDouble(path);
    }

    @Override
    public double getDouble(String path, double def) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getDouble(newPath, def);
        }

        return parent.getDouble(path, def);
    }

    @Override
    public boolean isDouble(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.isDouble(newPath);
        }

        return parent.isDouble(path);
    }

    @Override
    public long getLong(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getLong(newPath);
        }

        return parent.getLong(path);
    }

    @Override
    public long getLong(String path, long def) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getLong(newPath, def);
        }

        return parent.getLong(path, def);
    }

    @Override
    public boolean isLong(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.isLong(newPath);
        }

        return parent.isLong(path);
    }

    @Override
    public List<?> getList(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getList(newPath);
        }

        return parent.getList(path);
    }

    @Override
    public List<?> getList(String path, List<?> def) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getList(newPath, def);
        }

        return parent.getList(path, def);
    }

    @Override
    public boolean isList(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.isList(newPath);
        }

        return parent.isList(path);
    }

    @Override
    public List<String> getStringList(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getStringList(newPath);
        }

        return parent.getStringList(path);
    }

    @Override
    public List<Integer> getIntegerList(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getIntegerList(newPath);
        }

        return parent.getIntegerList(path);
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getBooleanList(newPath);
        }

        return parent.getBooleanList(path);
    }

    @Override
    public List<Double> getDoubleList(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getDoubleList(newPath);
        }

        return parent.getDoubleList(path);
    }

    @Override
    public List<Float> getFloatList(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getFloatList(newPath);
        }

        return parent.getFloatList(path);
    }

    @Override
    public List<Long> getLongList(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getLongList(newPath);
        }

        return parent.getLongList(path);
    }

    @Override
    public List<Byte> getByteList(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getByteList(newPath);
        }

        return parent.getByteList(path);
    }

    @Override
    public List<Character> getCharacterList(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getCharacterList(newPath);
        }

        return parent.getCharacterList(path);
    }

    @Override
    public List<Short> getShortList(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getShortList(newPath);
        }

        return parent.getShortList(path);
    }

    @Override
    public List<Map<?, ?>> getMapList(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getMapList(newPath);
        }

        return parent.getMapList(path);
    }

    @Override
    public <T extends ConfigurationSerializable> T getSerializable(String s, Class<T> aClass) {
        return parent.getSerializable(s, aClass);
    }

    @Override
    public <T extends ConfigurationSerializable> T getSerializable(String s, Class<T> aClass, T t) {
        return parent.getSerializable(s, aClass, t);
    }

    @Override
    public <T extends Object> T getObject(String s, Class<T> aClass) {
        return parent.getObject(s, aClass);
    }

    @Override
    public <T extends Object> T getObject(String s, Class<T> aClass, T t) {
        return parent.getObject(s, aClass, t);
    }

    @Override
    public Vector getVector(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getVector(newPath);
        }

        return parent.getVector(path);
    }

    @Override
    public Vector getVector(String path, Vector def) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getVector(newPath, def);
        }

        return parent.getVector(path, def);
    }

    @Override
    public boolean isVector(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.isVector(newPath);
        }

        return parent.isVector(path);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getOfflinePlayer(newPath);
        }

        return parent.getOfflinePlayer(path);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String path, OfflinePlayer def) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getOfflinePlayer(newPath, def);
        }

        return parent.getOfflinePlayer(path, def);
    }

    @Override
    public boolean isOfflinePlayer(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.isOfflinePlayer(newPath);
        }

        return parent.isOfflinePlayer(path);
    }

    @Override
    public ItemStack getItemStack(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getItemStack(newPath);
        }

        return parent.getItemStack(path);
    }

    @Override
    public ItemStack getItemStack(String path, ItemStack def) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getItemStack(newPath, def);
        }

        return parent.getItemStack(path, def);
    }

    @Override
    public boolean isItemStack(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.isItemStack(newPath);
        }

        return parent.isItemStack(path);
    }

    @Override
    public Color getColor(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getColor(newPath);
        }

        return parent.getColor(path);
    }

    @Override
    public Color getColor(String path, Color def) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getColor(newPath, def);
        }

        return parent.getColor(path, def);
    }

    @Override
    public boolean isColor(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.isColor(newPath);
        }

        return parent.isColor(path);
    }

    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.getConfigurationSection(newPath);
        }

        return parent.getConfigurationSection(path);
    }

    @Override
    public boolean isConfigurationSection(String path) {
        String newPath = getPrefix() + path;

        if (parent.contains(newPath)) {
            return parent.isConfigurationSection(newPath);
        }

        return parent.isConfigurationSection(path);
    }

    @Override
    public ConfigurationSection getDefaultSection() {
        return parent.getDefaultSection();
    }

    @Override
    public void addDefault(String path, Object value) {
        parent.addDefault(path, value);
    }
}
