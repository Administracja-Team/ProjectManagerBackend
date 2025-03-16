package me.gnevilkoko.project_manager.models.services;

import me.gnevilkoko.project_manager.models.exceptions.TokenNotFoundException;
import me.gnevilkoko.project_manager.models.repositories.BearerTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BearerTokenService {
    private BearerTokenRepo repo;

    @Autowired
    public BearerTokenService(BearerTokenRepo repo) {
        this.repo = repo;
    }

    public UserDetailsService getUserDetailsService() {
        return token -> {
            String checkedToken = token;
            if(checkedToken.startsWith("Bearer")) {
                checkedToken = checkedToken.substring(7);
            }
            return repo.findByToken(checkedToken).orElseThrow(TokenNotFoundException::new);
        };
    }
}
