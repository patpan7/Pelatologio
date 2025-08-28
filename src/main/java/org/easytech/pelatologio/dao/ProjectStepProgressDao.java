package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.CustomerProjectSummary;
import org.easytech.pelatologio.models.GlobalProgressEntry;
import org.easytech.pelatologio.models.ProjectStepProgress;
import java.util.List;

public interface ProjectStepProgressDao {
    List<ProjectStepProgress> getProgressForProject(int projectId);
    void updateProgress(ProjectStepProgress progress);
    void createInitialProgressForProject(int projectId, int applicationId);
    List<GlobalProgressEntry> getGlobalProgress(int applicationId);
    List<CustomerProjectSummary> getGlobalProgressSummary(int applicationId);
}
