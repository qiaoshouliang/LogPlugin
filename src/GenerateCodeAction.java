import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SyntheticElement;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.awt.RelativePoint;
import entity.Element;
import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangke on 16/11/19.
 */
public class GenerateCodeAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        Project project = e.getProject();
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (null == editor) {
            return;
        }

        SelectionModel model = editor.getSelectionModel();
        //获取选中内容
        final String selectedText = model.getSelectedText();
        if (TextUtils.isEmpty(selectedText)) {
            Utils.showNotification(project,MessageType.ERROR,"请选中生成内容");
            return;
        }
        //Utils.showNotification(project,MessageType.ERROR,selectedText);

        PsiFile[] mPsiFiles = FilenameIndex.getFilesByName(project, selectedText+".xml", GlobalSearchScope.allScope(project));
        if (mPsiFiles.length<=0){
            Utils.showNotification(project,MessageType.INFO,"所输入的布局文件没有找到!");
            return;
        }

        XmlFile xmlFile =  (XmlFile) mPsiFiles[0];

        //解析布局文件

        List<Element> elements = new ArrayList<>();
        Utils.getIDsFromLayout(xmlFile,elements);

        if (!elements.isEmpty()) {
            PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
            new LayoutCreator(file, getTargetClass(editor, file), "Generate Injections", elements).execute();
            Utils.showNotification(project, MessageType.INFO, "文件生成成功");
        } else {
            Utils.showNotification(project, MessageType.ERROR, "在layout中没有ID被找到");
        }


    }
    protected PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if(element == null) {
            return null;
        } else {
            PsiClass target = (PsiClass) PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ?null:target;
        }
    }

}
