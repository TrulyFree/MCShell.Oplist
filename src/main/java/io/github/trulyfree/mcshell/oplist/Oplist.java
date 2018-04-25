package io.github.trulyfree.mcshell.oplist;

import lombok.Value;

import java.util.ArrayList;

public final class Oplist extends ArrayList<Oplist.OplistEntry> {
    @Value
    public static final class OplistEntry {
        String uuid;
        String name;
        int level;
        boolean bypassesPlayerLimit;
    }
}
