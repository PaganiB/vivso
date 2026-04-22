package com.vivso.Vivso.Service;

import com.vivso.Vivso.DTO.IntegranteDTO;
import com.vivso.Vivso.Mapper.Mapper;
import com.vivso.Vivso.Model.Integrante;
import com.vivso.Vivso.Model.Organizacion;
import com.vivso.Vivso.Model.Usuario;
import com.vivso.Vivso.Repository.IIntegranteRepository;
import com.vivso.Vivso.Repository.IOrganizacionRepository;
import com.vivso.Vivso.Repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IntegranteService implements IIntegranteService {

    @Autowired
    private IIntegranteRepository integranteRepo;
    @Autowired
    private IOrganizacionRepository orgRepo;
    @Autowired
    private IUsuarioRepository usuarioRepo;

    @Override
    public List<IntegranteDTO> getIntegrantes() {
        //Creamos listas para Integrantes y dto
        List<Integrante> integrantes = integranteRepo.findAll();
        List<IntegranteDTO> integrantesDto = new ArrayList<>();
        //Recorremos todos los integrantes de la lista integrantes
        //Las convertimos en dto y las agregamos a la lista dto
        IntegranteDTO dto;
        for (Integrante i : integrantes) {
            dto = Mapper.toDTO(i);
            integrantesDto.add (dto);
        }
        return integrantesDto;
    }

    @Override
    public IntegranteDTO saveIntegrantes(IntegranteDTO integranteDto) {

        // 1. Buscamos los objetos reales en la base de datos usando los nombres/IDs del DTO
        Organizacion org = orgRepo.findById(integranteDto.getCuitOrg())
                .orElseThrow(() -> new RuntimeException("Organizacion no encontrada"));

        Usuario user = usuarioRepo.findByUsername(integranteDto.getUsuario())
                .orElse(null);


        //Construir nuestro objeto integrante
        var i = Integrante.builder()
                .dni(integranteDto.getDni())
                .nombre(integranteDto.getNombre())
                .apellido(integranteDto.getApellido())
                .telefono(integranteDto.getTelefono())
                .funcion(integranteDto.getFuncion())
                .domicilio(integranteDto.getDomicilio())
                .activo(integranteDto.getActivo())
                .organizacion(org)
                .usuario(user)
                .build();
        //Guardamos en la BD
        integranteRepo.save(i);
        //Mapeo de salida
        IntegranteDTO integranteSalida = Mapper.toDTO(i);
        return integranteSalida;
    }

    @Override
    public IntegranteDTO updateIntegrantes(Integer idIntegrante,
                                           IntegranteDTO integranteDto) {
        //buscar si el integrante existe para actualizarlo
        Integrante i = integranteRepo.findById(idIntegrante).orElse(null);
        if (i == null) throw new RuntimeException("Integrante no encontrado");
        //hacemos el update en todos los atributos donde sea necesario
        if (integranteDto.getDni()!=null) {
            i.setDni(integranteDto.getDni());
        }
        if (integranteDto.getNombre()!=null) {
            i.setNombre(integranteDto.getNombre());
        }
        if (integranteDto.getApellido()!=null) {
            i.setApellido(integranteDto.getApellido());
        }
        if (integranteDto.getTelefono()!=null) {
            i.setTelefono(integranteDto.getTelefono());
        }
        if (integranteDto.getFuncion()!=null) {
            i.setFuncion(integranteDto.getFuncion());
        }
        if (integranteDto.getDomicilio()!=null) {
            i.setDomicilio(integranteDto.getDomicilio());
        }
        if (integranteDto.getActivo()!=null) {
            i.setActivo(integranteDto.getActivo());
        }
        if (integranteDto.getUsuario()!=null) {
            Usuario user = usuarioRepo.findByUsername(integranteDto.getUsuario()).orElse(null);
            if (user==null) throw new RuntimeException("Usuario no encontrado");
            i.setUsuario(user);
        }

        if (integranteDto.getCuitOrg()!=null) {
            Organizacion org = orgRepo.findById(integranteDto.getCuitOrg()).orElse(null);
            if (org == null) throw new RuntimeException("Organizacion no encontrada");
            i.setOrganizacion(org);
        }
        //Guardamos en la BD al integrante actualizado
        integranteRepo.save(i);
        //Mapeo de la salida
        IntegranteDTO integranteSalida = Mapper.toDTO(i);
        return integranteSalida;
    }

    @Override
    public void deleteIntegrantes(Integer idIntegrante) {
        Integrante i = integranteRepo.findById(idIntegrante).orElse(null);
        if (i == null) throw new RuntimeException("Integrante no encontrado");
        integranteRepo.delete(i);
    }

    @Override
    public List<IntegranteDTO> listarPorOrganizacion(String cuitOrg) {
        //Primero creamos una lista que contenga a los integrantes de una misma org
        List<Integrante> integrantes = integranteRepo.findByOrganizacion_Cuit(cuitOrg);
        //Creamos lista vacia para los dto
        List<IntegranteDTO> integrantesDto = new ArrayList<>();
        //Recorremos el for integrante por integrante y agregamos a la lista dto los integrantes
        for (Integrante i : integrantes) {
            integrantesDto.add(Mapper.toDTO(i));
        }
        return integrantesDto;
    }

    @Override
    public List<IntegranteDTO> buscarActivos() {
        //Primero creamos una lista que contenga a los integrantes activos
        List<Integrante> integrantes = integranteRepo.findByActivoTrue();
        //Creamos lista vacia para los dto
        List<IntegranteDTO> integrantesDto = new ArrayList<>();
        //Recorremos el for integrante por integrante y agregamos a la lista dto los integrantes activos
        for (Integrante i : integrantes) {
            integrantesDto.add(Mapper.toDTO(i));
        }
        return integrantesDto;
    }

}
