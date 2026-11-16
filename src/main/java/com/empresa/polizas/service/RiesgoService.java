package com.empresa.polizas.service;

import com.empresa.polizas.dto.CrearRiesgoRequest;
import com.empresa.polizas.dto.RiesgoResponse;
import com.empresa.polizas.entity.Poliza;
import com.empresa.polizas.entity.Riesgo;
import com.empresa.polizas.enums.EventoCore;
import com.empresa.polizas.exception.RecursoNoEncontradoException;
import com.empresa.polizas.exception.ReglaNegocioException;
import com.empresa.polizas.repository.PolizaRepository;
import com.empresa.polizas.repository.RiesgoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RiesgoService {

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final CoreEventService coreEventService;

    public RiesgoService(PolizaRepository polizaRepository,
                         RiesgoRepository riesgoRepository,
                         CoreEventService coreEventService) {
        this.polizaRepository = polizaRepository;
        this.riesgoRepository = riesgoRepository;
        this.coreEventService = coreEventService;
    }

    @Transactional
    public RiesgoResponse agregar(Long polizaId, CrearRiesgoRequest request) {
        Poliza poliza = polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la póliza con id " + polizaId));

        validarQueAdmiteRiesgos(poliza);

        Riesgo riesgo = new Riesgo(request.descripcion());
        poliza.agregarRiesgo(riesgo);
        riesgoRepository.save(riesgo);

        coreEventService.enviarEvento(EventoCore.ACTUALIZACION, polizaId);
        return RiesgoResponse.from(riesgo);
    }

    @Transactional
    public RiesgoResponse cancelar(Long riesgoId) {
        Riesgo riesgo = riesgoRepository.findById(riesgoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el riesgo con id " + riesgoId));

        if (riesgo.estaCancelado()) {
            throw new ReglaNegocioException("El riesgo ya se encuentra cancelado");
        }
        riesgo.cancelar();

        coreEventService.enviarEvento(EventoCore.ACTUALIZACION, riesgo.getPoliza().getId());
        return RiesgoResponse.from(riesgo);
    }

    private void validarQueAdmiteRiesgos(Poliza poliza) {
        if (poliza.estaCancelada()) {
            throw new ReglaNegocioException("No se pueden agregar riesgos a una póliza cancelada");
        }
        if (poliza.esIndividual()) {
            throw new ReglaNegocioException(
                    "Solo las pólizas colectivas admiten agregar riesgos; "
                            + "las individuales tienen un único riesgo");
        }
    }
}