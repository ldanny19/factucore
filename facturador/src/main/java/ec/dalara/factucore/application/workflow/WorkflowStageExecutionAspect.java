package ec.dalara.factucore.application.workflow;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import ec.dalara.factucore.domain.workflow.ContextoWorkflow;
import ec.dalara.factucore.domain.workflow.EtapaWorkflow;

@Aspect
@Component
public class WorkflowStageExecutionAspect {

	@Around("execution(public * ec.dalara.factucore.application.workflow.*WorkflowStep.ejecutar(..))")
	public Object registrarEjecucion(ProceedingJoinPoint joinPoint) throws Throwable {
		ContextoWorkflow contexto = obtenerContexto(joinPoint);
		EtapaWorkflow etapa = obtenerEtapa(joinPoint.getTarget());
		if (contexto == null || etapa == null) {
			return joinPoint.proceed();
		}

		contexto.iniciarEtapa(etapa);
		try {
			Object salida = joinPoint.proceed();
			if (contexto.getEjecucion(etapa) != null && contexto.getEjecucion(etapa).getFechaFin() == null) {
				contexto.registrarSalidaEtapa(etapa, salida);
			}
			return salida;
		} catch (RuntimeException exception) {
			throw exception;
		}
	}

	private ContextoWorkflow obtenerContexto(ProceedingJoinPoint joinPoint) {
		for (Object argumento : joinPoint.getArgs()) {
			if (argumento instanceof ContextoWorkflow contexto) {
				return contexto;
			}
		}
		return null;
	}

	private EtapaWorkflow obtenerEtapa(Object target) {
		try {
			Method metodo = target.getClass().getMethod("etapa");
			Object valor = metodo.invoke(target);
			return valor instanceof EtapaWorkflow etapa ? etapa : null;
		} catch (Exception exception) {
			return switch (target.getClass().getSimpleName()) {
			case "GenerarRespuestaWorkflowStep" -> EtapaWorkflow.GENERAR_RESPUESTA;
			case "RespuestaErrorNotificacionWorkflowStep" -> EtapaWorkflow.RESPUESTA_ERROR_NOTIFICACION;
			case "FinWorkflowStep" -> EtapaWorkflow.FIN;
			default -> null;
			};
		}
	}
}
