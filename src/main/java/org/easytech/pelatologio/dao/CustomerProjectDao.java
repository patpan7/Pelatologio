package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.CustomerProject;
import java.util.List;

public interface CustomerProjectDao {
    List<CustomerProject> getProjectsForCustomer(int customerId);
    void addProjectForCustomer(CustomerProject project);
    boolean hasProjects(int customerId, int applicationId);
}
