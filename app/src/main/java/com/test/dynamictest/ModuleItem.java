package com.test.dynamictest;

/**
 * Created by prankul.garg on 26/04/19.
 */
public class ModuleItem {
    private String name;
    private boolean isInstalled;

    public ModuleItem(String name, boolean isInstalled) {
        this.name = name;
        this.isInstalled = isInstalled;
    }

    public ModuleItem(String name) {
        this.name = name;
        this.isInstalled = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public void setInstalled(boolean installed) {
        isInstalled = installed;
    }
}
