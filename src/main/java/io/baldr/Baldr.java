package io.baldr;

import io.ran.AutoMapper;
import io.ran.AutoMapperClassLoader;
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

@SuppressWarnings("rawtypes")
public class Baldr {
    private static final Map<String, Class> mocks = new HashMap<>();

    private static final AutoMapperClassLoader classLoader = new AutoMapperClassLoader(AutoMapper.class.getClassLoader());

    @SuppressWarnings({"rawtype","unchecked"})
    public static <T> T mock(Class<T> tClass) {
        try {
            return (T)mocks.computeIfAbsent(tClass.getName(), c -> {

                try {
                    Path path = Paths.get("/tmp/" + tClass.getSimpleName() + "$Ran$Wrapper.class");

                    MockWriter visitor = new MockWriter(tClass.getSimpleName(), tClass);
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

    public static <T> MockVerification<T> assertCalled(T t, Consumer<T> consumer) {
        MockContext.get().enterAssert();
        try {
            return new MockVerificationImpl<>(t, consumer, null);
        } finally {
            MockContext.get().exitAssert();
        }
    }

    public static <T, R> Stub<T, R> when(T t, Function<T, R> consumer) {
        MockContext.get().enterStubbing();
        try {
            return new Stub<>(t, consumer);
        } finally {
            MockContext.get().exitStubbing();
        }
    }
}
