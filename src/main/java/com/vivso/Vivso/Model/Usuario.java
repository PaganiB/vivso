package com.vivso.Vivso.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password_hash;

    @Lob
    @Column(name = "rol", nullable = false)
    private String rol;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "debe_cambiar_password", nullable = false)
    private Boolean debeCambiarPassword = false;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "apellido", length = 100)
    private String apellido;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Pulsera de visitante: mientras tenga la contraseña temporal, solo puede pasar por recepción
        if (Boolean.TRUE.equals(this.debeCambiarPassword)) {
            return List.of(new SimpleGrantedAuthority("ROLE_CAMBIO_PASSWORD"));
        }

        // Spring Security es estricto: exige que los roles empiecen con el prefijo "ROLE_"
        // Convertimos el String de tu base de datos (ej: "ADMIN") a "ROLE_ADMIN"
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.rol.toUpperCase()));
    }

    @Override
    public String getPassword() {
        // Le avisamos a Spring que el hash de la contraseña vive en esta variable
        return this.password_hash;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // La cuenta nunca expira
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // La cuenta no se bloquea por intentos fallidos (por ahora)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Las contraseñas no vencen cada X meses
    }

    @Override
    public boolean isEnabled() {
        return this.activo != null ? this.activo : false;
    }

    public String getNombreUsuarioReal() {
        return this.username;
    }

}