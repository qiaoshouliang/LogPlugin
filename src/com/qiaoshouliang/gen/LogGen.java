package com.qiaoshouliang.gen;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Created by qiaoshouliang on 17/6/27.
 */
public class LogGen extends WriteCommandAction.Simple {


    private String selectedText;
    protected PsiFile mFile;
    protected Project mProject;
    protected PsiClass mClass;
    protected PsiElementFactory mFactory;
    private PsiElement selectElement;

    public LogGen(PsiFile file, Project project, PsiClass clazz, String selectedText, PsiElement selectElement) {
        super(project,file);
        this.selectElement = selectElement;
        mFile = file;
        mProject = project;
        mClass = clazz;
        mFactory = JavaPsiFacade.getElementFactory(mProject);
        this.selectedText = selectedText;
    }

    @Override
    protected void run() throws Throwable {
        PsiField field = mClass.findFieldByName("TAG", false);
        if (field == null) {
            mClass.add(mFactory.createFieldFromText("private String TAG =" + "\"" + mClass.getName() + "\"" + ";", mClass));
        }
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(selectElement, PsiMethod.class);
        if (psiMethod==null){
            Messages.showErrorDialog("Generation failed, " +
                            selectedText + " be not in method",
                    selectedText+"is not variable");
            return;
        }

        psiMethod.getBody().addAfter(genLog(selectedText), selectElement);

        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
        styleManager.optimizeImports(mFile);
        styleManager.shortenClassReferences(mClass);
//        new ReformatCodeProcessor(mProject, mClass.getContainingFile(), null, false).runWithoutProgress();

    }


    private PsiStatement genLog(String field) {
        if (field == null || (field = field.trim()).equals(""))
            return null;
        String log = "android.util.Log.e(TAG," + "\"" + mClass.getName() + " <" + PsiTreeUtil.getParentOfType(selectElement, PsiMethod.class).getName() + "> " + "\"+" + field + ".toString());";
        return mFactory.createStatementFromText(log, mClass);
    }


}
