package com.example.ghandbk.controller;

import com.example.ghandbk.collection.enums.SituacaoPagamento;
import com.example.ghandbk.collection.enums.SituacaoProduto;
import com.example.ghandbk.dto.schedule.payment.AgendaPaymentDto;
import com.example.ghandbk.dto.schedule.payment.AgendaPaymentRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.example.ghandbk.service.AgendaPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/agendaPagamento")
@Tag(name = "Controller da Agenda de Pagamentos", description = "Controller para ações relacionadas a agenda de pagamentos")
public class AgendaPaymentController {

    private final AgendaPaymentService agendaPaymentService;

    @PostMapping("/setDateToPay")
    @Operation(summary = "Cria um agendamento de pagamento", description = "Cria um agendamento apartir de um fornecedor existente")
    public ResponseEntity setDateToPay(@RequestBody AgendaPaymentRequestDto agendaPaymentRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        return new ResponseEntity(agendaPaymentService.setNewSchedule(agendaPaymentRequestDto), HttpStatus.CREATED);
    }

    @PostMapping("/findPaymentsByMonth")
    @Operation(summary = "Filtra os pagamentos agendados", description = "Lista os pagamentos agendados filtrados pelo mês")
    public ResponseEntity<List<AgendaPaymentDto>> findPaymentsByMonth(@RequestBody AgendaPaymentRequestDto agendaPaymentRequestDto) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(agendaPaymentService.findAgendaByMonth(agendaPaymentRequestDto), HttpStatus.FOUND);
    }

    @PostMapping("/findPaymentsByStatus")
    @Operation(summary = "Filtra os pagamentos agendados", description = "Lista os pagamentos agendados filtrados pelo status")
    public ResponseEntity<List<AgendaPaymentDto>> findPaymentsByStatus(@RequestBody AgendaPaymentRequestDto agendaPaymentRequestDto) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(agendaPaymentService.findAgendaByStatus(agendaPaymentRequestDto), HttpStatus.FOUND);
    }

    @DeleteMapping("/deletePayment")
    @Operation(summary = "Deleta um pagamento", description = "Deleta um pagamento pela combinação do cnpj e da data de pagamento")
    public ResponseEntity deletePayment(@RequestParam String username,
                                        @RequestParam String cnpj,
                                        @RequestParam LocalDate dateToPayOrReceive) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        agendaPaymentService.deletePayment(username, cnpj, dateToPayOrReceive);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping("/updateStatus")
    @Operation(summary = "Atualiza o status do pagamento agendado", description = "Atualiza o status do pagamento agendado pelo cnpj e data de pagamento")
    public ResponseEntity<AgendaPaymentDto> updateStatus(@RequestParam String username,
                                                         @RequestParam String name,
                                                         @RequestParam String cnpj,
                                                         @RequestParam LocalDate dateToPayOrReceive,
                                                         @RequestParam SituacaoPagamento status) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        return new ResponseEntity(agendaPaymentService.modifyStatus(username, name, cnpj, dateToPayOrReceive, status), HttpStatus.ACCEPTED);
    }
    @PostMapping("/listAgendas/{username}")
    @Operation(summary = "Lista os pagamentos agendados", description = "Lista todos os pagamentos agendados pelo uersename")
    public ResponseEntity<List<AgendaPaymentDto>> findAgendas(@PathVariable("username") String username) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(agendaPaymentService.findAgendas(username), HttpStatus.OK);
    }
}
