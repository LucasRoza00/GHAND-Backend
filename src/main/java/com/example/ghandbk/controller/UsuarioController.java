package com.example.ghandbk.controller;

import com.example.ghandbk.collection.user.Usuario;
import com.example.ghandbk.dto.user.UsuarioDto;
import com.example.ghandbk.dto.user.UsuarioRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.example.ghandbk.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/usuario")
@Tag(name = "Controller do usuario", description = "Controller para ações relacionadas ao Usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;


    @PostMapping("/create")
    @Operation(summary = "Cria um usuario", description = "Cria um usuário")
    public ResponseEntity addUser(@RequestBody UsuarioRequestDto usuarioRequestDto) throws InvalidValueException, NotAuthorizedException {
        usuarioService.insertUser(usuarioRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/updateUser")
    @Operation(summary = "Atualiza o Usuario", description = "Atualiza o usuário apartir do username")
    public ResponseEntity<Usuario> updateUser(@RequestBody UsuarioRequestDto usuarioRequestDto) throws InvalidValueException, NotAuthorizedException, NotFoundException {
        return new ResponseEntity(usuarioService.updateUser(usuarioRequestDto), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/deleteUser")
    @Operation(summary = "Delete um usuario", description = "Deleta o usuário apartir da combinação entre username e password")
    public ResponseEntity deleteUser(@RequestBody UsuarioRequestDto usuarioRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        usuarioService.deleteUser(usuarioRequestDto);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping("/allUser")
    @Operation(summary = "Lista todos os Usuários", description = "Lista todos os funcionarios para testes e realização de realeses")
    public ResponseEntity<List<Usuario>> getUsers() {
        return new ResponseEntity(usuarioService.getUsers(), HttpStatus.FOUND);
    }

    @GetMapping("findUserByid/{id}")
    @Operation(summary = "Lista um usuário", description = "Retorna um usuario pelo username")
    public ResponseEntity<Usuario> findById(@PathVariable("id") String username) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(usuarioService.findUserByid(username), HttpStatus.FOUND);
    }

    @PostMapping("/logUser")
    @Operation(summary = "Efetua login", description = "Retorna informações do usuário com a conbinação de username e password")
    public ResponseEntity<UsuarioDto> logUser(@RequestBody UsuarioRequestDto usuarioRequestDto) throws NotFoundException, NotAuthorizedException {
        return new ResponseEntity(usuarioService.loginUser(usuarioRequestDto), HttpStatus.ACCEPTED);
    }


    @PutMapping("/modifyUserInfos")
    @Operation(summary = "Altera informações do usuário", description = "Modifica as informações do usuário apartir de confirmação de senha")
    public ResponseEntity<UsuarioDto> modifyUser(@RequestParam String username,
                                                 @RequestParam String usernameToSet,
                                                 @RequestParam String nameToSet,
                                                 @RequestParam String password) throws InvalidValueException, NotAuthorizedException {
        return new ResponseEntity(usuarioService.modifyUsersInfo(username, usernameToSet, nameToSet, password), HttpStatus.OK);
    }
}
