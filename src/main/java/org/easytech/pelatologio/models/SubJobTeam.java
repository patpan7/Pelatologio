package org.easytech.pelatologio.models;

public class SubJobTeam {
    private int id;
    private String name;
    private int jobTeamId;

    public SubJobTeam(int id, String name, int jobTeamId) {
        this.id = id;
        this.name = name;
        this.jobTeamId = jobTeamId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getJobTeamId() {
        return jobTeamId;
    }

    public void setJobTeamId(int jobTeamId) {
        this.jobTeamId = jobTeamId;
    }

    @Override
    public String toString() {
        return name;
    }
}
