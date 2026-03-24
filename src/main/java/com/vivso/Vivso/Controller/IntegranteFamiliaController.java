package com.vivso.Vivso.Controller;

import com.vivso.Vivso.DTO.IntegranteFamiliaDTO;
import com.vivso.Vivso.Service.IntegranteFamiliaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/familiar")
public class IntegranteFamiliaController {

    @Autowired
    private IntegranteFamiliaService integranteFamiliaService;

    // MOSTRAR TODOS LOS INTEGRANTES FAMILIARES
    @GetMapping("/listar")
    public ResponseEntity<List<IntegranteFamiliaDTO>> listarTodos() {
        List<IntegranteFamiliaDTO> lista = integranteFamiliaService.listarTodos();
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }
}
