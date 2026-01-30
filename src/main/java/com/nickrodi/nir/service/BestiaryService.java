package com.nickrodi.nir.service;

import com.nickrodi.nir.model.PlayerData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BestiaryService {
    private BestiaryService() {
    }

    public static boolean markFound(PlayerData data, String id) {
        if (data == null || id == null || id.isBlank()) {
            return false;
        }
        List<String> found = data.getBestiaryFound();
        if (found == null) {
            found = new ArrayList<>();
        }
        if (found.contains(id)) {
            return false;
        }
        found.add(id);
        data.setBestiaryFound(found);
        return true;
    }

    public static boolean isFound(PlayerData data, String id) {
        if (data == null || id == null || id.isBlank()) {
            return false;
        }
        List<String> found = data.getBestiaryFound();
        if (found == null || found.isEmpty()) {
            return false;
        }
        return found.contains(id);
    }

    public static int countFound(PlayerData data, List<BestiaryCatalog.Entry> entries) {
        if (data == null || entries == null || entries.isEmpty()) {
            return 0;
        }
        List<String> found = data.getBestiaryFound();
        if (found == null || found.isEmpty()) {
            return 0;
        }
        Set<String> foundSet = new HashSet<>(found);
        int count = 0;
        for (BestiaryCatalog.Entry entry : entries) {
            if (foundSet.contains(entry.id())) {
                count++;
            }
        }
        return count;
    }
}
