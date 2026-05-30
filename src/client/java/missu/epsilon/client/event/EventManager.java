package missu.epsilon.client.event;

import missu.epsilon.client.event.impl.Event;
import missu.epsilon.client.event.impl.EventTarget;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {

    @FunctionalInterface
    public interface Listener<E> {
        void call(E event);
    }

    private record CallSite<E>(Object owner, Listener<E> listener, int priority) {}

    private static final Listener<?>[] EMPTY = new Listener[0];
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final Map<Class<?>, List<CallSite<Object>>> stagingMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, Listener<Object>[]> listenerCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public void subscribe(Object obj) {
        Set<Class<?>> affected = new HashSet<>();

        for (Method method : obj.getClass().getDeclaredMethods()) {
            EventTarget annotation = method.getAnnotation(EventTarget.class);
            if (annotation == null || method.getParameterTypes().length != 1) continue;

            Class<?> eventClass = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(eventClass)) continue;

            method.setAccessible(true);

            try {
                Listener<Object> listener = createLambda(obj, method, eventClass);

                stagingMap.compute(eventClass, (k, list) -> {
                    List<CallSite<Object>> entries = list == null ? new ArrayList<>() : list;
                    entries.add(new CallSite<>(obj, listener, annotation.value()));
                    return entries;
                });

                affected.add(eventClass);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to create listener for " + method, e);
            }
        }

        affected.forEach(this::rebuildCache);
    }

    public void unsubscribe(Object obj) {
        Set<Class<?>> affected = new HashSet<>();

        stagingMap.forEach((eventClass, entries) -> {
            if (entries.removeIf(cs -> cs.owner() == obj)) {
                affected.add(eventClass);
            }
        });

        affected.forEach(this::rebuildCache);
    }

    @SuppressWarnings("unchecked")
    private void rebuildCache(Class<?> eventClass) {
        List<CallSite<Object>> staging = stagingMap.get(eventClass);

        if (staging == null || staging.isEmpty()) {
            listenerCache.put(eventClass, (Listener<Object>[]) EMPTY);
            return;
        }

        staging.sort(Comparator.comparingInt(CallSite::priority));

        Listener<Object>[] arr = new Listener[staging.size()];
        for (int i = 0; i < staging.size(); i++) {
            arr[i] = staging.get(i).listener();
        }

        listenerCache.put(eventClass, arr);
    }

    @SuppressWarnings("unchecked")
    private Listener<Object> createLambda(Object obj, Method method, Class<?> eventClass) throws Throwable {
        MethodHandles.Lookup targetLookup = MethodHandles.privateLookupIn(obj.getClass(), LOOKUP);
        MethodHandle handle = targetLookup.unreflect(method);

        MethodType invokedType = MethodType.methodType(Listener.class, obj.getClass());
        MethodType samType = MethodType.methodType(void.class, Object.class);
        MethodType instantiatedType = MethodType.methodType(void.class, eventClass);

        java.lang.invoke.CallSite callSite = LambdaMetafactory.metafactory(
                targetLookup,
                "call",
                invokedType,
                samType,
                handle,
                instantiatedType
        );

        return (Listener<Object>) callSite.getTarget().invoke(obj);
    }

    @SuppressWarnings("unchecked")
    public Event call(Event event) {
        Listener<Object>[] listeners = listenerCache.getOrDefault(
                event.getClass(),
                (Listener<Object>[]) EMPTY
        );

        for (Listener<Object> listener : listeners) {
            try {
                listener.call(event);
            } catch (Throwable ignored) {
            }
        }

        return event;
    }
}