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
package org.eclipse.che.ide.newresource;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.InputCallback;
import org.eclipse.che.ide.api.dialogs.InputDialog;
import org.eclipse.che.ide.api.dialogs.InputValidator;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;
import org.eclipse.che.ide.util.NameUtils;

import static com.google.common.base.Preconditions.checkState;

/**
 * Action to create new folder.
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class NewFolderAction extends AbstractNewResourceAction {

    private InputValidator folderNameValidator;

    @Inject
    public NewFolderAction(CoreLocalizationConstant localizationConstant,
                           Resources resources,
                           DialogFactory dialogFactory,
                           EventBus eventBus,
                           AppContext appContext,
                           NotificationManager notificationManager) {
        super(localizationConstant.actionNewFolderTitle(),
              localizationConstant.actionNewFolderDescription(),
              resources.defaultFolder(), dialogFactory, localizationConstant, eventBus, appContext, notificationManager);
        this.folderNameValidator = new FolderNameValidator();
    }

    @Override
    public void actionPerformed(ActionEvent e) {


        InputDialog inputDialog = dialogFactory.createInputDialog(
                coreLocalizationConstant.newResourceTitle(coreLocalizationConstant.actionNewFolderTitle()),
                coreLocalizationConstant.newResourceLabel(coreLocalizationConstant.actionNewFolderTitle().toLowerCase()),
                new InputCallback() {
                    @Override
                    public void accepted(String value) {
                        onAccepted(value);
                    }
                }, null).withValidator(folderNameValidator);
        inputDialog.show();
    }

    private void onAccepted(String value) {
        Resource resource = appContext.getResource();

        if (!(resource instanceof Container)) {
            final Optional<Container> parent = resource.getParent();

            checkState(!parent.isPresent(), "Parent should be a container");

            resource = parent.get();
        }

        ((Container)resource).newFolder(value).then(new Operation<Folder>() {
            @Override
            public void apply(Folder folder) throws OperationException {
                eventBus.fireEvent(new RevealResourceEvent(folder));
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                dialogFactory.createMessageDialog("Error", error.getMessage(), null).show();
            }
        });
    }

    private class FolderNameValidator implements InputValidator {
        @Nullable
        @Override
        public Violation validate(String value) {
            if (!NameUtils.checkFolderName(value)) {
                return new Violation() {
                    @Override
                    public String getMessage() {
                        return coreLocalizationConstant.invalidName();
                    }

                    @Nullable
                    @Override
                    public String getCorrectedValue() {
                        return null;
                    }
                };
            }
            return null;
        }
    }
}
