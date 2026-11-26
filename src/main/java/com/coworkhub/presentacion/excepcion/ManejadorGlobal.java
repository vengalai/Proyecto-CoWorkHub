package com.coworkhub.presentacion.excepcion;

import com.coworkhub.negocio.excepcion.AccesoDenegadoException;
import com.coworkhub.negocio.excepcion.ExcepcionNegocio;
import com.coworkhub.negocio.excepcion.NoAutenticadoException;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;
import com.coworkhub.presentacion.dto.RespuestaError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * Convierte las excepciones en el cuerpo de error unico de la API:
 * { "error": "...", "codigo": "...", "timestamp": "..." }
 */
@RestControllerAdvice
public class ManejadorGlobal {

    private static final Logger registro = LoggerFactory.getLogger(ManejadorGlobal.class);

    private final Clock reloj;

    public ManejadorGlobal(Clock reloj) {
        this.reloj = reloj;
    }

    @ExceptionHandler(ExcepcionNegocio.class)
    public ResponseEntity<RespuestaError> manejarNegocio(ExcepcionNegocio excepcion) {
        HttpStatus estado = switch (excepcion) {
            case SolicitudInvalidaException e -> HttpStatus.BAD_REQUEST;
            case RecursoNoEncontradoException e -> HttpStatus.NOT_FOUND;
            case ReglaNegocioException e -> HttpStatus.CONFLICT;
            case AccesoDenegadoException e -> HttpStatus.FORBIDDEN;
            case NoAutenticadoException e -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.BAD_REQUEST;
        };
        return respuesta(estado, excepcion.getMessage(), excepcion.getCodigo());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidacion(MethodArgumentNotValidException excepcion) {
        String detalle = excepcion.getBindingResult().getFieldErrors().stream()
                .map(this::describir)
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, detalle, "SOLICITUD_INVALIDA");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<RespuestaError> manejarSolicitudMalFormada(Exception excepcion) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "La solicitud no tiene el formato esperado", "SOLICITUD_INVALIDA");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RespuestaError> manejarNoEncontrado(NoResourceFoundException excepcion) {
        return respuesta(HttpStatus.NOT_FOUND, "El recurso solicitado no existe", "NO_ENCONTRADO");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RespuestaError> manejarMetodo(HttpRequestMethodNotSupportedException excepcion) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED,
                "El metodo HTTP no esta permitido para esta ruta", "METODO_NO_PERMITIDO");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<RespuestaError> manejarTipoContenido(HttpMediaTypeNotSupportedException excepcion) {
        return respuesta(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "El tipo de contenido debe ser application/json", "TIPO_NO_SOPORTADO");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespuestaError> manejarAccesoDenegado(AccessDeniedException excepcion) {
        return respuesta(HttpStatus.FORBIDDEN,
                "No tiene permiso para realizar esta operacion", "ACCESO_DENEGADO");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RespuestaError> manejarAutenticacion(AuthenticationException excepcion) {
        return respuesta(HttpStatus.UNAUTHORIZED, "Usuario o contrasena incorrectos", "NO_AUTENTICADO");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> manejarIntegridad(DataIntegrityViolationException excepcion) {
        return respuesta(HttpStatus.CONFLICT,
                "La operacion choca con datos existentes o relacionados", "CONFLICTO_DE_DATOS");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarInesperado(Exception excepcion) {
        registro.error("Error interno no controlado", excepcion);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error interno en el servidor", "ERROR_INTERNO");
    }

    private String describir(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<RespuestaError> respuesta(HttpStatus estado, String mensaje, String codigo) {
        return ResponseEntity.status(estado)
                .body(new RespuestaError(mensaje, codigo, OffsetDateTime.now(reloj)));
    }
}
