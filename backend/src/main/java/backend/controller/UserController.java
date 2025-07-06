package backend.controller;

import backend.exception.UserNotFoundException;
import backend.model.UserModel;
import backend.model.UserDTO;
import backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("http://localhost:3000")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Register user
    @PostMapping("/users/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        Optional<UserModel> existingUser = userRepository.findByEmail(userDTO.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        UserModel newUser = new UserModel();
        newUser.setName(userDTO.getName());
        newUser.setEmail(userDTO.getEmail());
        newUser.setPassword(userDTO.getPassword()); // Plain password – hash in real apps
        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully");
    }

    // Login user
    @PostMapping("/users/login")
    public ResponseEntity<String> loginUser(@RequestBody UserDTO userDTO) {
        return userRepository.findByEmail(userDTO.getEmail())
                .map(user -> {
                    if (user.getPassword().equals(userDTO.getPassword())) {
                        return ResponseEntity.ok("Login successful");
                    } else {
                        return ResponseEntity.badRequest().body("Invalid password");
                    }
                }).orElse(ResponseEntity.badRequest().body("User not found"));
    }

    // Get all users
    @GetMapping("/users")
    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    @GetMapping("/users/{id}")
    public UserModel getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    // Update user
    @PutMapping("/users/{id}")
    public UserModel updateUser(@RequestBody UserDTO userDTO, @PathVariable Long id) {
        return userRepository.findById(id).map(existingUser -> {
            if (userDTO.getName() != null) {
                existingUser.setName(userDTO.getName());
            }
            if (userDTO.getEmail() != null) {
                existingUser.setEmail(userDTO.getEmail());
            }
            if (userDTO.getPassword() != null) {
                existingUser.setPassword(userDTO.getPassword());
            }
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new UserNotFoundException(id));
    }

    // Delete user
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.deleteById(id);
        return "User with ID " + id + " deleted";
    }
}
