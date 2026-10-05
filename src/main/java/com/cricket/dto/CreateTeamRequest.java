package com.cricket.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateTeamRequest {

    @NotBlank(message = "Team name is required")
    private String name;

    @NotBlank(message = "Short name is required")
    private String shortName;

    private String code;
    private String flag = "🏏";
    private String primaryColor = "#0078FF";
    private List<String> playerNames;

    public CreateTeamRequest() {}

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

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public void setPlayerNames(List<String> playerNames) {
        this.playerNames = playerNames;
    }
}
