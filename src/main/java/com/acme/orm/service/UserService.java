package com.acme.orm.service;

import com.acme.orm.domain.User;
import com.acme.orm.repository.UserRepository;
import com.acme.orm.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUserOrThrow(Long id) {
        return userRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("User %d not found".formatted(id)));
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}

