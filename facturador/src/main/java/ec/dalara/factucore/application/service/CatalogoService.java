package ec.dalara.factucore.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.dalara.factucore.application.ApplicationException;
import ec.dalara.factucore.domain.shared.EstadoRegistro;
import ec.dalara.factucore.domain.shared.MessageCodes;
import ec.dalara.factucore.infrastructure.persistence.entity.Catalogo;
import ec.dalara.factucore.infrastructure.persistence.repository.BaseRepository;
import ec.dalara.factucore.infrastructure.persistence.repository.CatalogoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogoService extends BaseService<Catalogo> {
    private final CatalogoRepository catalogoRepository;
    @Override protected BaseRepository<Catalogo, Long> getRepository() { return catalogoRepository; }

    @Override
    @Transactional
    public Catalogo guardar(Catalogo catalogo) {
        if (catalogo == null) throw new ApplicationException(MessageCodes.CATALOGO_REQUERIDO);
        if (catalogo.getCodigo() == null || catalogo.getCodigo().isBlank())
            throw new ApplicationException(MessageCodes.CATALOGO_CODIGO_REQUERIDO);
        if (catalogo.getNombre() == null || catalogo.getNombre().isBlank())
            throw new ApplicationException(MessageCodes.CATALOGO_NOMBRE_REQUERIDO);
        boolean duplicado = catalogo.getId() == null
                ? catalogoRepository.existsByCodigo(catalogo.getCodigo())
                : catalogoRepository.existsByCodigoAndIdNot(catalogo.getCodigo(), catalogo.getId());
        if (duplicado) throw new ApplicationException(MessageCodes.CATALOGO_CODIGO_DUPLICADO, catalogo.getCodigo());
        return super.guardar(catalogo);
    }

    public Optional<Catalogo> obtenerPorCodigo(String codigo) {
        return catalogoRepository.findByCodigo(codigo).filter(c -> EstadoRegistro.ACTIVO.equals(c.getEstadoRegistro()));
    }
    public boolean existePorCodigo(String codigo) {
        return catalogoRepository.findByCodigo(codigo).filter(c -> !EstadoRegistro.ELIMINADO.equals(c.getEstadoRegistro())).isPresent();
    }
}