package ru.kata.spring.boot_security.demo.service;


import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;

public interface UserService {
    User findByUsername(String username);
    List<User> findAllUsers();
    User saveUser(User user);
    void deleteUser(long id);
    UserDetailsService loadUserByUsername(String username) throws UsernameNotFoundException;
    User findById(long id);
    boolean isAdmin(User user);
    boolean hasRole(User user, String role);

}
