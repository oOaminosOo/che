package org.eclipse.che.api.workspace.server.jpa;

import com.google.inject.persist.Transactional;

import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Collection;

@Transactional
public class WorkspaceTckRepository implements TckRepository<WorkspaceImpl> {

    @Inject
    private EntityManager manager;

    @Override
    public void createAll(Collection<? extends WorkspaceImpl> entities) throws TckRepositoryException {
        for (WorkspaceImpl entity : entities) {
            manager.persist(entity);
        }
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        for (WorkspaceImpl workspace : manager.createQuery("SELECT w FROM Workspace w", WorkspaceImpl.class).getResultList()) {
            manager.remove(workspace);
        }
    }
}
