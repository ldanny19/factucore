package ec.dalara.factucore.application.validation;

import ec.dalara.factucore.application.MessageResolver;
import ec.dalara.factucore.application.contract.response.MensajeResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComprobanteValidationResult {

    private final MessageResolver messageResolver;
    private final List<MensajeResponse> errores = new ArrayList<>();
    private final List<MensajeResponse> advertencias = new ArrayList<>();
    private final List<MensajeResponse> mensajes = new ArrayList<>();

    public ComprobanteValidationResult(
            MessageResolver messageResolver
    ) {
        this.messageResolver = messageResolver;
    }

    public void agregarError(
            String codigo,
            String campo,
            Object... parametros
    ) {
        errores.add(crearMensaje(codigo, campo, parametros));
    }

    public void agregarAdvertencia(
            String codigo,
            String campo,
            Object... parametros
    ) {
        advertencias.add(crearMensaje(codigo, campo, parametros));
    }

    public void agregarMensaje(
            String codigo,
            String campo,
            Object... parametros
    ) {
        mensajes.add(crearMensaje(codigo, campo, parametros));
    }

    public boolean esValido() {
        return errores.isEmpty();
    }

    public List<MensajeResponse> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    public List<MensajeResponse> getAdvertencias() {
        return Collections.unmodifiableList(advertencias);
    }

    public List<MensajeResponse> getMensajes() {
        return Collections.unmodifiableList(mensajes);
    }

    private MensajeResponse crearMensaje(
            String codigo,
            String campo,
            Object... parametros
    ) {
        return MensajeResponse.builder()
                .codigo(codigo)
                .mensaje(messageResolver.resolver(codigo, parametros))
                .campo(campo)
                .build();
    }
}