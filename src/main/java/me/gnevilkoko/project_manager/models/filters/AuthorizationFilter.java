package me.gnevilkoko.project_manager.models.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.gnevilkoko.project_manager.models.exceptions.BaseApiException;
import me.gnevilkoko.project_manager.models.exceptions.TokenNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuthorizationFilter extends OncePerRequestFilter {
    private List<String> publicRoutes = new ArrayList<>() {
        {add("/authorization");}
        {add("/swagger");}
        {add("/v3");}
    };

    private UserDetailsService userDetailsService;

    @Autowired
    public AuthorizationFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if(isPublicRoute(uri)){
            chain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

       try {
           if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
               throw new TokenNotFoundException();
           }

           String token = authorizationHeader.substring(7);
           UserDetails userDetails = userDetailsService.loadUserByUsername(token);

           UsernamePasswordAuthenticationToken authenticationToken =
                   new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
           authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

           SecurityContextHolder.getContext().setAuthentication(authenticationToken);

           chain.doFilter(request, response);
       } catch (BaseApiException e){
           response.setStatus(e.getStatus().value());
           response.setContentType("application/json");
           response.getWriter().write(e.toJson());
       }
    }

    public boolean isPublicRoute(String route) {
        for (String publicRoute : publicRoutes) {
            if(route.startsWith(publicRoute)){
                return true;
            }
        }

        return false;
    }
}
