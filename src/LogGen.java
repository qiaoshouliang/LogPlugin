import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
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
    private PsiElement start;
    public LogGen(PsiFile file, PsiClass clazz, String selectedText,PsiElement start) {
        super(clazz.getProject());
        this.start = start;
        mFile = file;
        mProject = clazz.getProject();
        mClass = clazz;
        mFactory = JavaPsiFacade.getElementFactory(mProject);
        this.selectedText = selectedText;
    }

    @Override
    protected void run() throws Throwable {
        PsiField field = mClass.findFieldByName("TAG",false);
        if (field==null) {
            mClass.add(mFactory.createFieldFromText("private String TAG =" + "\""+mClass.getName() + "\""+";", mClass));
        }

        start.getParent().addAfter(genLog(selectedText),start);
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
        styleManager.optimizeImports(mFile);
        styleManager.shortenClassReferences(mClass);
        new ReformatCodeProcessor(mProject, mClass.getContainingFile(), null, false).runWithoutProgress();
    }

    private PsiElement genLog(String field) {
        if (field == null || (field = field.trim()).equals(""))
            return null;
        String log = "Log.e(TAG,"+"\""+mClass.getName()+" < "+ PsiTreeUtil.getParentOfType(start,PsiMethod.class).getName()+" > "+"\"+"+field+".toString());";
        return mFactory.createStatementFromText(log, mClass);
    }


}
