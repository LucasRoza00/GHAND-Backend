package com.example.ghandbk.service;

import com.example.ghandbk.collection.enums.SituacaoProduto;
import com.example.ghandbk.collection.enums.TipoHistorico;
import com.example.ghandbk.collection.history.History;
import com.example.ghandbk.collection.schedule.AgendaProduto;
import com.example.ghandbk.collection.supplier.Historico;
import com.example.ghandbk.collection.supplier.HistoricoProduto;
import com.example.ghandbk.dto.schedule.product.AgendaProdDto;
import com.example.ghandbk.dto.schedule.product.AgendaProdutoRequestDto;
import com.example.ghandbk.dto.supllier.FornecedorDto;
import com.example.ghandbk.dto.supllier.FornecedorRequestDto;
import com.example.ghandbk.dto.user.UsuarioRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.example.ghandbk.repository.HistoricoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class AgendaProductService {

    private final FornecedorService fornecedorService;
    private final UsuarioService usuarioService;
    private final ObjectMapper objectMapper;
    private final HistoricoRepository historicoRepository;

    public List<AgendaProdDto> findAgendaByStatus(AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (agendaProdutoRequestDto.getUsername().isEmpty()) throw new NotAuthorizedException("Usuario inválido");
        if (agendaProdutoRequestDto.getStatus().toString().isEmpty()) throw new InvalidValueException("Status inválido");
        List<AgendaProduto> agenda = usuarioService.getAgendaProdutcs(agendaProdutoRequestDto.getUsername());
        List<AgendaProdDto> agendaToReturn = agenda.stream().filter(prod -> prod.getStatus().equals(agendaProdutoRequestDto.getStatus()))
                .map(oldAgenda -> AgendaProdDto.builder()
                        .nameProduct(oldAgenda.getNameProduct())
                        .amount(oldAgenda.getAmount())
                        .status(oldAgenda.getStatus())
                        .dateToPayOrReceive(oldAgenda.getDateToPayOrReceive()).build()).toList();
        return  agendaToReturn;
    }
    public void insertNewSchedule(AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (agendaProdutoRequestDto.getCnpj() == null || agendaProdutoRequestDto.getCnpj().isBlank()) throw new InvalidValueException("Cnpj inválido");
        if (agendaProdutoRequestDto.getAmount() <= 0) throw new InvalidValueException("Quantidade inválida");
        if (agendaProdutoRequestDto.getNameProduct() == null || agendaProdutoRequestDto.getNameProduct().isBlank()) throw new InvalidValueException("Preencha o campo");
        if (agendaProdutoRequestDto.getStatus() == SituacaoProduto.RECEBIDO) throw new InvalidValueException("Situação inválida");
        AgendaProduto agendaProduto = objectMapper.convertValue(agendaProdutoRequestDto, AgendaProduto.class);
        FornecedorRequestDto fornecedorRequestDto = FornecedorRequestDto.builder().username(agendaProdutoRequestDto.getUsername()).cnpj(agendaProdutoRequestDto.getCnpj()).build();
        FornecedorDto fornecedorDto = fornecedorService.getFornecedorByCnpj(fornecedorRequestDto);
        agendaProduto.setFornecedor(fornecedorDto);
        UsuarioRequestDto user = getInstanceForUserRQDto(agendaProdutoRequestDto.getUsername(), agendaProdutoRequestDto.getName());
        user.setAgendaProduto(agendaProduto);
        usuarioService.updateUser(user);
    }

    public List<AgendaProdDto> findAgendaByMonth(AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException {
        if (agendaProdutoRequestDto.getDateToPayOrReceive().getMonth() == null) throw new InvalidValueException("Data inválida");
        List<AgendaProduto> agendaProdutos = usuarioService.getAgendaProdutcs(agendaProdutoRequestDto.getUsername());
        List<AgendaProduto> agenda = agendaProdutos.stream().filter(prod -> prod.getDateToPayOrReceive().getMonth().equals(agendaProdutoRequestDto.getDateToPayOrReceive().getMonth())).toList();
        if (agenda.isEmpty()) throw new NotFoundException("Não foram encontrados recebimentos para o mês selecionado");
        List<AgendaProdDto> agendaToReturn = agenda.stream().map(agendaP -> AgendaProdDto.builder().nameProduct(agendaP.getNameProduct()).amount(agendaP.getAmount()).status(agendaP.getStatus()).dateToPayOrReceive(agendaP.getDateToPayOrReceive()).fornecedorDto(agendaP.getFornecedor()).build()).toList();
        return agendaToReturn;
    }

    public void deleteReceive(String username, String name, String cnpj, LocalDate dateToPayOrReceive) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (cnpj.isEmpty()) throw new InvalidValueException("Cnpj inválido");
        if (dateToPayOrReceive == null) throw new InvalidValueException("Data inválida");
        usuarioService.deleteReceiveInAgenda(username, name, cnpj, dateToPayOrReceive);
    }

    public AgendaProdDto modifyStatus(String username, String name, String cnpj, LocalDate dateToPayOrreceive, SituacaoProduto status) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (cnpj.isEmpty()) throw new InvalidValueException("Cnpj inválido");
        if (dateToPayOrreceive == null) throw new InvalidValueException("Data inválida");
        List<AgendaProduto> produtos = usuarioService.getAgendaProdutcs(username);
        if (!produtos.isEmpty()) {
            try {
                List<AgendaProduto> agendaProdutos = produtos.stream().filter(prod -> prod.getDateToPayOrReceive().equals(dateToPayOrreceive)).toList();
                if (agendaProdutos.isEmpty()) {
                    throw new NotFoundException("Não há agenndamentos para este dia");
                }
                agendaProdutos.stream().filter(agendaProduto -> agendaProduto.getFornecedor().getCnpj().equals(cnpj)).findAny().get();
            } catch (NoSuchElementException e) {
                throw new NotFoundException("Não há agendamentos nesse dia com este cnpj");
            }
        } else {
            throw new NotFoundException("Não há agendamentos");
        }
        List<AgendaProduto> agendaProdutos = produtos.stream().filter(prod -> prod.getDateToPayOrReceive().equals(dateToPayOrreceive)).toList();
        AgendaProduto agendaToModity = agendaProdutos.stream().filter(agendaProduto -> agendaProduto.getFornecedor().getCnpj().equals(cnpj)).findAny().get();
        if (!agendaToModity.getStatus().equals(status)) {
            switch (status) {
                case RECEBIDO,NAO_RECEBIDO -> agendaToModity.setStatus(status);
            }
        }
        UsuarioRequestDto user = new UsuarioRequestDto();
        user.setUsername(username);
        user.setName(name);
        user.setAgendaProduto(agendaToModity);
        AgendaProdDto agendaToReturn = usuarioService.updateAgendaProductsByStatus(user, cnpj);
        insertAgendaInHistorico(agendaToReturn, cnpj, username);
        return agendaToReturn;
    }

    private void insertAgendaInHistorico(AgendaProdDto agendaProdDto, String cnpj, String username) throws NotAuthorizedException, InvalidValueException, NotFoundException {
        if (agendaProdDto == null) throw new NotAuthorizedException("Recebimento inválido");
        if (!agendaProdDto.getStatus().equals(SituacaoProduto.RECEBIDO)) throw new NotAuthorizedException("Operação inválida");
        if (cnpj.isEmpty()) throw new NotAuthorizedException("Cnpj inválido");
        if (username.isEmpty()) throw new NotAuthorizedException("Usuario inválido");
        FornecedorRequestDto fornecedorRequestDto = FornecedorRequestDto.builder().username(username).cnpj(cnpj).build();
        HistoricoProduto historico = new HistoricoProduto();
        historico.setHistoricoTipo(TipoHistorico.PRODUTO);
        historico.setAgendaProdDto(agendaProdDto);
        fornecedorRequestDto.setHistoricoProduto(historico);
        fornecedorService.updateFornecedor(fornecedorRequestDto, cnpj);
    }

    public List<AgendaProdDto> findAgendas(String username) throws InvalidValueException, NotFoundException {
        if (username.isEmpty()) throw new InvalidValueException("Usuário inválido");
        List<AgendaProdDto> agendaToReturn = usuarioService.getAgendaProdutcs(username).stream()
                .map(agenda -> AgendaProdDto.builder()
                        .nameProduct(agenda.getNameProduct())
                        .amount(agenda.getAmount())
                        .status(agenda.getStatus())
                        .dateToPayOrReceive(agenda.getDateToPayOrReceive())
                        .fornecedorDto(agenda.getFornecedor()).build()).toList();
        if (agendaToReturn.isEmpty()) throw new NotFoundException("Não há produtos agendados");
        return agendaToReturn;
    }
    private UsuarioRequestDto getInstanceForUserRQDto(String username, String name) {
        UsuarioRequestDto userToReturn = new UsuarioRequestDto();
        userToReturn.setUsername(username);
        userToReturn.setName(name);
        return userToReturn;
    }
    
}
