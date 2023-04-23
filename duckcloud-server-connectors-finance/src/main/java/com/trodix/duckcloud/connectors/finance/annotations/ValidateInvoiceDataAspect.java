package com.trodix.duckcloud.connectors.finance.annotations;

import com.trodix.duckcloud.presentation.exceptions.InvalidDataException;
import com.trodix.duckcloud.connectors.finance.models.FinanceModel;
import com.trodix.duckcloud.presentation.dto.requests.NodeRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class ValidateInvoiceDataAspect {

    @Around("@annotation(ValidateInvoiceData)")
    public Object validateInvoiceDataJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        List<NodeRequest> candidateArgs = Arrays.stream(args).toList().stream().filter(arg -> arg instanceof NodeRequest).map(o -> (NodeRequest)o).toList();
        if (candidateArgs.isEmpty()) {
            throw new IllegalArgumentException("Annotation @ValidateInvoiceData should decorate a method with at least one arg of type " + NodeRequest.class.getCanonicalName());
        }
        candidateArgs.forEach(this::validateInvoiceData);
        return joinPoint.proceed();
    }

    private void validateInvoiceData(NodeRequest nodeRequest) throws InvalidDataException {
        if (!FinanceModel.TYPE_INVOICE.equals(nodeRequest.getType())) {
            throw new InvalidDataException("Node should be of type " + FinanceModel.TYPE_INVOICE + " but was of type " + nodeRequest.getType());
        }
    }

}
