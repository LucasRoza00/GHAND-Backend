package com.example.ghandbk.repository;

import com.example.ghandbk.collection.supplier.Historico;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HistoricoRepository extends MongoRepository<Historico, String> {


}
