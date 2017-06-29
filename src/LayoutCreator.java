import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.EverythingGlobalScope;
import entity.Element;
import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class LayoutCreator extends WriteCommandAction.Simple {

    protected PsiFile mFile;
    protected Project mProject;
    protected PsiClass mClass;
    protected List<Element> mElements;
    protected PsiElementFactory mFactory;

    public LayoutCreator(PsiFile file, PsiClass clazz, String command, List<Element> elements) {
        super(clazz.getProject(), command);

        mFile = file;
        mProject = clazz.getProject();
        mClass = clazz;
        mElements = elements;
        mFactory = JavaPsiFacade.getElementFactory(mProject);
    }

    @Override
    public void run() throws Throwable {

        generateFields();
        generateFindViewById();
        // reformat class
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
        styleManager.optimizeImports(mFile);
        styleManager.shortenClassReferences(mClass);
        new ReformatCodeProcessor(mProject, mClass.getContainingFile(), null, false).runWithoutProgress();
    }


    /**
     * Create fields for injections inside main class
     */
    protected void generateFields() {
        for (Iterator<Element> iterator = mElements.iterator(); iterator.hasNext(); ) {
            Element element = iterator.next();

            if (!element.used) {
                iterator.remove();
                continue;
            }

            // remove duplicate field
            PsiField[] fields = mClass.getFields();
            boolean duplicateField = false;
            for (PsiField field : fields) {
                String name = field.getName();
                if (name != null && name.equals(element.getFieldName())) {
                    duplicateField = true;
                    break;
                }
            }

            if (duplicateField) {
                iterator.remove();
                continue;
            }
            String hint = element.xml.getAttributeValue("android:hint");
            mClass.add(mFactory.createFieldFromText("/** "+hint+" */\nprivate " + element.name + " " + element.getFieldName() + ";", mClass));
        }
    }

    protected void generateFindViewById() {
        PsiClass activityClass = JavaPsiFacade.getInstance(mProject).findClass(
                "android.app.Activity", new EverythingGlobalScope(mProject));
        PsiClass compatActivityClass = JavaPsiFacade.getInstance(mProject).findClass(
                "android.support.v7.app.AppCompatActivity", new EverythingGlobalScope(mProject));

        // Check for Activity class
        if ((activityClass != null && mClass.isInheritor(activityClass, true))
                || (compatActivityClass != null && mClass.isInheritor(compatActivityClass, true))
                || mClass.getName().contains("Activity")) {
            if (mClass.findMethodsByName("onCreate", false).length == 0) {
                // Add an empty stub of onCreate()
                StringBuilder method = new StringBuilder();
                method.append("@Override protected void onCreate(android.os.Bundle savedInstanceState) {\n");
                method.append("super.onCreate(savedInstanceState);\n");
                method.append("\t// TODO: add setContentView(...) and run LayoutCreator again\n");
                method.append("}");

                mClass.add(mFactory.createMethodFromText(method.toString(), mClass));
            } else {
                PsiStatement setContentViewStatement = null;
                boolean hasInitViewStatement = false;

                PsiMethod onCreate = mClass.findMethodsByName("onCreate", false)[0];
                for (PsiStatement statement : onCreate.getBody().getStatements()) {
                    // Search for setContentView()
                    if (statement.getFirstChild() instanceof PsiMethodCallExpression) {
                        PsiReferenceExpression methodExpression = ((PsiMethodCallExpression) statement.getFirstChild()).getMethodExpression();
                        if (methodExpression.getText().equals("setContentView")) {
                            setContentViewStatement = statement;
                        } else if (methodExpression.getText().equals("initView")) {
                            hasInitViewStatement = true;
                        }
                    }
                }

                if(!hasInitViewStatement && setContentViewStatement != null) {
                    // Insert initView() after setContentView()
                    onCreate.getBody().addAfter(mFactory.createStatementFromText("initView();", mClass), setContentViewStatement);
                }

                generatorLayoutCode();
            }
        }
    }



    private void generatorLayoutCode() {

        // generator findViewById code in initView() method
        StringBuilder initView = new StringBuilder();
            initView.append("private void initView() {\n");

        for (Element element : mElements) {
            initView.append(element.getFieldName() + " = (" + element.name + ")findViewById(" + element.getFullID() + ");\n");
        }

        initView.append("}\n");

        mClass.add(mFactory.createMethodFromText(initView.toString(), mClass));

    }
}