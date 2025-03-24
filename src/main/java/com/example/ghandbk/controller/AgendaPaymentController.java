package com.example.ghandbk.controller;

import com.example.ghandbk.collection.enums.SituacaoPagamento;
import com.example.ghandbk.collection.enums.SituacaoProduto;
import com.example.ghandbk.dto.schedule.payment.AgendaPaymentDto;
import com.example.ghandbk.dto.schedule.payment.AgendaPaymentRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.example.ghandbk.service.AgendaPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/agendaPagamento")
public class AgendaPaymentController {

    private final AgendaPaymentService agendaPaymentService;

    @PostMapping("/setDateToPay")
    public ResponseEntity setDateToPay(@RequestBody AgendaPaymentRequestDto agendaPaymentRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        return new ResponseEntity(agendaPaymentService.setNewSchedule(agendaPaymentRequestDto), HttpStatus.CREATED);
    }

    @PostMapping("/findPaymentsByMonth")
    public ResponseEntity<List<AgendaPaymentDto>> findPaymentsByMonth(@RequestBody AgendaPaymentRequestDto agendaPaymentRequestDto) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(agendaPaymentService.findAgendaByMonth(agendaPaymentRequestDto), HttpStatus.FOUND);
    }

    @PostMapping("/findPaymentsByStatus")
    public ResponseEntity<List<AgendaPaymentDto>> findPaymentsByStatus(@RequestBody AgendaPaymentRequestDto agendaPaymentRequestDto) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(agendaPaymentService.findAgendaByStatus(agendaPaymentRequestDto), HttpStatus.FOUND);
    }

    @DeleteMapping("/deletePayment")
    public ResponseEntity deletePayment(@RequestParam String username,
                                        @RequestParam String cnpj,
                                        @RequestParam LocalDate dateToPayOrReceive) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        agendaPaymentService.deletePayment(username, cnpj, dateToPayOrReceive);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping("/updateStatus")
    public ResponseEntity<AgendaPaymentDto> updateStatus(@RequestParam String username,
                                                         @RequestParam String name,
                                                         @RequestParam String cnpj,
                                                         @RequestParam LocalDate dateToPayOrReceive,
                                                         @RequestParam SituacaoPagamento status) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        return new ResponseEntity(agendaPaymentService.modifyStatus(username, name, cnpj, dateToPayOrReceive, status), HttpStatus.ACCEPTED);
    }
    @PostMapping("/listAgendas/{username}")
    public ResponseEntity<List<AgendaPaymentDto>> findAgendas(@PathVariable("username") String username) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(agendaPaymentService.findAgendas(username), HttpStatus.OK);
    }
}
