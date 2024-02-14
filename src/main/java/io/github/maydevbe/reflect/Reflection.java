package io.github.maydevbe.reflect;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Reflection {

    private static final String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");
    private static final String VERSION = OBC_PREFIX.replace("org.bukkit.craftbukkit", "").replace(".", "");
    private static final Pattern PATTERN_MATCH_VARIABLE = Pattern.compile("\\{([^\\}]+)\\}");

    public static Class<?> getUntypedClass(String lookupName) {
        return getClass(lookupName);
    }

    public static Class<?> getClass(String lookupName) {
        return getCanonicalClass(expandVariables(lookupName));
    }

    public static Class<?> getUntypedClasses(String... lookupNames) {
        for (String lookupName : lookupNames) {
            try {
                return getUntypedClass(lookupName);
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        return null;
    }

    private static Class<?> getCanonicalClass(String canonicalName) {
        try {
            return Class.forName(canonicalName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot find " + canonicalName, e);
        }
    }

    private static String expandVariables(String name) {
        // StringBuffer para construir la cadena resultante
        StringBuffer output = new StringBuffer();

        // Matcher para buscar las variables en el nombre
        Matcher matcher = PATTERN_MATCH_VARIABLE.matcher(name);

        // Iterar sobre todas las coincidencias encontradas
        while (matcher.find()) {
            // Obtener el nombre de la variable
            String variable = matcher.group(1);
            // Inicializar el valor de reemplazo
            String replacement = "";

            // Expandir todas las variables detectadas
            if ("nms".equalsIgnoreCase(variable))
                replacement = NMS_PREFIX;
            else if ("obc".equalsIgnoreCase(variable))
                replacement = OBC_PREFIX;
            else if ("version".equalsIgnoreCase(variable))
                replacement = VERSION;
            else
                // Lanzar una excepcisi se encuentra una variable desconocida
                throw new IllegalArgumentException("Unknown variable: " + variable);

            // Suponer que las variables expandidas son todas paquetes y agregar un punto
            if (replacement.length() > 0 && matcher.end() < name.length() && name.charAt(matcher.end()) != '.')
                replacement += ".";

            // Reemplazar la variable encontrada con su valor correspondiente
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
        }

        // Agregar el resto del nombre al resultado
        matcher.appendTail(output);
        // Convertir el StringBuffer a String y devolverlo
        return output.toString();
    }

    public static Class<?> getMinecraftClass(String name) {
        return getCanonicalClass(NMS_PREFIX + "." + name);
    }

    public static Class<?> getCraftBukkitClass(String name) {
        return getCanonicalClass(OBC_PREFIX + "." + name);
    }

    public static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType) {
        return getField(target, name, fieldType, 0);
    }

    public static <T> FieldAccessor<T> getField(String className, String name, Class<T> fieldType) {
        return getField(getClass(className), name, fieldType, 0);
    }

    public static <T> FieldAccessor<T> getField(Class<?> target, Class<T> fieldType, int index) {
        return getField(target, null, fieldType, index);
    }

    public static <T> FieldAccessor<T> getField(String className, Class<T> fieldType, int index) {
        return getField(getClass(className), fieldType, index);
    }

    private static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType, int index) {
        for (final Field field : target.getDeclaredFields()) {
            if ((name == null || field.getName().equals(name)) && fieldType.isAssignableFrom(field.getType()) && index-- <= 0) {
                field.setAccessible(true);

                return new FieldAccessor<T>() {
                    @Override
                    public T get(Object target) {
                        try {
                            return (T) field.get(target);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Cannot access reflection.", e);
                        }
                    }

                    @Override
                    public void set(Object target, Object value) {
                        try {
                            field.set(target, value);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Cannot access reflection.", e);
                        }
                    }

                    @Override
                    public boolean hasField(Object target) {
                        return field.getDeclaringClass().isAssignableFrom(target.getClass());
                    }

                    @Override
                    public String getFieldName() {
                        return field.getName();
                    }

                    @Override
                    public Class<?> getFieldType() {
                        return field.getType();
                    }

                    @Override
                    public boolean isStatic() {
                        return Modifier.isStatic(field.getModifiers());
                    }
                };
            }
        }

        if (target.getSuperclass() != null)
            return getField(target.getSuperclass(), name, fieldType, index);

        throw new IllegalArgumentException("Cannot find field with type " + fieldType);
    }

    public static MethodInvoker getMethod(String className, String methodName, Class<?>... params) {
        return getTypedMethod(getClass(className), methodName, null, true, params);
    }

    public static MethodInvoker getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        return getTypedMethod(clazz, methodName, null, true, params);
    }

    public static MethodInvoker getSingleMethod(Class<?> clazz, String methodName, Class<?>... params) {
        return getTypedMethod(clazz, methodName, null, false, params);
    }

    public static MethodInvoker getTypedMethod(Class<?> clazz, String methodName, Class<?> returnType, boolean declared, Class<?>... params) {
        for (final Method method : (declared ? clazz.getDeclaredMethods() : clazz.getMethods())) {
            if ((methodName == null || method.getName().equals(methodName))
                    && (returnType == null || method.getReturnType().equals(returnType))
                    && Arrays.equals(method.getParameterTypes(), params)) {
                method.setAccessible(true);

                return new MethodInvoker() {
                    @Override
                    public Object invoke(Object target, Object... arguments) {
                        try {
                            return method.invoke(target, arguments);
                        } catch (Exception e) {
                            throw new RuntimeException("Cannot invoke method " + method, e);
                        }
                    }

                    @Override
                    public String getMethodName() {
                        return method.getName();
                    }

                    @Override
                    public Class<?> getReturnType() {
                        return method.getReturnType();
                    }

                    @Override
                    public Class<?>[] getParameterTypes() {
                        return method.getParameterTypes();
                    }

                    @Override
                    public boolean isStatic() {
                        return Modifier.isStatic(method.getModifiers());
                    }
                };
            }
        }

        if (clazz.getSuperclass() != null)
            return getTypedMethod(clazz.getSuperclass(), methodName, returnType, declared, params);

        throw new IllegalStateException(String.format("Unable to find method %s (%s).", methodName, Arrays.asList(params)));
    }

    public static ConstructorInvoker getConstructor(String className, Class<?>... params) {
        return getConstructor(getClass(className), params);
    }

    public static ConstructorInvoker getConstructor(Class<?> clazz, int indexOf) {
        Constructor<?> constructor = clazz.getDeclaredConstructors()[indexOf];
        constructor.setAccessible(true);

        return createConstructorInvoker(constructor);
    }

    public static ConstructorInvoker getConstructor(Class<?> clazz, Class<?>... params) {
        for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (Arrays.equals(constructor.getParameterTypes(), params)) {
                constructor.setAccessible(true);
                return createConstructorInvoker(constructor);
            }
        }

        throw new IllegalStateException(String.format("Unable to find constructor for %s (%s).", clazz, Arrays.asList(params)));
    }

    private static ConstructorInvoker createConstructorInvoker(Constructor<?> constructor) {
        return new ConstructorInvoker() {
            @Override
            public Object invoke(Object... arguments) {
                try {
                    return constructor.newInstance(arguments);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot invoke constructor " + constructor, e);
                }
            }

            @Override
            public Class<?>[] getParameterTypes() {
                return constructor.getParameterTypes();
            }
        };
    }

    public static Object getEnum(String className, String enumName) {
        return getEnum(getCanonicalClass(expandVariables(className)), enumName);
    }

    public static Object getEnum(Class<?> enumType, String enumName) {
        try {
            Field field = enumType.getDeclaredField(enumName);
            field.setAccessible(true);

            return field.get(null);
        } catch (ReflectiveOperationException e) {
            Bukkit.getLogger().severe("Error while accessing enum constant: " + enumName);
        }
        return null;
    }


    public interface ConstructorInvoker {

        Object invoke(Object... arguments);

        Class<?>[] getParameterTypes();
    }
    public interface MethodInvoker {

        Object invoke(Object target, Object... arguments);

        String getMethodName();

        Class<?> getReturnType();

        Class<?>[] getParameterTypes();

        boolean isStatic();
    }

    public interface FieldAccessor<T> {

        T get(Object target);

        void set(Object target, Object value);

        boolean hasField(Object target);

        String getFieldName();

        Class<?> getFieldType();

        boolean isStatic();
    }
}
