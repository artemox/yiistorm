package com.yiistorm.references.referenceContributors;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.StringPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.yiistorm.helpers.PsiPhpHelper;
import com.yiistorm.references.ReferenceProviders.ControllerRenderViewReferenceProvider;
import com.yiistorm.references.ReferenceProviders.ViewRenderViewReferenceProvider;
import com.yiistorm.references.ReferenceProviders.WidgetCallReferenceProvider;
import com.yiistorm.references.YiiPsiReferenceProvider;

public class YiiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(StandardPatterns.instanceOf(PhpPsiElement.class), new YiiPsiReferenceProvider());
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(PsiElement.class).withParent(isParamListInMethodWithName(".+?widget\\(.+"))
                , new WidgetCallReferenceProvider());
        //View-to-view
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(PhpPsiElement.class)
                        .withParent(isParamListInMethodWithName(".+?render(Partial|Ajax)*\\(.+"))
                        .andNot(inFile(PlatformPatterns.string().endsWith("Controller.php")))
                , new ViewRenderViewReferenceProvider());
        //Controller-to-view
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(PhpPsiElement.class)
                        .withParent(isParamListInMethodWithName("(?sim).+?render(Partial|Ajax)*\\(.+"))
                        .and(inFile(PlatformPatterns.string().endsWith("Controller.php")))
                , new ControllerRenderViewReferenceProvider());
    }

    /**
     * Check element is param is parameterList in method reference
     */
    private PsiElementPattern.Capture<PsiElement> isParamListInMethodWithName(String name) {

        return PlatformPatterns.psiElement(PsiPhpHelper.type(PsiPhpHelper.PARAMETER_LIST))
                .withParent(
                        PlatformPatterns.psiElement(PsiPhpHelper.type(PsiPhpHelper.METHOD_REFERENCE))
                                .withText(StandardPatterns.string().matches(name))
                );
    }

    /**
     * Check file name
     */
    private PsiElementPattern.Capture<PsiElement> inFile(StringPattern namePattern) {
        return PlatformPatterns.psiElement(PsiElement.class).inFile(PlatformPatterns.psiFile().withName(namePattern));
    }
}
