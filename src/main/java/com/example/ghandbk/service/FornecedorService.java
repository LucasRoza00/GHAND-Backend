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
        String cnpjSemMascara = fornecedorRequestDto.getCnpj().replaceAll("[^\\d]", "");
        if (cnpjSemMascara.length() != 14) throw new InvalidValueException("Cnpj inválido");
        String cnpjComMascara = formatarCnpjComMascara(cnpjSemMascara);
        if (fornecedorRequestDto.getName().isBlank() && fornecedorRequestDto.getUsername().isBlank()) throw new InvalidValueException("Faça login novamente ");
        String telefoneSemMascara = fornecedorRequestDto.getContactNumber().replaceAll("[^\\d]", "");
        if (telefoneSemMascara.length() != 11) throw new InvalidValueException("Número de telefone inválido");
        String telefoneComMascara = formatarNumeroComMascara(telefoneSemMascara);
        if (!isEmailValido(fornecedorRequestDto.getEletronicAddres())) {
            throw new InvalidValueException("E-mail inválido");
        }
        UsuarioRequestDto usuarioRequestDto = new UsuarioRequestDto();
        usuarioRequestDto.setUsername(fornecedorRequestDto.getUsername());
        usuarioRequestDto.setName(fornecedorRequestDto.getName());
        Fornecedor fornecedor = objectMapper.convertValue(fornecedorRequestDto, Fornecedor.class);
        fornecedor.setCnpj(cnpjComMascara);
        fornecedor.setContactNumber(telefoneComMascara);
        fornecedor.setStatus(Situacao.ATIVA);
        usuarioRequestDto.setFornecedor(fornecedor);
        usuarioService.updateUser(usuarioRequestDto);
    }

    private boolean isEmailValido(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }
    private String formatarNumeroComMascara(String numero) throws InvalidValueException {
        String cleaned = numero.replaceAll("[^\\d]", "");
        if (cleaned.length() != 11) {
            throw new InvalidValueException("Número de telefone inválido");
        }
        return String.format("(%s) %s-%s",
                cleaned.substring(0, 2),  // DDD
                cleaned.substring(2, 7),  // Primeiros 5 dígitos
                cleaned.substring(7));    // Últimos 4 dígitos
    }
    private String formatarCnpjComMascara(String cnpj) {
        return cnpj.substring(0, 2) + "." +
                cnpj.substring(2, 5) + "." +
                cnpj.substring(5, 8) + "/" +
                cnpj.substring(8, 12) + "-" +
                cnpj.substring(12, 14);
    }

    public List<FornecedorDto> findAllFornecedores(String username) throws InvalidValueException, NotFoundException {
       List<Fornecedor> fornecedores = usuarioService.getFornecedores(username);
       List<FornecedorDto> fornecedorDtos = fornecedores.stream().map(a -> FornecedorDto.builder()
               .razaoSocial(a.getRazaoSocial())
               .cnpj(a.getCnpj())
               .status(a.getStatus())
               .contactNumber(a.getContactNumber())
               .eletronicAddres(a.getEletronicAddres())
               .historico(a.getHistorico()).build()).collect(Collectors.toList());
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
        String cnpjSemMascara = fornecedorRequestDto.getCnpj().replaceAll("[^\\d]", "");
        if (fornecedorRequestDto.getCnpj().isBlank() || cnpjSemMascara.length() != 14) throw new InvalidValueException("Cnpj inválido");
        verifyCnpj(fornecedorRequestDto.getUsername(), fornecedorRequestDto.getCnpj());
        List<FornecedorDto> fornecedores = findAllFornecedores(fornecedorRequestDto.getUsername());
        Stream<FornecedorDto> fornecedorDto = fornecedores.stream().filter(fornecedor -> fornecedor.getCnpj().equals(fornecedorRequestDto.getCnpj()));
        FornecedorDto fornecedorReturn = fornecedorDto.findAny().get();

        return fornecedorReturn;
    }

    public void deleteFornecedor(String username, String cnpj) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (username.isBlank()) throw new InvalidValueException("Preencha o campo");
        String cnpjSemMascara = cnpj.replaceAll("[^\\d]", "");
        if (cnpj.isBlank() || cnpjSemMascara.length() != 14) throw new InvalidValueException("Cnpj Inválido");
        usuarioService.deleteFornecedor(username, cnpj);

    }

    public FornecedorDto updateFornecedor(FornecedorRequestDto fornecedorRequestDto, String cnpj) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        String cnpjSemMascara = fornecedorRequestDto.getCnpj().replaceAll("[^\\d]", "");
        if (fornecedorRequestDto.getCnpj().isBlank() || cnpjSemMascara.length() != 14) throw new InvalidValueException("Cnpj inválido");
        if (fornecedorRequestDto.getUsername() == null || fornecedorRequestDto.getUsername().isBlank()) throw new InvalidValueException("Usuario inválido");
        List<Fornecedor> fornecedors = usuarioService.getFornecedores(fornecedorRequestDto.getUsername());
        UsuarioRequestDto user = new UsuarioRequestDto();
        try {
            Fornecedor fornecedorToSave = fornecedors.stream().filter(fornecedor -> fornecedor.getCnpj().equals(cnpj)).findAny().get();
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
            if (fornecedorRequestDto.getEletronicAddres() != null) {
                fornecedorToSave.setEletronicAddres(fornecedorRequestDto.getEletronicAddres());
            }
            if (fornecedorRequestDto.getContactNumber() != null) {
                fornecedorToSave.setContactNumber(fornecedorRequestDto.getContactNumber());
            }
            fornecedorToSave.setCnpj(fornecedorRequestDto.getCnpj());
            user.setUsername(fornecedorRequestDto.getUsername());
            user.setName(fornecedorRequestDto.getName());
            user.setFornecedor(fornecedorToSave);
        } catch (NoSuchElementException e){
            throw new NotFoundException("Fornecedor inexistente");
        }
        return usuarioService.updateFornecedor(user, cnpj);
    }

    public FornecedorDto updateFornecedorByStatus(FornecedorRequestDto fornecedorRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        String cnpjSemMascara = fornecedorRequestDto.getCnpj().replaceAll("[^\\d]", "");
        if (fornecedorRequestDto.getCnpj().isBlank() || cnpjSemMascara.length() != 14) throw new InvalidValueException("Cnpj inválido");
        if (fornecedorRequestDto.getUsername() == null || fornecedorRequestDto.getUsername().isBlank()) throw new InvalidValueException("Usuario inválido");
        verifyCnpj(fornecedorRequestDto.getUsername(), fornecedorRequestDto.getCnpj());
        List<Fornecedor> fornecedors = usuarioService.getFornecedores(fornecedorRequestDto.getUsername());
        Stream<Fornecedor> fornecedorStream = fornecedors.stream().filter(fornecedor -> fornecedor.getCnpj().equals(fornecedorRequestDto.getCnpj()));
        Fornecedor fornecedorToSave = fornecedorStream.findAny().get();
        if (fornecedorRequestDto.getStatus().equals(Situacao.ATIVA)) {
            fornecedorToSave.setStatus(Situacao.INATIVA);
        } else {
            fornecedorToSave.setStatus(Situacao.ATIVA);
        }
        UsuarioRequestDto user = new UsuarioRequestDto();
        user.setUsername(fornecedorRequestDto.getUsername());
        user.setName(fornecedorRequestDto.getName());
        user.setFornecedor(fornecedorToSave);
        return usuarioService.updateFornecedor(user, fornecedorRequestDto.getCnpj());
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
