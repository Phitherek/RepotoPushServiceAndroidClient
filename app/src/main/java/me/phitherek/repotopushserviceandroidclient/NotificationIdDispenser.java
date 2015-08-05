package me.phitherek.repotopushserviceandroidclient;

import java.util.ArrayList;

/**
 * Created by phitherek on 05.08.15.
 */
public class NotificationIdDispenser {
    private static ArrayList<Integer> activeIds = new ArrayList<Integer>();

    public static Integer nextId() {
        Integer i = 0;
        while(activeIds.contains(i)) {
            i = i + 1;
        }
        activeIds.add(i);
        return i;
    }

    public static void removeActive(Integer i) {
        if(i != -1) {
            activeIds.remove(i);
        }
    }
}
