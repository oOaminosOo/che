package org.eclipse.che.api.workspace.server.jpa;

import com.google.inject.TypeLiteral;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.repository.TckRepository;

import javax.inject.Inject;

public class WorkspaceTckModule extends TckModule {

    @Override
    protected void configure() {
        bind(WorkspaceDao.class).to(JpaWorkspaceDao.class);
        bind(new TypeLiteral<TckRepository<WorkspaceImpl>>(){}).to(WorkspaceTckRepository.class);
        install(new JpaPersistModule("main"));
        bind(JpaInitializer.class).asEagerSingleton();
    }

    private static class JpaInitializer {
        @Inject
        private JpaInitializer(PersistService service) {
            service.start();
        }
    }
}
