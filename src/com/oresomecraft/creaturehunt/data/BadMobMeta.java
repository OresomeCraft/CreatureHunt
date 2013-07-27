package com.oresomecraft.creaturehunt.data;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import com.oresomecraft.creaturehunt.CreatureHunt;

public class BadMobMeta implements MetadataValue {
    
    private boolean metaValue;
    
    public BadMobMeta(boolean init) {
        metaValue = init;
    }

    @Override
    public boolean asBoolean() {
        // TODO Auto-generated method stub
        return metaValue;
    }

    @Override
    public byte asByte() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double asDouble() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float asFloat() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int asInt() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long asLong() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public short asShort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String asString() {
        // TODO Auto-generated method stub
        return null;
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
