package ru.kata.spring.boot_security.demo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleServiceImpl;
import ru.kata.spring.boot_security.demo.service.UserServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserServiceImpl userService;
    private final RoleServiceImpl roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(UserServiceImpl userService, RoleServiceImpl roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showAllUsers(Model model, Principal principal) {
        User current = userService.findByUsername(principal.getName());
        model.addAttribute("newUser", new User());
        model.addAttribute("allUsers", userService.findAllUsers());
        model.addAttribute("users", List.of(current));
        model.addAttribute("principalEmail", current.getEmail());
        model.addAttribute("formattedRoles", RoleServiceImpl.humanRoles(current.getRoles()));
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin";
    }

    @PostMapping("/save-user")
    public String saveUser(@ModelAttribute("user") User newUser,
                           @RequestParam(value = "roles", required = false) List<Long> roleIds){
        Set<Role> roles;
        if(roleIds != null && !roleIds.isEmpty()) {
            roles = Set.of(roleService.findByName("ROLE_USER"));
        } else {
            roles = new HashSet<>(roleService.findRolesByIds(roleIds));
        }
        newUser.setRoles(roles);

        userService.save(newUser);
        return "redirect:/admin";
    }

    @PostMapping("/update-user")
    public String updateUser(@ModelAttribute User updated,
                             @RequestParam("roles") List<Long> roleIds) {
        User existing = userService.findById(updated.getId());
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setAge(updated.getAge());
        existing.setEmail(updated.getEmail());
        if (!updated.getPassword().isBlank()) {          // не перешифровывать пустой пароль
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }
        existing.setRoles(new HashSet<>(roleService.findRolesByIds(roleIds)));
        userService.save(existing);
        return "redirect:/admin";
    }

    @PostMapping("delete-user")
    public String deleteUser(@RequestParam("id") Long id, Principal principal) {
        if (principal.getName().equals(userService.findById(id).getUsername())) {
            userService.deleteUser(id);
            return "redirect:/login";
        }
        if(id == null || userService.findById(id) == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        userService.deleteUser(id);
        return "redirect:/admin";
    }

}