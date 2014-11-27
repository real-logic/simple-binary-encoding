package uk.co.real_logic.sbe.codec.java;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Daneel Yaitskov
 */
public class MethodSelector
{
    private final Set<String> ignoredMethods;
    private final Map<Class, Set<String>> sortedMethods = new HashMap<>();

    public static Set<String> objectAndIteratorMethods()
    {
        return new HashSet<>(Arrays.asList("hashCode", "clone", "toString", "getClass",
                "next", "hasNext", "remove", "iterator"));
    }

    public MethodSelector(Set<String> ignoredMethods)
    {
        this.ignoredMethods = ignoredMethods;
    }

    public List<Method> select(Class clazz)
    {
        final Method[] methods = clazz.getMethods();
        final Set<String> sortedMethNames = getSortedMethods(clazz, methods);
        final Map<String, Method> sortedMethods = new HashMap<String, Method>();
        final List<Method> unsortedMethods = new ArrayList<Method>();
        for (Method method : methods)
        {
            selectMethod(sortedMethNames, sortedMethods, unsortedMethods, method);
        }
        for (String name : sortedMethNames)
        {
            unsortedMethods.add(sortedMethods.get(name));
        }
        return unsortedMethods;
    }

    private Set<String> getSortedMethods(Class clazz, Method[] methods)
    {
        final Set<String> sortedMethNames = sortedMethods.get(clazz);
        if (sortedMethNames == null)
        {
            GroupOrder order = (GroupOrder) clazz.getAnnotation(GroupOrder.class);
            if (order == null)
            {
                sortedMethods.put(clazz, Collections.<String>emptySet());
                return Collections.emptySet();
            }
            else
            {
                Set<String> result = new LinkedHashSet<>();
                for (Class groupClazz : order.value())
                {
                    for (Method method : methods)
                    {
                        if (method.getReturnType() == groupClazz
                                && method.getParameterTypes().length == 0)
                        {
                            result.add(method.getName());
                        }
                    }
                }
                sortedMethods.put(clazz, result);
                return result;
            }
        }
        return sortedMethNames;
    }

    private void selectMethod(Set<String> sortedMethNames,
                              Map<String, Method> sortedMethods,
                              List<Method> unsortedMethods,
                              Method method)
    {
        final int mods = method.getModifiers();
        if (!Modifier.isPublic(mods))
        {
            return;
        }
        if (Modifier.isStatic(mods))
        {
            return;
        }
        if (method.getParameterTypes().length != 0)
        {
            return;
        }
        if (method.getReturnType().equals(Void.TYPE))
        {
            return;
        }
        final String name = method.getName();
        if (ignoredMethods.contains(name))
        {
            return;
        }
        if (sortedMethNames == null)
        {
            unsortedMethods.add(method);
        }
        else
        {
            if (sortedMethNames.contains(name))
            {
                sortedMethods.put(name, method);
            }
            else
            {
                unsortedMethods.add(method);
            }
        }
    }
}
