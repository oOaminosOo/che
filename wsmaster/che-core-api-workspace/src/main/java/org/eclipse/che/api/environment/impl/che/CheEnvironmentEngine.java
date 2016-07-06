/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.environment.impl.che;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.environment.server.spi.EnvironmentEngine;

import java.util.List;

/**
 * @author Alexander Garagatyi
 */
public class CheEnvironmentEngine implements EnvironmentEngine {
    @Override
    public String getType() {
        return null;
    }

    @Override
    public List<Machine> start(String workspaceId, Environment env, boolean recover) throws ServerException {
        return null;
    }

    @Override
    public void stop(String workspaceId) throws ServerException {

    }
}
