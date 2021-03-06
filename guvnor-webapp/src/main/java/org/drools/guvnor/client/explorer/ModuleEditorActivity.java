/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.client.explorer;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.LoadingPopup;
import org.drools.guvnor.client.common.RulePackageSelector;
import org.drools.guvnor.client.packages.PackageEditorWrapper;
import org.drools.guvnor.client.rpc.PackageConfigData;

public class ModuleEditorActivity extends AbstractActivity {

    private final ClientFactory clientFactory;
    private ModuleEditorActivityView view;
    private String uuid;

    public ModuleEditorActivity( String uuid, ClientFactory clientFactory ) {
        this.view = clientFactory.getModuleEditorActivityView();

        this.uuid = uuid;

        this.clientFactory = clientFactory;
    }

    public void start( final AcceptsOneWidget panel, final EventBus eventBus ) {

        view.showLoadingPackageInformationMessage();

        clientFactory.getPackageService().loadPackageConfig( uuid,
                new GenericCallback<PackageConfigData>() {
                    public void onSuccess( PackageConfigData packageConfigData ) {
                        RulePackageSelector.currentlySelectedPackage = packageConfigData.getUuid();
                        panel.setWidget( new PackageEditorWrapper( packageConfigData, clientFactory ) );

                        LoadingPopup.close();
                    }
                } );
    }
}
