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

package org.drools.guvnor.client.ruleeditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import org.drools.guvnor.client.common.*;
import org.drools.guvnor.client.explorer.ClientFactory;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.packages.PackageEditorWrapper;
import org.drools.guvnor.client.resources.Images;
import org.drools.guvnor.client.rpc.*;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This widget shows a list of versions for packages or assets
 */
public class VersionBrowser extends Composite {

    private Constants constants = GWT.create( Constants.class );
    private static Images images = GWT.create( Images.class );

    private Image refresh;
    private FlexTable layout;
    private final String uuid;
    private final Command refreshCommand;
    private final boolean isPackage;
    private final ClientFactory clientFactory;

    public VersionBrowser( ClientFactory clientFactory,
                           String uuid,
                           boolean isPackage,
                           Command ref ) {
        this.clientFactory = clientFactory;
        this.uuid = uuid;
        this.refreshCommand = ref;
        this.isPackage = isPackage;

        HorizontalPanel wrapper = new HorizontalPanel();

        ClickHandler clickHandler = new ClickHandler() {

            public void onClick( ClickEvent event ) {
                clickLoadHistory();
            }
        };
        layout = new FlexTable();
        ClickableLabel vh = new ClickableLabel( constants.VersionHistory1(),
                clickHandler );
        layout.setWidget( 0,
                0,
                vh );
        layout.getCellFormatter().setStyleName( 0,
                0,
                "metadata-Widget" ); //NON-NLS
        FlexCellFormatter formatter = layout.getFlexCellFormatter();
        formatter.setHorizontalAlignment( 0,
                0,
                HasHorizontalAlignment.ALIGN_LEFT );

        refresh = new ImageButton( images.refresh() );

        refresh.addClickHandler( clickHandler );

        layout.setWidget( 0,
                1,
                refresh );
        formatter.setHorizontalAlignment( 0,
                1,
                HasHorizontalAlignment.ALIGN_RIGHT );

        wrapper.setStyleName( "version-browser-Border" );

        wrapper.add( layout );

        layout.setWidth( "100%" );
        wrapper.setWidth( "100%" );

        initWidget( wrapper );
    }

    protected void clickLoadHistory() {
        showBusyIcon();
        Scheduler scheduler = Scheduler.get();
        scheduler.scheduleDeferred( new Command() {
            public void execute() {
                loadHistoryData();
            }
        } );

    }

    private void showBusyIcon() {
        refresh.setResource( images.searching() );
    }

    /**
     * Actually load the history data, as demanded.
     */
    protected void loadHistoryData() {

        RepositoryServiceFactory.getAssetService().loadItemHistory( this.uuid,
                new GenericCallback<TableDataResult>() {

                    public void onSuccess( TableDataResult table ) {
                        if ( table == null || table.data.length == 0 ) {
                            layout.setWidget( 1,
                                    0,
                                    new Label( constants.NoHistory() ) );
                            showStaticIcon();
                            return;
                        }
                        TableDataRow[] rows = table.data;
                        Arrays.sort( rows,
                                new Comparator<TableDataRow>() {
                                    public int compare( TableDataRow r1,
                                                        TableDataRow r2 ) {
                                        Integer v2 = Integer.valueOf( r2.values[0] );
                                        Integer v1 = Integer.valueOf( r1.values[0] );

                                        return v2.compareTo( v1 );
                                    }
                                } );

                        final ListBox history = new ListBox( true );

                        for (int i = 0; i < rows.length; i++) {
                            TableDataRow row = rows[i];
                            String s = constants.property0ModifiedOn12(
                                    row.values[0],
                                    row.values[2],
                                    row.values[1] );
                            history.addItem( s,
                                    row.id );
                        }

                        layout.setWidget( 1,
                                0,
                                history );
                        FlexCellFormatter formatter = layout.getFlexCellFormatter();

                        formatter.setColSpan( 1,
                                0,
                                2 );

                        Button open = new Button( constants.View() );

                        open.addClickHandler( new ClickHandler() {

                            public void onClick( ClickEvent event ) {
                                showVersion( history.getValue( history.getSelectedIndex() ) );
                            }

                        } );

                        layout.setWidget( 2,
                                0,
                                open );
                        formatter.setColSpan( 2,
                                1,
                                3 );
                        formatter.setHorizontalAlignment( 2,
                                1,
                                HasHorizontalAlignment.ALIGN_CENTER );

                        showStaticIcon();

                    }

                } );

    }

    /**
     * This should popup a view of the chosen historical version.
     */
    private void showVersion( final String versionUUID ) {

        LoadingPopup.showMessage( constants.LoadingVersionFromHistory() );

        if ( isPackage ) {
            RepositoryServiceFactory.getPackageService().loadPackageConfig( versionUUID,
                    new GenericCallback<PackageConfigData>() {
                        public void onSuccess( PackageConfigData conf ) {
                            final FormStylePopup pop = new FormStylePopup( images.snapshot(),
                                    constants.VersionNumber0Of1( conf.getVersionNumber(),
                                            conf.getName() ),
                                    new Integer( 800 ) );

                            PackageEditorWrapper ed = new PackageEditorWrapper(
                                    conf,
                                    clientFactory,
                                    true );
                            ed.setWidth( "100%" );
                            ed.setHeight( "100%" );
                            //pop.addRow( restore );
                            pop.addRow( ed );
                            pop.show();

                            //LoadingPopup.close();
                        }
                    } );
        } else {
            RepositoryServiceFactory.getAssetService().loadRuleAsset( versionUUID,
                    new GenericCallback<RuleAsset>() {

                        public void onSuccess( RuleAsset asset ) {
                            asset.setReadonly( true );
                            //asset.metaData.name = metaData.name;
                            final FormStylePopup pop = new FormStylePopup( images.snapshot(),
                                    constants.VersionNumber0Of1(
                                            asset.getVersionNumber(),
                                            asset.getName() ),
                                    new Integer( 800 ) );

                            Button restore = new Button( constants.RestoreThisVersion() );
                            restore.addClickHandler( new ClickHandler() {

                                public void onClick( ClickEvent event ) {
                                    restore( (Widget) event.getSource(),
                                            versionUUID,
                                            new Command() {
                                                public void execute() {
                                                    refreshCommand.execute();
                                                    pop.hide();
                                                }
                                            } );
                                }
                            } );

                            RuleViewerWrapper viewer = new RuleViewerWrapper(
                                    clientFactory,
                                    asset,
                                    null,
                                    null,
                                    null,
                                    null,
                                    true,
                                    null,
                                    null );
                            viewer.setWidth( "100%" );
                            viewer.setHeight( "100%" );

                            pop.addRow( restore );
                            pop.addRow( viewer );
                            pop.show();
                        }
                    } );
        }
    }

    private void restore( Widget w,
                          final String versionUUID,
                          final Command refresh ) {

        final CheckinPopup pop = new CheckinPopup( constants.RestoreThisVersionQ() );
        pop.setCommand( new Command() {
            public void execute() {
                RepositoryServiceFactory.getAssetService().restoreVersion( versionUUID,
                        uuid,
                        pop.getCheckinComment(),
                        new GenericCallback<Void>() {
                            public void onSuccess( Void v ) {
                                refresh.execute();
                            }
                        } );
            }
        } );
        pop.show();
    }

    private void showStaticIcon() {
        refresh.setResource( images.refresh() );
    }

}
