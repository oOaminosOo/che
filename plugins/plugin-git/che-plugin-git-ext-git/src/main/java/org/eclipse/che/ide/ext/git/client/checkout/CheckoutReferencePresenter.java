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
package org.eclipse.che.ide.ext.git.client.checkout;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.workspace.Workspace;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for checkout reference(branch, tag) name or commit hash.
 *
 * @author Roman Nikitenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class CheckoutReferencePresenter implements CheckoutReferenceView.ActionDelegate {
    public static final String CHECKOUT_COMMAND_NAME = "Git checkout";

    private final NotificationManager     notificationManager;
    private final GitServiceClient        service;
    private final AppContext              appContext;
    private final GitLocalizationConstant constant;
    private final CheckoutReferenceView   view;
    private final Workspace               workspace;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ConsolesPanelPresenter  consolesPanelPresenter;

    private Project project;

    @Inject
    public CheckoutReferencePresenter(CheckoutReferenceView view,
                                      GitServiceClient service,
                                      AppContext appContext,
                                      GitLocalizationConstant constant,
                                      NotificationManager notificationManager,
                                      GitOutputConsoleFactory gitOutputConsoleFactory,
                                      ConsolesPanelPresenter consolesPanelPresenter,
                                      Workspace workspace) {
        this.view = view;
        this.workspace = workspace;
        this.view.setDelegate(this);
        this.service = service;
        this.appContext = appContext;
        this.constant = constant;
        this.notificationManager = notificationManager;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
    }

    /** Show dialog. */
    public void showDialog(Project project) {
        this.project = project;
        view.setCheckoutButEnableState(false);
        view.showDialog();
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void onCheckoutClicked(final String reference) {
        service.checkout(workspace.getId(), project.getLocation(), reference, null, false, null, false).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                project.synchronize().then(new Operation<Resource[]>() {
                    @Override
                    public void apply(Resource[] arg) throws OperationException {
                        view.close();
                    }
                });
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                final String errorMessage = (error.getMessage() != null)
                                            ? error.getMessage()
                                            : constant.checkoutFailed();
                GitOutputConsole console = gitOutputConsoleFactory.create(CHECKOUT_COMMAND_NAME);
                console.printError(errorMessage);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                notificationManager.notify(constant.checkoutFailed(), FAIL, true);
            }
        });
    }

    @Override
    public void referenceValueChanged(String reference) {
        view.setCheckoutButEnableState(isInputCorrect(reference));
    }

    @Override
    public void onEnterClicked() {
        String reference = view.getReference();
        if (isInputCorrect(reference)) {
            onCheckoutClicked(reference);
        }
    }

    private boolean isInputCorrect(String reference) {
        return reference != null && !reference.isEmpty();
    }
}
