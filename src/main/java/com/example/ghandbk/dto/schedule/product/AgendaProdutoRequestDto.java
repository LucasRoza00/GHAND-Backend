package com.example.ghandbk.dto.schedule.product;

import com.example.ghandbk.collection.enums.SituacaoProduto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class AgendaProdutoRequestDto {

    private String username;
    private String name;
    private String cnpj;
    private LocalDate  dateToPayOrReceive;
    private String nameProduct;
    private Integer amount;
    private SituacaoProduto status;

}
