package com.test.dynamictest.test;

/**
 * Created by prankul.garg on 26/04/19.
 */
public class DynamicModuleItem {
    private String name;
    private boolean isInstalled;

    public DynamicModuleItem(String name, boolean isInstalled) {
        this.name = name;
        this.isInstalled = isInstalled;
    }

    public DynamicModuleItem(String name) {
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
