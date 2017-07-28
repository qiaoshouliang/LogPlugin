package com.qiaoshouliang.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.qiaoshouliang.gen.LogGen;
import com.qiaoshouliang.utils.EventLogger;
import org.apache.http.util.TextUtils;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;

/**
 * Created by qiaoshouliang on 17/6/27.
 */
public class LogAction extends AnAction {

    private PsiElementFactory mFactory;
    private PsiClass mClass;

    @Override
    public void actionPerformed(AnActionEvent e) {
        //获取Editor和Project对象
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (editor == null || project == null)
            return;
        // 获取PsiFile
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);
        // 根据PsiFile获取PsiClass
        mClass = getTargetClass(file);

        //获取SelectionModel对象
        SelectionModel selectionModel = editor.getSelectionModel();
        //拿到选中部分字符串
        String selectedText = selectionModel.getSelectedText();
        //获取 CaretModel 对象
        CaretModel caretModel = editor.getCaretModel();
        //获取当前光标所在的行号
        int line = caretModel.getLogicalPosition().line;
        //获取Document对象
        Document document = editor.getDocument();
        //获取该行行尾的偏移量
        int lineEndOffset = document.getLineEndOffset(line);
        // 通过PsiFile找到这个PsiElement
        PsiElement element = file.findElementAt(lineEndOffset);

        new LogGen(file, project,mClass, selectedText, element).execute();
    }

    private PsiClass getTargetClass(PsiFile file) {
        if ((file instanceof PsiJavaFile) && ((PsiJavaFile) file).getClasses().length > 0) {
            return ((PsiJavaFile) file).getClasses()[0];
        } else {
            return null;
        }

    }

    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        SelectionModel selectionModel = editor.getSelectionModel();
        //拿到选中部分字符串
        String selectedText = selectionModel.getSelectedText();
        if (!TextUtils.isEmpty(selectedText))
            e.getPresentation().setEnabled(true);
        else
            e.getPresentation().setEnabled(false);

    }
}
