package com.agrupa.tat_3ds.controller;

import com.agrupa.tat_3ds.dto.GrupoUsuarioResponseDTO;
import com.agrupa.tat_3ds.dto.MoverUsuarioDTO;
import com.agrupa.tat_3ds.models.GrupoUsuario;
import com.agrupa.tat_3ds.models.Usuario;
import com.agrupa.tat_3ds.repository.GrupoRepository;
import com.agrupa.tat_3ds.repository.UsuarioRepository;
import com.agrupa.tat_3ds.service.GrupoUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grupos-usuarios")
@CrossOrigin(origins = "http://172.100.125.25:4200")
public class GrupoUsuarioController {

    @Autowired
    private GrupoUsuarioService grupoUsuarioService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private GrupoRepository grupoRepository;

    @PostMapping("/mover")
    public ResponseEntity<?> mover(@RequestBody MoverUsuarioDTO dto) {

        if (dto.idUsuario() == null || dto.idGrupo() == null) {
            return ResponseEntity.badRequest().body("idUsuario e idGrupo são obrigatórios");
        }

        if (dto.posicao() != null && dto.posicao() < 0) {
            return ResponseEntity.badRequest().body("posicao deve ser um número inteiro não negativo");
        }

        var optUsuario = usuarioRepository.findById(dto.idUsuario());
        if (optUsuario.isEmpty()) {
            return ResponseEntity.status(404).body("Usuário não encontrado");
        }
        var optGrupo = grupoRepository.findById(dto.idGrupo());
        if (optGrupo.isEmpty()) {
            return ResponseEntity.status(404).body("Grupo não encontrado");
        }
        Usuario usuario = optUsuario.get();
        var grupo = optGrupo.get();

        if (usuario.getTrabalho() == null || grupo.getTrabalhoEmGrupo() == null
                || !usuario.getTrabalho().getIdTrabalho().equals(grupo.getTrabalhoEmGrupo().getIdTrabalho())) {
            return ResponseEntity.badRequest().body("O usuário e o grupo devem pertencer ao mesmo trabalho");
        }

        GrupoUsuario resultado = grupoUsuarioService.moverUsuario(dto.idUsuario(), dto.idGrupo(), dto.posicao());

        if (resultado == null) {
            return ResponseEntity.badRequest().body("Não foi possível mover/registrar o usuário (possivelmente o grupo está cheio ou dados inválidos)");
        }

        GrupoUsuarioResponseDTO resp = new GrupoUsuarioResponseDTO(
                resultado.getIdSequencial(),
                resultado.getGrupo().getIdGrupo(),
                resultado.getUsuario().getIdUsuario(),
                resultado.getPosicao()
        );
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/reset-posicoes")
    public ResponseEntity<?> resetPosicoesAll() {
        int count = grupoUsuarioService.resetPosicoesAll();
        return ResponseEntity.ok("Vagas limpas (usuários removidos) em " + count + " registros.");
    }

    @PostMapping("/{idGrupo}/reset-posicoes")
    public ResponseEntity<?> resetPosicoesDoGrupo(@PathVariable Integer idGrupo) {
        if (idGrupo == null) {
            return ResponseEntity.badRequest().body("idGrupo é obrigatório");
        }
        int count = grupoUsuarioService.resetPosicoesGrupo(idGrupo);
        if (count == 0) {
            return ResponseEntity.ok("Nenhuma vaga foi encontrada no grupo " + idGrupo + ".");
        }
        return ResponseEntity.ok("Vagas limpas (usuários removidos) em " + count + " registros no grupo " + idGrupo + ".");
    }

}
