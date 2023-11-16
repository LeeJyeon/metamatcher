package com.kakaobankcard;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.JBColor;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

public class CheckMetaField extends AnAction {

    private static final Map<String, String> allowedFieldNames = initializeAllowedFieldNames();
    private static final TextAttributes highlightAttr = new TextAttributes(
            JBColor.WHITE,
            JBColor.RED,
            JBColor.RED,
            EffectType.WAVE_UNDERSCORE, Font.PLAIN);

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        showMessageBox();

        Editor editor = event.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);

        if (this.isJavaFile(editor, psiFile)) {
            checkFileAndEditor((PsiJavaFile) psiFile, editor);
        }

    }

    private void checkFileAndEditor(PsiJavaFile psiFile, Editor editor) {
        PsiClass[] allClasses = psiFile.getClasses();

        for (PsiClass aClass : allClasses) {
            PsiField[] allFields = aClass.getAllFields();

            for (PsiField aField : allFields) {
                if (this.isDeclareFieldInThisClass(aClass, aField)) {
                    Set<String> words = this.convertToWordsWillValidate(aField.getName());
                    int dictionaryWordCounts = words.size();

                    for (String word : words) {
                        if (!allowedFieldNames.containsKey(word)) {
                            --dictionaryWordCounts;
                        }
                    }

                    if (dictionaryWordCounts < words.size()) {
                        this.highlight(editor, aField);
                    }
                }
            }
        }
    }

    private static void showMessageBox() {
        Messages.showMessageDialog("Field 정합성 검사를 진행합니다.", "🥹Meta 용어 검증",
                Messages.getInformationIcon());
    }

    private static Map<String, String> initializeAllowedFieldNames() {
        MetaFileReader metaFileReader = new MetaFileReader("metadata");
        return metaFileReader.readMetaCsv();
    }

    private boolean isJavaFile(Editor editor, PsiFile psiFile) {
        return editor != null && psiFile != null && "java".equals(
                psiFile.getFileType().getDefaultExtension());
    }

    private void highlight(Editor editor, PsiField aField) {
        int startOffset = aField.getTextRange().getStartOffset() + aField.getNameIdentifier()
                .getStartOffsetInParent();
        int endOffset = startOffset + aField.getNameIdentifier().getText().length();
        editor.getMarkupModel().addRangeHighlighter(startOffset, endOffset, 5000, highlightAttr,
                HighlighterTargetArea.EXACT_RANGE);
    }

    private Set<String> convertToWordsWillValidate(@NotNull String string) {
        String[] words = StringUtils.splitByCharacterTypeCamelCase(string);
        return !Objects.isNull(words) && words.length >= 1 ?
                Arrays.stream(words)
                        .filter((word) -> word.length() > 1)
                        .filter((word) -> !StringUtils.isNumeric(word))
                        .filter((word) -> !this.isContainNonAsciiChars(word))
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet())
                : Collections.emptySet();
    }

    private boolean isContainNonAsciiChars(@NotNull String word) {
        for (int i = 0; i < word.length(); ++i) {
            if (!CharUtils.isAscii(word.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isDeclareFieldInThisClass(@NotNull PsiClass aClass, @NotNull PsiField aField) {
        return Objects.equals(aClass.getQualifiedName(),
                Objects.requireNonNull(aField.getContainingClass()).getQualifiedName());
    }

}

