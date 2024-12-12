package com.example.ghandbk.collection.supplier;

import com.example.ghandbk.collection.enums.Situacao;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Fornecedor {

    private String razaoSocial;
    private String cnpj;
    private Situacao status;
    private List historico;

}
