import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;

/**
 * Created by qiaoshouliang on 17/6/27.
 */
public class TAGAction extends AnAction {

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

        //获取SelectionModel和Document对象
        SelectionModel selectionModel = editor.getSelectionModel();
        Document document = editor.getDocument();

        //拿到选中部分字符串
        String selectedText = selectionModel.getSelectedText();

        //得到选中字符串的起始和结束位置
        int startOffset = selectionModel.getSelectionStart();
        int endOffset = selectionModel.getSelectionEnd();

        //得到最大插入字符串（即生成的Getter和Setter函数字符串）位置
        int maxOffset = document.getTextLength() - 1;

        //计算选中字符串所在的行号，并通过行号得到下一行的第一个字符的起始偏移量
        int curLineNumber = document.getLineNumber(endOffset);
        int nextLineStartOffset = document.getLineStartOffset(curLineNumber + 1);

        //计算字符串的插入位置
        int insertOffset = maxOffset > nextLineStartOffset ? nextLineStartOffset : maxOffset;

        //得到选中字符串在Java类中对应的字段的类型
        String type = getSelectedType(document, startOffset);

        //对文档进行操作部分代码，需要放入Runnable接口中实现，由IDEA在内部将其通过一个新线程执行
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                //genGetterAndSetter为生成getter和setter函数部分
//                document.insertString(insertOffset, genLog(selectedText));
//            }
//        };
//
//        //加入任务，由IDEA调度执行这个任务
//        WriteCommandAction.runWriteCommandAction(project, runnable);



        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);

//
//        mFactory = JavaPsiFacade.getElementFactory(project);
//
        mClass = getTargetClass(editor,file);
//
//        mClass.add(mFactory.createFieldFromText("\nprivate static final String TAG = " +"mClass.getName()", mClass));




    }

    public void sayHello(String msg) {

        // Show dialog with message

        Messages.showMessageDialog(

                msg,

                "Sample",

                Messages.getInformationIcon()

        );

    }
    private String getSelectedType(Document document, int startOffset) {

        String text = document.getText().substring(0, startOffset).trim();
        int startIndex = text.lastIndexOf(' ');

        return text.substring(startIndex + 1);
    }

    private String genLog(String field) {
        if (field == null || (field = field.trim()).equals(""))
            return "";
        String upperField = field;



        String log = "\tLog.e(TAG,"+field+".toString"+")\n";


        return "\n"+log+"\n";
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



}
