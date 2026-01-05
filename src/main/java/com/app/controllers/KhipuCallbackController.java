package com.app.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.enums.EstadoPago;
import com.app.service.IOrdenPagoService;
import com.app.service.IPagoService;

@RestController
@RequestMapping("/api/khipu")
public class KhipuCallbackController {

	@Autowired
    private IOrdenPagoService ordenPagoService;

    @Autowired
    private IPagoService pagoService;

    @PostMapping("/notify")
    @Transactional
    public ResponseEntity<String> recibirNotificacion(
            @RequestParam String paymentId,
            @RequestParam String status,
            @RequestParam(value = "transaction_id", required = false) String transactionId
    ) {

        if (!"done".equalsIgnoreCase(status)) {
            return ResponseEntity.ok("IGNORED");
        }

        OrdenPago orden = ordenPagoService.findByKhipuPaymentId(paymentId);

        if (orden == null) {
            return ResponseEntity.ok("ORDER_NOT_FOUND");
        }

        // Evitar doble procesamiento
        if (orden.getEstado() == EstadoPago.PAGADO) {
            return ResponseEntity.ok("ALREADY_PAID");
        }

        // Actualizar orden
        orden.setEstado(EstadoPago.PAGADO);
        orden.setFechaPago(LocalDateTime.now());
        orden.setKhipuTransactionId(transactionId);

        // Actualizar pagos
        for (Pago p : orden.getPagos()) {
            p.setEstado(EstadoPago.PAGADO);
            p.setObservacion("Pago confirmado por Khipu");
            pagoService.save(p);
        }

        ordenPagoService.save(orden);

        return ResponseEntity.ok("OK");
    }
    
}
