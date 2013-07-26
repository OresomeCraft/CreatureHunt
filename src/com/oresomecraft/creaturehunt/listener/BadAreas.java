package com.oresomecraft.creaturehunt.listener;

public class BadAreas {

    public int x1, x2, y1, y2, z1, z2;
    
    public BadAreas(int[] points) {
        x1 = points[0];
        y1 = points[1];
        z1 = points[2];
        x2 = points[3];
        y2 = points[4];
        z2 = points[5];
        
        int temp = 0;
        if (x1 < x2) {
            temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 < y2) {
            temp = y1;
            y1 = y2;
            y2 = temp;
        }
        if (z1 < z2) {
            temp = z1;
            z1 = z2;
            z2 = temp;
        }
    }
    
    public boolean isInArea(int x, int y, int z) {
        if (x <= x1 && x >= x2 && y <= y1 && y >= y2 && z <= z1 && z >= z2) {
            return true;
        }
        return false;
    }
}
