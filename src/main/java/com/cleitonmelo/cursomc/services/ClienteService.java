package com.cleitonmelo.cursomc.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cleitonmelo.cursomc.domain.Cidade;
import com.cleitonmelo.cursomc.domain.Cliente;
import com.cleitonmelo.cursomc.domain.Endereco;
import com.cleitonmelo.cursomc.domain.enums.TipoCliente;
import com.cleitonmelo.cursomc.dto.ClienteDTO;
import com.cleitonmelo.cursomc.dto.ClienteNewDTO;
import com.cleitonmelo.cursomc.repositories.ClienteRepository;
import com.cleitonmelo.cursomc.repositories.EnderecoRepository;
import com.cleitonmelo.cursomc.services.exceptions.DataIntegrityException;
import com.cleitonmelo.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepository repo;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private EnderecoRepository enderecoRepository;

	public Cliente find(Integer id) {
		Optional<Cliente> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo " + Cliente.class.getName()));
	}

	/**
	 * Inserir um objeto do tipo categoria
	 * 
	 * @param categoria
	 * @return Salva objeto do tipo categoria
	 */
	@Transactional
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = repo.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());
		return obj;
	}

	/**
	 * Atualizando um objeto do tipo categoria
	 * 
	 * @param obj
	 * @return Salva um objeto do tipo categoria
	 */
	public Cliente update(Cliente obj) {
		Cliente newObj = find(obj.getId());
		updateData(newObj, obj);
		return repo.save(newObj);
	}

	/**
	 * Deletando o objeto com base no ID da Cliente
	 * 
	 * @param id
	 */
	public void delete(Integer id) {
		find(id);
		try {
			repo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir um cliente que possua pedidos relacionados");
		}

	}

	/**
	 * Lista todas as categorias
	 * 
	 * @return
	 */
	public List<Cliente> findAll() {
		return repo.findAll();
	}

	/**
	 * Retorna dados de categoria conforme paginação selecionada
	 * 
	 * @param page
	 * @param linesPerPage
	 * @param direction
	 * @param orderBy
	 * @return
	 */
	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String direction, String orderBy) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}

	/**
	 * 
	 * @param objDto
	 * @return
	 */
	public Cliente fromDto(ClienteDTO objDto) {
		return new Cliente(objDto.getId(), objDto.getNome(), objDto.getEmail(), null, null, null);
	}

	/**
	 * 
	 * @param objDto
	 * @return
	 */
	public Cliente fromDto(ClienteNewDTO objDto) {
		Cliente cli = new Cliente(null, objDto.getNome(), objDto.getEmail(), objDto.getCpfOuCnpj(),
				TipoCliente.toEnum(objDto.getTipo()), passwordEncoder.encode(objDto.getSenha()));
		Cidade cid = new Cidade(objDto.getCidadeId(), null, null);
		Endereco end = new Endereco(null, objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(),
				objDto.getBairro(), objDto.getCep(), cli, cid);

		cli.getEnderecos().add(end);
		cli.getTelefones().add(objDto.getTelefone1());
		if (objDto.getTelefone2() != null) {
			cli.getTelefones().add(objDto.getTelefone2());
		}
		if (objDto.getTelefone3() != null) {
			cli.getTelefones().add(objDto.getTelefone3());
		}

		return cli;
	}

	/**
	 * Atualiza os dados
	 * 
	 * @param newObj
	 * @param obj
	 */
	private void updateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());

	}

}
