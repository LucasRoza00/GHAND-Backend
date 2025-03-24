package com.example.ghandbk.service;

import com.example.ghandbk.collection.enums.SituacaoPagamento;
import com.example.ghandbk.collection.enums.TipoHistorico;
import com.example.ghandbk.collection.schedule.AgendaPagamento;
import com.example.ghandbk.collection.schedule.AgendaProduto;
import com.example.ghandbk.collection.supplier.HistoricoPagamento;
import com.example.ghandbk.dto.schedule.payment.AgendaPaymentDto;
import com.example.ghandbk.dto.schedule.payment.AgendaPaymentRequestDto;
import com.example.ghandbk.dto.supllier.FornecedorDto;
import com.example.ghandbk.dto.supllier.FornecedorRequestDto;
import com.example.ghandbk.dto.user.UsuarioRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class AgendaPaymentService {

    private final FornecedorService fornecedorService;
    private final ObjectMapper objectMapper;
    private final UsuarioService usuarioService;

    public AgendaPagamento setNewSchedule(AgendaPaymentRequestDto agendaPaymentRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (agendaPaymentRequestDto.getCnpj().isEmpty()) throw new InvalidValueException("Cnpj inválido");
        if (agendaPaymentRequestDto.getSituacaoPagamento().toString().isEmpty() || agendaPaymentRequestDto.getSituacaoPagamento().equals(SituacaoPagamento.PAGA)) throw new InvalidValueException("Status inválido");
        if (agendaPaymentRequestDto.getValueToPay() <= 0) throw new InvalidValueException("Valor inválido");
        AgendaPagamento agenda = objectMapper.convertValue(agendaPaymentRequestDto, AgendaPagamento.class);
        FornecedorRequestDto fornecedorRequestDto = FornecedorRequestDto.builder().username(agendaPaymentRequestDto.getUsername()).cnpj(agendaPaymentRequestDto.getCnpj()).build();
        FornecedorDto fornecedorToSave = fornecedorService.getFornecedorByCnpj(fornecedorRequestDto);
        agenda.setFornecedorDto(fornecedorToSave);
        UsuarioRequestDto user = getInstanceForUserRQDto(agendaPaymentRequestDto.getUsername(), agendaPaymentRequestDto.getName());
        user.setAgendaPagamento(agenda);
        usuarioService.updateUser(user);
        return agenda;
    }

    public List<AgendaPaymentDto> findAgendas(String username) throws InvalidValueException, NotFoundException {
        if (username.isEmpty()) throw new InvalidValueException("Username inválido");
        List<AgendaPaymentDto> agendaToReturn = usuarioService.getAgendaPayments(username).stream().map(agenda -> AgendaPaymentDto
                .builder()
                .valueToPay(agenda.getValueToPay())
                .situacaoPagamento(agenda.getSituacaoPagamento())
                .dateToPayOrReceive(agenda.getDateToPayOrReceive())
                .fornecedorDto(agenda.getFornecedorDto())
                .build()).toList();
        if (agendaToReturn.isEmpty()) throw new NotFoundException("Não há agendamentos");
        return agendaToReturn;
    }

    public List<AgendaPaymentDto> findAgendaByMonth(AgendaPaymentRequestDto agendaPaymentRequestDto) throws InvalidValueException, NotFoundException {
        if (agendaPaymentRequestDto.getDateToPayOrReceive().getMonth() == null) throw new InvalidValueException("Data inválida");
        List<AgendaPagamento> agenda = usuarioService.getAgendaPayments(agendaPaymentRequestDto.getUsername());
        List<AgendaPaymentDto> agendaToReturn = agenda.stream().filter(pagamento -> pagamento.getDateToPayOrReceive().getMonth().equals(agendaPaymentRequestDto.getDateToPayOrReceive().getMonth()))
                .map(saveNewAgenda -> AgendaPaymentDto.builder().dateToPayOrReceive(saveNewAgenda.getDateToPayOrReceive())
                        .valueToPay(saveNewAgenda.getValueToPay()).situacaoPagamento(saveNewAgenda.getSituacaoPagamento()).fornecedorDto(saveNewAgenda.getFornecedorDto()).build()).toList();
        if (agendaToReturn.isEmpty()) throw new NotFoundException("Não há pagamentos agendados para este mês");
        return agendaToReturn;
    }

    public List<AgendaPaymentDto> findAgendaByStatus(AgendaPaymentRequestDto agendaPaymentRequestDto) throws InvalidValueException, NotFoundException {
        if (agendaPaymentRequestDto.getSituacaoPagamento().toString().isEmpty()) throw new InvalidValueException("Situação inválida");
        List<AgendaPagamento> agenda = usuarioService.getAgendaPayments(agendaPaymentRequestDto.getUsername());
        List<AgendaPaymentDto> agendaToReturn = agenda.stream().filter(payments -> payments.getSituacaoPagamento().equals(agendaPaymentRequestDto.getSituacaoPagamento()))
                .map(newAgenda -> AgendaPaymentDto.builder().valueToPay(newAgenda.getValueToPay()).dateToPayOrReceive(newAgenda.getDateToPayOrReceive()).situacaoPagamento(newAgenda.getSituacaoPagamento()).fornecedorDto(newAgenda.getFornecedorDto()).build()).toList();
        if (agendaToReturn.isEmpty()) throw new NotFoundException("Não há pagamentos agendados com esse status");
        return agendaToReturn;
    }

    public void deletePayment(String username, String cnpj, LocalDate dateToPayOrReceive) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (cnpj.isEmpty()) throw new InvalidValueException("Cnpj inválido");
        if (dateToPayOrReceive == null) throw new InvalidValueException("Data inválida");
        usuarioService.deletePaymentInAgenda(username, cnpj, dateToPayOrReceive);
    }

    public AgendaPaymentDto modifyStatus(String username, String name, String cnpj, LocalDate dateToPayOrReceive, SituacaoPagamento status) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (cnpj.isEmpty()) throw new InvalidValueException("Cnpj inválido");
        if (dateToPayOrReceive == null) throw new InvalidValueException("Data inválida");
        List<AgendaPagamento> pagamentos = usuarioService.getAgendaPayments(username);
        if (!pagamentos.isEmpty()) {
            try {
                List<AgendaPagamento> agendaPagamentos = pagamentos.stream().filter(pay -> pay.getDateToPayOrReceive().equals(dateToPayOrReceive)).toList();
                if (agendaPagamentos.isEmpty()) {
                    throw new NotFoundException("Não há agenndamentos para este dia");
                }
                agendaPagamentos.stream().filter(agendaProduto -> agendaProduto.getFornecedorDto().getCnpj().equals(cnpj)).findAny().get();
            } catch (NoSuchElementException e) {
                throw new NotFoundException("Não há agendamentos nesse dia com este cnpj");
            }
        } else {
            throw new NotFoundException("Não há agendamentos");
        }
        List<AgendaPagamento> payments = pagamentos.stream().filter(newPayment -> newPayment.getDateToPayOrReceive().equals(dateToPayOrReceive)).toList();
        AgendaPagamento payToSave = payments.stream().filter(pay -> pay.getFornecedorDto().getCnpj().equals(cnpj)).findAny().get();
        if (!payToSave.getDateToPayOrReceive().equals(status)) {
            switch (status) {
                case PAGA, A_PAGAR: payToSave.setSituacaoPagamento(status);
            }
        }
        UsuarioRequestDto user = getInstanceForUserRQDto(username, name);
        user.setAgendaPagamento(payToSave);
        AgendaPaymentDto agendaToInsert = usuarioService.updatePaymentByStatus(user, cnpj);
        insertAgendaInHistorico(username, cnpj, agendaToInsert);
        return agendaToInsert;
    }

    private void insertAgendaInHistorico(String username, String cnpj, AgendaPaymentDto agendaPagamento) throws InvalidValueException, NotAuthorizedException, NotFoundException {
        if (agendaPagamento == null) throw new NotAuthorizedException("Agendamento inválido");
        if (!agendaPagamento.getSituacaoPagamento().equals(SituacaoPagamento.PAGA)) throw new InvalidValueException("Status inválido");
        if (cnpj.isEmpty()) throw new NotAuthorizedException("Cnpj inválido");
        if (username.isEmpty()) throw new NotAuthorizedException("Usuario inválido");
        HistoricoPagamento historico = new HistoricoPagamento();
        historico.setHistoricoTipo(TipoHistorico.PAGAMENTO);
        historico.setAgendaPagamento(agendaPagamento);
        FornecedorRequestDto fornecedorToUpdate = FornecedorRequestDto.builder().username(username).cnpj(cnpj).build();
        fornecedorToUpdate.setHistoricoPagamento(historico);
        fornecedorService.updateFornecedor(fornecedorToUpdate, cnpj);
    }
    private UsuarioRequestDto getInstanceForUserRQDto(String username, String name) {
        UsuarioRequestDto userToReturn = new UsuarioRequestDto();
        userToReturn.setUsername(username);
        userToReturn.setName(name);
        return userToReturn;
    }

}
