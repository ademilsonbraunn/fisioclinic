package com.fisioclinic.dto;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * TokenResponse — Saída do endpoint de login com JWT e dados do usuário
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — response)
 *
 * expiresIn: tempo de expiração em milissegundos a partir do momento do login
 *   (valor configurado em jwt.expiration no application.properties).
 * nome e perfil: usados pelo frontend para personalizar a interface sem
 *   precisar decodificar o JWT no lado cliente.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record TokenResponse(

    String token,
    String nome,
    String perfil,
    long expiresIn

) {}
