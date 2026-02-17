package com.vivso.Vivso.Service;

import com.vivso.Vivso.Model.EstadoSolicitud;
import com.vivso.Vivso.Model.Solicitud;
import com.vivso.Vivso.Repository.ISolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudService implements ISolicitudService{

    @Autowired
    private ISolicitudRepository solicitudRepo;

    @Override
    public List<Solicitud> listarTodas() {
        return solicitudRepo.findAll();
    }

    @Override
    public Solicitud buscarPorId(Integer id) {
        return solicitudRepo.findById(id).orElse(null);
    }

    @Override
    public Solicitud guardar(Solicitud solicitud) {
        solicitudRepo.save(solicitud);

        return solicitud;
    }

    @Override
    public void eliminar(Integer id) {
        if(!solicitudRepo.existsById(id)){
            throw new RuntimeException("No existe solicitud con el ID: "+ id);
        }
        solicitudRepo.deleteById(id);
    }

    @Override
    public List<Solicitud> buscarPorEstado(EstadoSolicitud estado) {
        return solicitudRepo.findByEstado(estado);
    }

    @Override
    public List<Solicitud> buscarPorOrganizacion(String cuitOrg) {
        return List.of();
    }
}
