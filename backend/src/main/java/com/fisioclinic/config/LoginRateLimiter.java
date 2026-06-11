package com.fisioclinic.config;

import com.fisioclinic.exception.TooManyRequestsException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * [M0 — Segurança] Rate limiting por IP no endpoint de login.
 * Máximo de 5 tentativas falhas em 15 minutos por endereço IP.
 * Limpeza automática via @Scheduled a cada 15 minutos para evitar acúmulo de memória.
 */
@Component
public class LoginRateLimiter {

    private static final int  MAX_TENTATIVAS = 5;
    private static final long JANELA_MS      = 15 * 60 * 1000L;

    private final ConcurrentHashMap<String, Deque<Long>> falhas = new ConcurrentHashMap<>();

    public void verificar(String ip) {
        Deque<Long> timestamps = falhas.get(ip);
        if (timestamps == null) return;
        removerExpirados(timestamps);
        if (timestamps.size() >= MAX_TENTATIVAS) {
            throw new TooManyRequestsException(
                "Muitas tentativas de login. Aguarde 15 minutos antes de tentar novamente.");
        }
    }

    public void registrarFalha(String ip) {
        falhas.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>())
              .addLast(System.currentTimeMillis());
    }

    public void registrarSucesso(String ip) {
        falhas.remove(ip);
    }

    @Scheduled(fixedDelay = 900_000)
    public void limparExpirados() {
        long limite = System.currentTimeMillis() - JANELA_MS;
        falhas.values().forEach(dq -> dq.removeIf(t -> t < limite));
        falhas.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    private void removerExpirados(Deque<Long> timestamps) {
        long limite = System.currentTimeMillis() - JANELA_MS;
        timestamps.removeIf(t -> t < limite);
    }
}
