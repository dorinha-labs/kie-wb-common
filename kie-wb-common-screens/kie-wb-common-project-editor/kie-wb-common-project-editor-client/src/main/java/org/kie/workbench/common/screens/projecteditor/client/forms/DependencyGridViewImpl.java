/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.projecteditor.client.forms;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.DataGrid;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.project.model.Dependency;
import org.kie.workbench.common.screens.projecteditor.client.resources.ProjectEditorResources;
import java.util.List;

public class DependencyGridViewImpl
        extends Composite
        implements DependencyGridView {


    interface Binder
            extends
            UiBinder<Widget, DependencyGridViewImpl> {

    }

    private static Binder uiBinder = GWT.create(Binder.class);

    private Presenter presenter;

    @UiField(provided = true)
    DataGrid<Dependency> dataGrid = new DataGrid<Dependency>();

    @UiField
    Button addDependencyButton;

    @UiField
    Button addFromRepositoryDependencyButton;

    public DependencyGridViewImpl() {
        dataGrid.setEmptyTableWidget(new Label("---"));

        dataGrid.setAutoHeaderRefreshDisabled(true);

        addGroupIdColumn();
        addArtifactIdColumn();
        addVersionColumn();
        addRemoveRowColumn();

        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * <p>Custom field updater for handling dependency GAV values.</p>
     * <p>If cell's value is invalid, this field updater restores the old value for the cell.</p>
     *
     * <p>Some handlers are provided:</p>
     * <ul>
     *     <li><code>emptyHandler</code>: Handler for empty value (not allowed).</li>
     *     <li><code>notValidValueHandler</code>: Handler for not valid value.</li>
     *     <li><code>validHandler</code>: Handler for valid value.</li>
     * </ul>
     *
     * NOTE: BZ-1007894
     *
     * @param <T> the data type that will be modified
     * @param <C> the data type of the modified field
     */
    private class DependencyFieldUpdater<T, C> implements  FieldUpdater<T, C> {

        private EditTextCell cell;
        private DependencyFieldUpdaterHandler emptyHandler;
        private DependencyFieldUpdaterHandler notValidValueHandler;
        private DependencyFieldUpdaterHandler validHandler;

        DependencyFieldUpdater(EditTextCell cell) {
            this.cell = cell;
        }

        @Override
        public void update(int index, T object, C value) {
            try {
                Dependency dependency = (Dependency) object;
                String sValue = (String) value;

                boolean hasError = false;
                if (checkIsNotEmpty(sValue)) {
                    if (emptyHandler != null) emptyHandler.handle(dependency, sValue);
                    hasError = true;
                }
                if (checkIsInValid(sValue)) {
                    if (notValidValueHandler != null) notValidValueHandler.handle(dependency, sValue);
                    hasError = true;
                }

                // Clear view data. Restore old data.
                if (hasError) {
                    cell.clearViewData(dependency);
                    refresh();
                } else {
                    if (validHandler != null) validHandler.handle(dependency, sValue);
                }
            } catch (ClassCastException e) {
                throw new UnsupportedOperationException("Class DependencyGridViewImpl.DependencyFieldUpdater only supports org.guvnor.common.services.project.model.Dependency as source");
            }
        }

    };

    /**
     * BZ-1007894:
     *
     * Dependency field update handler.
     */
    private interface DependencyFieldUpdaterHandler {
        void handle(Dependency dep, String value);
    }


    private void addArtifactIdColumn() {
        final Column<Dependency, String> column = new Column<Dependency, String>(new EditTextCell()) {

            @Override
            public String getValue(Dependency dependency) {
                if (dependency.getArtifactId() != null) {
                    return dependency.getArtifactId();
                } else {
                    return "";
                }
            }
        };

        // BZ-1007894: If field value is not correct, do not update the model and the widet cell value.
        DependencyFieldUpdater fieldUpdater = new DependencyFieldUpdater((EditTextCell) column.getCell());
        fieldUpdater.emptyHandler = new DependencyFieldUpdaterHandler() {
            @Override
            public void handle(Dependency dep, String value) {
                Window.alert(ProjectEditorResources.CONSTANTS.ArtifactIdMissing());
            }
        };
        fieldUpdater.notValidValueHandler = new DependencyFieldUpdaterHandler() {
            @Override
            public void handle(Dependency dep, String value) {
                Window.alert(ProjectEditorResources.CONSTANTS.XMLMarkIsNotAllowed());
            }
        };
        fieldUpdater.validHandler = new DependencyFieldUpdaterHandler() {
            @Override
            public void handle(Dependency dep, String value) {
                dep.setArtifactId(value);
            }
        };


        column.setFieldUpdater(fieldUpdater);

        dataGrid.addColumn(column, ProjectEditorResources.CONSTANTS.ArtifactID());
        dataGrid.setColumnWidth(column, 60, Style.Unit.PCT);
    }

    private void addGroupIdColumn() {
        Column<Dependency, String> column = new Column<Dependency, String>(new EditTextCell()) {
            @Override
            public String getValue(Dependency dependency) {
                if (dependency.getGroupId() != null) {
                    return dependency.getGroupId();
                } else {
                    return "";
                }
            }
        };

        // BZ-1007894: If field value is not correct, do not update the model and the widet cell value.
        DependencyFieldUpdater fieldUpdater = new DependencyFieldUpdater((EditTextCell) column.getCell());
        fieldUpdater.emptyHandler = new DependencyFieldUpdaterHandler() {
            @Override
            public void handle(Dependency dep, String value) {
                Window.alert(ProjectEditorResources.CONSTANTS.GroupIdMissing());
            }
        };
        fieldUpdater.notValidValueHandler = new DependencyFieldUpdaterHandler() {
            @Override
            public void handle(Dependency dep, String value) {
                Window.alert(ProjectEditorResources.CONSTANTS.XMLMarkIsNotAllowed());
            }
        };
        fieldUpdater.validHandler = new DependencyFieldUpdaterHandler() {
            @Override
            public void handle(Dependency dep, String value) {
                dep.setGroupId(value);
            }
        };

        column.setFieldUpdater(fieldUpdater);

        dataGrid.addColumn(column, ProjectEditorResources.CONSTANTS.GroupID());
        dataGrid.setColumnWidth(column, 60, Style.Unit.PCT);
    }

    private void addVersionColumn() {
        Column<Dependency, String> column = new Column<Dependency, String>(new EditTextCell()) {
            @Override
            public String getValue(Dependency dependency) {
                if (dependency.getVersion() != null) {
                    return dependency.getVersion();
                } else {
                    return "";
                }
            }
        };

        // BZ-1007894: If field value is not correct, do not update the model and the widet cell value.
        DependencyFieldUpdater fieldUpdater = new DependencyFieldUpdater((EditTextCell) column.getCell());
        fieldUpdater.emptyHandler = new DependencyFieldUpdaterHandler() {
            @Override
            public void handle(Dependency dep, String value) {
                Window.alert(ProjectEditorResources.CONSTANTS.VersionIdMissing());
            }
        };
        fieldUpdater.notValidValueHandler = new DependencyFieldUpdaterHandler() {
            @Override
            public void handle(Dependency dep, String value) {
                Window.alert(ProjectEditorResources.CONSTANTS.XMLMarkIsNotAllowed());
            }
        };
        fieldUpdater.validHandler = new DependencyFieldUpdaterHandler() {
            @Override
            public void handle(Dependency dep, String value) {
                dep.setVersion(value);
            }
        };

        column.setFieldUpdater(fieldUpdater);

        dataGrid.addColumn(column, ProjectEditorResources.CONSTANTS.VersionID());
        dataGrid.setColumnWidth(column, 60, Style.Unit.PCT);
    }

    private void updateCellValue(int index, Dependency dependency, String value) {

    }

    // BZ-1007894
    boolean checkIsNotEmpty(String content) {
        if (content != null && content.trim().length() > 0) {
            return false;
        }

        return true;
    }

    boolean checkIsInValid(String content) {
        if (content != null && (content.contains("<") || content.contains(">") || content.contains("&"))) {
            return true;
        }

        return false;
    }

    private void addRemoveRowColumn() {
        Column<Dependency, ImageResource> column = new Column<Dependency, ImageResource>(new TrashCanImageCell()) {
            @Override
            public ImageResource getValue(Dependency dependency) {
                return ProjectEditorResources.INSTANCE.Trash();
            }
        };

        column.setFieldUpdater(new FieldUpdater<Dependency, ImageResource>() {
            @Override
            public void update(int index, Dependency dependency, ImageResource value) {
                presenter.onRemoveDependency(dependency);
            }
        });


        dataGrid.addColumn(column);
        dataGrid.setColumnWidth(column, 40, Style.Unit.PCT);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setReadOnly() {
        addDependencyButton.setEnabled(false);
        addFromRepositoryDependencyButton.setEnabled(false);
    }

    @Override
    public void setList(List<Dependency> dependencies) {
        dataGrid.setRowData(dependencies);
    }

    @UiHandler("addDependencyButton")
    void onAddDependency(ClickEvent event) {
        presenter.onAddDependencyButton();
    }

    @UiHandler("addFromRepositoryDependencyButton")
    void onAddDependencyFromRepository(ClickEvent event) {
        presenter.onAddDependencyFromRepositoryButton();
    }

    @Override
    public void refresh() {
        dataGrid.redraw();
    }
}
