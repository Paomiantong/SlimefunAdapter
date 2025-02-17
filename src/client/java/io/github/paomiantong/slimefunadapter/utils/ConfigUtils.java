package io.github.paomiantong.slimefunadapter.utils;

import io.github.paomiantong.slimefunadapter.config.Configurable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

public class ConfigUtils {
    public static Stream<Field> getConfigurableFields(Class<?> clazz) {
        return Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Configurable.class))
                .filter(field -> !Modifier.isFinal(field.getModifiers()) &&
                        Modifier.isPublic(field.getModifiers()) &&
                        Modifier.isStatic(field.getModifiers()));
    }
}
