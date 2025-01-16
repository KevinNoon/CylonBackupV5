package com.optimised.cylonbackup.data;

public enum Role {
   ENGINEER("Engineer"),SUPERVISOR("Supervisor"), ADMIN("Administrator");

    public final String label;

    private Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
