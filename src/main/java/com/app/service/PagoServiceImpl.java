package com.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.repository.IDeportistaRepository;
import com.app.repository.IOrdenPagoRepository;
import com.app.repository.IPagoRepository;
import com.app.util.AfterCommitRunner;
import com.app.dto.KhipuResponse;
import com.app.dto.MesPagoDTO;
import com.app.dto.MorosidadClubDTO;
import com.app.entity.Deportista;
import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.enums.EstadoPago;
import com.app.enums.MedioPago;

@Service
public class PagoServiceImpl implements IPagoService {
	
	@Autowired
	private IDeportistaRepository deportistaRepository;
	
	@Autowired
	private IPagoRepository pagoRepository;
	
	@Autowired
	private IOrdenPagoRepository ordenPagoRepository;
	
	@Autowired
	private IKhipuService khipuService;

	@Autowired
	private AsyncEmailService asyncEmailService;

//	@Override
//	public List<MesPagoDTO> obtenerMesesParaPagar(Long usuarioId,Long idClub) {
//
//		List<Deportista> deportistas = deportistaDao.findByUsuarioAndClub(usuarioId,idClub);
//		List<MesPagoDTO> lista = new ArrayList<>();
//
//		LocalDate hoy = LocalDate.now();
//		int anioActual = hoy.getYear();
//		int mesActual = hoy.getMonthValue();
//
//		for (Deportista d : deportistas) {
//
//			int valorCuota = d.getCategoria().getValorCuota();
//
//			// recorrido de meses desde mesActual hasta diciembre
//			for (int mes = mesActual; mes <= 12; mes++) {
//
//				boolean existePago = pagoDao.findByDeportistaIdAndMesAndAnio(d.getId(), mes, anioActual).isPresent();
//
//				if (!existePago) {
//					lista.add(new MesPagoDTO(d.getId(), d.getNombre() + " " + d.getApellido(), mes, anioActual,
//							valorCuota));
//				}
//			}
//		}
//
//		return lista;
//	}
	
	@Override
	public List<MesPagoDTO> obtenerMesesParaPagar(Long usuarioId, Long idClub) {
	    List<Deportista> deportistas = deportistaRepository.findByUsuarioAndClub(usuarioId, idClub);
	    List<MesPagoDTO> lista = new ArrayList<>();

	    LocalDate hoy = LocalDate.now();
	    int anioActual = hoy.getYear();
	    int mesActual = hoy.getMonthValue();

	    for (Deportista d : deportistas) {
	        int valorCuota = d.getCategoria().getValorCuota();
	        LocalDate fechaIngreso = d.getFechaIngreso();
	        
	        int anioIngreso = fechaIngreso.getYear();
	        int mesIngreso = fechaIngreso.getMonthValue();

	        // Recorremos desde el año de ingreso hasta el año actual
	        for (int anio = anioIngreso; anio <= anioActual; anio++) {
	            
	            // Inicio: Si es el año de ingreso, empezamos en su mes. Si no, en Enero (1).
	            int mesInicio = (anio == anioIngreso) ? mesIngreso : 1;
	            
	            // Fin: 
	            int mesFin;
	            if (anio < anioActual) {
	                // Para años pasados, recorremos hasta Diciembre
	                mesFin = 12;
	            } else {
	                // Para el año actual: Mes presente + 6 meses, pero sin pasarnos de Diciembre (12)
	                mesFin = Math.min(mesActual + 6, 12);
	            }

	            for (int mes = mesInicio; mes <= mesFin; mes++) {
	                boolean existePago = pagoRepository.existsPagoBloqueante(d.getId(), mes, anio);

	                if (!existePago) {
	                    lista.add(new MesPagoDTO(
	                        d.getId(), 
	                        d.getNombre() + " " + d.getApellido(), 
	                        mes, 
	                        anio,
	                        valorCuota
	                    ));
	                }
	            }
	        }
	    }
	    return lista;
	}

	@Override
	@Transactional
	public List<Pago> obtenerPagosRealizados(Long usuarioId,Long idClub) {

		List<Deportista> deportistas = deportistaRepository.findByUsuarioAndClub(usuarioId,idClub);
		List<Pago> lista = new ArrayList<>();

		for (Deportista d : deportistas) {
			List<Pago> pagos = pagoRepository.findByDeportistaId(d.getId());
			lista.addAll(pagos);
		}

		return lista;
	}

	@Override
	public void registrarPagoEfectivo(List<String> seleccionados) {
		List<Long> idsRegistrados = new ArrayList<>();
		for (String item : seleccionados) {
            String[] data = item.split("-");
            Long deportistaId = Long.parseLong(data[0]);
            int mes = Integer.parseInt(data[1]);
            int anio = Integer.parseInt(data[2]);

            Deportista d = deportistaRepository.findById(deportistaId).orElseThrow();
            Pago pago = new Pago();
            pago.setDeportista(d);
            pago.setClub(d.getCategoria().getClub());
            pago.setFecha(LocalDateTime.now());
            pago.setMes(mes);
            pago.setAnio(anio);
            pago.setEstado(EstadoPago.PENDIENTE);
            pago.setMedioPago(MedioPago.EFECTIVO);
            pago.setObservacion("Pago registrado desde portal MiClub (efectivo)");
            pagoRepository.save(pago);
            idsRegistrados.add(pago.getId());
        }
		if (!idsRegistrados.isEmpty()) {
			List<Long> copia = List.copyOf(idsRegistrados);
			AfterCommitRunner.run(() -> asyncEmailService.notificarClubNuevoPagoEfectivoLote(copia));
		}
	}

	@Override
	@Transactional
	public OrdenPago generarOrdenPagoKhipu(List<String> seleccionados, Long idUsuario) {
		List<Pago> pagos = new ArrayList<>();
        int total = 0;

        for (String item : seleccionados) {
            String[] d = item.split("-");
            Long deportistaId = Long.parseLong(d[0]);
            int mes = Integer.parseInt(d[1]);
            int anio = Integer.parseInt(d[2]);

            Deportista dep = deportistaRepository.findById(deportistaId).orElseThrow();

            Pago p = new Pago();
            p.setDeportista(dep);
            p.setClub(dep.getCategoria().getClub());
            p.setFecha(LocalDateTime.now());
            p.setMes(mes);
            p.setAnio(anio);
            p.setEstado(EstadoPago.PENDIENTE_KHIPU);
            p.setMedioPago(MedioPago.KHIPU);
            p.setObservacion("Pago vía Khipu iniciado");
            pagoRepository.save(p);

            pagos.add(p);

            // calcular valor por deportista: asumo m.getValorCuota o similar
            // si no tienes el valor en Deportista, reemplaza con m.valorCuota pasado desde front
            Integer valor = dep.getCategoria().getValorCuota(); // ajusta segun tu modelo
            total += (valor != null ? valor : 0);
        }

        OrdenPago orden = new OrdenPago();
        orden.setIdUsuario(idUsuario);
        orden.setMontoTotal(total);
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setMedioPago(MedioPago.KHIPU);
        orden.setPagos(pagos);

        OrdenPago saved = ordenPagoRepository.save(orden);

        // llamar a Khipu
        KhipuResponse resp = khipuService.crearPago(total, pagos, saved.getId());

        saved.setKhipuPaymentId(resp.getPaymentId());
        saved.setKhipuUrl(resp.getPaymentUrl());

        return ordenPagoRepository.save(saved);
	}
	
	@Override
    @Transactional
    public void confirmarPagoKhipu(String paymentId, String status) {
        OrdenPago orden = ordenPagoRepository.findByKhipuPaymentIdWithDetalle(paymentId);
        if (orden == null) {
            orden = ordenPagoRepository.findByKhipuPaymentId(paymentId);
        }
        if (orden == null) {
            return;
        }

        boolean ok = "done".equalsIgnoreCase(status) || "paid".equalsIgnoreCase(status);
        String motivoUsuario = ok
                ? "Tu pago con Khipu se acreditó correctamente."
                : "El pago no se completó o fue rechazado. Estado informado por el proveedor: " + (status != null ? status : "desconocido") + ".";

        Long ordenId = orden.getId();
        if (ok) {
            orden.getPagos().forEach(p -> p.setEstado(EstadoPago.PAGADO));
            orden.getPagos().forEach(pagoRepository::save);
            AfterCommitRunner.run(() -> {
                asyncEmailService.notificarClubOrdenKhipuPagada(ordenId);
                asyncEmailService.notificarUsuarioResultadoKhipu(ordenId, true, motivoUsuario);
            });
        } else {
            String motivoObs = "Khipu: pago no completado. Estado: " + (status != null ? status : "desconocido");
            orden.getPagos().forEach(p -> {
                p.setEstado(EstadoPago.RECHAZADO);
                p.setObservacion(motivoObs);
                pagoRepository.save(p);
            });
            AfterCommitRunner.run(() -> asyncEmailService.notificarUsuarioResultadoKhipu(ordenId, false, motivoUsuario));
        }
    }

	@Override
	public void save(Pago p) {
		pagoRepository.save(p);		
	}

	@Override
	public List<Pago> buscarPagosPorClub(Long idClubSession) {
		return pagoRepository.findByClubIdOrderByFechaDesc(idClubSession);
	}
	
	@Transactional
    public void aprobarPagoEfectivo(Long idPago) {
        Pago pago = pagoRepository.findById(idPago).orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        pago.setEstado(EstadoPago.PAGADO);
        pago.setFecha(LocalDateTime.now());

        pagoRepository.save(pago);
        AfterCommitRunner.run(() -> asyncEmailService.notificarUsuarioEstadoPagoEfectivo(idPago, true,
                "El club validó y aprobó tu pago en efectivo."));
    }

	@Override
	public List<Pago> buscarPagosFiltrados(Long idClubSession, Integer mes, EstadoPago estado, Long idDeportista) {
		return pagoRepository.buscarPagosFiltrados(idClubSession,mes,estado,idDeportista);
	}

	@Override
	public void rechazarYReactivarPago(Long id, String observacion) {
		Pago pagoRechazado = pagoRepository.findById(id).orElse(null);
	    
	    if (pagoRechazado != null) {
	        String observacionOriginal = pagoRechazado.getObservacion();
	        String observacionRechazo = "RECHAZO: " + observacion;
	        if (observacionOriginal != null && !observacionOriginal.isBlank()) {
	        	observacionRechazo = observacionRechazo + " | Registro original: " + observacionOriginal;
	        }
	    	
	        // Se mantiene el registro rechazado para historial completo.
	        pagoRechazado.setEstado(EstadoPago.RECHAZADO);
	        pagoRechazado.setObservacion(observacionRechazo);
	        pagoRechazado.setFecha(LocalDateTime.now());
	        pagoRepository.save(pagoRechazado);
	        String motivoCorreo = observacion != null && !observacion.isBlank()
	        		? observacion
	        		: "El club rechazó el registro de tu pago en efectivo.";
	        AfterCommitRunner.run(() -> asyncEmailService.notificarUsuarioEstadoPagoEfectivo(id, false, motivoCorreo));
	    }
	}
	
	@Override
	public List<Pago> obtenerPendientesAprobacion(Long idClubSession) {
		return pagoRepository.findByClubIdAndEstadoAndMedioPagoOrderByFechaDesc(
				idClubSession,
				EstadoPago.PENDIENTE,
				MedioPago.EFECTIVO);
	}
	
	@Override
	public List<MorosidadClubDTO> obtenerMorososCriticos(Long idClubSession, Integer mes, Integer anio, int minimoCuotas) {
		List<Deportista> deportistas = deportistaRepository.findByClub(idClubSession);
		List<Pago> pagos = pagoRepository.obtenerEstadoAcumulado(idClubSession, mes, anio);
		
		Map<String, Pago> ultimoPagoPorPeriodo = new HashMap<>();
		Map<Long, LocalDateTime> ultimaFechaPagoPorDeportista = new HashMap<>();
		
		for (Pago pago : pagos) {
			Long deportistaId = pago.getDeportista().getId();
			String periodoKey = buildPeriodoKey(deportistaId, pago.getAnio(), pago.getMes());
			ultimoPagoPorPeriodo.putIfAbsent(periodoKey, pago);
			
			if (EstadoPago.PAGADO == pago.getEstado() && pago.getFecha() != null) {
				LocalDateTime actual = ultimaFechaPagoPorDeportista.get(deportistaId);
				if (actual == null || pago.getFecha().isAfter(actual)) {
					ultimaFechaPagoPorDeportista.put(deportistaId, pago.getFecha());
				}
			}
		}
		
		YearMonth periodoConsulta = YearMonth.of(anio, mes);
		List<MorosidadClubDTO> morosos = new ArrayList<>();
		
		for (Deportista deportista : deportistas) {
			if (deportista.getFechaIngreso() == null) {
				continue;
			}
			
			YearMonth inicio = YearMonth.from(deportista.getFechaIngreso());
			if (inicio.isAfter(periodoConsulta)) {
				continue;
			}
			
			int cuotasAdeudadas = 0;
			YearMonth cursor = inicio;
			while (!cursor.isAfter(periodoConsulta)) {
				String key = buildPeriodoKey(deportista.getId(), cursor.getYear(), cursor.getMonthValue());
				Pago pagoPeriodo = ultimoPagoPorPeriodo.get(key);
				
				if (pagoPeriodo == null || pagoPeriodo.getEstado() != EstadoPago.PAGADO) {
					cuotasAdeudadas++;
				}
				cursor = cursor.plusMonths(1);
			}
			
			if (cuotasAdeudadas >= minimoCuotas) {
				MorosidadClubDTO dto = new MorosidadClubDTO();
				dto.setIdDeportista(deportista.getId());
				dto.setNombreCompleto(deportista.getNombre() + " " + deportista.getApellido());
				dto.setCuotasAdeudadas(cuotasAdeudadas);
				
				Integer valorCuota = deportista.getCategoria() != null ? deportista.getCategoria().getValorCuota() : 0;
				dto.setMontoAdeudado(cuotasAdeudadas * (valorCuota != null ? valorCuota : 0));
				dto.setUltimaFechaPago(ultimaFechaPagoPorDeportista.get(deportista.getId()));
				if (deportista.getCategoria() != null) {
					dto.setIdCategoria(deportista.getCategoria().getId());
					dto.setNombreCategoria(deportista.getCategoria().getNombre());
				}
				morosos.add(dto);
			}
		}
		
		return morosos;
	}
	
	private String buildPeriodoKey(Long deportistaId, Integer anio, Integer mes) {
		return deportistaId + "-" + anio + "-" + mes;
	}

	@Override
	public long contarPagadosEnMes(Long idClub, Integer mes, Integer anio) {
		return pagoRepository.countByClub_IdAndMesAndAnioAndEstado(idClub, mes, anio, EstadoPago.PAGADO);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Pago> obtenerHistorialDeportistaClub(Long idClub, Long deportistaId) {
		return pagoRepository.findByClub_IdAndDeportista_IdOrderByFechaDesc(idClub, deportistaId);
	}

}
