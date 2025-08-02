package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.JobTeam;

import java.util.List;

import java.util.Map;

public interface JobTeamDao {
    // ... (υπάρχουσες μέθοδοι)

    Map<String, Integer> getCustomerCountPerJobTeam();

    Map<String, Integer> getCustomerCountPerSubJobTeam(int jobTeamId);

    int getJobTeamIdByName(String teamName);

    int getParentTeamIdBySubTeamId(int subTeamId);

    List<JobTeam> getJobTeams();

    void saveJobTeam(JobTeam jobTeam);

    void updateJobTeam(JobTeam jobTeam);

    void deleteJobTeam(int id);
}
