package com.example.ghandbk.controller;

import com.example.ghandbk.collection.supplier.Fornecedor;
import com.example.ghandbk.dto.supllier.FornecedorDto;
import com.example.ghandbk.dto.supllier.FornecedorRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.example.ghandbk.service.FornecedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/fornecedor")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @PostMapping("/createFornecedor")
    public ResponseEntity insertFornecedor(@RequestBody FornecedorRequestDto fornecedorRequestDto) throws NotFoundException, InvalidValueException, NotAuthorizedException {
        fornecedorService.addFornecedor(fornecedorRequestDto);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @PostMapping("/findAllFornecedores/{id}")
    public ResponseEntity<List<FornecedorDto>> findAllFornecedoresByUser(@PathVariable("id") String username) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(fornecedorService.findAllFornecedores(username), HttpStatus.OK);
    }

    @GetMapping("/findFornecedorByCnpj")
    public ResponseEntity<FornecedorDto> findByCnpj(@RequestBody FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(fornecedorService.getFornecedorByCnpj(fornecedorRequestDto), HttpStatus.FOUND);
    }
    @GetMapping("/findFornecedorByRazaoSocial")
    public ResponseEntity<List<FornecedorDto>> findFornecedoresByRazaoSocial(@RequestBody FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(fornecedorService.findFornecedorByrazaoSocial(fornecedorRequestDto), HttpStatus.FOUND);
    }

    @PostMapping("/findFornecedorByStatus")
    public ResponseEntity<List<FornecedorDto>> findByStatus(@RequestBody FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(fornecedorService.findByStatus(fornecedorRequestDto), HttpStatus.FOUND);
    }

    @DeleteMapping("deleteFornecedor")
    public ResponseEntity deleteByCnpj(@RequestBody FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        fornecedorService.deleteFornecedor(fornecedorRequestDto);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @PutMapping(value = "/updateFornecedor/{cnpj}", produces = "application/json")
    public ResponseEntity<FornecedorDto> updateFornecedor(@PathVariable("cnpj") String cnpj, @RequestBody FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        return new ResponseEntity(fornecedorService.updateFornecedor(fornecedorRequestDto, cnpj), HttpStatus.ACCEPTED);
    }

    @PutMapping(value = "/updateStatus", produces = "application/json")
    public ResponseEntity<FornecedorDto> updateStatus(@RequestBody FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        return new ResponseEntity(fornecedorService.updateFornecedorByStatus(fornecedorRequestDto), HttpStatus.ACCEPTED);
    }
}
