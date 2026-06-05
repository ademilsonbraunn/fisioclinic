package com.fisioclinic.service;

import com.fisioclinic.dto.AtualizacaoSistemaResponse;
import com.fisioclinic.repository.AtualizacaoSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AtualizacaoSistemaService {

    private final AtualizacaoSistemaRepository repository;

    @Transactional(readOnly = true)
    public List<AtualizacaoSistemaResponse> listar() {
        return repository.findTop10ByAtivoTrueOrderByDataLancamentoDesc()
                .stream()
                .map(AtualizacaoSistemaResponse::from)
                .toList();
    }
}
