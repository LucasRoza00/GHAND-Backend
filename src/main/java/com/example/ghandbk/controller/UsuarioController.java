package com.example.ghandbk.controller;

import com.example.ghandbk.collection.user.Usuario;
import com.example.ghandbk.dto.user.UsuarioDto;
import com.example.ghandbk.dto.user.UsuarioRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.example.ghandbk.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;


    @PostMapping("/create")
    public ResponseEntity addUser(@RequestBody UsuarioRequestDto usuarioRequestDto) throws InvalidValueException, NotAuthorizedException {
        usuarioService.insertUser(usuarioRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/updateUser")
    public ResponseEntity<Usuario> updateUser(@RequestBody UsuarioRequestDto usuarioRequestDto) throws InvalidValueException, NotAuthorizedException, NotFoundException {
        return new ResponseEntity(usuarioService.updateUser(usuarioRequestDto), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity deleteUser(@RequestBody UsuarioRequestDto usuarioRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        usuarioService.deleteUser(usuarioRequestDto);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping("/allUser")
    public ResponseEntity<List<Usuario>> getUsers() {
        return new ResponseEntity(usuarioService.getUsers(), HttpStatus.FOUND);
    }
//
//    @GetMapping("/findByUsername/{id}")
//    public ResponseEntity<UsuarioDto> findUser(@PathVariable("id")String username) throws  InvalidValueException, NotFoundException {
//        return new ResponseEntity(usuarioService.findUser(username), HttpStatus.FOUND);
//    }

    @GetMapping("findUserByid/{id}")
    public ResponseEntity<Usuario> findById(@PathVariable("id") String username) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(usuarioService.findUserByid(username), HttpStatus.FOUND);
    }

    @GetMapping("logUser")
    public ResponseEntity<UsuarioDto> logUser(@RequestBody UsuarioRequestDto usuarioRequestDto) throws NotFoundException, NotAuthorizedException {
        return new ResponseEntity(usuarioService.loginUser(usuarioRequestDto), HttpStatus.ACCEPTED);
    }

}
