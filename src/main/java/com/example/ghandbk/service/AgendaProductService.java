package com.example.ghandbk.service;

import com.example.ghandbk.collection.enums.SituacaoProduto;
import com.example.ghandbk.collection.enums.TipoHistorico;
import com.example.ghandbk.collection.schedule.AgendaProduto;
import com.example.ghandbk.collection.supplier.HistoricoProduto;
import com.example.ghandbk.dto.schedule.product.AgendaProdDto;
import com.example.ghandbk.dto.schedule.product.AgendaProdutoRequestDto;
import com.example.ghandbk.dto.supllier.FornecedorDto;
import com.example.ghandbk.dto.supllier.FornecedorRequestDto;
import com.example.ghandbk.dto.user.UsuarioRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class AgendaProductService {

    private final FornecedorService fornecedorService;
    private final UsuarioService usuarioService;
    private final ObjectMapper objectMapper;

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

    public void deleteReceive(AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (agendaProdutoRequestDto.getCnpj().isEmpty() || agendaProdutoRequestDto.getCnpj().length() <= 11) throw new InvalidValueException("Cnpj inválido");
        if (agendaProdutoRequestDto.getDateToPayOrReceive() == null) throw new InvalidValueException("Data inválida");
        usuarioService.deleteReceiveInAgenda(agendaProdutoRequestDto);
    }

    public AgendaProdDto modifyStatus(AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (agendaProdutoRequestDto.getCnpj().isEmpty() || agendaProdutoRequestDto.getCnpj().length() <= 11) throw new InvalidValueException("Cnpj inválido");
        if (agendaProdutoRequestDto.getDateToPayOrReceive() == null) throw new InvalidValueException("Data inválida");
        List<AgendaProduto> produtos = usuarioService.getAgendaProdutcs(agendaProdutoRequestDto.getUsername());
        if (!produtos.isEmpty()) {
            try {
                List<AgendaProduto> agendaProdutos = produtos.stream().filter(prod -> prod.getDateToPayOrReceive().equals(agendaProdutoRequestDto.getDateToPayOrReceive())).toList();
                if (agendaProdutos.isEmpty()) {
                    throw new NotFoundException("Não há agenndamentos para este dia");
                }
                agendaProdutos.stream().filter(agendaProduto -> agendaProduto.getFornecedor().getCnpj().equals(agendaProdutoRequestDto.getCnpj())).findAny().get();
            } catch (NoSuchElementException e) {
                throw new NotFoundException("Não há agendamentos nesse dia com este cnpj");
            }
        } else {
            throw new NotFoundException("Não há agendamentos");
        }
        List<AgendaProduto> agendaProdutos = produtos.stream().filter(prod -> prod.getDateToPayOrReceive().equals(agendaProdutoRequestDto.getDateToPayOrReceive())).toList();
        AgendaProduto agendaToModity = agendaProdutos.stream().filter(agendaProduto -> agendaProduto.getFornecedor().getCnpj().equals(agendaProdutoRequestDto.getCnpj())).findAny().get();
        if (!agendaToModity.getStatus().equals(agendaProdutoRequestDto.getStatus())) {
            switch (agendaProdutoRequestDto.getStatus()) {
                case RECEBIDO,NAO_RECEBIDO -> agendaToModity.setStatus(agendaProdutoRequestDto.getStatus());
            }
        }
        UsuarioRequestDto user = new UsuarioRequestDto();
        user.setUsername(agendaProdutoRequestDto.getUsername());
        user.setName(agendaProdutoRequestDto.getName());
        user.setAgendaProduto(agendaToModity);
        AgendaProdDto agendaToReturn = usuarioService.updateAgendaProductsByStatus(user, agendaProdutoRequestDto.getCnpj());
        insertAgendaInHistorico(agendaToReturn, agendaProdutoRequestDto.getCnpj(), agendaProdutoRequestDto.getUsername());
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
        fornecedorService.updateFornecedor(fornecedorRequestDto);
    }

    private UsuarioRequestDto getInstanceForUserRQDto(String username, String name) {
        UsuarioRequestDto userToReturn = new UsuarioRequestDto();
        userToReturn.setUsername(username);
        userToReturn.setName(name);
        return userToReturn;
    }


}
