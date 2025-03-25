package com.example.ghandbk.controller;

import com.example.ghandbk.collection.enums.SituacaoProduto;
import com.example.ghandbk.collection.schedule.AgendaProduto;
import com.example.ghandbk.dto.schedule.product.AgendaProdDto;
import com.example.ghandbk.dto.schedule.product.AgendaProdutoRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.example.ghandbk.service.AgendaProductService;
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
@RequestMapping("/agendaProduto")
@Tag(name = "Controller de Produtos Agendados", description = "Controller para ações relacionadas as entregas agendadas")
public class AgendaProdutoController {

    private final AgendaProductService agendaProductService;

    @PostMapping("/setDateToReceive")
    @Operation(summary = "Cria uma entrega agendada", description = "Cria uma entrega agendada apartir de um fornecedor existente")
    public ResponseEntity setDateToReceive(@RequestBody AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        agendaProductService.insertNewSchedule(agendaProdutoRequestDto);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @PostMapping("/findAgendaByMonth")
    @Operation(summary = "Lista as entregas agendados", description = "Lista as entregas agendadas filtrada pelo mês")
    public ResponseEntity<List<AgendaProdDto>> findAgendaByMonth(@RequestBody AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(agendaProductService.findAgendaByMonth(agendaProdutoRequestDto), HttpStatus.ACCEPTED);
    }

    @PostMapping("/findByStatus")
    @Operation(summary = "Lista as entregas agendadas", description = "Lista todas entregas agendadas filtrada pelo status")
    public ResponseEntity<List<AgendaProdDto>> findAgendaByStatus(@RequestBody AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        return new ResponseEntity(agendaProductService.findAgendaByStatus(agendaProdutoRequestDto), HttpStatus.OK);
    }
    @DeleteMapping("deleteReceive")
    @Operation(summary = "Deleta uma entrega agendada", description = "Delete a entrega agendada apartir da combinação de cnpj e data de entrega")
    public ResponseEntity deleteReceive(@RequestParam String username,
                                        @RequestParam String name,
                                        @RequestParam String cnpj,
                                        @RequestParam LocalDate dateToPayOrReceive) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        agendaProductService.deleteReceive(username, name, cnpj, dateToPayOrReceive);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @PutMapping(value = "/updateStatus", produces = "application/json")
    @Operation(summary = "Atualiza o status de uma entrega agendada", description = "Atualiza o status de uma entrega agendada apartir da combinação de cnpj e data de entrega")
    public ResponseEntity<AgendaProdDto> updateStatus(@RequestParam String username,
                                                      @RequestParam String name,
                                                      @RequestParam String cnpj,
                                                      @RequestParam LocalDate dateToPayOrReceive,
                                                      @RequestParam SituacaoProduto status) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        return new ResponseEntity(agendaProductService.modifyStatus(username, name, cnpj, dateToPayOrReceive, status), HttpStatus.ACCEPTED);
    }


    @PostMapping("findAgenda/{username}")
    @Operation(summary = "Lista todas as entregas agendadas", description = "Lista todas as entregas agendadas pelo username")
    public ResponseEntity<List<AgendaProdDto>> findAgenda(@PathVariable("username") String username) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(agendaProductService.findAgendas(username), HttpStatus.OK);
    }
}
