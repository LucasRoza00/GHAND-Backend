package com.example.ghandbk.controller;

import com.example.ghandbk.collection.supplier.Fornecedor;
import com.example.ghandbk.dto.supllier.FornecedorDto;
import com.example.ghandbk.dto.supllier.FornecedorRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.example.ghandbk.service.FornecedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/fornecedor")
@Tag(name = "Controller do Fornecedor", description = "Controller para ações relacionadas ao Fornecedor")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @PostMapping("/createFornecedor")
    @Operation(summary = "Cria um fornecedor", description = "Cria um fornecedor com as informações apartir de um Usuario")
    public ResponseEntity insertFornecedor(@RequestBody FornecedorRequestDto fornecedorRequestDto) throws NotFoundException, InvalidValueException, NotAuthorizedException {
        fornecedorService.addFornecedor(fornecedorRequestDto);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @PostMapping("/findAllFornecedores/{id}")
    @Operation(summary = "Lista todos os fornecedores", description = "Lista todos os fornecedores pelo Username")
    public ResponseEntity<List<FornecedorDto>> findAllFornecedoresByUser(@PathVariable("id") String username) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(fornecedorService.findAllFornecedores(username), HttpStatus.OK);
    }

    @PostMapping("/findFornecedorByCnpj")
    @Operation(summary = "Lista fornecedor", description = "Filtra o fornecedor pelo cnpj.")
    public ResponseEntity<FornecedorDto> findByCnpj(@RequestBody FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(fornecedorService.getFornecedorByCnpj(fornecedorRequestDto), HttpStatus.OK);
    }
    @PostMapping("/findFornecedorByRazaoSocial")
    @Operation(summary = "Lista fornecedor", description = "Filtra os fornecedores pela razaoSocial.")
    public ResponseEntity<List<FornecedorDto>> findFornecedoresByRazaoSocial(@RequestBody FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(fornecedorService.findFornecedorByrazaoSocial(fornecedorRequestDto), HttpStatus.OK);
    }

    @PostMapping("/findFornecedorByStatus")
    @Operation(summary = "Lista fornecedor", description = "Filtra os fornecedores pelo status.")
    public ResponseEntity<List<FornecedorDto>> findByStatus(@RequestBody FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(fornecedorService.findByStatus(fornecedorRequestDto), HttpStatus.OK);
    }

    @DeleteMapping("deleteFornecedor")
    @Operation(summary = "Deleta o fornecedor", description = "Detela o fornecedor com o username e pelo cnpj.")
    public ResponseEntity deleteByCnpj(@RequestParam String username,
                                       @RequestParam String cnpj) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        fornecedorService.deleteFornecedor(username,cnpj);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @PutMapping(value = "/updateFornecedor", produces = "application/json")
    @Operation(summary = "Atualiza um fornecedor", description = "Atualiza os dados de um fornecedor pelo CNPJ.")
    public ResponseEntity<FornecedorDto> updateFornecedor(@RequestParam String cnpj, @RequestBody FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        return new ResponseEntity(fornecedorService.updateFornecedor(fornecedorRequestDto, cnpj), HttpStatus.ACCEPTED);
    }

    @PutMapping(value = "/updateStatus", produces = "application/json")
    @Operation(summary = "Atualiza o status do Fornecedor", description = "Atualiza o status de um fornecedor pelo cnpj.")
    public ResponseEntity<FornecedorDto> updateStatus(@RequestBody FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        return new ResponseEntity(fornecedorService.updateFornecedorByStatus(fornecedorRequestDto), HttpStatus.ACCEPTED);
    }
}
