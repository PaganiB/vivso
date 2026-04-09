package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.OrganizacionDTO;
import com.vivso.Vivso.Mapper.Mapper;
import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizacionService implements IOrganizacionService {

    @Autowired
    private IOrganizacionRepository orgRepo;

    @Override
    public List<OrganizacionDTO> getOrganizacion() {
        //Creamos listas para organizaciones y dto
        List<Organizacion> organizaciones = orgRepo.findAll();
        List<OrganizacionDTO> organizacionesDto = new ArrayList<>();
        //Recorremos todas las orgs de la lista organizaciones
        //Las convertimos en dto y las agregamos a la lista dto
        OrganizacionDTO dto;
        for (Organizacion o : organizaciones) {
            dto = Mapper.toDTO(o);
            organizacionesDto.add (dto);
        }
        return organizacionesDto;
    }

    @Override
    public OrganizacionDTO saveOrganizacion(OrganizacionDTO organizacionDto) {

        if (orgRepo.existsOrganizacionByCuit(organizacionDto.getCuit()) == true) throw new RuntimeException("Ese cuit ya existe");
        //Construir nuestro objeto organizacion
        var org = Organizacion.builder()
                .cuit(organizacionDto.getCuit())
                .nombre(organizacionDto.getNombre())
                .tipo(organizacionDto.getTipo())
                .dom_legal(organizacionDto.getDom_legal())
                .contacto(organizacionDto.getContacto())
                .cpe(organizacionDto.getCpe())
                .nota_solicitud_url(organizacionDto.getNota_solicitud_url())
                .vigencia_url(organizacionDto.getVigencia_url())
                .acta_compromiso_url(organizacionDto.getActa_compromiso_url())
                .acta_asamblea_url(organizacionDto.getActa_asamblea_url())
                .dni_autoridades_url(organizacionDto.getDni_autoridades_url())
                .certificado_residencia_url(organizacionDto.getCertificado_residencia_url())
                .cuenta_bancaria_url(organizacionDto.getCuenta_bancaria_url())
                .alta_afip_url(organizacionDto.getAlta_afip_url())
                .build();
        //Guardamos en la BD
        orgRepo.save(org);
        //Mapeo de salida
        OrganizacionDTO organizacionSalida = Mapper.toDTO(org);
        return organizacionSalida;
    }

    @Override
    public OrganizacionDTO updateOrganizacion(String cuit, OrganizacionDTO organizacionDto) {
        //buscar si la organizacion existe para actualizarla
        Organizacion o = orgRepo.findById(cuit).orElse(null);
        if (o == null) throw new RuntimeException("organizacion no encontrada");
        //hacemos el update en todos los atributos donde sea necesario
        if (organizacionDto.getNombre()!=null) {
            o.setNombre(organizacionDto.getNombre());
        }
        if (organizacionDto.getTipo()!=null) {
            o.setTipo(organizacionDto.getTipo());
        }
        if (organizacionDto.getDom_legal()!=null) {
            o.setDom_legal(organizacionDto.getDom_legal());
        }
        if (organizacionDto.getContacto()!=null) {
            o.setContacto(organizacionDto.getContacto());
        }
        if (organizacionDto.getCpe()!=null) {
            o.setCpe(organizacionDto.getCpe());
        }
        if (organizacionDto.getNota_solicitud_url()!=null) {
            o.setNota_solicitud_url(organizacionDto.getNota_solicitud_url());
        }
        if (organizacionDto.getVigencia_url()!=null) {
            o.setVigencia_url(organizacionDto.getVigencia_url());
        }
        if (organizacionDto.getActa_compromiso_url()!=null) {
            o.setActa_compromiso_url(organizacionDto.getActa_compromiso_url());
        }
        if (organizacionDto.getActa_asamblea_url()!=null) {
            o.setActa_asamblea_url(organizacionDto.getActa_asamblea_url());
        }
        if (organizacionDto.getDni_autoridades_url()!=null) {
            o.setDni_autoridades_url(organizacionDto.getDni_autoridades_url());
        }
        if (organizacionDto.getCertificado_residencia_url()!=null) {
            o.setCertificado_residencia_url(organizacionDto.getCertificado_residencia_url());
        }
        if (organizacionDto.getCuenta_bancaria_url()!=null) {
            o.setCuenta_bancaria_url(organizacionDto.getCuenta_bancaria_url());
        }
        if (organizacionDto.getAlta_afip_url()!=null) {
            o.setAlta_afip_url(organizacionDto.getAlta_afip_url());
        }
        //Guardamos en la BD la organizacion actualizada
        orgRepo.save(o);
        //Mapeo de la salida
        OrganizacionDTO organizacionSalida = Mapper.toDTO(o);
        return organizacionSalida;
    }

    @Override
    public void deleteOrganizacion(String cuit) {
        Organizacion o = orgRepo.findById(cuit).orElse(null);
        if (o == null) throw new RuntimeException("organizacion no encontrada");
        orgRepo.delete(o);
    }

    @Override
    public OrganizacionDTO buscarPorCuit(String cuit) {
        //Buscamos la organizacion
        Organizacion o = orgRepo.findOrganizacionByCuit(cuit).orElse(null);
        if (o == null) throw new RuntimeException("organizacion no encontrada");
        //Convertivos organizacion a Dto
        return Mapper.toDTO(o);
    }

    @Override
    public List<OrganizacionDTO> buscarPorTipo(String tipo) {
        //Buscamos las organizaciones pertenecientes al tipo elegido
        List<Organizacion> o = orgRepo.findOrganizacionByTipo(tipo);
        if (o.isEmpty()) {
            throw new RuntimeException("Ninguna organizacion pertenece a este tipo");
        }
        //Creamos una lista para guardar los dto
        List<OrganizacionDTO> organizacionesDto = new ArrayList<>();
        for (Organizacion organizacion : o) {
            organizacionesDto.add(Mapper.toDTO(organizacion));
        }
        return organizacionesDto;
    }

    @Override
    public List<OrganizacionDTO> buscarPorNombre(String nombre) {
        //Buscamos las organizaciones que contengan el nombre indicado
        List<Organizacion> o = orgRepo.findByNombreContainingIgnoreCase(nombre);
        if (o.isEmpty()) {
            throw new RuntimeException("Ninguna organizacion tiene o contiene ese nombre");
        }
        //Creamos una lista para guardar los dto
        List<OrganizacionDTO> organizacionesDto = new ArrayList<>();
        for (Organizacion organizacion : o) {
            organizacionesDto.add(Mapper.toDTO(organizacion));
        }
        return organizacionesDto;
    }
}
