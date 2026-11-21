package com.biblioteca.api.config;

import com.biblioteca.api.domain.Usuario;
import com.biblioteca.api.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarios;

    public JwtFilter(JwtUtil jwtUtil, UsuarioRepository usuarios) {
        this.jwtUtil = jwtUtil;
        this.usuarios = usuarios;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith("/api/")
                || request.getServletPath().equals("/api/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null) {
            if (!header.startsWith("Bearer ") || header.length() <= 7) {
                unauthorized(response);
                return;
            }
            try {
                String email = jwtUtil.parseToken(header.substring(7)).getSubject();
                Usuario usuario = usuarios.buscarPorEmail(email);
                if (usuario == null || !("ADMIN".equals(usuario.getRole())
                        || "USER".equals(usuario.getRole()))) {
                    unauthorized(response);
                    return;
                }
                // A permissão atual vem do banco, não de um perfil antigo no token.
                var authentication = new UsernamePasswordAuthenticationToken(email, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
                unauthorized(response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"Token inválido ou expirado.\"}");
    }
}
