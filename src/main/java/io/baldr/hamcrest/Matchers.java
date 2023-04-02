package io.baldr.hamcrest;

import io.baldr.Baldr;
import io.baldr.MockWriter;
import io.ran.AutoMapper;
import io.ran.AutoMapperClassLoader;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsSame;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
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
        BaldrMatcher generated = (BaldrMatcher) buildMatcher(t.getClass());
        generated.setMatcher(supplier.get());
        return (T) generated;
    }

    public static  <T> T equalTo(T t) {
        return matcher(t, () -> IsEqual.equalTo(t));
    }

    public static  <T> T sameInstance(T t) {
        return matcher(t, () -> IsSame.sameInstance(t));
    }

}
