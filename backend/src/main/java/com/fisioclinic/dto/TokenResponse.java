package com.fisioclinic.dto;

public record TokenResponse(

    String token,
    String nome,
    String perfil,
    long expiresIn

) {}
