package br.com.leoparedes.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.leoparedes.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks")) {

            // pegar a autenticação (username e password)        
            var authorization =  request.getHeader("Authorization");

            var authEncoded = authorization.substring("Basic".length()).trim();

            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);

            var authString = new String(authDecoded);

            String[] credentials = authString.split(":");

            String userName = credentials[0];

            String password = credentials[1];

            // Validar usuário
            var user = this.userRepository.findByUserName(userName);

            if (user == null) {
                response.sendError(401);
            } else {                
                // Validar senha
                var passwordVerify = BCrypt.verifyer(null, null).verify(password.toCharArray(), user.getPassword());
                
                if (passwordVerify.verified) {                
                    // Segue viagem
                    request.setAttribute("userId", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }                
            }
            // Segue viagem
        } else {
            filterChain.doFilter(request, response);
        }
        
    

    }

    
}
