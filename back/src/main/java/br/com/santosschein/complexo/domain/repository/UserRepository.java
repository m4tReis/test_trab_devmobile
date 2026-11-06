package br.com.santosschein.complexo.domain.repository;

import br.com.santosschein.complexo.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByDocument(String document);

    User save(User user);

    List<User> findAllOrderedByName();
}
