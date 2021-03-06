/*
 * Copyright 2005 JBoss Inc
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

package org.drools.guvnor.client.packages;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.LoadingPopup;
import org.drools.guvnor.client.explorer.ClientFactory;
import org.drools.guvnor.client.explorer.TabContentWidget;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.rpc.PackageConfigData;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.ruleeditor.toolbar.ActionToolbar;

/**
 * This is the package editor and viewer for package configuration.
 */
public class PackageEditorWrapper extends Composite implements TabContentWidget {
    private Constants constants = GWT.create( Constants.class );

    private ArtifactEditor artifactEditor;
    private PackageEditor packageEditor;
    private ActionToolbar actionToolBar;
    private PackageConfigData packageConfigData;
    private boolean isHistoricalReadOnly = false;

    VerticalPanel layout = new VerticalPanel();
    private final ClientFactory clientFactory;

    public PackageEditorWrapper( PackageConfigData data,
                                 ClientFactory clientFactory ) {
        this( data, clientFactory, false );
    }

    public PackageEditorWrapper( PackageConfigData data,
                                 ClientFactory clientFactory,
                                 boolean isHistoricalReadOnly ) {
        this.packageConfigData = data;
        this.clientFactory = clientFactory;
        this.isHistoricalReadOnly = isHistoricalReadOnly;

        initWidget( layout );
        render();
        setWidth( "100%" );
    }

    private void render() {
        this.artifactEditor = new ArtifactEditor( clientFactory, packageConfigData, this.isHistoricalReadOnly );
        this.packageEditor = new PackageEditor(
                packageConfigData,
                clientFactory,
                this.isHistoricalReadOnly,
                new Command() {
                    public void execute() {
                        refresh();
                    }
                } );
        this.actionToolBar = this.packageEditor.getActionToolbar();

        layout.clear();
        layout.add( this.actionToolBar );

        TabPanel tPanel = new TabPanel();
        tPanel.setWidth( "100%" );

        ScrollPanel pnl = new ScrollPanel();
        pnl.setWidth( "100%" );
        pnl.add( this.artifactEditor );
        tPanel.add( pnl, "Attributes" );
        tPanel.selectTab( 0 );

        pnl = new ScrollPanel();
        pnl.setWidth( "100%" );
        pnl.add( this.packageEditor );
        tPanel.add( pnl, "Edit" );
        tPanel.selectTab( 0 );

        tPanel.setHeight( "100%" );
        layout.add( tPanel );
        layout.setHeight( "100%" );
    }

    /**
     * Will refresh all the data.
     */
    public void refresh() {
        LoadingPopup.showMessage( constants.RefreshingPackageData() );
        RepositoryServiceFactory.getPackageService().loadPackageConfig( this.packageConfigData.getUuid(),
                new GenericCallback<PackageConfigData>() {
                    public void onSuccess( PackageConfigData data ) {
                        LoadingPopup.close();
                        packageConfigData = data;
                        render();
                    }
                } );
    }

    public String getTabTitle() {
        return packageConfigData.name;
    }

    public String getID() {
        return packageConfigData.uuid;
    }
}
