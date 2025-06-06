package ru.gitverse.parking.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Maxim Khamzin
 * @link <a href="https://mkcoder.net">mkcoder.net</a>
 */
@Component
public class ParkingLocker {
    private final Map<String, Lock> carLocks = new ConcurrentHashMap<>();

    /**
     * Блокирую операцию для указанного номера автомобиля
     *
     * @return true, если блокировка получена
     */
    public boolean tryLock(final String carNumber) {
        final Lock lock = carLocks.computeIfAbsent(carNumber, k -> new ReentrantLock());
        return lock.tryLock(3L, TimeUnit.SECONDS);
    }

    /**
     * Разблокирую операцию
     */
    public void unlock(final String carNumber) {
        final Lock lock = carLocks.get(carNumber);
        if (lock != null) {
            lock.unlock();
        }
    }

    /**
     * Очистка неиспользуемых локов.
     * Или можно каждые 60 секунд очищать то, что старше 5 минут
     */
    @Scheduled(fixedRate = 60_000)
    public void cleanupUnusedLocks() {
        carLocks.entrySet().removeIf(entry ->
                !((ReentrantLock) entry.getValue()).isLocked()
        );
    }
}
