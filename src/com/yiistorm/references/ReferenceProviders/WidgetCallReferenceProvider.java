package com.yiistorm.references.ReferenceProviders;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.yiistorm.helpers.CommonHelper;
import com.yiistorm.references.FileReference;
import org.jetbrains.annotations.NotNull;

public class WidgetCallReferenceProvider extends PsiReferenceProvider {

    public static final PsiReferenceProvider[] EMPTY_ARRAY = new PsiReferenceProvider[0];
    public static String projectPath;
    public static Project project;
    public static PropertiesComponent properties;

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        project = element.getProject();
        String elname = element.getClass().getName();
        properties = PropertiesComponent.getInstance(project);
        projectPath = project.getBaseDir().getCanonicalPath();
        if (elname.endsWith("StringLiteralExpressionImpl")) {

            try {
                PsiFile file = element.getContainingFile();
                VirtualFile vfile = file.getVirtualFile();
                if (vfile != null) {
                    String path = vfile.getPath();

                    VirtualFile baseDir = project.getBaseDir();
                    if (baseDir != null) {
                        String inProtectedPath = path.replace(projectPath, "");
                        String protectedPath = CommonHelper.searchCurrentProtected(inProtectedPath);
                        String widgetPath = element.getText().replace("'", "");
                        String widgetFilePath = "";
                        if (widgetPath.matches("^components.+")) {
                            widgetFilePath = protectedPath + "/" + widgetPath.replace(".", "/") + ".php";
                        } else if (widgetPath.matches("^ext\\..+")) {
                            widgetFilePath = (protectedPath + "/" + widgetPath.replace(".", "/")).replace("/ext/", "/extensions/") + ".php";
                        } else if (widgetPath.matches("^app\\..+")) {
                            widgetFilePath = widgetPath.replace(".", "/").replace("app", protectedPath) + ".php";
                        } else if (widgetPath.matches("^application.+")) {
                            widgetFilePath = widgetPath.replace(".", "/").replace("application", protectedPath) + ".php";
                        } else {
                            if (!widgetPath.contains(".")) {
                                String currentFolder = inProtectedPath.replaceAll("[a-z0-9A-Z_]+?.php", "");
                                VirtualFile existsNear = baseDir.findFileByRelativePath(currentFolder + widgetPath + ".php");
                                if (existsNear == null) {
                                    VirtualFile existsInParentDir = baseDir.findFileByRelativePath(currentFolder + ".." + "/"
                                            + widgetPath + ".php");
                                    if (existsInParentDir != null) {
                                        widgetFilePath = currentFolder + ".." + "/" + widgetPath + ".php";
                                    } else {
                                        VirtualFile existsInProtectedComponents = baseDir.findFileByRelativePath(protectedPath
                                                + "/" + "components" + "/" + widgetPath + ".php");
                                        if (existsInProtectedComponents != null) {
                                            widgetFilePath = protectedPath + "/" + "components" + "/" + widgetPath + ".php";
                                        }
                                    }
                                } else {
                                    widgetFilePath = currentFolder + widgetPath + ".php";
                                }
                            }
                        }
                        VirtualFile widgetfile = baseDir.findFileByRelativePath(widgetFilePath);
                        VirtualFile protectedPathDir = (!protectedPath.equals("")) ? baseDir.findFileByRelativePath(protectedPath) : null;

                        String str = element.getText();
                        TextRange textRange = CommonHelper.getTextRange(element, str);
                        String uri = str.substring(textRange.getStartOffset(), textRange.getEndOffset());
                        int start = textRange.getStartOffset();
                        int len = textRange.getLength();

                        if (widgetfile != null) {
                            PsiReference ref = new FileReference(widgetfile, uri, element,
                                    new TextRange(start, start + len), project, protectedPathDir, protectedPathDir);
                            return new PsiReference[]{ref};
                        }
                        return PsiReference.EMPTY_ARRAY;
                    }
                }
            } catch (Exception e) {
                System.err.println("error" + e.getMessage());
            }
        }
        return PsiReference.EMPTY_ARRAY;
    }
}
