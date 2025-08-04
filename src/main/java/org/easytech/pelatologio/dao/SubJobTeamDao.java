package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.SubJobTeam;

import java.util.List;

public interface SubJobTeamDao {

    List<SubJobTeam> getSubJobTeams(int id);

    void saveSubJobTeam(SubJobTeam subJobTeam);

    void updateSubJobTeam(SubJobTeam subJobTeam);

    void deleteSubJobTeam(int id);

    List<Integer> getSubJobTeamIdsByTeam(int id);
}
