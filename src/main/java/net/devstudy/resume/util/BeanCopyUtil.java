package net.devstudy.resume.util;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Безпечне копіювання властивостей JavaBean через публічні гетери/сетери
 * (без setAccessible і доступу до private полів).
 */
public final class BeanCopyUtil {

    private BeanCopyUtil() {
    }

    /**
     * Копіює ВСІ доступні властивості (через гетери/сетери).
     * Можна вказати імена властивостей, які треба ігнорувати.
     */
    public static void copy(Object source, Object target, String... ignoredProperties) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target must not be null");
        }
        BeanUtils.copyProperties(source, target, ignoredProperties);
    }

    /**
     * Копіює РІВНО ОДНУ властивість за назвою propertyName (через гетер/сетер).
     *
     * @return true — якщо значення реально змінилося у target
     */
    public static boolean copyProperty(Object source, Object target, String propertyName) {
        if (source == null || target == null || propertyName == null)
            return false;

        PropertyDescriptor srcPd = BeanUtils.getPropertyDescriptor(source.getClass(), propertyName);
        PropertyDescriptor trgPd = BeanUtils.getPropertyDescriptor(target.getClass(), propertyName);
        if (srcPd == null || trgPd == null)
            return false;

        Method read = srcPd.getReadMethod();
        Method write = trgPd.getWriteMethod();
        if (read == null || write == null)
            return false;

        try {
            Object newVal = read.invoke(source);
            Object oldVal = (trgPd.getReadMethod() != null) ? trgPd.getReadMethod().invoke(target) : null;

            if (!Objects.equals(newVal, oldVal)) {
                write.invoke(target, newVal);
                return true;
            }
            return false;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to copy property '" + propertyName + "'", e);
        }
    }

    /**
     * Копіює лише властивості, позначені анотацією ann (на полі або аксесорах).
     *
     * @return кількість реально змінених властивостей
     */
    public static <A extends Annotation> int copyAnnotated(Object source, Object target, Class<A> ann) {
        if (source == null || target == null)
            return 0;

        return Arrays.stream(BeanUtils.getPropertyDescriptors(target.getClass()))
                .filter(BeanCopyUtil::hasWriter) // є сетер
                .filter(pd -> hasMatchingReader(source, pd)) // є відповідний гетер у source
                .filter(pd -> ann == null || hasAnnotationOnProperty(target.getClass(), pd, ann)) // фільтр за анотацією
                .mapToInt(pd -> applyIfChanged(source, target, pd)) // 1 якщо змінило, інакше 0
                .sum();
    }

    /*
     * ---------- helpers (дрібні, плоскі, знижують когнітивну складність)
     * ----------
     */

    private static boolean hasWriter(PropertyDescriptor pd) {
        return pd.getWriteMethod() != null;
    }

    private static boolean hasMatchingReader(Object source, PropertyDescriptor targetPd) {
        PropertyDescriptor spd = BeanUtils.getPropertyDescriptor(source.getClass(), targetPd.getName());
        return spd != null && spd.getReadMethod() != null;
    }

    private static int applyIfChanged(Object source, Object target, PropertyDescriptor targetPd) {
        try {
            PropertyDescriptor srcPd = BeanUtils.getPropertyDescriptor(source.getClass(), targetPd.getName());
            if (srcPd == null || srcPd.getReadMethod() == null) {
                return 0; // немає властивості або гетера у source → пропускаємо
            }

            Method srcRead = srcPd.getReadMethod();
            Object newVal = invoke(srcRead, source);

            Method trgRead = targetPd.getReadMethod(); // може бути null для write-only властивості
            Object oldVal = (trgRead != null) ? invoke(trgRead, target) : null;

            if (Objects.equals(newVal, oldVal))
                return 0;

            invoke(targetPd.getWriteMethod(), target, newVal);
            return 1;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to copy annotated property '" + targetPd.getName() + "'", e);
        }
    }

    private static Object invoke(Method m, Object instance, Object... args) throws ReflectiveOperationException {
        return m.invoke(instance, args);
    }

    /** Перевірка анотації на полі або геттері/сеттері (залишена з вашої версії) */
    private static boolean hasAnnotationOnProperty(Class<?> targetClass,
            PropertyDescriptor pd,
            Class<? extends Annotation> ann) {
        var field = ReflectionUtils.findField(targetClass, pd.getName());
        if (field != null && field.isAnnotationPresent(ann))
            return true;

        var read = pd.getReadMethod();
        if (read != null && read.isAnnotationPresent(ann))
            return true;

        var write = pd.getWriteMethod();
        return write != null && write.isAnnotationPresent(ann);
    }
}
