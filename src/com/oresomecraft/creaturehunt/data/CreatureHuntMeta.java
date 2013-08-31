package com.oresomecraft.creaturehunt.data;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import com.oresomecraft.creaturehunt.CreatureHunt;

public class CreatureHuntMeta implements MetadataValue {

    private boolean value;
    
    public CreatureHuntMeta(boolean value) {
        this.value = value;
    }
    
    @Override
    public boolean asBoolean() {
        return value;
    }

    @Override
    public byte asByte() {
        if (value) {
            return 1;
        }
        return 0;
    }

    @Override
    public double asDouble() {
        if (value) {
            return 1;
        }
        return 0;
    }

    @Override
    public float asFloat() {
        if (value) {
            return 1;
        }
        return 0;
    }

    @Override
    public int asInt() {
        if (value) {
            return 1;
        }
        return 0;
    }

    @Override
    public long asLong() {
        if (value) {
            return 1;
        }
        return 0;
    }

    @Override
    public short asShort() {
        if (value) {
            return 1;
        }
        return 0;
    }

    @Override
    public String asString() {
        return value + "";
    }

    @Override
    public Plugin getOwningPlugin() {
        return CreatureHunt.instance;
    }

    @Override
    public void invalidate() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Object value() {
        // TODO Auto-generated method stub
        return null;
    }

}
