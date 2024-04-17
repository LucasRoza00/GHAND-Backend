package com.example.ghandbk.service;

import com.example.ghandbk.collection.enums.Situacao;
import com.example.ghandbk.collection.supplier.Fornecedor;
import com.example.ghandbk.dto.supllier.FornecedorDto;
import com.example.ghandbk.dto.supllier.FornecedorRequestDto;
import com.example.ghandbk.dto.user.UsuarioRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class FornecedorService {

    private final UsuarioService usuarioService;
    private final ObjectMapper objectMapper;

    public void addFornecedor(FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotAuthorizedException, NotFoundException {
        if (fornecedorRequestDto.getRazaoSocial().isBlank()) throw new InvalidValueException("Preencha o campo");
        if (fornecedorRequestDto.getCnpj().length() <= 11) throw new InvalidValueException("Cnpj inválido");
        UsuarioRequestDto usuarioRequestDto = new UsuarioRequestDto();
        usuarioRequestDto.setUsername(fornecedorRequestDto.getUsername());
        usuarioRequestDto.setName(fornecedorRequestDto.getName());
        Fornecedor fornecedor = objectMapper.convertValue(fornecedorRequestDto, Fornecedor.class);
        fornecedor.setStatus(Situacao.ATIVA);
        usuarioRequestDto.setFornecedor(fornecedor);
        usuarioService.updateUser(usuarioRequestDto);
    }

    public List<FornecedorDto> findAllFornecedores(String username) throws InvalidValueException, NotFoundException {
       List<Fornecedor> fornecedores = usuarioService.getFornecedores(username);
       List<FornecedorDto> fornecedorDtos = fornecedores.stream().map(a -> new FornecedorDto(a.getRazaoSocial(), a.getCnpj(), a.getStatus(), a.getHistorico())).collect(Collectors.toList());
       return fornecedorDtos;
    }

    public List<FornecedorDto> findFornecedorByrazaoSocial(FornecedorRequestDto fornecedorRequestDto) throws NotFoundException, InvalidValueException {
        List<FornecedorDto> fornecedores = findAllFornecedores(fornecedorRequestDto.getUsername());
        if (fornecedorRequestDto.getRazaoSocial().isBlank()) throw new InvalidValueException("Preencha o campo!");
        List<FornecedorDto> fornecedorReturn = fornecedores.stream().filter(fornecedor -> fornecedor.getRazaoSocial().equals(fornecedorRequestDto.getRazaoSocial())).toList();
        if (fornecedorReturn.isEmpty()) throw new NotFoundException("Fornecedores não encontrados");
        return fornecedorReturn;
    }

    public FornecedorDto getFornecedorByCnpj(FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException {
        if (fornecedorRequestDto.getCnpj().isBlank() || fornecedorRequestDto.getCnpj().length() <= 11) throw new InvalidValueException("Cnpj inválido");
        verifyCnpj(fornecedorRequestDto.getUsername(), fornecedorRequestDto.getCnpj());
        List<FornecedorDto> fornecedores = findAllFornecedores(fornecedorRequestDto.getUsername());
        Stream<FornecedorDto> fornecedorDto = fornecedores.stream().filter(fornecedor -> fornecedor.getCnpj().equals(fornecedorRequestDto.getCnpj()));
        FornecedorDto fornecedorReturn = fornecedorDto.findAny().get();

        return fornecedorReturn;
    }

    public void deleteFornecedor(FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (fornecedorRequestDto.getUsername().isBlank()) throw new InvalidValueException("Preencha o campo");
        UsuarioRequestDto usuarioRequestDto = new UsuarioRequestDto();
        Fornecedor fornecedor = objectMapper.convertValue(fornecedorRequestDto, Fornecedor.class);
        usuarioRequestDto.setUsername(fornecedorRequestDto.getUsername());
        usuarioRequestDto.setFornecedor(fornecedor);
        usuarioService.deleteFornecedor(usuarioRequestDto);
    }

    public FornecedorDto updateFornecedor(FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (fornecedorRequestDto.getCnpj().isBlank() || fornecedorRequestDto.getCnpj().length() <= 11) throw new InvalidValueException("Cnpj inválido");
        if (fornecedorRequestDto.getUsername() == null || fornecedorRequestDto.getUsername().isBlank()) throw new InvalidValueException("Usuario inválido");
        List<Fornecedor> fornecedors = usuarioService.getFornecedores(fornecedorRequestDto.getUsername());
        Stream<Fornecedor> fornecedorStream = fornecedors.stream().filter(fornecedor -> fornecedor.getCnpj().equals(fornecedorRequestDto.getCnpj()));
        Fornecedor fornecedorToSave = fornecedorStream.findAny().get();
        if (fornecedorRequestDto.getRazaoSocial() != null) {
            fornecedorToSave.setRazaoSocial(fornecedorRequestDto.getRazaoSocial());
        } else if (fornecedorRequestDto.getStatus() != null) {
            fornecedorToSave.setStatus(fornecedorRequestDto.getStatus());
        }
        if (fornecedorRequestDto.getHistoricoPagamento() != null) {
            fornecedorToSave.setHistorico(insertHistory(fornecedorRequestDto));
        } else if (fornecedorRequestDto.getHistoricoProduto() != null) {
            fornecedorToSave.setHistorico(insertHistory(fornecedorRequestDto));
        }
        fornecedorToSave.setCnpj(fornecedorRequestDto.getCnpj());
        UsuarioRequestDto user = new UsuarioRequestDto();
        user.setUsername(fornecedorRequestDto.getUsername());
        user.setName(fornecedorRequestDto.getName());
        user.setFornecedor(fornecedorToSave);
        return usuarioService.updateFornecedor(user);
    }

    public FornecedorDto updateFornecedorByStatus(FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (fornecedorRequestDto.getCnpj().isBlank() || fornecedorRequestDto.getCnpj().length() <= 11) throw new InvalidValueException("Cnpj inválido");
        if (fornecedorRequestDto.getUsername() == null || fornecedorRequestDto.getUsername().isBlank()) throw new InvalidValueException("Usuario inválido");
        verifyCnpj(fornecedorRequestDto.getUsername(), fornecedorRequestDto.getCnpj());
        List<Fornecedor> fornecedors = usuarioService.getFornecedores(fornecedorRequestDto.getUsername());
        Stream<Fornecedor> fornecedorStream = fornecedors.stream().filter(fornecedor -> fornecedor.getCnpj().equals(fornecedorRequestDto.getCnpj()));
        Fornecedor fornecedorToSave = fornecedorStream.findAny().get();
        switch (fornecedorRequestDto.getStatus()) {
            case ATIVA, INATIVA -> fornecedorToSave.setStatus(fornecedorRequestDto.getStatus());
        }
        UsuarioRequestDto user = new UsuarioRequestDto();
        user.setUsername(fornecedorRequestDto.getUsername());
        user.setName(fornecedorRequestDto.getName());
        user.setFornecedor(fornecedorToSave);
        return usuarioService.updateFornecedor(user);
    }
    public List<FornecedorDto> findByStatus(FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException {
        if (fornecedorRequestDto.getUsername() == null || fornecedorRequestDto.getUsername().isBlank()) throw new InvalidValueException("Preencha o campo");
        List<FornecedorDto> fornecedores = findAllFornecedores(fornecedorRequestDto.getUsername());
        List<FornecedorDto> fornecedoresReturn = fornecedores
                .stream().filter(fornecedor -> fornecedor.getStatus()
                        .equals(fornecedorRequestDto.getStatus())).toList();
        if (fornecedoresReturn.isEmpty()) throw new NotFoundException("Fornecedores com este status não encontrados");
        return fornecedoresReturn;
    }

    private void verifyCnpj(String username, String cnpj) throws InvalidValueException, NotFoundException {
        try {
            Fornecedor fornecedor = usuarioService.getFornecedores(username).stream().filter(fornecedores -> fornecedores.getCnpj().equals(cnpj)).findAny().get();
        } catch (NoSuchElementException e) {
            throw new NotFoundException("Fornecedor não encontrado");
        }
    }
    private List insertHistory(FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException {
        List<Fornecedor> fornecedores = usuarioService.getFornecedores(fornecedorRequestDto.getUsername());
        Fornecedor fornecedor = fornecedores.stream().filter(forne -> forne.getCnpj().equals(fornecedorRequestDto.getCnpj())).findAny().get();
        if (fornecedor != null) {
            try {
                if (fornecedorRequestDto.getHistoricoPagamento() != null) {
                    fornecedor.getHistorico().add(fornecedorRequestDto.getHistoricoPagamento());
                } else if (fornecedorRequestDto.getHistoricoProduto() != null) {
                    fornecedor.getHistorico().add(fornecedorRequestDto.getHistoricoProduto());
                }
            } catch (NullPointerException e) {
                List<Object> f1 = new ArrayList<>();
                if (fornecedorRequestDto.getHistoricoProduto() != null) {
                    f1.add(fornecedorRequestDto.getHistoricoProduto());
                } else if (fornecedorRequestDto.getHistoricoPagamento() != null) {
                    f1.add(fornecedorRequestDto.getHistoricoPagamento());
                }
                fornecedor.setHistorico(f1);
            }
        }

        return fornecedor.getHistorico();
    }

}
