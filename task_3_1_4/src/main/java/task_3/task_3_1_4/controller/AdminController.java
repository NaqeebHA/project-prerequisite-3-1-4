package task_3.task_3_1_4.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import task_3.task_3_1_4.dto.UserDTO;
import task_3.task_3_1_4.model.Role;
import task_3.task_3_1_4.model.User;
import task_3.task_3_1_4.repo.RoleRepository;
import task_3.task_3_1_4.service.AuthenticationService;
import task_3.task_3_1_4.service.UserService;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserService userService;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    BCryptPasswordEncoder encoder;
    @Autowired
    AuthenticationService authenticationService;

    @ModelAttribute
    public void addAttributes(Model model) {
        if (authenticationService.getUser() != null) {
            model.addAttribute("userDetails", authenticationService.getUser());
            Set<String> roles = AuthorityUtils.authorityListToSet(authenticationService.getUser().getAuthorities());
            if (authenticationService.getUser().getAuthorities() != null) {
                model.addAttribute("role_admin", roles.contains("ROLE_ADMIN"));
                model.addAttribute("role_user", roles.contains("ROLE_USER"));
            }
        }
    }

    @GetMapping("")
    public String adminHomePage(Model model) {
        model.addAttribute("users", userService.getUserList());
        model.addAttribute("allRoles", (List<Role>) roleRepository.findAll());
        return "admin";
    }

    @GetMapping("/getOne")
    @ResponseBody
    public User getById(Long id) throws Exception {
        return userService.getUserById(id);
    }

    @GetMapping("/add_user")
    public String addUser(Model model) {
        model.addAttribute("userForm", new User());
        List<Role> roles = (List<Role>) roleRepository.findAll();
        Role admin = roleRepository.findByRole("ADMIN").get();
        Role user = roleRepository.findByRole("USER").get();
        model.addAttribute("allRoles", roles);
        model.addAttribute("user_role", user);
        model.addAttribute("admin_role", admin);
        return "add_user";
    }

    @PostMapping("/added")
    public String addUser(@Valid @ModelAttribute("userForm") User newUser, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            List<Role> roles = (List<Role>) roleRepository.findAll();
            model.addAttribute("allRoles", roles);
            return "add_user";
        }

        if (newUser.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByRole("USER").get();
            List<Role> newRole = List.of(userRole);
            newUser.setRoles(new HashSet<Role>(newRole));
        }

        String encodedPassword = encoder.encode(newUser.getPassword());
        newUser.setPassword(encodedPassword);
        userService.addUser(newUser);
        model.addAttribute("userForm", new User());
        model.addAttribute("users", userService.getUserList());
        return "redirect:/admin";
    }

    @PostMapping("/edited")
    public String postEditUser(UserDTO newUser) throws Exception {
        User userToEdit = userService.getUserById(newUser.getId());

        if (newUser.getPassword().isEmpty()) {
            newUser.setPassword(userToEdit.getPassword());
        } else {
            newUser.setPassword(encoder.encode(newUser.getPassword()));
        }

        if (newUser.getRoles().isEmpty()) {
            newUser.setRoles(userToEdit.getRoles());
        }
        userToEdit.fromUserDTO(newUser);
        userService.updateUser(userToEdit);
        return "redirect:/admin";
    }

    @GetMapping("/delete")
    public String deleteUser(UserDTO newUser) {
        userService.deleteUserById(newUser.getId());
        return "redirect:/admin";
    }
}
