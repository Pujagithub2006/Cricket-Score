package com.cricket.model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String name;
    private String shortName;
    private String code;
    private String flagEmoji;
    private String color;
    private List<String> squad = new ArrayList<>();

    public Team() {}

    public Team(String name, String shortName, String code, String flagEmoji, String color, List<String> squad) {
        this.name = name;
        this.shortName = shortName;
        this.code = code;
        this.flagEmoji = flagEmoji;
        this.color = color;
        if (squad != null) {
            this.squad = new ArrayList<>(squad);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFlagEmoji() {
        return flagEmoji;
    }

    public void setFlagEmoji(String flagEmoji) {
        this.flagEmoji = flagEmoji;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<String> getSquad() {
        return squad;
    }

    public void setSquad(List<String> squad) {
        this.squad = squad;
    }
}
