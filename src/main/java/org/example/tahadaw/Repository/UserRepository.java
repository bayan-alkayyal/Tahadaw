package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findUserById(Long userId);


    User findUserByUsername(String username);

    User findUserByEmail(String email);

    Boolean existsUserByUsername(String username);

    Boolean existsUserByEmail(String email);
}
