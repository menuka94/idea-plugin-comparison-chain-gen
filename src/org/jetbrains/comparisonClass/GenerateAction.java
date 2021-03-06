package org.jetbrains.comparisonClass;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.List;

/**
 * Created by menuka on 3/23/17.
 */
public class GenerateAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        GenerateDialog dlg = new GenerateDialog(psiClass);
        dlg.show();
        if(dlg.isOK()){
            generateCompareTo(psiClass, dlg.getFields());
        }

    }

    private void generateComparable(PsiClass psiClass, List<PsiField> fields) {
        new WriteCommandAction.Simple(psiClass.getProject(), psiClass.getContainingFile()){
            @Override
            protected void run() throws Throwable {
                generateCompareTo(psiClass, fields);
                generateImplementsComparable(psiClass);
            }


        }.execute();
    }

    private void generateImplementsComparable(PsiClass psiClass) {
        PsiClassType[] implementsListTypes = psiClass.getImplementsListTypes();
        for(PsiClassType implementListType: implementsListTypes){
            PsiClass resolved = implementListType.resolve();
            if(resolved != null && "java.lang.Comparable".equals(resolved.getQualifiedName())) {
                return;
            }

        String implementsType = "Comparable<" + psiClass.getName() + ">";
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
            PsiJavaCodeReferenceElement referenceElement = elementFactory.createReferenceFromText(implementsType, psiClass);
            psiClass.getImplementsList().add(referenceElement);
        }
    }

    private void generateCompareTo(PsiClass psiClass, List<PsiField> fields) {
        StringBuilder builder = new StringBuilder("public int compareTo(");
        builder.append(psiClass.getName()).append(" that) {\n");
        builder.append("return com.google.com.common.collect.ComparisonChain.start()");
        for(PsiField field: fields){
            builder.append(".compareTo(this.").append(field.getName()).append(", that.");
            builder.append(field.getName()).append(")");
        }
        builder.append(".result();\n}");
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiMethod compareTo = elementFactory.createMethodFromText(builder.toString(), psiClass);
        PsiElement method = psiClass.add(compareTo);
        JavaCodeStyleManager.getInstance(psiClass.getProject()).shortenClassReferences(method);
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private PsiClass getPsiClassFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        if(psiFile == null || editor == null){
            e.getPresentation().setEnabled(false);
            return null;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);

        PsiClass psiClass = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
        if(psiClass == null){
            e.getPresentation().setEnabled(false);
        }

        return psiClass;
    }
}
