package ru.gitverse.parking.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Низкоуровневая многопоточка, можно еще добавить юзер-френдли методы,
 * что бы можно было удобно дергать api этого класса</p>
 *
 * <p>Класс для блокировки операций по строковым ключам (в тестовом задании - по номерам автомобилей).
 * Для одного номера авто, не выполняется несколько операций одновременно.</p>
 *
 * <p>Постарался обработать все случаи</p>
 *
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Component
public class StringLocker {
    /**
     * Внутренний класс для хранения блокировки и счетчика ее использований.
     * Нужен, чтобы правильно очищать неиспользуемые блокировки из мапы.
     */
    private static class LockWithCounter {
        final ReentrantLock lock = new ReentrantLock();               // Сама блокировка
        final AtomicInteger counter = new AtomicInteger(1); // Счетчик использования (начинаю сразу с 1)
    }

    // Потокобезопасная мапа для хранения блокировок
    private final Map<String, LockWithCounter> stringLocks = new ConcurrentHashMap<>();

    /**
     * Пытается захватить блокировку (в нашем случае по номеру авто),
     *
     * @param str строка (номер автомобиля для блокировки)
     * @return true, если блокировка успешно получена
     * @throws IllegalArgumentException если carNumber == null
     */
    public boolean tryLock(final String str) {
        if (str == null) throw new IllegalArgumentException("Lock key не может быть null");

        // Получает или создаёт блокировку для этого номера
        final LockWithCounter lockWithCounter = stringLocks.compute(str, (key, existingLock) -> {
            // Если блокировки ещё нет — создаём новую
            if (existingLock == null) return new LockWithCounter();

            // Если блокировка уже есть - увеличиваем счетчик использования и идём ждать
            existingLock.counter.incrementAndGet();
            return existingLock;
        });

        try {
            // Пытается захватить блокировку с таймаутом 3 секунды.
            // Если не получается (другой поток не освободил лок за 3сек), то false
            final boolean lockAcquired = lockWithCounter.lock.tryLock(3L, TimeUnit.SECONDS);

            // Если не получилось (false) - уменьшаем счетчик на один
            if (!lockAcquired) decrementCounter(str);
            return lockAcquired;
        }
        catch (InterruptedException e) {
            lockWithCounter.lock.unlock();      // В случае прерывания - освобождаем блокировку
            decrementCounter(str);
            Thread.currentThread().interrupt(); // и восстанавливаем флаг прерывания
            return false;
        }
    }

    /** Разблокирую операцию и удаляю лок если он больше не нужен (Освобождает блокировку для указанного номера автомобиля) */
    public void unlock(final String str) {
        if (str == null) throw new IllegalArgumentException("Lock key не может быть null");

        stringLocks.computeIfPresent(str, (key, lockWithCounter) -> {
            lockWithCounter.lock.unlock();

            if (lockWithCounter.counter.decrementAndGet() == 0) {

                // Дополнительная проверка, что лок действительно свободен (защита от редкого случая)
                // Поток 1 освобождает лок (counter=1), поток 2 успевает захватить лок до удаления.
                // Поток 1 проверяет isLocked(), видит true и не удаляет
                if (!lockWithCounter.lock.isLocked()) {
                    return null; // Удаляю из мапы
                }
            }
            return lockWithCounter;
        });
    }

    /** Удаляет блокировку из мапы, если счетчик достиг нуля */
    private void decrementCounter(final String str) {
        stringLocks.computeIfPresent(str, (key, lockWithCounter) ->
                lockWithCounter.counter.decrementAndGet() == 0 ? null : lockWithCounter
        );
    }

    /** Принимает лямбду и выполняет её только если блокировка получена. Пока private. Потом можно дописать если потребуется */
    private  <T> Optional<T> executeLocked(final String key, final Callable<T> task) {
        boolean locked = tryLock(key);
        if (!locked) return Optional.empty();

        try {
            return Optional.of(task.call());
        }
        catch (Exception e) {
            throw new RuntimeException("Ошибка внутри критической секции", e);
        }
        finally {
            unlock(key);
        }
    }

    public void executeLocked(String key, Runnable task) {
        boolean locked = tryLock(key);
        if (!locked) return;

        try {
            task.run();
        }
        finally {
            unlock(key);
        }
    }
}
