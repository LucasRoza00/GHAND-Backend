package com.example.ghandbk.repository;

import com.example.ghandbk.collection.user.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    @Query("{'username' : ?0, 'password': ?1}")
    Usuario findUser(String username, String password);
    @Query("{'username' : ?0, 'name': ?1}")
    Usuario getUser(String username, String name);

}
