package com.example.ghandbk.controller;

import com.example.ghandbk.collection.schedule.AgendaProduto;
import com.example.ghandbk.dto.schedule.product.AgendaProdDto;
import com.example.ghandbk.dto.schedule.product.AgendaProdutoRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.example.ghandbk.service.AgendaProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/agendaProduto")
public class AgendaProdutoController {

    private final AgendaProductService agendaProductService;

    @PostMapping("/setDateToReceive")
    public ResponseEntity setDateToReceive(@RequestBody AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        agendaProductService.insertNewSchedule(agendaProdutoRequestDto);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @GetMapping("/findAgendaByMonth")
    public ResponseEntity<List<AgendaProdDto>> findAgendaByMonth(@RequestBody AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(agendaProductService.findAgendaByMonth(agendaProdutoRequestDto), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("deleteReceive")
    public ResponseEntity deleteReceive(@RequestBody AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        agendaProductService.deleteReceive(agendaProdutoRequestDto);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @PutMapping("updateStatus")
    public ResponseEntity<AgendaProdDto> updateStatus(@RequestBody AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        return new ResponseEntity(agendaProductService.modifyStatus(agendaProdutoRequestDto), HttpStatus.ACCEPTED);
    }

    @PostMapping("findAgenda/{username}")
    public ResponseEntity<List<AgendaProdDto>> findAgenda(@PathVariable("username") String username) throws InvalidValueException, NotFoundException {
        return new ResponseEntity(agendaProductService.findAgendas(username), HttpStatus.OK);
    }
}
