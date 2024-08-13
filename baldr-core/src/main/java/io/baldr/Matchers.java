package io.baldr;

import io.baldr.hamcrest.BaldrMatcher;
import io.baldr.hamcrest.MatcherWriter;
import io.ran.AutoMapper;
import io.ran.AutoMapperClassLoader;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"rawtypes","unchecked"})
public class Matchers {
    private static final Map<String, Class<Matcher<?>>> matchers = new HashMap<>();
    private static final AutoMapperClassLoader classLoader = new AutoMapperClassLoader(AutoMapper.class.getClassLoader());
    private static <T> T buildMatcher(Class<T> tClass) {
        try {
            if (BaldrMatcher.class.isAssignableFrom(tClass)) {
                return (T) matchers.get(tClass.getSuperclass().getName()).getConstructor().newInstance();
            }
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

//    public static  <T> T matcherById(String id, Class<T> tClass, Supplier<Matcher<T>> supplier) {
//        MockContext.get().registerMatcher(id, tClass, supplier.get());
//
//    }

    public static  <T> T matcher(Class<T> tClass, T t, Supplier<Matcher<T>> supplier) {
        if (tClass.isAssignableFrom(String.class)) {
            String id = UUID.randomUUID().toString();
            MockContext.get().registerMatcher(id, tClass, supplier.get());
            return (T) id;
        }
        if (Boolean.class.isAssignableFrom(tClass)) {
                MockContext.get().registerMatcher(t==null?"null":t.toString(), tClass, supplier.get());
                if (t == null) {
                    return (T)Boolean.valueOf("true");
                }
                return (T) t;
//
//            Queue<Boolean> possibleValues = new ArrayDeque<>(Arrays.asList(false, true));
//            Boolean id = null;
//            for (Boolean pv : possibleValues) {
//                if (!MockContext.get().hasMatcher(pv.getClass(), pv.toString())) {
//                    id = pv;
//                    break;
//                }
//            }
//            if (id != null) {
//                MockContext.get().registerMatcher(id.toString(), tClass, supplier.get());
//                return (T) id;
//            }
//            return t;
        }
        if (isNumber(tClass)) {
            try {
                String id;
                do {
                    String tid = String.valueOf(getRandom(t.getClass()));
                    if (Character.class.isAssignableFrom(t.getClass())) {
                        id = Character.valueOf(tid.charAt(0)).toString();
                    } else {
                        id = t.getClass().getMethod("valueOf", String.class).invoke(null, tid).toString();
                    }
                } while (MockContext.get().hasMatcher(tClass, id));

                MockContext.get().registerMatcher(id, tClass, supplier.get());

                if (Character.class.isAssignableFrom(tClass)) {
                    return (T) Character.valueOf(id.charAt(0));
                } else {
                    return (T) t.getClass().getMethod("valueOf", String.class).invoke(null, id);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        BaldrMatcher generated = (BaldrMatcher) buildMatcher(tClass);
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
            return ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE/100, Integer.MAX_VALUE/100);
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

    protected static Optional<Matcher> getPrimitiveMatcher(MockInvocationParameter matcherParameter) {
        return MockContext.get().getPrimitiveMatcher(matcherParameter);
    }

    public static  <T> T delegate(Class<T> tClass, Function<T, Boolean> matcher) {
        return matcher(tClass, null, () -> new ConsumerMatcher<T>(matcher));
    }

    public static  <T> T equalTo(T t) {
        return matcher((Class<T>)t.getClass(), t, () -> IsEqual.equalTo(t));
    }

    public static  <T> T not(T t) {
        return matcher((Class<T>)t.getClass(), t, () -> org.hamcrest.Matchers.not(t));
    }

    public static  <T> T nullValue(Class<T> tClass) {
        return matcher(tClass, null, () -> org.hamcrest.Matchers.nullValue(tClass));
    }

    public static  <T> T notNullValue(Class<T> tClass) {
        return matcher(tClass, null, () -> org.hamcrest.Matchers.notNullValue(tClass));
    }

    public static  <T> T any(Class<T> tClass) {
        return matcher(tClass, null, () -> org.hamcrest.Matchers.any(tClass));
    }

    public static  <T> T anything(Class<T> tClass) {
        return matcher(tClass, null, () -> (Matcher<T>)org.hamcrest.Matchers.anything());
    }

    public static  <T> T sameInstance(T t) {
        return matcher((Class<T>)t.getClass(), t, () -> IsSame.sameInstance(t));
    }

    public static String startsWith(String s) {
        return matcher(String.class,s, () -> org.hamcrest.Matchers.startsWith(s));
    }

    public static String startsWithIgnoringCase(String s) {
        return matcher(String.class, s, () -> org.hamcrest.Matchers.startsWithIgnoringCase(s));
    }

    public static String endsWith(String s) {
        return matcher(String.class, s, () -> org.hamcrest.Matchers.endsWith(s));
    }

    public static String endsWithIgnoringCase(String s) {
        return matcher(String.class, s, () -> org.hamcrest.Matchers.endsWithIgnoringCase(s));
    }

    public static String matchesPattern(String s) {
        return matcher(String.class, s, () -> org.hamcrest.Matchers.matchesPattern(s));
    }

    private static class ConsumerMatcher<T> extends BaseMatcher<T> {
        private Function<T, Boolean> matcher;

        public ConsumerMatcher(Function<T, Boolean> matcher) {
            this.matcher = matcher;
        }

        @Override
        public boolean matches(Object actual) {
            return matcher.apply((T) actual);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("<delegate matcher>");
        }
    }
}
