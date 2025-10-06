package net.devstudy.resume.util;

import java.lang.reflect.InvocationTargetException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.BeanUtils;

import net.devstudy.resume.domain.Certificate;
import net.devstudy.resume.domain.ProfileCollectionField;

/**
 * Утиліти для роботи з даними (сумісно з Java 21 / Spring Boot 3).
 */
public final class DataUtil {

    private DataUtil() {
        // utility class
    }

    /**
     * Копіює у цільове поле значення з джерела для всіх полів, які проходять
     * фільтр.
     *
     * @return кількість полів, що були змінені у {@code to}
     */
    public static <T extends Annotation> int copyFields(final Object from, final Object to, Class<T> annotation) {
        if (annotation == null) {
            return BeanCopyUtil.copyAnnotated(from, to, null); // копіюємо всі доступні проперті
        }
        return BeanCopyUtil.copyAnnotated(from, to, annotation); // копіюємо лише анотовані
    }

    public static int copyFields(final Object from, final Object to) {
        return BeanCopyUtil.copyAnnotated(from, to, null);
    }

    public static Object readProperty(Object obj, String propertyName) {
        try {
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(obj.getClass(), propertyName);
            if (pd == null || pd.getReadMethod() == null) {
                throw new IllegalArgumentException("No readable property '" + propertyName + "' on " + obj.getClass());
            }
            return pd.getReadMethod().invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException e) {
            throw new IllegalArgumentException(
                    "Can't read property '" + propertyName + "' from object '" + obj.getClass() + "': "
                            + e.getMessage(),
                    e);
        }
    }

    public static void writeProperty(Object obj, String propertyName, Object value) {
        try {
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(obj.getClass(), propertyName);
            if (pd == null || pd.getWriteMethod() == null) {
                throw new IllegalArgumentException("No writable property '" + propertyName + "' on " + obj.getClass());
            }
            pd.getWriteMethod().invoke(obj, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(
                    "Can't write property '" + propertyName + "' for object '" + obj.getClass() + "': "
                            + e.getMessage(),
                    e);
        }
    }

    public static List<String> getCertificateImageUrls(List<Certificate> certificates) {
        if (certificates == null || certificates.isEmpty()) {
            return List.of();
        }
        List<String> res = new ArrayList<>(certificates.size() * 2);
        for (Certificate certificate : certificates) {
            if (certificate == null)
                continue;
            res.add(certificate.getLargeUrl());
            res.add(certificate.getSmallUrl());
        }
        return res;
    }

    public static <T extends ProfileCollectionField> String getCollectionName(Class<T> clazz) {
        String className = clazz.getSimpleName().toLowerCase();
        if (className.endsWith("y")) {
            className = className.substring(0, className.length() - 1) + "ie";
        }
        return className + "s";
    }

    public static String normalizeName(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }

    /**
     * Капіталізація кожного слова без сторонніх бібліотек.
     */
    public static String capitalizeName(String name) {
        String s = normalizeName(name);
        if (s.isEmpty())
            return s;
        String[] parts = s.split("\\s+");
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1)
                    sb.append(p.substring(1));
            }
            if (i < parts.length - 1)
                sb.append(' ');
        }
        return sb.toString();
    }

    public static String generateRandomString(String alphabet, int letterCount) {
        if (alphabet == null || alphabet.isEmpty() || letterCount <= 0)
            return "";
        StringBuilder uid = new StringBuilder(letterCount);
        for (int i = 0; i < letterCount; i++) {
            int idx = ThreadLocalRandom.current().nextInt(alphabet.length());
            uid.append(alphabet.charAt(idx));
        }
        return uid.toString();
    }

    public static void removeEmptyElements(Collection<?> collection) {
        if (collection == null || collection.isEmpty())
            return;
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            Object element = it.next();
            if (element == null || isAllFieldsNull(element)) {
                it.remove();
            }
        }
    }

    public static boolean areListsEqual(final List<?> a, final List<?> b) {
        if (a == b)
            return true;
        if (a == null || b == null)
            return false;
        if (a.size() != b.size())
            return false;
        for (int i = 0; i < a.size(); i++) {
            if (!Objects.equals(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static <T> int compareByFields(Comparable<T> firstFieldValue,
            Comparable<T> secondFieldValue,
            boolean nullFirst) {
        if (firstFieldValue == null) {
            if (secondFieldValue == null)
                return 0;
            return nullFirst ? 1 : -1;
        } else {
            if (secondFieldValue == null)
                return nullFirst ? -1 : 1;
            return firstFieldValue.compareTo((T) secondFieldValue);
        }
    }

    // ======== private helpers ========

    /**
     * Перевіряє, що всі JavaBean-властивості (з гетерами) дорівнюють null.
     * Без доступу до private-полів (жодного setAccessible) → не порушує
     * інкапсуляцію.
     */
    private static boolean isAllFieldsNull(Object element) {
        try {
            for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(element.getClass())) {
                if ("class".equals(pd.getName()))
                    continue; // службова
                var read = pd.getReadMethod();
                if (read != null) {
                    Object val = read.invoke(element);
                    if (val != null)
                        return false;
                }
            }
            return true;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to inspect bean properties for " + element.getClass(), e);
        }
    }
}
