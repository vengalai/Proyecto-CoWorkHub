package com.coworkhub.negocio.implementacion;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;
import com.coworkhub.negocio.model.CuentaUsuario;
import com.coworkhub.negocio.model.EstadoMiembro;
import com.coworkhub.negocio.model.Miembro;
import com.coworkhub.negocio.model.PlanMembresia;
import com.coworkhub.negocio.model.Rol;
import com.coworkhub.negocio.servicio.ServicioMiembros;
import com.coworkhub.persistencia.repositorio.RepositorioCuentasUsuario;
import com.coworkhub.persistencia.repositorio.RepositorioMiembros;
import com.coworkhub.persistencia.repositorio.RepositorioPlanes;
import com.coworkhub.persistencia.repositorio.RepositorioReservas;
import com.coworkhub.presentacion.dto.Pagina;
import com.coworkhub.presentacion.dto.RespuestaMiembro;
import com.coworkhub.presentacion.dto.ResumenConsumo;
import com.coworkhub.presentacion.dto.SolicitudActualizarMiembro;
import com.coworkhub.presentacion.dto.SolicitudMiembro;
import com.coworkhub.presentacion.mapper.Mapeador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.coworkhub.negocio.servicio.Validaciones.texto;

@Service
@Transactional(readOnly = true)
public class ServicioMiembrosImpl implements ServicioMiembros {

    private static final Map<String, String> CAMPOS_ORDEN = Map.of(
            "nombre", "nombreCompleto",
            "documento", "numeroDocumento",
            "email", "email",
            "estado", "estado");

    private final RepositorioMiembros repositorioMiembros;
    private final RepositorioPlanes repositorioPlanes;
    private final RepositorioCuentasUsuario repositorioCuentas;
    private final RepositorioReservas repositorioReservas;
    private final PasswordEncoder codificadorContrasenas;
    private final Mapeador mapeador;
    private final Clock reloj;

    public ServicioMiembrosImpl(RepositorioMiembros repositorioMiembros, RepositorioPlanes repositorioPlanes,
                                RepositorioCuentasUsuario repositorioCuentas,
                                RepositorioReservas repositorioReservas,
                                PasswordEncoder codificadorContrasenas, Mapeador mapeador, Clock reloj) {
        this.repositorioMiembros = repositorioMiembros;
        this.repositorioPlanes = repositorioPlanes;
        this.repositorioCuentas = repositorioCuentas;
        this.repositorioReservas = repositorioReservas;
        this.codificadorContrasenas = codificadorContrasenas;
        this.mapeador = mapeador;
        this.reloj = reloj;
    }

    @Override
    public Pagina<RespuestaMiembro> listar(String nombre, EstadoMiembro estado, int pagina, int tamano,
                                           String orden) {
        if (pagina < 0) {
            throw new SolicitudInvalidaException("El parametro 'page' no puede ser negativo");
        }
        if (tamano < 1 || tamano > 50) {
            throw new SolicitudInvalidaException("El parametro 'size' debe estar entre 1 y 50");
        }
        Pageable pageable = PageRequest.of(pagina, tamano, interpretarOrden(orden));
        Page<Miembro> resultado = repositorioMiembros.buscar(nombre, estado, pageable);
        List<RespuestaMiembro> contenido = resultado.getContent().stream()
                .map(mapeador::aRespuesta)
                .toList();
        return new Pagina<>(contenido, resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages());
    }

    @Override
    public RespuestaMiembro buscar(Long id) {
        return mapeador.aRespuesta(obtenerCompleto(id));
    }

    @Override
    @Transactional
    public RespuestaMiembro crear(SolicitudMiembro solicitud) {
        PlanMembresia plan = obtenerPlan(solicitud.planId());
        String documento = texto(solicitud.documento(), "documento").toUpperCase(Locale.ROOT);
        String nombre = texto(solicitud.nombre(), "nombre");
        String email = texto(solicitud.email(), "email").toLowerCase(Locale.ROOT);
        String nombreUsuario = texto(solicitud.nombreUsuario(), "nombreUsuario").toLowerCase(Locale.ROOT);

        if (repositorioMiembros.existsByNumeroDocumentoIgnoreCase(documento)) {
            throw new ReglaNegocioException("RN-11", "El numero de documento ya esta registrado");
        }
        if (repositorioMiembros.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException("RN-11", "El email ya esta registrado");
        }
        if (repositorioCuentas.existsByNombreUsuarioIgnoreCase(nombreUsuario)) {
            throw new ReglaNegocioException("RN-11", "El nombre de usuario ya esta registrado");
        }

        Miembro miembro = new Miembro(plan, documento, nombre, email, EstadoMiembro.ACTIVE);
        miembro = repositorioMiembros.save(miembro);
        CuentaUsuario cuenta = new CuentaUsuario(nombreUsuario,
                codificadorContrasenas.encode(solicitud.contrasena()), Rol.MEMBER, miembro);
        repositorioCuentas.save(cuenta);
        return mapeador.aRespuesta(obtenerCompleto(miembro.getId()));
    }

    @Override
    @Transactional
    public RespuestaMiembro actualizar(Long id, SolicitudActualizarMiembro solicitud) {
        Miembro miembro = obtenerCompleto(id);
        PlanMembresia plan = obtenerPlan(solicitud.planId());
        String email = texto(solicitud.email(), "email").toLowerCase(Locale.ROOT);
        if (repositorioMiembros.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ReglaNegocioException("RN-11", "El email ya esta registrado");
        }
        miembro.setNombreCompleto(texto(solicitud.nombre(), "nombre"));
        miembro.setEmail(email);
        miembro.setPlan(plan);
        return mapeador.aRespuesta(repositorioMiembros.save(miembro));
    }

    @Override
    @Transactional
    public RespuestaMiembro suspender(Long id) {
        Miembro miembro = obtenerCompleto(id);
        miembro.setEstado(EstadoMiembro.SUSPENDED);
        return mapeador.aRespuesta(repositorioMiembros.save(miembro));
    }

    @Override
    @Transactional
    public RespuestaMiembro activar(Long id) {
        Miembro miembro = obtenerCompleto(id);
        miembro.setEstado(EstadoMiembro.ACTIVE);
        return mapeador.aRespuesta(repositorioMiembros.save(miembro));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Miembro miembro = obtenerCompleto(id);
        if (repositorioReservas.existsByMiembroId(id)) {
            throw new ReglaNegocioException("RN-12",
                    "No se puede eliminar el miembro: tiene reservas registradas (activas o historicas)");
        }
        repositorioCuentas.findByMiembroId(id).ifPresent(repositorioCuentas::delete);
        repositorioMiembros.delete(miembro);
    }

    @Override
    public ResumenConsumo consumo(Long id, String mes) {
        Miembro miembro = obtenerCompleto(id);
        YearMonth periodo = interpretarMes(mes);
        LocalDateTime desde = periodo.atDay(1).atStartOfDay();
        LocalDateTime hasta = desde.plusMonths(1);
        long minutos = repositorioReservas.sumarMinutosConsumidos(id, desde, hasta);

        BigDecimal horasIncluidas = BigDecimal.valueOf(miembro.getPlan().getHorasIncluidasMes())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal horasUsadas = BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal horasIncluidasUsadas = horasUsadas.min(horasIncluidas);
        BigDecimal horasRestantes = horasIncluidas.subtract(horasIncluidasUsadas).max(BigDecimal.ZERO);
        BigDecimal horasExcedentes = horasUsadas.subtract(horasIncluidas).max(BigDecimal.ZERO);

        return new ResumenConsumo(miembro.getId(), miembro.getNombreCompleto(), miembro.getPlan().getNombre(),
                periodo.toString(), horasIncluidas, horasUsadas, horasIncluidasUsadas, horasRestantes,
                horasExcedentes);
    }

    private Miembro obtenerCompleto(Long id) {
        return repositorioMiembros.buscarCompleto(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Miembro", id));
    }

    private PlanMembresia obtenerPlan(Long planId) {
        return repositorioPlanes.findById(planId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan", planId));
    }

    private YearMonth interpretarMes(String mes) {
        if (mes == null || mes.isBlank()) {
            return YearMonth.now(reloj);
        }
        try {
            return YearMonth.parse(mes);
        } catch (DateTimeParseException excepcion) {
            throw new SolicitudInvalidaException("El parametro 'mes' debe tener el formato yyyy-MM");
        }
    }

    private Sort interpretarOrden(String orden) {
        String valor = (orden == null || orden.isBlank()) ? "nombre,asc" : orden.trim();
        String[] partes = valor.split(",");
        if (partes.length != 2) {
            throw new SolicitudInvalidaException(
                    "El parametro 'sort' debe tener el formato campo,asc o campo,desc");
        }
        String campo = CAMPOS_ORDEN.get(partes[0].trim());
        if (campo == null) {
            throw new SolicitudInvalidaException(
                    "El parametro 'sort' solo admite nombre, documento, email o estado");
        }
        Sort.Direction direccion = switch (partes[1].trim().toLowerCase(Locale.ROOT)) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new SolicitudInvalidaException(
                    "El parametro 'sort' debe indicar asc o desc");
        };
        return Sort.by(direccion, campo);
    }
}
