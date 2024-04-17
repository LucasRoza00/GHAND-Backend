package com.example.ghandbk.service;



import com.example.ghandbk.collection.schedule.AgendaPagamento;
import com.example.ghandbk.collection.schedule.AgendaProduto;
import com.example.ghandbk.collection.supplier.Fornecedor;
import com.example.ghandbk.collection.user.Usuario;
import com.example.ghandbk.dto.schedule.payment.AgendaPaymentDto;
import com.example.ghandbk.dto.schedule.payment.AgendaPaymentRequestDto;
import com.example.ghandbk.dto.schedule.product.AgendaProdDto;
import com.example.ghandbk.dto.schedule.product.AgendaProdutoRequestDto;
import com.example.ghandbk.dto.supllier.FornecedorDto;
import com.example.ghandbk.dto.user.UsuarioDto;
import com.example.ghandbk.dto.user.UsuarioRequestDto;
import com.example.ghandbk.exceptions.InvalidValueException;
import com.example.ghandbk.exceptions.NotAuthorizedException;
import com.example.ghandbk.exceptions.NotFoundException;
import com.example.ghandbk.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final ObjectMapper objectMapper;
    private final UsuarioRepository usuarioRepo;

    public UsuarioDto insertUser(UsuarioRequestDto usuarioRequestDto) throws InvalidValueException, NotAuthorizedException {
        if(usuarioRepo.existsById(usuarioRequestDto.getUsername())) throw new NotAuthorizedException("Usuário já existente");
        if (usuarioRequestDto.getUsername().isBlank()) throw new InvalidValueException("Preencha o campo!!");
        if (usuarioRequestDto.getName().isBlank()) throw new InvalidValueException("Preencha o campo!!");
        if (usuarioRequestDto.getPassword().isBlank() || usuarioRequestDto.getPassword().length() <= 4) throw new InvalidValueException("Senha inválida");
        Usuario userToSave = objectMapper.convertValue(usuarioRequestDto, Usuario.class);
        return objectMapper.convertValue(usuarioRepo.save(userToSave), UsuarioDto.class);
    }

    public void deleteUser(UsuarioRequestDto usuarioRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (usuarioRequestDto.getUsername().isBlank() && usuarioRequestDto.getPassword().isBlank()) throw new InvalidValueException("Preencha os campos");
        if (usuarioRepo.existsById(usuarioRequestDto.getUsername())) throw new NotFoundException("Usuário não encontrado");
        if (usuarioRepo.findUser(usuarioRequestDto.getUsername(), usuarioRequestDto.getPassword()) == null) throw new NotAuthorizedException("Usuário inválido para deletar");
        usuarioRepo.delete(usuarioRepo.findUser(usuarioRequestDto.getUsername(), usuarioRequestDto.getPassword()));
    }

    public Usuario updateUser(UsuarioRequestDto usuarioRequestDto) throws InvalidValueException, NotAuthorizedException, NotFoundException {
        if (!usuarioRepo.existsById(usuarioRequestDto.getUsername())) throw new NotAuthorizedException("Usuário inválido");
        Usuario user = usuarioRepo.findById(usuarioRequestDto.getUsername()).get();
        if (usuarioRequestDto.getUsername() == null || usuarioRequestDto.getUsername().isBlank()) throw new InvalidValueException("Preencha o campo");
        if (usuarioRequestDto.getName().isEmpty()) throw new InvalidValueException("Preencha o campo");
        if (usuarioRequestDto.getFornecedor() != null) {
            insertFornecedor(user, usuarioRequestDto.getFornecedor());
        }
        if (usuarioRequestDto.getAgendaProduto() != null) {
            insertAgendaProduto(user, usuarioRequestDto.getAgendaProduto());
        }
        if (usuarioRequestDto.getAgendaPagamento() != null) {
            insertAgendaPagamento(user, usuarioRequestDto.getAgendaPagamento());
        }
        user.setName(usuarioRequestDto.getName());
        user.setUsername(usuarioRequestDto.getUsername());
        usuarioRepo.save(user);
        return usuarioRepo.findById(user.getUsername()).get();
    }

//    public UsuarioDto findUser(String username) throws  InvalidValueException, NotFoundException {
//        if (username.isBlank()) throw new InvalidValueException("Username inválido");
//        if (!usuarioRepo.existsById(username)) throw new NotFoundException("Usuário não encontrado");
//        Usuario usuario = usuarioRepo.findById(username).get();
//        UsuarioDto userReturn = objectMapper.convertValue(usuario, UsuarioDto.class);
//        try {
//            List<FornecedorDto> fornecedoresToReturn = usuario.getFornecedores().stream()
//                    .map(a -> new FornecedorDto
//                            (a.getRazaoSocial(), a.getCnpj(), a.getStatus(), a.getHistorico())).collect(Collectors.toList());
//            userReturn.setFornecedoresDto(fornecedoresToReturn);
//        } catch (NullPointerException e) {
//            return userReturn;
//        }
//        return userReturn;
//    }
    public Usuario findUserByid(String username) throws NotFoundException, InvalidValueException {
        if (username.isBlank()) throw new InvalidValueException("Username inválido");
        if (!usuarioRepo.existsById(username)) throw new NotFoundException("Usuário não encontrado");
        return usuarioRepo.findById(username).get();
    }

    public List<Fornecedor> getFornecedores(String username) throws InvalidValueException, NotFoundException {
        if (username.isBlank()) throw new InvalidValueException("Username inválido");
        if (!usuarioRepo.existsById(username)) throw new NotFoundException("Usuário não encontrado");
        Usuario usuario = usuarioRepo.findById(username).get();
        if (usuario.getFornecedores().isEmpty()) throw new NotFoundException("Fornecedores não encontrados");
        return usuario.getFornecedores();
    }

    public List<AgendaProduto> getAgendaProdutcs(String username) throws InvalidValueException, NotFoundException {
        if (username.isBlank()) throw new InvalidValueException("Username inválido");
        if (!usuarioRepo.existsById(username)) throw new NotFoundException("Usuário não encontrado");
        Usuario user = usuarioRepo.findById(username).get();
        if (user.getProdutos().isEmpty()) throw new NotFoundException("Não há produtos agendados");
        return user.getProdutos();
    }

    public List<AgendaPagamento> getAgendaPayments(String username) throws InvalidValueException, NotFoundException {
        if (username.isBlank()) throw new InvalidValueException("Username inválido");
        if (!usuarioRepo.existsById(username)) throw new NotFoundException("Usuário não encontrado");
        Usuario user = usuarioRepo.findById(username).get();
        if (user.getPagamentos().isEmpty()) throw new NotFoundException("Não há pagamentos agendados");
        return user.getPagamentos();
    }

    public FornecedorDto updateFornecedor(UsuarioRequestDto usuarioRequestDto) throws NotAuthorizedException, InvalidValueException, NotFoundException {
        Usuario user = usuarioRepo.findById(usuarioRequestDto.getUsername()).get();
        if (usuarioRequestDto.getFornecedor() == null) throw new NotAuthorizedException("Fornecedor não autorizado");
        try {
            Fornecedor fornecedorToRemove = user.getFornecedores().stream().filter(fornecedor -> fornecedor.getCnpj().equals(usuarioRequestDto.getFornecedor().getCnpj())).findAny().get();
            if (fornecedorToRemove != null) {
                user.getFornecedores().remove(fornecedorToRemove);
            }
            user.getFornecedores().add(usuarioRequestDto.getFornecedor());
        } catch (NullPointerException exception) {
            ArrayList<Fornecedor> fornecedors = new ArrayList<>();
            fornecedors.add(usuarioRequestDto.getFornecedor());
            user.setFornecedores(fornecedors);
        }
        user.setUsername(usuarioRequestDto.getUsername());
        user.setName(usuarioRequestDto.getName());
        usuarioRepo.save(user);
        List<Fornecedor> fornecedores = getFornecedores(usuarioRequestDto.getUsername());
        List<FornecedorDto> fornecedorDtos = fornecedores.stream().map(a -> new FornecedorDto(a.getRazaoSocial(), a.getCnpj(), a.getStatus(), a.getHistorico())).collect(Collectors.toList());
        Stream<FornecedorDto> fornecedorToReturn = fornecedorDtos.stream().filter(fornecedo -> fornecedo.getCnpj().equals(usuarioRequestDto.getFornecedor().getCnpj()));
        FornecedorDto fornecedorDto = fornecedorToReturn.findAny().get();
        return fornecedorDto;
    }

    public AgendaProdDto updateAgendaProductsByStatus(UsuarioRequestDto usuarioRequestDto, String cnpj) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (usuarioRequestDto.getUsername().isEmpty()) throw new InvalidValueException("Usuario inválido");
        if (usuarioRequestDto.getName().isEmpty()) throw new InvalidValueException("Usuario inválido");
        if (!usuarioRepo.existsById(usuarioRequestDto.getUsername())) throw new NotFoundException("Usuário não encontrado");
        Usuario user = usuarioRepo.findById(usuarioRequestDto.getUsername()).get();
        if (usuarioRequestDto.getAgendaProduto() == null) throw new NotAuthorizedException("Agendamento inválido");
        if (!user.getProdutos().isEmpty()) {
            try {
                List<AgendaProduto> agendaProdutos = user.getProdutos().stream().filter(prod -> prod.getDateToPayOrReceive().equals(usuarioRequestDto.getAgendaProduto().getDateToPayOrReceive())).toList();
                if (agendaProdutos.isEmpty()) {
                    throw new NotFoundException("Não há produtos agendados para este dia");
                }
                AgendaProduto agendaToRemove = agendaProdutos.stream().filter(produto -> produto.getFornecedor().getCnpj().equals(cnpj)).findAny().get();
                user.getProdutos().remove(agendaToRemove);
                user.getProdutos().add(usuarioRequestDto.getAgendaProduto());
            } catch (NoSuchElementException e) {
                throw new NotFoundException("Não há agendamentos neste dia para este cnpj");
            }
        }
        user.setUsername(usuarioRequestDto.getUsername());
        user.setName(usuarioRequestDto.getName());
        usuarioRepo.save(user);
        List<AgendaProdDto> agendaProdutosToFilter = user.getProdutos().stream()
                .filter(prod -> prod.getDateToPayOrReceive()
                        .equals(usuarioRequestDto.getAgendaProduto().getDateToPayOrReceive())).map(agenda -> AgendaProdDto.builder().nameProduct(agenda.getNameProduct()).amount(agenda.getAmount()).status(agenda.getStatus()).dateToPayOrReceive(agenda.getDateToPayOrReceive())
                        .fornecedorDto(FornecedorDto.builder().status(agenda.getFornecedor().getStatus()).razaoSocial(agenda.getFornecedor().getRazaoSocial()).cnpj(agenda.getFornecedor().getCnpj()).build()).build()).toList();
        AgendaProdDto agendaToReturn = agendaProdutosToFilter.stream().filter(filtro -> filtro.getFornecedorDto().getCnpj().equals(cnpj)).findAny().get();
        return agendaToReturn;
    }

    public AgendaPaymentDto updatePaymentByStatus(UsuarioRequestDto usuarioRequestDto, String cnpj) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (usuarioRequestDto.getUsername().isEmpty()) throw new InvalidValueException("Usuario inválido");
        if (usuarioRequestDto.getName().isEmpty()) throw new InvalidValueException("Usuario inválido");
        if (!usuarioRepo.existsById(usuarioRequestDto.getUsername())) throw new NotFoundException("Usuário não encontrado");
        Usuario user = usuarioRepo.findById(usuarioRequestDto.getUsername()).get();
        if (usuarioRequestDto.getAgendaPagamento() == null) throw new NotAuthorizedException("Agendamento inválido");
        if (!user.getPagamentos().isEmpty()) {
            try {
                List<AgendaPagamento> pagamentos = user.getPagamentos().stream().filter(pay -> pay.getDateToPayOrReceive().equals(usuarioRequestDto.getAgendaPagamento().getDateToPayOrReceive())).toList();
                if (pagamentos.isEmpty()) {
                    throw new NotFoundException("Não há pagamentos agendados para este dia");
                }
                AgendaPagamento agendaToRemove = pagamentos.stream().filter(payRemove -> payRemove.getFornecedorDto().getCnpj().equals(cnpj)).findAny().get();
                user.getPagamentos().remove(agendaToRemove);
                user.getPagamentos().add(usuarioRequestDto.getAgendaPagamento());
            } catch (NoSuchElementException e) {
                throw new NotFoundException("Não há agendamentos neste dia para este cnpj");
            }
        }
        user.setUsername(usuarioRequestDto.getUsername());
        user.setName(usuarioRequestDto.getName());
        usuarioRepo.save(user);
        List<AgendaPaymentDto> agendaToReturn = user.getPagamentos().stream().filter(payment -> payment.getDateToPayOrReceive().equals(usuarioRequestDto.getAgendaPagamento().getDateToPayOrReceive())).map(newAgenda -> AgendaPaymentDto.builder().valueToPay(newAgenda.getValueToPay()).dateToPayOrReceive(newAgenda.getDateToPayOrReceive()).situacaoPagamento(newAgenda.getSituacaoPagamento()).build()).toList();
        AgendaPaymentDto paymentToReturn = agendaToReturn.stream().filter(pay -> pay.getFornecedorDto().getCnpj().equals(cnpj)).findAny().get();
        return paymentToReturn;
    }
    public void deleteFornecedor(UsuarioRequestDto usuarioRequestDto) throws NotFoundException, InvalidValueException, NotAuthorizedException {
        if (usuarioRequestDto.getUsername() == null || usuarioRequestDto.getUsername().isEmpty()) throw new InvalidValueException("Usuario inválido");
        if (!usuarioRepo.existsById(usuarioRequestDto.getUsername())) throw new NotAuthorizedException("Usuário não existe");
        Usuario usuario = usuarioRepo.findById(usuarioRequestDto.getUsername()).get();
        try {
            usuario.getFornecedores().stream().filter(fornecedor -> fornecedor.getCnpj().equals(usuarioRequestDto.getFornecedor().getCnpj())).findAny().get();
        } catch (NoSuchElementException e) {
            throw new NotFoundException("Fornecedor não encontrado");
        }
        Stream<Fornecedor> fornecedorStream = usuario.getFornecedores().stream().filter(fornecedor -> fornecedor.getCnpj().equals(usuarioRequestDto.getFornecedor().getCnpj()));
        Fornecedor fornecedor = fornecedorStream.findFirst().get();
        usuario.getFornecedores().remove(fornecedor);
        usuario.setFornecedores(usuario.getFornecedores());
        usuarioRepo.save(usuario);
    }

    public void deleteReceiveInAgenda(AgendaProdutoRequestDto agendaProdutoRequestDto) throws InvalidValueException, NotFoundException, NotAuthorizedException {
        if (agendaProdutoRequestDto.getUsername().isEmpty()) throw new NotAuthorizedException("Usuario inválido");
        if (!usuarioRepo.existsById(agendaProdutoRequestDto.getUsername())) throw new NotFoundException("Usuário não encontrado");
        Usuario user = usuarioRepo.findById(agendaProdutoRequestDto.getUsername()).get();
        if (!user.getProdutos().isEmpty()) {
            try {
                List<AgendaProduto> agendaProdutos = user.getProdutos().stream().filter(produtos -> produtos.getDateToPayOrReceive().equals(agendaProdutoRequestDto.getDateToPayOrReceive())).toList();
                if (agendaProdutos.isEmpty()) {
                    throw new NotFoundException("Não há agenndamentos para este dia");
                }
                AgendaProduto agenda = agendaProdutos.stream().filter(agendaProduto -> agendaProduto.getFornecedor().getCnpj().equals(agendaProdutoRequestDto.getCnpj())).findAny().get();
                user.getProdutos().remove(agenda);
            } catch (NoSuchElementException e) {
                throw new NotAuthorizedException("Não há produtos agendados");
            }
        }
        user.setProdutos(user.getProdutos());
        usuarioRepo.save(user);
    }

    public void deletePaymentInAgenda(AgendaPaymentRequestDto agendaPaymentRequestDto) throws NotAuthorizedException, NotFoundException {
        if (agendaPaymentRequestDto.getUsername().isEmpty()) throw new NotAuthorizedException("Usuario inválido");
        if (!usuarioRepo.existsById(agendaPaymentRequestDto.getUsername())) throw new NotFoundException("Usuario não encontrado");
        Usuario user = usuarioRepo.findById(agendaPaymentRequestDto.getUsername()).get();
        if (!user.getPagamentos().isEmpty()) {
            try {
                List<AgendaPagamento> agendaPagamentos = user.getPagamentos().stream().filter(
                        agendaPagamento -> agendaPagamento.getDateToPayOrReceive().equals(agendaPaymentRequestDto.getDateToPayOrReceive())).toList();
                if (agendaPagamentos.isEmpty()) {
                    throw new NotFoundException("Não há agendamentos para este dia");
                }
                AgendaPagamento agenda = agendaPagamentos.stream().filter(pagamento -> pagamento.getFornecedorDto().getCnpj().equals(agendaPaymentRequestDto.getCnpj())).findAny().get();
                user.getPagamentos().remove(agenda);
            } catch (NoSuchElementException e) {
                throw new NotAuthorizedException("Não há pagamentos agendados");
            }
        }
        user.setPagamentos(user.getPagamentos());
        usuarioRepo.save(user);
    }

    private Usuario insertFornecedor(Usuario user, Fornecedor fornecedor) throws NotAuthorizedException {
        try {
            if (!user.getFornecedores().isEmpty()) {
                try {
                    Fornecedor fornecedorToRemove = user.getFornecedores().stream().filter(fornecedors -> fornecedors.getCnpj().equals(fornecedor.getCnpj())).findAny().get();
                    if (fornecedorToRemove != null) {
                        throw new NotAuthorizedException("Cnpj já existente");
                    }
                } catch (NoSuchElementException e) {
                    user.getFornecedores().add(fornecedor);
                }
            }
        } catch (NullPointerException e) {
            ArrayList<Fornecedor> fornecedors = new ArrayList<>();
            fornecedors.add(fornecedor);
            user.setFornecedores(fornecedors);
        }
        return user;
    }

    private Usuario insertAgendaProduto(Usuario user, AgendaProduto agendaProduto) throws NotAuthorizedException {
        try {
            if (!user.getProdutos().isEmpty()) {
                if (!agendaProduto.getDateToPayOrReceive().isAfter(LocalDate.now()))
                    throw new NotAuthorizedException("Datas passadas não são aceitas");
                List<AgendaProduto> agenda = user.getProdutos().stream().filter(prod -> prod.getDateToPayOrReceive().equals(agendaProduto.getDateToPayOrReceive())).toList();
                if (!agenda.isEmpty()) {
                    AgendaProduto forne = agenda.stream().filter(prod -> prod.getFornecedor().getCnpj().equals(agendaProduto.getFornecedor().getCnpj())).findAny().get();
                    if (forne != null)
                        throw new NotAuthorizedException("Já possui um produto agendado para este dia com este fornecedor");
                }
                user.getProdutos().add(agendaProduto);
            }
        } catch (NullPointerException e) {
            List<AgendaProduto> agenda = new ArrayList<>();
            agenda.add(agendaProduto);
            user.setProdutos(agenda);
        }
        return user;
    }

    private Usuario insertAgendaPagamento(Usuario user, AgendaPagamento agendaPagamento) throws NotAuthorizedException {
        try {
            if (!user.getPagamentos().isEmpty()) {
                if (!agendaPagamento.getDateToPayOrReceive().isAfter(LocalDate.now()))
                    throw new NotAuthorizedException("Datas passadas não são aceitas");
                List<AgendaPagamento> agenda = user.getPagamentos().stream().filter(prod -> prod.getDateToPayOrReceive().equals(agendaPagamento.getDateToPayOrReceive())).toList();
                if (!agenda.isEmpty()) {
                    AgendaPagamento forne = agenda.stream().filter(payment -> payment.getFornecedorDto().getCnpj().equals(agendaPagamento.getFornecedorDto().getCnpj())).findAny().get();
                    if (forne != null)
                        throw new NotAuthorizedException("Já possui um pagamento agendado para este dia com este fornecedor");
                }
                user.getPagamentos().add(agendaPagamento);
            }
        } catch (NullPointerException e) {
            List<AgendaPagamento> agenda = new ArrayList<>();
            agenda.add(agendaPagamento);
            user.setPagamentos(agenda);
        }
        return user;
    }


    public List<Usuario> getUsers() {
        List<Usuario> user = usuarioRepo.findAll();
        return user;
    }
    public UsuarioDto loginUser(UsuarioRequestDto usuarioRequestDto) throws NotAuthorizedException, NotFoundException {
        if (usuarioRequestDto.getUsername().isEmpty() && usuarioRequestDto.getPassword().isEmpty()) throw new NotAuthorizedException("Dados inválidos");
        if (!usuarioRepo.existsById(usuarioRequestDto.getUsername())) throw new NotFoundException("Usuário não encontrado");
        if (usuarioRepo.findUser(usuarioRequestDto.getUsername(), usuarioRequestDto.getPassword()) == null) throw new NotAuthorizedException("Usuário inválido");
        Usuario userToLog = usuarioRepo.findUser(usuarioRequestDto.getUsername(), usuarioRequestDto.getPassword());
        return objectMapper.convertValue(userToLog, UsuarioDto.class);

    }
}
