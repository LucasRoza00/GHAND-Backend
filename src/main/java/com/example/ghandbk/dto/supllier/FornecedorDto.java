package com.example.ghandbk.dto.supllier;

import com.example.ghandbk.collection.enums.Situacao;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FornecedorDto {
    @Builder
    public FornecedorDto(String razaoSocial, String cnpj, String contactNumber, String eletronicAddres, Situacao status, List historico) {
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.contactNumber = contactNumber;
        this.eletronicAddres = eletronicAddres;
        this.status = status;
        this.historico = historico;
    }

    private String razaoSocial;
    private String cnpj;
    private Situacao status;
    private String contactNumber;
    private String eletronicAddres;
    private List historico;

}
