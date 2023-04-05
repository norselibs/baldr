package io.baldr.hamcrest;

import io.baldr.MockContext;
import io.baldr.MockedObject;
import io.ran.AutoMapper;
import io.ran.AutoMapperClassLoader;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsSame;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class Matchers {
    private static final Map<String, Class<Matcher<?>>> matchers = new HashMap<>();
    private static final AutoMapperClassLoader classLoader = new AutoMapperClassLoader(AutoMapper.class.getClassLoader());
    private static <T> T buildMatcher(Class<T> tClass) {
        try {
            return (T)matchers.computeIfAbsent(tClass.getName(), c -> {

                try {
                    Path path = Paths.get("/tmp/" + tClass.getSimpleName() + "$Ran$Matcher.class");

                    MatcherWriter visitor = new MatcherWriter(tClass.getSimpleName(), tClass);
                    byte[] bytes = visitor.toByteArray();
                    try (FileOutputStream outputStream = new FileOutputStream(path.toFile())) {
                        outputStream.write(bytes);
                    }

                    CheckClassAdapter.verify(new ClassReader(bytes), false, new PrintWriter(System.out));
                    return classLoader.define(visitor.getName(), bytes);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static  <T> T matcher(T t, Supplier<Matcher<T>> supplier) {
        if (t instanceof String) {
            String id = UUID.randomUUID().toString();
            MockContext.get().registerMatcher(id, t.getClass(), supplier.get());
            return (T) id;
        }
        if (Boolean.class.isAssignableFrom(t.getClass())) {
            Queue<Boolean> possibleValues = new ArrayDeque<>(Arrays.asList(false, true));
            Boolean id = null;
            for (Boolean pv : possibleValues) {
                if (!MockContext.get().hasMatcher(pv.getClass(), pv.toString())) {
                    id = pv;
                }
            }
            if (id != null) {
                MockContext.get().registerMatcher(UUID.randomUUID().toString(), t.getClass(), supplier.get());
                return (T) id;
            }
            return t;
        }
        if (isNumber(t.getClass())) {
            try {
                String id;
                do {
                    String tid = String.valueOf(getRandom(t.getClass()));
                    if (Character.class.isAssignableFrom(t.getClass())) {
                        id = Character.valueOf(tid.charAt(0)).toString();
                    }  else {
                        id = t.getClass().getMethod("valueOf", String.class).invoke(null, tid).toString();
                    }
                } while(MockContext.get().hasMatcher(t.getClass(), id));

                MockContext.get().registerMatcher(id, t.getClass(), supplier.get());

                if (Character.class.isAssignableFrom(t.getClass())) {
                    return (T)Character.valueOf(id.charAt(0));
                }  else {
                    return (T) t.getClass().getMethod("valueOf", String.class).invoke(null, id);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        BaldrMatcher generated = (BaldrMatcher) buildMatcher(t.getClass());
        generated.setMatcher(supplier.get());
        return (T) generated;
    }

    private static int getRandom(Class<?> aClass) {
        if (aClass.isAssignableFrom(byte.class) || aClass.isAssignableFrom(Byte.class)) {
            return ThreadLocalRandom.current().nextInt(-127, 127);
        }
        if (aClass.isAssignableFrom(short.class) || aClass.isAssignableFrom(Short.class)) {
            return ThreadLocalRandom.current().nextInt(Short.MIN_VALUE, Short.MAX_VALUE);
        }
        if (aClass.isAssignableFrom(float.class) || aClass.isAssignableFrom(Float.class)) {
            return ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE/100, (int) Integer.MAX_VALUE/100);
        }
        if (aClass.isAssignableFrom(char.class) || aClass.isAssignableFrom(Character.class)) {
            return ThreadLocalRandom.current().nextInt(Character.MIN_VALUE, Character.MAX_VALUE);
        }
        return ThreadLocalRandom.current().nextInt();
    }

    private static boolean isNumber(Class<?> aClass) {
        return aClass.isAssignableFrom(int.class)
                || aClass.isAssignableFrom(Integer.class)
                || aClass.isAssignableFrom(long.class)
                || aClass.isAssignableFrom(Long.class)
                || aClass.isAssignableFrom(byte.class)
                || aClass.isAssignableFrom(Byte.class)
                || aClass.isAssignableFrom(short.class)
                || aClass.isAssignableFrom(Short.class)
                || aClass.isAssignableFrom(char.class)
                || aClass.isAssignableFrom(Character.class)
                || aClass.isAssignableFrom(double.class)
                || aClass.isAssignableFrom(Double.class)
                || aClass.isAssignableFrom(float.class)
                || aClass.isAssignableFrom(Float.class)

                ;
    }

    public static Optional<Matcher> getPrimitiveMatcher(Class type, String id) {
        return MockContext.get().getPrimitiveMatcher(id, type);
    }

    public static  <T> T equalTo(T t) {
        return matcher(t, () -> IsEqual.equalTo(t));
    }

    public static  <T> T sameInstance(T t) {
        return matcher(t, () -> IsSame.sameInstance(t));
    }

}
