package org.openl.rules.project.instantiation.variation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.binding.MethodUtil;
import org.openl.exception.OpenLCompilationException;
import org.openl.exception.OpenlNotCheckedException;

/**
 * InvocationHandler for proxy that injects variations into service class.
 * 
 * Handles both original methods and enhanced with variations.
 * 
 * @author PUdalau
 */
class VariationsInvocationHandler implements InvocationHandler {

    private final Log log = LogFactory.getLog(VariationsInvocationHandler.class);

    private Map<Method, Method> methodsMap;
    private Map<Method, Method> variationsFromRules;
    private Object serviceClassInstance;

    public VariationsInvocationHandler(Map<Method, Method> methodsMap, Object serviceClassInstance) throws OpenLCompilationException {
        this.methodsMap = methodsMap;
        this.serviceClassInstance = serviceClassInstance;
        initVariationFromRules(methodsMap, serviceClassInstance);
    }

    private void initVariationFromRules(Map<Method, Method> methodsMap, Object serviceClassInstance) throws OpenLCompilationException {
        variationsFromRules = new HashMap<Method, Method>();
        for (Method method : methodsMap.keySet()) {
            VariationsFromRules annotation = method.getAnnotation(VariationsFromRules.class);
            if (annotation != null) {
                String ruleName = annotation.ruleName();
                Class<?>[] parameterTypes = Arrays.copyOf(method.getParameterTypes(),
                    method.getParameterTypes().length - 1);
                Method variationsGetter = MethodUtils.getMatchingAccessibleMethod(serviceClassInstance.getClass(),
                    ruleName,
                    parameterTypes);
                if (variationsGetter != null) {
                    variationsFromRules.put(method, variationsGetter);
                } else {
                    throw new OpenLCompilationException("Can not find variation from rules getter for method " + MethodUtil.printMethod(method.getName(),
                        method.getParameterTypes()) + ". Make sure you have method " + MethodUtil.printMethod(ruleName,
                        parameterTypes) + " in service class.");
                }
            }
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method member = methodsMap.get(method);
        if (VariationsEnhancerHelper.isEnhancedMethod(method)) {
            log.debug(String.format("Invoking service class method with variations: %s -> %s",
                method.toString(),
                member.toString()));
            return calculateWithVariations(method, args, member);
        } else {
            log.debug(String.format("Invoking service class method without variations: %s -> %s",
                method.toString(),
                member.toString()));
            return calculateWithoutVariations(args, member);
        }
    }

    /**
     * Simple invocation.
     */
    public Object calculateWithoutVariations(Object[] args, Method member) throws Exception {
        return member.invoke(serviceClassInstance, args);
    }

    /**
     * Calculate with variations.
     */
    @SuppressWarnings("rawtypes")
    public Object calculateWithVariations(Method method, Object[] args, Method member) throws Exception {
        VariationsPack variationsPack = getVariationsPack(method, args);
        VariationsResult variationsResults = new VariationsResult();

        Object[] arguments = Arrays.copyOf(args, args.length - 1);

        // invoke without variations
        calculateSingleVariation(member, variationsResults, arguments, new NoVariation());

        if (variationsPack != null) {
            for (Variation variation : variationsPack.getVariations()) {
                calculateSingleVariation(member, variationsResults, arguments, variation);
            }
        }

        return variationsResults;
    }

    private VariationsPack getVariationsPack(Method method, Object[] args) throws Exception {
        VariationsPack variationsPack = (VariationsPack) args[args.length - 1];
        Method variationsGetter = variationsFromRules.get(method);
        if (variationsGetter != null) {
            if (variationsPack == null) {
                variationsPack = new VariationsPack();
            }
            VariationDescription[] variationDescriptions = getVariationsFromRules(args, variationsGetter);
            for (VariationDescription description : variationDescriptions) {
                try {
                    variationsPack.addVariation(VariationsFactory.getVariation(description));
                } catch (Exception e) {
                    log.error("Failed to create variation defined in rules with id: " + description.getVariationID(), e);
                }
            }
        }
        return variationsPack;
    }

    private VariationDescription[] getVariationsFromRules(Object[] args,
            Method variationsGetter) {
        try {
            Object[] argumentsForVariationsGetter = Arrays.copyOf(args, args.length - 1);
            return (VariationDescription[]) variationsGetter.invoke(serviceClassInstance,
                argumentsForVariationsGetter);
        } catch (Exception e) {
            throw new OpenlNotCheckedException("Failed to retrieve vairiations from rules", e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void calculateSingleVariation(Method member,
            VariationsResult variationsResults,
            Object[] arguments,
            Variation variation) {
        Stack<Object> stack = new Stack<Object>();
        Object[] modifiedArguments = null;
        try {
            try {
                modifiedArguments = variation.applyModification(arguments, stack);
                Object result = member.invoke(serviceClassInstance, modifiedArguments);
                variationsResults.registerResults(variation.getVariationID(), result);
            } catch (Exception e) {
                log.warn("Failed to calculate \"" + variation.getVariationID() + "\"", e);
                variationsResults.registerFailure(variation.getVariationID(), e.getMessage());
            }
        } finally {
            if (modifiedArguments != null) {
                try {
                    variation.revertModifications(modifiedArguments, stack);
                } catch (Exception e) {
                    log.error("Failed to revert modifications in variation \"" + variation.getVariationID() + "\"");
                }
            }
        }
    }
}
