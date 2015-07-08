package com.yiistorm.helpers;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocReturnTag;
import com.jetbrains.php.lang.psi.elements.impl.*;

import java.util.ArrayList;
import java.util.Arrays;

public class PsiPhpTypeHelper {
    public static String detectType(PsiElement psiElement) {
        String type = "";
        if (psiElement.toString().equals("Method reference")) {
            PhpReferenceImpl composite_value = (PhpReferenceImpl) psiElement;
            if (composite_value.resolve() != null) {
                MethodImpl cm = (MethodImpl) composite_value.resolve();

                if (cm != null) {
                    PhpDocComment phpDoc = cm.getDocComment();
                    if (phpDoc != null && phpDoc.getReturnTag() != null) {
                        PhpDocReturnTag returnTag = phpDoc.getReturnTag();
                        if (returnTag != null) {
                            type = returnTag.getType().toStringResolved();
                        }
                    }
                }
            }

        } else if (psiElement.toString().equals("Number")) {
            PhpExpressionImpl composite_value = (PhpExpressionImpl) psiElement;
            type = composite_value.getType().toStringResolved();
        }
        //Class::CONST
        else if (psiElement.toString().equals("Class constant reference")) {
            ClassConstantReferenceImpl value_cri = (ClassConstantReferenceImpl) psiElement;
            type = value_cri.getText();
        }
        //null,bool,etc..
        else if (psiElement.toString().equals("Constant reference")) {
            ConstantReferenceImpl value_cri = (ConstantReferenceImpl) psiElement;

            type = value_cri.getType().toStringResolved();
        }
        // new Class();
        else if (psiElement.toString().equals("New expression")) {
            PsiElement[] value_new_exr = psiElement.getChildren();
            if (value_new_exr.length > 0) {
                PsiElement classref = value_new_exr[0];
                if (classref.toString().equals("Class reference")) {
                    type = classref.getText();
                } else {   //can't detect class
                    type = "";
                    //System.err.println("Bad 'New expression' founded by phpstorm :" + value_new_exr.toString());
                }
            }
        }
        // $var
        else if (psiElement.toString().equals("Variable")) {

            VariableImpl psi = (VariableImpl) psiElement;
            type = psi.getType().toStringResolved();
            if (type.matches("(?sim)^#M#M#C\\\\[a-z_0-9]+?.model.find.+?\\?")) {
                try {
                    VariableImpl vd = (VariableImpl) psi.resolve();
                    if (type.matches(".+\\|[^\\?]+.*")) {
                        String[] types = type.replaceAll("\\|\\?$", "").split("\\|");
                        ArrayList<String> typeList = new ArrayList<String>();
                        for (String typeName : types) {
                            if (typeName.matches("(?sim)\\.model\\.find.*")) {
                                typeList.add(typeName.replaceFirst("(?sim)^.+\\\\", "").replaceAll("(?sim)\\.model.+", ""));
                            } else {
                                typeList.add(typeName);
                            }
                        }
                        if (typeList.size() > 0) {
                            return Arrays.toString(typeList.toArray());
                        }

                        PsiElement findMetodCall = vd.getNextPsiSibling();
                        if (findMetodCall != null) {
                            MethodReferenceImpl modelStaticCall = (MethodReferenceImpl) findMetodCall.getFirstChild();
                            if (modelStaticCall != null) {
                                type = modelStaticCall.getText().replaceAll("(?sim)::.+", "");
                            }
                        }
                    }
                } catch (Exception e) {
                    type = "";
                }
            } else if (type.startsWith("#F") || type.startsWith("#M")) {
                PsiElement vd = (PsiElement) psi.resolve();
                if (vd != null) {
                    PsiElement last = vd.getNextSibling();
                    type = PsiPhpTypeHelper.detectType(last);
                }
            }

        }
        // Function call
        else if (psiElement.toString().equals("Function call")) {

            FunctionReferenceImpl psi = (FunctionReferenceImpl) psiElement;
            FunctionImpl functionEl = (FunctionImpl) psi.resolve();
            if (functionEl != null) {
                PhpDocComment pdc = functionEl.getDocComment();
                if (pdc != null) {
                    PhpDocReturnTag pdrt = pdc.getReturnTag();
                    if (pdrt != null) {
                        type = pdrt.getType().toString();
                        if (type.startsWith("#F")) {
                            type = " ";
                        } else if (type.startsWith("#")) {
                            VariableImpl vd = (VariableImpl) psi.resolve();
                            if (vd != null) {
                                PsiElement last = vd.getNextPsiSibling();
                                type = PsiPhpTypeHelper.detectType(last);
                            }
                        }
                    }
                }
            }
        }
        //all over types
        else {

            try {
                VariableImpl psi = (VariableImpl) psiElement;
                type = psi.getType().toStringResolved();
            } catch (Exception e) {
                type = psiElement.toString();
            }
        }

        //Standartize some types
        if (type.startsWith("#F")) {
            type = " ";
        }
        if (type.equals("Array creation expression")) {
            type = "mixed";
        }

        return type;
    }
}
