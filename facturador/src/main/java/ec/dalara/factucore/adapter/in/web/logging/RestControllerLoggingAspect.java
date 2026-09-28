package ec.dalara.factucore.adapter.in.web.logging;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;
@Aspect @Component
public class RestControllerLoggingAspect {
 private static final Logger LOG=LoggerFactory.getLogger(RestControllerLoggingAspect.class);
 @Around("@within(org.springframework.web.bind.annotation.RestController) && execution(public * *(..))")
 public Object registrarEjecucion(ProceedingJoinPoint jp) throws Throwable { String metodo=jp.getSignature().toShortString(); LOG.info("INICIO {}",metodo); try { Object r=jp.proceed(); LOG.info("FIN {}",metodo); return r; } catch(Throwable e){ LOG.error("ERROR {}",metodo,e); throw e; } }
}
