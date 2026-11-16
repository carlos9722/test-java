package com.empresa.polizas.service;

import com.empresa.polizas.config.IpcProperties;
import com.empresa.polizas.dto.PolizaResponse;
import com.empresa.polizas.dto.RiesgoResponse;
import com.empresa.polizas.entity.Poliza;
import com.empresa.polizas.enums.EstadoPoliza;
import com.empresa.polizas.enums.TipoPoliza;
import com.empresa.polizas.exception.RecursoNoEncontradoException;
import com.empresa.polizas.exception.ReglaNegocioException;
import com.empresa.polizas.repository.PolizaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PolizaService {

    private final PolizaRepository polizaRepository;
    private final IpcProperties ipcProperties;

    public PolizaService(PolizaRepository polizaRepository, IpcProperties ipcProperties) {
        this.polizaRepository = polizaRepository;
        this.ipcProperties = ipcProperties;
    }

    public List<PolizaResponse> listar(TipoPoliza tipo, EstadoPoliza estado) {
        return polizaRepository.buscarPorFiltros(tipo, estado).stream()
                .map(PolizaResponse::from)
                .toList();
    }

    public List<RiesgoResponse> obtenerRiesgos(Long polizaId) {
        return buscarPoliza(polizaId).getRiesgos().stream()
                .map(RiesgoResponse::from)
                .toList();
    }

    @Transactional
    public PolizaResponse renovar(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        if (poliza.estaCancelada()) {
            throw new ReglaNegocioException("No se puede renovar una póliza cancelada");
        }
        poliza.renovar(ipcProperties.ipc());
        return PolizaResponse.from(poliza);
    }

    @Transactional
    public PolizaResponse cancelar(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        if (poliza.estaCancelada()) {
            throw new ReglaNegocioException("La póliza ya se encuentra cancelada");
        }
        poliza.cancelar();
        return PolizaResponse.from(poliza);
    }

    private Poliza buscarPoliza(Long polizaId) {
        return polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la póliza con id " + polizaId));
    }
}