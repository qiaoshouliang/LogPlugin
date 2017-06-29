import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.apache.http.util.TextUtils;

/**
 * Created by qiaoshouliang on 17/6/27.
 */
public class LogAction extends AnAction {

    private PsiElementFactory mFactory;
    private PsiClass mClass;
    @Override
    public void actionPerformed(AnActionEvent e) {
//        // TODO: insert action logic here
        //获取Editor和Project对象
      Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (editor == null||project==null)
            return;
        //获取SelectionModel对象
        SelectionModel selectionModel = editor.getSelectionModel();
        PsiElement element = e.getData(LangDataKeys.PSI_ELEMENT);
        //拿到选中部分字符串
        String selectedText = selectionModel.getSelectedText();
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        mClass = getTargetClass(editor,file);
        new LogGen(file,mClass,selectedText,element).execute();

    }

    public void sayHello(String msg) {
        Messages.showMessageDialog(
                msg,
                "Sample",
                Messages.getInformationIcon()
        );

    }

    protected PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if(element == null) {
            return null;
        } else {
            PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ?null:target;
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
