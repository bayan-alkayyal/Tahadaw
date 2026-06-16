package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.UserDTOIn;
import org.example.tahadaw.DTO.OUT.UserDTOOut;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Model.enums.Role;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public void addUser(UserDTOIn userDTOIn) {

        if (userRepository.existsUserByUsername(userDTOIn.getUsername())) {
            throw new ApiException("Username already exists");
        }

        if (userRepository.existsUserByEmail(userDTOIn.getEmail())) {
            throw new ApiException("Email already exists");
        }

        User user = new User();

        user.setUsername(userDTOIn.getUsername());
        user.setPassword(userDTOIn.getPassword());
        user.setFullName(userDTOIn.getFullName());
        user.setEmail(userDTOIn.getEmail());
        user.setPhoneNumber(userDTOIn.getPhoneNumber());

        user.setRole(Role.USER);
        user.setIsPremium(false);

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }


    public List<UserDTOOut> getAllUsers() {

        List<User> users = userRepository.findAll();
        List<UserDTOOut> userDTOOuts = new ArrayList<>();

        for (User user : users) {
            UserDTOOut userDTOOut = convertToDTOOut(user);
            userDTOOuts.add(userDTOOut);
        }

        return userDTOOuts;
    }

    public void updateUser(Long userId, UserDTOIn userDTOIn) {

        User user = userRepository.findUserById(userId);

        if (user == null) {
            throw new ApiException("User not found");
        }

        User usernameOwner = userRepository.findUserByUsername(userDTOIn.getUsername());

        if (usernameOwner != null && !usernameOwner.getId().equals(userId)) {
            throw new ApiException("Username already exists");
        }

        User emailOwner = userRepository.findUserByEmail(userDTOIn.getEmail());

        if (emailOwner != null && !emailOwner.getId().equals(userId)) {
            throw new ApiException("Email already exists");
        }

        user.setUsername(userDTOIn.getUsername());
        user.setPassword(userDTOIn.getPassword());
        user.setFullName(userDTOIn.getFullName());
        user.setEmail(userDTOIn.getEmail());
        user.setPhoneNumber(userDTOIn.getPhoneNumber());

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }



    public void deleteUser(Long userId){

        User user = userRepository.findUserById(userId);

        if(user == null){
            throw new ApiException("User not found");
        }

        userRepository.delete(user);
    }


    private UserDTOOut convertToDTOOut(User user) {

        return new UserDTOOut(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getIsPremium()
        );
    }
}
