package com.kakaobankcard;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CheckMetaField extends AnAction {

    private static final Map<String, String> allowedFieldNames = initializeAllowedFieldNames();

    private static Map<String, String> initializeAllowedFieldNames() {
        // 여기에 특정 string을 매핑하여 사용 가능한 필드 이름을 정의합니다.
        Map<String, String> allowedNames = new HashMap<>();
        allowedNames.put("name", "String");
        allowedNames.put("age", "int");
        // 추가 필드 정의 가능
        return allowedNames;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // 현재 열려있는 에디터에서 PSI 파일 가져오기
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) {
            return;
        }

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return;
        }

        // 클래스 검증 수행
        verifyClass(psiFile, editor);
    }


    private void verifyClass(PsiFile psiFile, Editor editor) {
        psiFile.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                PsiModifierList modifierList = field.getModifierList();
                if (modifierList != null) {
                    String fieldName = field.getName();
                    if (fieldName != null) {
                        String allowedType = allowedFieldNames.get(fieldName);
                        if (allowedType == null) {
                            // 필드 이름이 허용되지 않는 경우 하이라이팅
                            highlightField(editor, field);
                        }
                    }
                }
            }
        });
    }

    private void highlightField(Editor editor, PsiField field) {
        int startOffset = field.getTextOffset();
        int endOffset = startOffset + field.getTextLength();
        editor.getSelectionModel().setSelection(startOffset, endOffset);
    }
}
