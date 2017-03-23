package org.jetbrains.comparisonClass;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * Created by menuka on 3/23/17.
 */
public class GenerateDialog extends DialogWrapper {
    private CollectionListModel<PsiField> myFields;
    private final LabeledComponent<JPanel> component;

    public GenerateDialog(PsiClass psiClass) {
        super(psiClass.getProject());
        setTitle("Select Fields for ComparisonChain");

        myFields = new CollectionListModel<>(psiClass.getAllFields());
//        JList fieldList = new JList(myFields);
        JList fieldList = new JBList<>(myFields);
        fieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        decorator.disableAddAction();
        JPanel panel = decorator.createPanel();
        component = LabeledComponent.create(panel, "Fields to include in compareTo():");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return component;
    }

    public List<PsiField> getFields() {
        return myFields.getItems();
    }
}
