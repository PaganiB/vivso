package com.vivso.Vivso.Service;

import com.vivso.Vivso.Repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private IUsuarioRepository usuarioRepo;

    /*@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscamos al usuario en MariaDB usando el repositorio.
        // Si no existe, arrojamos la excepción oficial de Spring Security.
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario con username '" + username + "' no fue encontrado en el sistema."));
    }*/
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }
}