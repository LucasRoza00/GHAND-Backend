package com.example.ghandbk.collection.history;

import com.example.ghandbk.collection.enums.TipoHistorico;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Getter
@Setter
@Data
@Document(collection = "historico")
public class History {

    @Id
    private String username;
    private TipoHistorico tipoHistorico;
    private Object historicoToSave;
}
