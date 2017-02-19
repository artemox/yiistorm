package com.yiistorm.helpers;

import com.intellij.ide.util.gotoByName.GotoClassModel2;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for managing and parsing Psi files for PHP
 *
 * @author Enrique Piatti
 */
public class PsiPhpHelper {

    public static final String LANGUAGE_PHP = "PHP";
    public static final String ASSIGNMENT_EXPRESSION = "Assignment expression";
    public static final String VARIABLE_DECLARATION = "Variable";
    public static final String VARIABLE = "variable";
    public static final String METHOD_REFERENCE = "Method reference";
    public static final String WHITE_SPACE = "WHITE_SPACE";
    public static final String ASSIGN = "assign";
    public static final String STATEMENT = "Statement";
    public static final String GROUP_STATEMENT = "Group statement";
    public static final String CLASS_METHOD = "Class method";
    public static final String CLASS = "Class";
    public static final String FILE = "FILE";
    public static final String ARROW = "arrow";
    public static final String COMMENT_BLOCK = "C style comment";
    public static final String COMMENT_LINE = "line comment";
    public static final String IDENTIFIER = "identifier";
    public static final String PARAMETER_LIST = "Parameter list";
    public static final String PARAMETER = "Parameter";
    public static final String STRING = "String";
    public static final String SINGLE_QUOTED_STRING = "single quoted string";
    public static final String DOUBLE_QUOTED_STRING = "double quoted string";
    public static final String EXTENDS_LIST = "Extends list";
    public static final String CLASS_REFERENCE = "Class reference";

    public static IElementType type(String name) {
        return new IElementType(name, PhpLanguage.INSTANCE);
    }

    @NotNull
    private static String getElementType(PsiElement psiElement) {
        if (psiElement != null && psiElement.getNode() != null && psiElement.getNode().getElementType() != null) {
            return psiElement.getNode().getElementType().toString();
        }
        return "";
    }

    public static boolean isElementType(PsiElement psiElement, String type) {
        return isElementType(psiElement, new String[]{type});
    }

    public static boolean isElementType(PsiElement psiElement, String[] types) {
        if (psiElement != null && types != null) {
            String elementType = getElementType(psiElement);
            if (!elementType.isEmpty()) {
                for (String type : types) {
                    if (elementType.equals(type))
                        return true;
                }
            }
        }
        return false;
    }

    public static boolean isNotElementType(PsiElement psiElement, String[] types) {
        return !isElementType(psiElement, types);
    }

    public static boolean isNotElementType(PsiElement psiElement, String type) {
        return isNotElementType(psiElement, new String[]{type});
    }

    public static boolean isPhp(PsiElement psiElement) {
        return psiElement.getLanguage().getID().equals(LANGUAGE_PHP);
    }

    public static boolean isVariable(PsiElement psiElement) {
        String[] types = {VARIABLE_DECLARATION, VARIABLE};
        return isElementType(psiElement, types);
    }

    public static boolean isMethodRefence(PsiElement psiElement) {
        String[] types = {METHOD_REFERENCE};
        return isElementType(psiElement, types);
    }

    public static boolean isWhiteSpace(PsiElement psiElement) {
        String[] types = {WHITE_SPACE};
        return isElementType(psiElement, types);
    }

    public static boolean isIdentifier(PsiElement psiElement) {
        String[] types = {IDENTIFIER};
        return isElementType(psiElement, types);
    }

    public static boolean isNotWhiteSpaceOrComment(PsiElement psiElement) {
        String[] types = {WHITE_SPACE, COMMENT_BLOCK, COMMENT_LINE};
        return isNotElementType(psiElement, types);
    }

    public static boolean isAssignmentExpression(PsiElement psiElement) {
        String[] types = {ASSIGNMENT_EXPRESSION};
        return isElementType(psiElement, types);
    }

    public static boolean isAssign(PsiElement psiElement) {
        String[] types = {ASSIGN};
        return isElementType(psiElement, types);
    }

    public static boolean isStatement(PsiElement psiElement) {
        String[] types = {STATEMENT};
        return isElementType(psiElement, types);
    }

    public static boolean isClassMethod(PsiElement psiElement) {
        String[] types = {CLASS_METHOD};
        return isElementType(psiElement, types);
    }


    public static boolean isString(PsiElement psiElement) {
        String[] types = {STRING, SINGLE_QUOTED_STRING, DOUBLE_QUOTED_STRING};
        boolean isAnyString = isElementType(psiElement, types);
        if (!isAnyString) {
            PsiElement parent = psiElement.getParent();
            String[] typeString = {STRING};
            return isElementType(parent, typeString);
        }
        return true;
    }

    public static boolean isParameterList(PsiElement psiElement) {
        String[] types = {PARAMETER_LIST};
        return isElementType(psiElement, types);
    }

    public static String getVarName(PsiElement psiElement) {
        if (getElementType(psiElement).equals(VARIABLE)) {
            return psiElement.getText();    // with "$"
        } else if (getElementType(psiElement).equals(VARIABLE_DECLARATION)) {
            //return psiElement.getChildren()[0].getText();
            return psiElement.getText();
        }
        return null;
    }

    public static boolean isArrow(PsiElement psiElement) {
        String[] types = {ARROW};
        return isElementType(psiElement, types);
    }

    public static boolean hasChainedMethod(PsiElement psiElement) {
        PsiElement[] children = psiElement.getChildren();
        if (children.length > 0) {
            PsiElement child = children[0];
            while (child != null) {
                if (isArrow(child))
                    return true;
                child = child.getNextSibling();
            }
        }
        return false;
    }

    /**
     * @param psiMethodReference
     * @return
     */
    public static String getMethodName(PsiElement psiMethodReference) {
        if (isMethodRefence(psiMethodReference)) {
            PsiElement[] children = psiMethodReference.getChildren();
            if (children.length > 0) {

                // This fails because getChildren is not returning all the children !! why???
                for (PsiElement child : children) {
                    if (isIdentifier(child)) {
                        return child.getText();
                    }
                }

                // the identifier after the arrow is the method name
                // TODO add support for static methods
                PsiElement child = children[0];
                boolean prevSiblingWasArrow = false;
                while (child != null) {
                    // if previous element was an arrow, and the element is an identifier (this bypass whitespaces...)
                    if (prevSiblingWasArrow && isIdentifier(child)) {
                        // and the next element is a parenthesis (this bypass properties)
                        PsiElement nextSibling = child.getNextSibling();
                        if (nextSibling != null && nextSibling.getText().equals("(")) {
                            return child.getText();
                        }
                    } else {
                        if (isArrow(child)) {
                            prevSiblingWasArrow = true;
                        } else if (!(child instanceof PsiWhiteSpace)) {
                            // allow methods separated of the arrow by whitespaces
                            prevSiblingWasArrow = false;
                        }
                    }
                    child = child.getNextSibling();
                }
            }
        }
        return null;
    }


    public static List<PsiElement> getChainedMethodReferences(PsiElement psiElement) {
        if (psiElement == null) {
            return null;
        }
        List<PsiElement> methods = new ArrayList<PsiElement>();
        if (isMethodRefence(psiElement)) {
            methods.add(psiElement);
        }
        PsiElement childMethodReference = PsiPhpHelper.findFirstChildOfType(psiElement, METHOD_REFERENCE);
        while (childMethodReference != null) {
            methods.add(childMethodReference);
            childMethodReference = PsiPhpHelper.findFirstChildOfType(childMethodReference, METHOD_REFERENCE);
        }

        if (methods.size() > 0) {
            Collections.reverse(methods);
        }

        return methods;
    }

    public static List<String> getChainedMethodNames(PsiElement psiElement) {
        if (psiElement == null) {
            return null;
        }

        List<String> methodNames = new ArrayList<String>();

        List<PsiElement> methods = getChainedMethodReferences(psiElement);
        if (methods != null) {
            for (PsiElement methodReference : methods) {
                String methodName = getMethodName(methodReference);
                if (methodName != null) {
                    methodNames.add(methodName);
                }
            }
        }

        return methodNames;

    }

    public static String getLastChainedMethodName(PsiElement psiElement) {
        List<String> methods = getChainedMethodNames(psiElement);
        if (methods != null && methods.size() > 0) {
            return methods.get(methods.size() - 1);
        }
        return null;
    }


    public static PsiElement findFirstChildOfType(PsiElement psiElement, String type) {
        PsiElement[] children = psiElement.getChildren();
        if (children.length > 0) {
            for (PsiElement child : children) {
                //  System.err.println(child.getNode().getElementType().toString() + " VVV " + child.getText());
                if (isElementType(child, new String[]{type})) {
                    return child;
                }
            }
        }
        return null;
    }


    public static PsiElement findNextSiblingOfType(PsiElement psiElement, String type) {
        String[] types = {type};
        return findNextSiblingOfType(psiElement, types);
    }

    public static PsiElement findNextSiblingOfType(PsiElement psiElement, String[] types) {
        PsiElement siblingElement = psiElement.getNextSibling();
        while (siblingElement != null && isNotElementType(siblingElement, types)) {
            siblingElement = siblingElement.getNextSibling();
        }
        return siblingElement;
    }

    public static PsiElement findPrevSiblingOfType(PsiElement psiElement, String type) {
        String[] types = {type};
        return findPrevSiblingOfType(psiElement, types);
    }

    public static PsiElement findPrevSiblingOfType(PsiElement psiElement, String[] types) {
        PsiElement siblingElement = psiElement.getPrevSibling();
        while (siblingElement != null && isNotElementType(siblingElement, types)) {
            siblingElement = siblingElement.getPrevSibling();
            //     System.err.println(siblingElement.getNode().getElementType().toString() + " <<< " + siblingElement.getText());
        }
        return siblingElement;
    }

    public static PsiElement findFirstParentOfType(PsiElement psiElement, String type) {
        String[] types = {type};
        return findFirstParentOfType(psiElement, types);
    }

    public static PsiElement findFirstParentOfType(PsiElement psiElement, String[] types) {
        return findFirstParentOfType(psiElement, types, null);
    }

    public static PsiElement findFirstParentOfType(PsiElement psiElement, String type, String limitType) {
        String[] types = {type};
        String[] limits = {limitType};
        return findFirstParentOfType(psiElement, types, limits);
    }

    public static PsiElement findFirstParentOfType(PsiElement psiElement, String[] types, String[] limitTypes) {
        if (psiElement != null) {
            PsiElement parentElement = psiElement.getParent();
            boolean isRequestingSecuredLimits = false;
            if (limitTypes != null && limitTypes.length > 0) {
                for (String limit : limitTypes) {
                    if (limit.equals(FILE) || limit.equals(CLASS) || limit.equals(GROUP_STATEMENT)) {
                        isRequestingSecuredLimits = true;
                        break;
                    }
                }
            }
            if (!isRequestingSecuredLimits) {
                String[] securityLimits = {FILE, CLASS, GROUP_STATEMENT};
                limitTypes = (String[]) ArrayUtils.addAll(limitTypes, securityLimits);
            }

            while (parentElement != null && isNotElementType(parentElement, types)) {
                if (limitTypes != null) {
                    if (isElementType(parentElement, limitTypes))
                        return null;
                }
                parentElement = parentElement.getParent();
            }
            return parentElement;
        }
        return null;

    }


    /**
     * The IDE is not returning the full list of children sometimes when using "PsiElemetn.getChildren()" I don'w know why, this method fixes that
     *
     * @return
     */
    @NotNull
    public static List<PsiElement> getFullListOfChildren(PsiElement psiElement) {
        List<PsiElement> children = new ArrayList<PsiElement>();
        if (psiElement != null) {
            // this doesn't work as expected for example for a Method Reference it returns only Class reference and Parameter List
            // it doesn't return the scope resolution (or arrow), psiwhitespaces (if exists), identifier (method name), etc...
            // PsiElement[] children = psiElement.getChildren();
            PsiElement child = psiElement.getFirstChild();
            while (child != null) {
                children.add(child);
                child = child.getNextSibling();
            }
        }
        return children;
    }

    /**
     * this returns a list because could be more than one class with the same name
     *
     */
    @NotNull
    public static List<PsiElement> getPsiElementsFromClassName(String className, Project project) {
        List<String> classes = new ArrayList<String>();
        classes.add(className);
        return getPsiElementsFromClassesNames(classes, project);
    }

    /**
     * useful for go to declaration using only the class name
     *
     */
    @NotNull
    public static List<PsiElement> getPsiElementsFromClassesNames(List<String> classes, Project project) {
        List<PsiElement> psiElements = new ArrayList<PsiElement>();
        if (classes != null && project != null) {
            GotoClassModel2 model = new GotoClassModel2(project);
            for (String className : classes) {
                Object[] elements = model.getElementsByName(className, true, className);
                if (elements.length > 0) {
                    for (Object element : elements) {
                        if (element instanceof PsiElement) {
                            psiElements.add((PsiElement) element);

                        }
                    }
                }
            }

        }
        return psiElements;
    }


    @NotNull
    public static List<PsiElement> findMethodInClass(String methodName, String className, Project project) {
        return findMethodInClass(methodName, className, project, false);
    }

    @NotNull
    public static List<PsiElement> findMethodInClass(String methodName, String className, Project project, boolean inherited) {
        List<PsiElement> methods = new ArrayList<PsiElement>();
        if (methodName != null && className != null && !methodName.isEmpty() && !className.isEmpty()) {
            List<PsiElement> classes = getPsiElementsFromClassName(className, project);
            if (classes.size() > 0) {
                for (PsiElement psiClass : classes) {
                    PsiElement method = findMethodInClass(methodName, psiClass, inherited);
                    if (method != null) {
                        methods.add(method);
                    }
                }
            }
        }
        return methods;
    }


    public static PsiElement findMethodInClass(String methodName, PsiElement psiClass) {
        return findMethodInClass(methodName, psiClass, false);
    }

    public static PsiElement findMethodInClass(String methodName, PsiElement psiClass, boolean inherited) {
        if (methodName != null && !methodName.isEmpty() && psiClass != null) {
            PsiElement[] children = psiClass.getChildren();
            PsiElement extendsList = null;
            for (PsiElement child : children) {
                if (isClassMethod(child)) {
                    if (methodName.equals(((PsiNamedElement) child).getName())) {
                        return child;
                    }
                } else if (inherited && isElementType(child, EXTENDS_LIST)) {
                    extendsList = child;
                }
            }

            // postponed read of inherited methods so original methods have higher priority
            if (extendsList != null) {
                PsiElement[] extendsElements = extendsList.getChildren();
                for (PsiElement extendsElement : extendsElements) {
                    if (isElementType(extendsElement, CLASS_REFERENCE)) {
                        List<PsiElement> classes = getPsiElementsFromClassName(extendsElement.getText(), extendsElement.getProject());
                        for (PsiElement parentClass : classes) {
                            PsiElement method = findMethodInClass(methodName, parentClass, true);
                            if (method != null) {
                                return method;
                            }
                        }
                        break;
                    }
                }
            }

        }
        return null;
    }


    public static String getParentClassName(PsiElement child) {
        return getExtendsClassName(child);
    }

    public static String getSuperClassName(PsiElement child) {
        PsiElement superclass = PsiPhpHelper.getSuperClassElement(child);
        return superclass.getText();
    }

    public static PsiElement getSuperClassElement(PsiElement child) {
        String parent = getExtendsClassNameForClass(child);
        List<PsiElement> classes = getPsiElementsFromClassName(parent, child.getProject());
        if (classes.size() > 0) {
            for (PsiElement parentClass : classes) {
                return getSuperClassElement(parentClass);
            }
        }
        return child;
    }

    public static boolean isExtendsSuperclass(PsiElement child, String superclass) {
        if (child == null) {
            return false;
        }

        try {
            PhpClass[] supers = ((PhpClass) child).getSupers();
            String className = ((PhpClass) child).getName().toLowerCase();
            if (className.equals(superclass.toLowerCase())) {
                return true;
            }
            for (PhpClass cl : supers) {
                if (cl.getName().toLowerCase().equals(superclass.toLowerCase())) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static String getExtendsClassName(PsiElement child) {
        PsiElement psiClass = getClassElement(child);
        if (psiClass != null) {
            PsiElement[] children = psiClass.getChildren();
            for (PsiElement psiClassChild : children) {
                if (isElementType(psiClassChild, EXTENDS_LIST)) {
                    PsiElement[] extendsElements = psiClassChild.getChildren();
                    for (PsiElement extendsElement : extendsElements) {
                        if (isElementType(extendsElement, CLASS_REFERENCE)) {
                            return extendsElement.getText();

                        }
                    }
                    break;
                }
            }
        }
        return null;
    }

    public static String getExtendsClassNameForClass(PsiElement psiClass) {
        if (psiClass != null) {
            PsiElement[] children = psiClass.getChildren();
            for (PsiElement psiClassChild : children) {
                if (isElementType(psiClassChild, EXTENDS_LIST)) {
                    PsiElement[] extendsElements = psiClassChild.getChildren();
                    for (PsiElement extendsElement : extendsElements) {
                        if (isElementType(extendsElement, CLASS_REFERENCE)) {
                            return extendsElement.getText();

                        }
                    }
                    break;
                }
            }
        }
        return null;
    }


    public static PsiElement getparentClassElement(PsiElement child) {
        return getExtendsClassElement(child);
    }

    /**
     * returns the first class element (is possible to have more than one psiClass for the same class name but this only returns the first)
     *
     */
    public static PsiElement getExtendsClassElement(PsiElement child) {
        String extendsClassName = getExtendsClassName(child);
        if (extendsClassName != null) {
            List<PsiElement> classes = getPsiElementsFromClassName(extendsClassName, child.getProject());
            for (PsiElement parentClass : classes) {
                return parentClass;
            }
        }
        return null;
    }

    public static PsiElement getClassElement(PsiElement child) {
        PsiElement[] elc = child.getContainingFile().getChildren();
        if (elc.length > 0 && elc[0] != null) {
            for (PsiElement element : elc[0].getChildren()) {
                if (PlatformPatterns.psiElement().withElementType(PhpElementTypes.CLASS).accepts(element)) {
                    return element;
                }
            }
        }
        return null;
    }

    public static String getClassName(PsiElement child) {
        PsiElement psiClass = getClassElement(child);
        if (psiClass != null) {
            PsiElement classNameElement = findNextSiblingOfType(psiClass.getFirstChild(), IDENTIFIER);
            if (classNameElement != null) {
                return classNameElement.getText();
            }
        }
        return null;
    }

    public static String getClassIdentifierName(PsiElement psiClass) {
        if (psiClass != null) {
            PsiElement classNameElement = findNextSiblingOfType(psiClass.getFirstChild(), IDENTIFIER);
            if (classNameElement != null) {
                return classNameElement.getText();
            }
        }
        return null;
    }


    @NotNull
    public static List<PsiNamedElement> getAllMethodsFromClass(@NotNull PsiElement psiClass, boolean includeInherited) {
        List<PsiNamedElement> methods = new ArrayList<PsiNamedElement>();
        PsiElement[] children = psiClass.getChildren();   // getFullListOfChildren

        PsiElement extendsList = null;

        for (PsiElement child : children) {
            if (isClassMethod(child)) {
                methods.add((PsiNamedElement) child);
            } else if (includeInherited && isElementType(child, EXTENDS_LIST)) {
                extendsList = child;
            }
        }

        // postponed read of inherited methods so the original methos are first on the list
        if (extendsList != null) {
            PsiElement[] extendsElements = extendsList.getChildren();
            for (PsiElement extendsElement : extendsElements) {
                if (isElementType(extendsElement, CLASS_REFERENCE)) {
                    List<PsiElement> classes = getPsiElementsFromClassName(extendsElement.getText(), extendsElement.getProject());
                    for (PsiElement parentClass : classes) {
                        methods.addAll(getAllMethodsFromClass(parentClass, true));
                    }
                    break;
                }
            }
        }

        return methods;
    }

    public static boolean isMethodProtected(@NotNull PsiElement psiMethod) {
        return psiMethod.getText().startsWith("protected");
    }

    public static boolean isMethodPublic(@NotNull PsiElement psiMethod) {
        return psiMethod.getText().startsWith("public");
    }

    public static boolean isMethodPrivate(@NotNull PsiElement psiMethod) {
        return psiMethod.getText().startsWith("private");
    }


    @NotNull
    public static List<PsiElement> getMethodParameters(@NotNull PsiElement psiMethod) {
        List<PsiElement> params = new ArrayList<PsiElement>();
        PsiElement parameterList = findFirstChildOfType(psiMethod, PARAMETER_LIST);
        if (parameterList != null) {
            PsiElement[] parameters = parameterList.getChildren();
            for (PsiElement parameter : parameters) {
                if (isElementType(parameter, PARAMETER)) {
                    params.add(parameter);
                }
            }
        }
        return params;
    }

    public static String getParameterName(@NotNull PsiElement parameter) {
        List<PsiElement> parameterChildren = PsiPhpHelper.getFullListOfChildren(parameter); // parameter.getChildren();
        if (parameterChildren.size() > 0) {
            for (PsiElement child : parameterChildren) {
                if (isElementType(child, VARIABLE)) {
                    return child.getText();
                }
            }
        }
        return null;
    }

}
