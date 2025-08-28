package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.ApplicationStep;
import java.util.List;

public interface ApplicationStepDao {
    List<ApplicationStep> getStepsForApplication(int applicationId);
    void addStep(ApplicationStep step);
    void updateStep(ApplicationStep step);
    void deleteStep(int stepId);
}
