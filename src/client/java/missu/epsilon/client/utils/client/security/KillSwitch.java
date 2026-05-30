package missu.epsilon.client.utils.client.security;

import lombok.experimental.UtilityClass;
import sun.misc.Unsafe;

import java.lang.reflect.Field;


//KillSwitch based off Unsafe.
//This still can be hooked:What if the Unsafe is null and Runtime.getRuntime().halt() has been hijacked?
//FFM API/JNI crash is required to fully solve this problem.
@UtilityClass
public class KillSwitch {

    private static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    public void suicide() {
        nFreeIllegalMemory();
    }

    private void nFreeIllegalMemory() {
        try {
            Unsafe unsafe = getUnsafe();
            if (unsafe == null) {
                nAccessViolation();
                return;
            }
            unsafe.freeMemory(Long.MAX_VALUE);
            unsafe.freeMemory(Long.MIN_VALUE);
            unsafe.freeMemory(Integer.MAX_VALUE);
            unsafe.freeMemory(Integer.MIN_VALUE);
            Thread.sleep(100);
            nAccessViolation();
        } catch (Throwable e) {
            nAccessViolation();
        }
    }

    private void nAccessViolation() {
        try {
            Unsafe unsafe = getUnsafe();
            if (unsafe == null) {
                nThreadViolation();
                return;
            }
            unsafe.putAddress(0, 0);
            Thread.sleep(100);
            nThreadViolation();
        } catch (Throwable e) {
            nThreadViolation();
        }
    }

    private void nThreadViolation() {
        try {
            Unsafe unsafe = getUnsafe();
            if (unsafe == null) {
                nAccessViolation2();
                return;
            }
            Thread t = Thread.currentThread();
            Field eetop = Thread.class.getDeclaredField("eetop");
            long offset = unsafe.objectFieldOffset(eetop);
            unsafe.putLong(t, offset, 0L);
            t.interrupt();
            Thread.sleep(100);
            nAccessViolation2();
        } catch (Throwable e) {
            nAccessViolation2();
        }
    }

    private void nAccessViolation2() {
        try {
            Unsafe unsafe = getUnsafe();
            if (unsafe == null) {
                nClassLoaderViolation();
                return;
            }
            long addr = unsafe.allocateMemory(8);
            unsafe.freeMemory(addr);
            unsafe.getLong(addr);
            Thread.sleep(100);
            nClassLoaderViolation();
        } catch (Throwable e) {
            nClassLoaderViolation();
        }
    }

    private void nClassLoaderViolation() {
        try {
            Unsafe unsafe = getUnsafe();
            if (unsafe == null) {
                nStackOverflow();
                return;
            }
            Field f = ClassLoader.class.getDeclaredField("parent");
            long offset = unsafe.objectFieldOffset(f);
            unsafe.putObject(ClassLoader.getSystemClassLoader(), offset, null);
            ClassLoader.getSystemClassLoader().loadClass("java.lang.String");
            Thread.sleep(100);
            nStackOverflow();
        } catch (Throwable e) {
            nStackOverflow();
        }
    }

    private void nStackOverflow() {
        try {
            nStackOverflow();
        } catch (StackOverflowError e) {
            nStackOverflow();
        } catch (Throwable e) {
            nMultiThreadCrash();
        }
    }

    private void nMultiThreadCrash() {
        try {
            Unsafe unsafe = getUnsafe();
            if (unsafe == null) {
                nRuntimeHalt();
                return;
            }
            for (int i = 0; i < 10; i++) {
                new Thread(() -> {
                    unsafe.freeMemory(Long.MAX_VALUE);
                    unsafe.putAddress(0, 0);
                }).start();
            }
            Thread.sleep(100);
            nRuntimeHalt();
        } catch (Throwable e) {
            nRuntimeHalt();
        }
    }

    private void nRuntimeHalt() {
        Runtime.getRuntime().halt(-1);
    }
}