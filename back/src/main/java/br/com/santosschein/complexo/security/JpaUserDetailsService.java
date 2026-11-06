package br.com.santosschein.complexo.security;

import br.com.santosschein.complexo.application.service.UserIdentityNormalizer;
import br.com.santosschein.complexo.infrastructure.persistence.entity.UserEntity;
import br.com.santosschein.complexo.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final JpaUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(UserIdentityNormalizer.email(email))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado."));

        return new AuthenticatedUser(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole(), null);
    }
}
