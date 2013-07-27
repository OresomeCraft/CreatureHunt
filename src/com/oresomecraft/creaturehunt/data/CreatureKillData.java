package com.oresomecraft.creaturehunt.data;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.inventory.ItemStack;


public class CreatureKillData {
    
    private static Random randgen = new Random();
    
    public short maxPoint;
    public short minPoint;
    
    public double maxMoney;
    public double minMoney;
    
    public byte dropChance;
    
    public ArrayList<CreatureItemDrop> itemDrops;
    
    public CreatureKillData() {
        itemDrops = new ArrayList<CreatureItemDrop>();
    }

    public short getPoints() {
        if (maxPoint == 0) {
            return -1;
        } else if (maxPoint == minPoint) {
            return maxPoint;
        } else {
            return (short) (randgen.nextInt(Math.abs(maxPoint - minPoint) + 1) + minPoint);
        }
    }

    public double getMoney() {
        if (maxMoney <= 0) {
            return -1;
        } else if (maxMoney == minMoney) {
            return maxMoney;
        } else {
            double temp = maxMoney * 100;
            int iMaxMoney = (int) temp;
            temp = minMoney * 100;
            int iMinMoney = (int) temp;
            temp = randgen.nextInt(Math.abs(iMaxMoney - iMinMoney) + 1) + iMinMoney;
            return temp / 100;
        }
    }

    public ItemStack getItem() {
        for (CreatureItemDrop item : itemDrops) {
            if (randgen.nextInt(10000) < item.dropChance) {
                ItemStack droppedItem = new ItemStack(item.itemID, item.amount, item.dataValue);
                return droppedItem;
            }
        }
        return null;
    }
    
}
