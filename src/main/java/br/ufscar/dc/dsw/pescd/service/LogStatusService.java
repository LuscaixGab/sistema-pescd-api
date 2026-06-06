package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.LogStatusInscricao;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.LogStatusInscricaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LogStatusService {

    private final LogStatusInscricaoRepository logRepository;

    public LogStatusService(LogStatusInscricaoRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Transactional
    public void registrarLog(Inscricao inscricao, StatusInscricao novoStatus, Usuario usuarioResponsavel) {
        LogStatusInscricao log = new LogStatusInscricao(
                inscricao,
                novoStatus,
                LocalDateTime.now(),
                usuarioResponsavel
        );
        logRepository.save(log);
    }
}