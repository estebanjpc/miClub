package com.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.app.repository.IDeportistaRepository;
import com.app.repository.IOrdenPagoRepository;
import com.app.repository.IPagoRepository;
import com.app.util.AfterCommitRunner;
import com.app.dto.ComprobanteTransferenciaDTO;
import com.app.dto.CobroAdicionalForm;
import com.app.dto.KhipuResponse;
import com.app.dto.MesPagoDTO;
import com.app.dto.MorosidadClubDTO;
import com.app.entity.Deportista;
import com.app.entity.Club;
import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.enums.ConceptoPago;
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

	@Autowired
	private ICategoriaCuotaVigenciaService categoriaCuotaVigenciaService;

	@Autowired
	private INoPagoConfigService noPagoConfigService;

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
	            	if (noPagoConfigService.aplicaNoPago(idClub, d, mes, anio)) {
	            		continue;
	            	}
	                boolean existePago = pagoRepository.existsPagoBloqueante(d.getId(), mes, anio);

	                if (!existePago) {
	                	Integer valorCuota = resolverValorCuota(d, anio, mes);
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
	    List<Pago> cobrosAdicionales = pagoRepository.findCobrosAdicionalesPendientesUsuario(idClub, usuarioId);
	    for (Pago p : cobrosAdicionales) {
	    	MesPagoDTO item = new MesPagoDTO(p.getDeportista().getId(),
	    			p.getDeportista().getNombre() + " " + p.getDeportista().getApellido(), p.getMes(), p.getAnio(),
	    			p.getMonto() != null ? p.getMonto() : 0);
	    	item.setConceptoLabel(nombreConcepto(p.getConcepto()));
	    	item.setDetalle(p.getObservacion() != null ? p.getObservacion() : "");
	    	item.setSeleccionKey("ADICIONAL-" + p.getId());
	    	lista.add(item);
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

	private Deportista requireDeportistaDelUsuarioEnClub(Long deportistaId, Long usuarioId, Long idClub) {
		Deportista d = deportistaRepository.findById(deportistaId)
				.orElseThrow(() -> new AccessDeniedException("Deportista no encontrado"));
		if (d.getUsuario() == null || !d.getUsuario().getId().equals(usuarioId)) {
			throw new AccessDeniedException("Deportista no pertenece al usuario");
		}
		if (d.getCategoria() == null || d.getCategoria().getClub() == null
				|| !d.getCategoria().getClub().getId().equals(idClub)) {
			throw new AccessDeniedException("Deportista no pertenece al club seleccionado");
		}
		return d;
	}

	@Override
	public void registrarPagoEfectivo(List<String> seleccionados, Long usuarioId, Long idClub) {
		List<Long> idsRegistrados = new ArrayList<>();
		for (String item : seleccionados) {
			if (item != null && item.startsWith("ADICIONAL-")) {
				Pago extra = requireCobroAdicionalDelUsuario(item, usuarioId, idClub);
				extra.setEstado(EstadoPago.PENDIENTE);
				extra.setMedioPago(MedioPago.EFECTIVO);
				extra.setFecha(LocalDateTime.now());
				extra.setObservacion(appendObs(extra.getObservacion(), "Pago adicional registrado en portal MiClub (efectivo)"));
				pagoRepository.save(extra);
				idsRegistrados.add(extra.getId());
				continue;
			}
            String[] data = item.split("-");
            if (data.length != 3) {
            	throw new IllegalArgumentException("Formato de selección inválido");
            }
            Long deportistaId = Long.parseLong(data[0]);
            int mes = Integer.parseInt(data[1]);
            int anio = Integer.parseInt(data[2]);

            Deportista d = requireDeportistaDelUsuarioEnClub(deportistaId, usuarioId, idClub);
            Pago pago = new Pago();
            pago.setDeportista(d);
            pago.setClub(d.getCategoria().getClub());
            pago.setFecha(LocalDateTime.now());
            pago.setMes(mes);
            pago.setAnio(anio);
            pago.setEstado(EstadoPago.PENDIENTE);
            pago.setMedioPago(MedioPago.EFECTIVO);
            pago.setConcepto(ConceptoPago.MENSUALIDAD);
            pago.setMonto(resolverValorCuota(d, anio, mes));
            pago.setObservacion("Pago registrado desde portal MiClub (efectivo)");
            pagoRepository.save(pago);
            idsRegistrados.add(pago.getId());
        }
		if (!idsRegistrados.isEmpty()) {
			List<Long> copia = List.copyOf(idsRegistrados);
			AfterCommitRunner.run(() -> asyncEmailService.notificarClubNuevoPagoEfectivoLote(copia));
		}
	}

	private static final long MAX_COMPROBANTE_BYTES = 5 * 1024 * 1024;

	@Override
	@Transactional
	public void registrarPagoTransferencia(List<String> seleccionados, Long usuarioId, Long idClub,
			MultipartFile comprobante) {
		if (comprobante == null || comprobante.isEmpty()) {
			throw new IllegalArgumentException("Debe adjuntar el comprobante de la transferencia (imagen o PDF).");
		}
		String ct = comprobante.getContentType() != null ? comprobante.getContentType() : "";
		if (!ct.startsWith("image/") && !"application/pdf".equalsIgnoreCase(ct)) {
			throw new IllegalArgumentException("El comprobante debe ser una imagen (JPG, PNG, etc.) o un PDF.");
		}
		if (comprobante.getSize() > MAX_COMPROBANTE_BYTES) {
			throw new IllegalArgumentException("El archivo no puede superar 5 MB.");
		}
		byte[] bytes;
		try {
			bytes = comprobante.getBytes();
		} catch (Exception e) {
			throw new IllegalArgumentException("No se pudo leer el archivo adjunto.");
		}
		if (bytes.length == 0) {
			throw new IllegalArgumentException("El archivo adjunto está vacío.");
		}

		String nombre = comprobante.getOriginalFilename();
		if (nombre != null && nombre.length() > 240) {
			nombre = nombre.substring(0, 240);
		}

		List<Long> idsRegistrados = new ArrayList<>();
		for (String item : seleccionados) {
			if (item != null && item.startsWith("ADICIONAL-")) {
				Pago extra = requireCobroAdicionalDelUsuario(item, usuarioId, idClub);
				extra.setEstado(EstadoPago.PENDIENTE);
				extra.setMedioPago(MedioPago.TRANSFERENCIA);
				extra.setFecha(LocalDateTime.now());
				extra.setComprobanteTransferencia(bytes);
				extra.setComprobanteContentType(ct);
				extra.setComprobanteNombreArchivo(nombre);
				extra.setObservacion(appendObs(extra.getObservacion(),
						"Transferencia de cobro adicional — comprobante adjunto (pendiente de validación del club)"));
				pagoRepository.save(extra);
				idsRegistrados.add(extra.getId());
				continue;
			}
			String[] data = item.split("-");
			if (data.length != 3) {
				throw new IllegalArgumentException("Formato de selección inválido");
			}
			Long deportistaId = Long.parseLong(data[0]);
			int mes = Integer.parseInt(data[1]);
			int anio = Integer.parseInt(data[2]);

			Deportista d = requireDeportistaDelUsuarioEnClub(deportistaId, usuarioId, idClub);
			Pago pago = new Pago();
			pago.setDeportista(d);
			pago.setClub(d.getCategoria().getClub());
			pago.setFecha(LocalDateTime.now());
			pago.setMes(mes);
			pago.setAnio(anio);
			pago.setEstado(EstadoPago.PENDIENTE);
			pago.setMedioPago(MedioPago.TRANSFERENCIA);
			pago.setConcepto(ConceptoPago.MENSUALIDAD);
			pago.setMonto(resolverValorCuota(d, anio, mes));
			pago.setObservacion("Transferencia bancaria — comprobante adjunto (pendiente de validación del club)");
			pago.setComprobanteTransferencia(bytes);
			pago.setComprobanteContentType(ct);
			pago.setComprobanteNombreArchivo(nombre);
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
	public OrdenPago generarOrdenPagoKhipu(List<String> seleccionados, Long idUsuario, Long idClub) {
		List<Pago> pagos = new ArrayList<>();
        int total = 0;

        for (String item : seleccionados) {
        	if (item != null && item.startsWith("ADICIONAL-")) {
        		Pago extra = requireCobroAdicionalDelUsuario(item, idUsuario, idClub);
        		extra.setEstado(EstadoPago.PENDIENTE_KHIPU);
        		extra.setMedioPago(MedioPago.KHIPU);
        		extra.setFecha(LocalDateTime.now());
        		extra.setObservacion(appendObs(extra.getObservacion(), "Pago adicional vía Khipu iniciado"));
        		pagoRepository.save(extra);
        		pagos.add(extra);
        		total += (extra.getMonto() != null ? extra.getMonto() : 0);
        		continue;
        	}
            String[] d = item.split("-");
            if (d.length != 3) {
            	throw new IllegalArgumentException("Formato de selección inválido");
            }
            Long deportistaId = Long.parseLong(d[0]);
            int mes = Integer.parseInt(d[1]);
            int anio = Integer.parseInt(d[2]);

            Deportista dep = requireDeportistaDelUsuarioEnClub(deportistaId, idUsuario, idClub);

            Pago p = new Pago();
            p.setDeportista(dep);
            p.setClub(dep.getCategoria().getClub());
            p.setFecha(LocalDateTime.now());
            p.setMes(mes);
            p.setAnio(anio);
            p.setEstado(EstadoPago.PENDIENTE_KHIPU);
            p.setMedioPago(MedioPago.KHIPU);
            p.setConcepto(ConceptoPago.MENSUALIDAD);
            Integer valor = resolverValorCuota(dep, anio, mes);
            p.setMonto(valor);
            p.setObservacion("Pago vía Khipu iniciado");
            pagoRepository.save(p);

            pagos.add(p);

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
        KhipuResponse resp = khipuService.crearPago(total, pagos, saved.getId(), idClub);

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
    public void aprobarPagoEfectivo(Long idPago, Long idClub) {
        Pago pago = pagoRepository.findById(idPago).orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        if (pago.getClub() == null || !pago.getClub().getId().equals(idClub)) {
        	throw new AccessDeniedException("El pago no pertenece a su club");
        }

        pago.setEstado(EstadoPago.PAGADO);
        pago.setFecha(LocalDateTime.now());
        if (pago.getMonto() == null && pago.getDeportista() != null && pago.getDeportista().getCategoria() != null) {
        	pago.setMonto(pago.getDeportista().getCategoria().getValorCuota());
        }

        pagoRepository.save(pago);
        AfterCommitRunner.run(() -> {
        	asyncEmailService.notificarUsuarioEstadoPagoEfectivo(idPago, true,
        			"El club validó y aprobó tu pago en efectivo.");
        	asyncEmailService.notificarClubPagoAcreditado(idPago);
        });
    }

	@Override
	public List<Pago> buscarPagosFiltrados(Long idClubSession, Integer mes, EstadoPago estado, Long idDeportista) {
		return pagoRepository.buscarPagosFiltrados(idClubSession,mes,estado,idDeportista);
	}

	@Override
	public void rechazarYReactivarPago(Long id, String observacion, Long idClub) {
		Pago pagoRechazado = pagoRepository.findById(id).orElse(null);
	    
	    if (pagoRechazado != null) {
	    	if (pagoRechazado.getClub() == null || !pagoRechazado.getClub().getId().equals(idClub)) {
	    		throw new AccessDeniedException("El pago no pertenece a su club");
	    	}
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
		return pagoRepository.findByClubIdAndEstadoAndMedioPagoInOrderByFechaDesc(idClubSession, EstadoPago.PENDIENTE,
				EnumSet.of(MedioPago.EFECTIVO, MedioPago.TRANSFERENCIA));
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
			int montoAdeudado = 0;
			YearMonth cursor = inicio;
			while (!cursor.isAfter(periodoConsulta)) {
				if (noPagoConfigService.aplicaNoPago(idClubSession, deportista, cursor.getMonthValue(), cursor.getYear())) {
					cursor = cursor.plusMonths(1);
					continue;
				}
				String key = buildPeriodoKey(deportista.getId(), cursor.getYear(), cursor.getMonthValue());
				Pago pagoPeriodo = ultimoPagoPorPeriodo.get(key);
				
				if (pagoPeriodo == null || pagoPeriodo.getEstado() != EstadoPago.PAGADO) {
					cuotasAdeudadas++;
					Integer valorPeriodo = resolverValorCuota(deportista, cursor.getYear(), cursor.getMonthValue());
					montoAdeudado += (valorPeriodo != null ? valorPeriodo : 0);
				}
				cursor = cursor.plusMonths(1);
			}
			
			if (cuotasAdeudadas >= minimoCuotas) {
				MorosidadClubDTO dto = new MorosidadClubDTO();
				dto.setIdDeportista(deportista.getId());
				dto.setNombreCompleto(deportista.getNombre() + " " + deportista.getApellido());
				dto.setCuotasAdeudadas(cuotasAdeudadas);
				
				dto.setMontoAdeudado(montoAdeudado);
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

	private Integer resolverValorCuota(Deportista d, int anio, int mes) {
		if (d == null || d.getCategoria() == null || d.getCategoria().getId() == null) {
			return null;
		}
		return categoriaCuotaVigenciaService.obtenerValorCuota(d.getCategoria().getId(), anio, mes);
	}

	private Pago requireCobroAdicionalDelUsuario(String item, Long usuarioId, Long idClub) {
		Long pagoId;
		try {
			pagoId = Long.parseLong(item.replace("ADICIONAL-", ""));
		} catch (Exception e) {
			throw new IllegalArgumentException("Formato de cobro adicional inválido");
		}
		Pago p = pagoRepository.findByIdWithDetalle(pagoId)
				.orElseThrow(() -> new AccessDeniedException("Cobro adicional no encontrado"));
		if (p.getClub() == null || !idClub.equals(p.getClub().getId())) {
			throw new AccessDeniedException("Cobro adicional fuera del club");
		}
		if (p.getDeportista() == null || p.getDeportista().getUsuario() == null
				|| !usuarioId.equals(p.getDeportista().getUsuario().getId())) {
			throw new AccessDeniedException("Cobro adicional no pertenece al usuario");
		}
		if (!esCobroAdicional(p.getConcepto()) || p.getEstado() != EstadoPago.MOROSO) {
			throw new IllegalArgumentException("Cobro adicional no disponible para pago");
		}
		return p;
	}

	private boolean esCobroAdicional(ConceptoPago c) {
		return c == ConceptoPago.MATRICULA || c == ConceptoPago.IMPLEMENTACION || c == ConceptoPago.OTRO;
	}

	private String nombreConcepto(ConceptoPago c) {
		if (c == null) {
			return "Mensualidad";
		}
		return switch (c) {
		case MATRICULA -> "Matrícula";
		case IMPLEMENTACION -> "Implementación";
		case OTRO -> "Otro";
		default -> "Mensualidad";
		};
	}

	private String appendObs(String original, String extra) {
		if (original == null || original.isBlank()) {
			return extra;
		}
		return original + " | " + extra;
	}

	@Override
	public long contarPagadosEnMes(Long idClub, Integer mes, Integer anio) {
		return pagoRepository.countPagadosMensualesEnMes(idClub, mes, anio, EstadoPago.PAGADO);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Pago> obtenerHistorialDeportistaClub(Long idClub, Long deportistaId) {
		return pagoRepository.findByClub_IdAndDeportista_IdOrderByFechaDesc(idClub, deportistaId);
	}

	@Override
	@Transactional(readOnly = true)
	public ComprobanteTransferenciaDTO obtenerComprobanteTransferenciaSiAutorizado(Long pagoId, Long usuarioId,
			Long idClubSession, boolean esApoderado) {
		Pago p = pagoRepository.findByIdWithDetalle(pagoId)
				.orElseThrow(() -> new AccessDeniedException("Pago no encontrado"));
		if (!p.hasComprobanteTransferencia() || p.getMedioPago() != MedioPago.TRANSFERENCIA) {
			throw new AccessDeniedException("Comprobante no disponible");
		}
		if (esApoderado) {
			if (p.getDeportista() == null || p.getDeportista().getUsuario() == null
					|| !p.getDeportista().getUsuario().getId().equals(usuarioId)) {
				throw new AccessDeniedException("No autorizado");
			}
		} else {
			if (idClubSession == null || p.getClub() == null || !p.getClub().getId().equals(idClubSession)) {
				throw new AccessDeniedException("No autorizado");
			}
		}
		String ct = p.getComprobanteContentType() != null ? p.getComprobanteContentType() : "application/octet-stream";
		return new ComprobanteTransferenciaDTO(p.getComprobanteTransferencia(), ct, p.getComprobanteNombreArchivo());
	}

	@Override
	@Transactional
	public int crearCobroAdicional(Long idClub, CobroAdicionalForm form) {
		List<Deportista> objetivo = new ArrayList<>();
		if (form.getAlcance() == CobroAdicionalForm.Alcance.DEPORTISTA) {
			Deportista d = deportistaRepository.findById(form.getDeportistaId())
					.orElseThrow(() -> new IllegalArgumentException("Deportista no encontrado"));
			if (d.getUsuario() == null || d.getUsuario().getClub() == null || !idClub.equals(d.getUsuario().getClub().getId())) {
				throw new IllegalArgumentException("El deportista no pertenece al club.");
			}
			objetivo.add(d);
		} else if (form.getAlcance() == CobroAdicionalForm.Alcance.CATEGORIA) {
			objetivo = deportistaRepository.findByClub(idClub).stream()
					.filter(d -> d.getCategoria() != null && form.getCategoriaId().equals(d.getCategoria().getId()))
					.toList();
		} else {
			objetivo = deportistaRepository.findByClub(idClub);
		}
		if (objetivo.isEmpty()) {
			return 0;
		}
		ConceptoPago concepto = switch (form.getTipoCobro()) {
		case MATRICULA -> ConceptoPago.MATRICULA;
		case IMPLEMENTACION -> ConceptoPago.IMPLEMENTACION;
		case OTRO -> ConceptoPago.OTRO;
		};
		Club club = objetivo.get(0).getUsuario().getClub();
		int creados = 0;
		for (Deportista d : objetivo) {
			Pago p = new Pago();
			p.setClub(club);
			p.setDeportista(d);
			p.setFecha(LocalDateTime.now());
			p.setMes(form.getMes());
			p.setAnio(form.getAnio());
			p.setEstado(EstadoPago.MOROSO);
			p.setMedioPago(null);
			p.setConcepto(concepto);
			p.setMonto(form.getMonto());
			p.setObservacion(form.getDescripcion());
			pagoRepository.save(p);
			creados++;
		}
		return creados;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Pago> listarCobrosAdicionalesClub(Long idClub) {
		return pagoRepository.findCobrosAdicionalesByClub(idClub);
	}

}

