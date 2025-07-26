package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.JobTeam;
import org.easytech.pelatologio.models.Recommendation;

import java.util.List;

public interface JobTeamDao {

    List<JobTeam> getJobTeams();

    void saveJobTeam(JobTeam jobTeam);

    void updateJobTeam(JobTeam jobTeam);

    void deleteJobTeam(int id);
}
