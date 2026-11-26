/* CoWorkHub - frontend en JavaScript puro.
   Consume la API REST protegida con JWT y arma la interfaz segun el rol. */

const CLAVE_TOKEN = 'coworkhub.token';

const ETIQUETAS_ESTADO = {
    PENDING: 'Pendiente',
    CONFIRMED: 'Confirmada',
    COMPLETED: 'Completada',
    CANCELLED: 'Cancelada'
};

const ETIQUETAS_TIPO = {
    MEETING_ROOM: 'Sala de reuniones',
    PRIVATE_OFFICE: 'Oficina privada',
    DESK: 'Escritorio'
};

const ETIQUETAS_ROL = {
    ADMIN: 'Administrador',
    RECEPTIONIST: 'Recepcion',
    MEMBER: 'Miembro'
};

const estado = {
    token: null,
    perfil: null,
    sedes: [],
    salas: [],
    salasVista: [],
    equipamientos: [],
    servicios: [],
    planes: [],
    miembrosSeleccion: [],
    miembrosVista: [],
    serviciosReserva: [],
    ultimosFiltrosReservas: null,
    ultimaDisponibilidad: null,
    paginaMiembros: 0,
    tamanoMiembros: 8,
    filtroMiembros: { nombre: '', estado: '' }
};

const $ = (selector, raiz = document) => raiz.querySelector(selector);

/* ===== Utilidades ===== */

function texto(valor) {
    return String(valor ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;');
}

function moneda(valor) {
    return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(Number(valor ?? 0));
}

function fechaHora(valor) {
    if (!valor) return '-';
    return new Date(valor).toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' });
}

function soloHora(valor) {
    return valor ? String(valor).slice(0, 5) : '-';
}

function hoyIso(dias = 0) {
    const fecha = new Date();
    fecha.setDate(fecha.getDate() + dias);
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${fecha.getFullYear()}-${mes}-${dia}`;
}

function fechaIso(valor) {
    if (!valor) return null;
    return valor.length === 16 ? `${valor}:00` : valor;
}

function etiquetaEstado(estadoReserva) {
    return ETIQUETAS_ESTADO[estadoReserva] ?? estadoReserva;
}

function insigniaEstado(estadoReserva) {
    return `<span class="insignia ${texto(estadoReserva)}">${texto(etiquetaEstado(estadoReserva))}</span>`;
}

function esAdmin() {
    return estado.perfil?.rol === 'ADMIN';
}

function esPersonal() {
    return estado.perfil?.rol === 'ADMIN' || estado.perfil?.rol === 'RECEPTIONIST';
}

function esMiembro() {
    return estado.perfil?.rol === 'MEMBER';
}

function nombreSede(id) {
    return estado.sedes.find(sede => sede.id === id)?.nombre ?? '-';
}

function mostrarAviso(mensaje, tipo = 'info') {
    const aviso = document.createElement('div');
    aviso.className = `aviso ${tipo}`;
    aviso.textContent = mensaje;
    $('#avisos').appendChild(aviso);
    setTimeout(() => aviso.remove(), 4200);
}

/* ===== Cliente HTTP ===== */

async function solicitar(ruta, opciones = {}) {
    const { sinSesion = false, ...resto } = opciones;
    const cabeceras = { ...(resto.body ? { 'Content-Type': 'application/json' } : {}) };
    if (estado.token) {
        cabeceras.Authorization = `Bearer ${estado.token}`;
    }
    const respuesta = await fetch(ruta, { ...resto, headers: cabeceras });

    if (respuesta.status === 401 && !sinSesion) {
        cerrarSesion();
        throw new Error('La sesion expiro. Vuelve a iniciar sesion.');
    }
    if (!respuesta.ok) {
        let cuerpo = null;
        try { cuerpo = await respuesta.json(); } catch { /* sin cuerpo JSON */ }
        const detalle = cuerpo?.error ?? `Error ${respuesta.status}`;
        throw new Error(detalle);
    }
    const contenido = await respuesta.text();
    return contenido ? JSON.parse(contenido) : null;
}

/* ===== Sesion ===== */

async function iniciarSesion(evento) {
    evento.preventDefault();
    const formulario = evento.target;
    try {
        const datos = await solicitar('/auth/login', {
            method: 'POST',
            sinSesion: true,
            body: JSON.stringify({
                nombreUsuario: formulario.nombreUsuario.value.trim(),
                contrasena: formulario.contrasena.value
            })
        });
        estado.token = datos.token;
        sessionStorage.setItem(CLAVE_TOKEN, datos.token);
        await entrar();
    } catch (error) {
        mostrarAviso(error.message, 'error');
    }
}

async function entrar() {
    estado.perfil = await solicitar('/auth/perfil');
    $('#etiqueta-usuario').textContent = estado.perfil.nombreUsuario;
    const insigniaRol = $('#etiqueta-rol');
    insigniaRol.textContent = ETIQUETAS_ROL[estado.perfil.rol] ?? estado.perfil.rol;
    insigniaRol.className = `insignia ${estado.perfil.rol}`;

    $('#pantalla-login').hidden = true;
    $('#aplicacion').hidden = false;
    aplicarPermisos();
    await cargarCatalogos();
    await mostrarVista('reservas');
}

function cerrarSesion() {
    estado.token = null;
    estado.perfil = null;
    estado.serviciosReserva = [];
    estado.ultimosFiltrosReservas = null;
    sessionStorage.removeItem(CLAVE_TOKEN);
    $('#aplicacion').hidden = true;
    $('#pantalla-login').hidden = false;
    $('#formulario-login').reset();
}

function aplicarPermisos() {
    document.querySelectorAll('[data-solo-admin]').forEach(elemento => {
        elemento.hidden = !esAdmin();
    });
    document.querySelectorAll('[data-solo-personal]').forEach(elemento => {
        elemento.hidden = !esPersonal();
    });
    document.querySelectorAll('[data-solo-miembro]').forEach(elemento => {
        elemento.hidden = !esMiembro();
    });
}

/* ===== Catalogos ===== */

async function cargarCatalogos() {
    const [sedes, salas, equipamientos, servicios, planes] = await Promise.all([
        solicitar('/api/sedes'),
        solicitar('/api/salas'),
        solicitar('/api/equipamientos'),
        solicitar('/api/servicios-adicionales'),
        solicitar('/api/planes')
    ]);
    estado.sedes = sedes;
    estado.salas = salas;
    estado.equipamientos = equipamientos;
    estado.servicios = servicios;
    estado.planes = planes;
    llenarSelectSedes();
    llenarSelectSalas();
    llenarSelectServicios();
}

function opciones(items, valor, etiqueta, seleccionado) {
    return items.map(item => {
        const marcado = String(item[valor]) === String(seleccionado ?? '') ? 'selected' : '';
        return `<option value="${item[valor]}" ${marcado}>${texto(etiqueta(item))}</option>`;
    }).join('');
}

function llenarSelectSedes() {
    const opcionesSede = opciones(estado.sedes, 'id', sede => sede.nombre);
    ['#select-sede-disponibilidad', '#filtro-sede-salas'].forEach(selector => {
        const select = $(selector);
        if (!select) return;
        const actual = select.value;
        const primera = selector === '#filtro-sede-salas' ? '<option value="">Todas las sedes</option>' : '';
        select.innerHTML = primera + opcionesSede;
        if (actual) select.value = actual;
    });
}

function llenarSelectSalas() {
    const select = $('#select-sala-reserva');
    if (!select) return;
    const actual = select.value;
    select.innerHTML = estado.salas
        .filter(sala => sala.activa)
        .map(sala => `<option value="${sala.id}">${texto(sala.nombre)} - ${texto(nombreSede(sala.sedeId))}</option>`)
        .join('');
    if (actual) select.value = actual;

    const selectBusqueda = $('#select-sala-busqueda');
    if (selectBusqueda) {
        const valorActual = selectBusqueda.value;
        selectBusqueda.innerHTML = estado.salas
            .map(sala => `<option value="${sala.id}">${texto(sala.nombre)} - ${texto(nombreSede(sala.sedeId))}</option>`)
            .join('');
        if (valorActual) selectBusqueda.value = valorActual;
    }
}

function llenarSelectServicios() {
    const select = $('#select-servicio-reserva');
    if (!select) return;
    const actual = select.value;
    select.innerHTML = estado.servicios
        .map(servicio => `<option value="${servicio.id}">${texto(servicio.nombre)} (${moneda(servicio.precioUnitario)})</option>`)
        .join('');
    if (actual) select.value = actual;

    const equipamientos = $('#equipamientos-disponibilidad');
    if (equipamientos) {
        equipamientos.innerHTML = estado.equipamientos
            .map(equipo => `<label><input type="checkbox" name="equipamientoDisponibilidad" value="${equipo.id}"> ${texto(equipo.nombre)}</label>`)
            .join('');
    }
}

async function cargarMiembrosSeleccion() {
    if (!esPersonal()) return;
    const pagina = await solicitar('/api/miembros?page=0&size=50&sort=nombre,asc');
    estado.miembrosSeleccion = pagina.contenido;
    const activos = estado.miembrosSeleccion.filter(miembro => miembro.estado === 'ACTIVE');

    const paraReserva = $('#select-miembro-reserva');
    if (paraReserva) {
        const actual = paraReserva.value;
        paraReserva.innerHTML = opciones(activos, 'id', miembro => `${miembro.nombre} (${miembro.documento})`);
        if (actual) paraReserva.value = actual;
    }

    const paraBusqueda = $('#select-miembro-busqueda');
    if (paraBusqueda) {
        const actual = paraBusqueda.value;
        paraBusqueda.innerHTML = opciones(estado.miembrosSeleccion, 'id',
            miembro => `${miembro.nombre} (${miembro.documento})`);
        if (actual) paraBusqueda.value = actual;
    }
}

/* ===== Navegacion ===== */

async function mostrarVista(vista) {
    document.querySelectorAll('.vista').forEach(seccion => {
        seccion.hidden = seccion.id !== `vista-${vista}`;
    });
    document.querySelectorAll('#menu button[data-vista]').forEach(boton => {
        boton.classList.toggle('activo', boton.dataset.vista === vista);
    });

    try {
        switch (vista) {
            case 'reservas': await iniciarVistaReservas(); break;
            case 'disponibilidad': prepararDisponibilidad(); break;
            case 'sedes': renderSedes(); break;
            case 'salas': await cargarSalasVista(); break;
            case 'equipamientos': renderEquipamientos(); break;
            case 'servicios': renderServicios(); break;
            case 'planes': renderPlanes(); break;
            case 'miembros': await cargarMiembros(); break;
        }
    } catch (error) {
        mostrarAviso(error.message, 'error');
    }
}

/* ===== Reservas ===== */

async function iniciarVistaReservas() {
    llenarSelectSalas();
    llenarSelectServicios();
    if (esPersonal()) {
        await cargarMiembrosSeleccion();
        if (!estado.ultimosFiltrosReservas) {
            await consultarReservas({ estado: 'PENDING' });
        }
    } else if (estado.perfil?.miembroId) {
        await consultarReservas({ miembroId: estado.perfil.miembroId });
    }
}

async function consultarReservas(filtros) {
    const parametros = new URLSearchParams();
    Object.entries(filtros).forEach(([clave, valor]) => {
        if (valor !== null && valor !== undefined && valor !== '') {
            parametros.append(clave, valor);
        }
    });
    const reservas = await solicitar(`/api/reservas?${parametros.toString()}`);
    estado.ultimosFiltrosReservas = filtros;
    renderTablaReservas(reservas);
}

async function refrescarReservas() {
    if (estado.ultimosFiltrosReservas) {
        await consultarReservas(estado.ultimosFiltrosReservas);
    } else if (esMiembro() && estado.perfil?.miembroId) {
        await consultarReservas({ miembroId: estado.perfil.miembroId });
    }
}

function renderTablaReservas(reservas) {
    const contenedor = $('#tabla-reservas');
    if (!reservas || reservas.length === 0) {
        contenedor.innerHTML = '<p class="vacio">No hay reservas para el filtro elegido.</p>';
        return;
    }
    const filas = reservas.map(reserva => `
        <tr>
            <td>#${reserva.id}</td>
            <td>${texto(reserva.miembro)}</td>
            <td>
                <strong>${texto(reserva.sala)}</strong><br>
                <span class="nota">${texto(reserva.sede)}</span>
            </td>
            <td>${fechaHora(reserva.inicio)}</td>
            <td>${fechaHora(reserva.fin)}</td>
            <td>${insigniaEstado(reserva.estado)}</td>
            <td>${moneda(reserva.costoTotal)}</td>
            <td class="acciones">${accionesReserva(reserva)}</td>
        </tr>
    `).join('');

    contenedor.innerHTML = `
        <table>
            <thead>
            <tr>
                <th>ID</th><th>Miembro</th><th>Sala</th><th>Inicio</th><th>Fin</th>
                <th>Estado</th><th>Total</th><th>Acciones</th>
            </tr>
            </thead>
            <tbody>${filas}</tbody>
        </table>
    `;
}

function accionesReserva(reserva) {
    const botones = [`<button class="boton chico secundario" data-accion="ver-reserva" data-id="${reserva.id}">Ver</button>`];
    if (reserva.estado === 'PENDING') {
        botones.push(`<button class="boton chico secundario" data-accion="gestionar-servicios" data-id="${reserva.id}">Servicios</button>`);
        if (esPersonal()) {
            botones.push(`<button class="boton chico secundario" data-accion="confirmar-reserva" data-id="${reserva.id}">Confirmar</button>`);
        }
    }
    if (reserva.estado === 'CONFIRMED' && esPersonal()) {
        botones.push(`<button class="boton chico secundario" data-accion="completar-reserva" data-id="${reserva.id}">Completar</button>`);
    }
    if (reserva.estado === 'PENDING' || reserva.estado === 'CONFIRMED') {
        botones.push(`<button class="boton chico peligro" data-accion="cancelar-reserva" data-id="${reserva.id}">Cancelar</button>`);
    }
    return botones.join('');
}

function renderServiciosReserva() {
    const lista = $('#lista-servicios-reserva');
    if (!lista) return;
    if (estado.serviciosReserva.length === 0) {
        lista.innerHTML = '<li class="vacio">Sin servicios adicionales.</li>';
        return;
    }
    lista.innerHTML = estado.serviciosReserva.map((item, indice) => `
        <li>
            <span>${texto(item.nombre)} x ${item.cantidad} = <strong>${moneda(item.precio * item.cantidad)}</strong></span>
            <button type="button" class="boton chico peligro" data-accion="quitar-servicio-temporal" data-indice="${indice}">Quitar</button>
        </li>
    `).join('');
}

async function crearReserva(evento) {
    evento.preventDefault();
    const formulario = evento.target;
    const miembroId = esPersonal() ? Number(formulario.miembroId.value) : estado.perfil.miembroId;
    if (!miembroId) {
        mostrarAviso('Selecciona un miembro activo para reservar.', 'error');
        return;
    }
    const datos = {
        miembroId,
        salaId: Number(formulario.salaId.value),
        inicio: fechaIso(formulario.inicio.value),
        fin: fechaIso(formulario.fin.value),
        asistentes: Number(formulario.asistentes.value),
        servicios: estado.serviciosReserva.map(item => ({
            servicioId: item.servicioId,
            cantidad: item.cantidad
        }))
    };
    try {
        const reserva = await solicitar('/api/reservas', { method: 'POST', body: JSON.stringify(datos) });
        estado.serviciosReserva = [];
        renderServiciosReserva();
        formulario.reset();
        mostrarAviso(`Reserva #${reserva.id} creada. Total ${moneda(reserva.costoTotal)}.`, 'exito');
        await refrescarReservas();
    } catch (error) {
        mostrarAviso(error.message, 'error');
    }
}

function mostrarDetalleReserva(reserva) {
    const servicios = reserva.detalles.length === 0
        ? '<p class="vacio">Sin servicios adicionales.</p>'
        : `<table>
             <thead><tr><th>Servicio</th><th>Cantidad</th><th>Precio</th><th>Subtotal</th></tr></thead>
             <tbody>${reserva.detalles.map(detalle => `
                <tr>
                    <td>${texto(detalle.servicio)}</td>
                    <td>${detalle.cantidad}</td>
                    <td>${moneda(detalle.precioUnitarioAplicado)}</td>
                    <td>${moneda(detalle.subtotal)}</td>
                </tr>`).join('')}
             </tbody>
           </table>`;

    abrirDialogo(`Reserva #${reserva.id}`, `
        <div class="detalle-reserva">
            <dl>
                <dt>Miembro</dt><dd>${texto(reserva.miembro)}</dd>
                <dt>Sala</dt><dd>${texto(reserva.sala)} (${texto(reserva.sede)})</dd>
                <dt>Inicio</dt><dd>${fechaHora(reserva.inicio)}</dd>
                <dt>Fin</dt><dd>${fechaHora(reserva.fin)}</dd>
                <dt>Asistentes</dt><dd>${reserva.asistentes}</dd>
                <dt>Estado</dt><dd>${insigniaEstado(reserva.estado)}</dd>
                <dt>Costo sala</dt><dd>${moneda(reserva.costoSala)}</dd>
                <dt>Costo servicios</dt><dd>${moneda(reserva.costoServicios)}</dd>
                <dt>Total</dt><dd>${moneda(reserva.costoTotal)}</dd>
                <dt>Cargo por cancelacion</dt><dd>${moneda(reserva.cargoCancelacion)}</dd>
            </dl>
            <h4>Servicios</h4>
            ${servicios}
        </div>
    `);
}

async function abrirServicios(reservaId) {
    const reserva = await solicitar(`/api/reservas/${reservaId}`);
    if (reserva.estado !== 'PENDING') {
        mostrarAviso('Solo se pueden modificar los servicios de una reserva pendiente.', 'error');
        return;
    }
    const lista = reserva.detalles.length === 0
        ? '<p class="vacio">Sin servicios adicionales.</p>'
        : `<table>
             <thead><tr><th>Servicio</th><th>Cantidad</th><th>Subtotal</th><th></th></tr></thead>
             <tbody>${reserva.detalles.map(detalle => `
                <tr>
                    <td>${texto(detalle.servicio)}</td>
                    <td>${detalle.cantidad}</td>
                    <td>${moneda(detalle.subtotal)}</td>
                    <td><button class="boton chico peligro" data-accion="quitar-servicio"
                        data-id="${reserva.id}" data-servicio="${detalle.servicioId}">Quitar</button></td>
                </tr>`).join('')}
             </tbody>
           </table>`;

    abrirDialogo(`Servicios de la reserva #${reserva.id}`, `
        <div class="detalle-reserva">
            ${lista}
            <div class="fila">
                <select id="servicio-nuevo">
                    ${opciones(estado.servicios, 'id', servicio => `${servicio.nombre} (${moneda(servicio.precioUnitario)})`)}
                </select>
                <input id="cantidad-nuevo" type="number" min="1" value="1" class="corto">
                <button class="boton secundario" data-accion="agregar-servicio" data-id="${reserva.id}">Agregar</button>
            </div>
            <p class="nota">Total actual: ${moneda(reserva.costoTotal)}</p>
        </div>
    `);
}

/* ===== Disponibilidad ===== */

function prepararDisponibilidad() {
    llenarSelectSedes();
    llenarSelectServicios();
    const fecha = $('#fecha-disponibilidad');
    if (!fecha.value) {
        fecha.value = hoyIso(1);
    }
}

async function consultarDisponibilidad(evento) {
    evento.preventDefault();
    const formulario = evento.target;
    const parametros = new URLSearchParams({
        sedeId: formulario.sedeId.value,
        fecha: formulario.fecha.value,
        horaInicio: formulario.horaInicio.value,
        horaFin: formulario.horaFin.value,
        capacidadMinima: formulario.capacidadMinima.value
    });
    const equipos = [...document.querySelectorAll('input[name="equipamientoDisponibilidad"]:checked')]
        .map(equipo => equipo.value);
    if (equipos.length > 0) {
        parametros.append('equipamientoIds', equipos.join(','));
    }
    try {
        const salas = await solicitar(`/api/disponibilidad?${parametros.toString()}`);
        estado.ultimaDisponibilidad = {
            fecha: formulario.fecha.value,
            horaInicio: formulario.horaInicio.value,
            horaFin: formulario.horaFin.value
        };
        renderDisponibilidad(salas, estado.ultimaDisponibilidad);
    } catch (error) {
        mostrarAviso(error.message, 'error');
    }
}

function renderDisponibilidad(salas, rango) {
    const contenedor = $('#resultado-disponibilidad');
    if (!salas || salas.length === 0) {
        contenedor.innerHTML = '<p class="vacio">No hay salas libres para ese horario.</p>';
        return;
    }
    contenedor.innerHTML = `
        <table>
            <thead><tr><th>Sala</th><th>Sede</th><th>Tipo</th><th>Capacidad</th><th>Tarifa</th><th>Equipamiento</th><th></th></tr></thead>
            <tbody>${salas.map(sala => `
                <tr>
                    <td><strong>${texto(sala.nombre)}</strong></td>
                    <td>${texto(sala.sede)}</td>
                    <td>${texto(ETIQUETAS_TIPO[sala.tipo] ?? sala.tipo)}</td>
                    <td>${sala.capacidad}</td>
                    <td>${moneda(sala.tarifaPorHora)} / h</td>
                    <td>${sala.equipamientos.map(equipo => texto(equipo.nombre)).join(', ') || '-'}</td>
                    <td><button class="boton chico primario" data-accion="usar-sala" data-id="${sala.id}"
                        data-fecha="${rango.fecha}" data-inicio="${rango.horaInicio}" data-fin="${rango.horaFin}">Reservar</button></td>
                </tr>`).join('')}
            </tbody>
        </table>
        <p class="nota">Horario consultado: ${rango.fecha} de ${rango.horaInicio} a ${rango.horaFin}.</p>
    `;
}

/* ===== Sedes ===== */

function renderSedes() {
    const contenedor = $('#tabla-sedes');
    if (estado.sedes.length === 0) {
        contenedor.innerHTML = '<p class="vacio">No hay sedes registradas.</p>';
        return;
    }
    contenedor.innerHTML = `
        <table>
            <thead><tr><th>Nombre</th><th>Ciudad</th><th>Direccion</th><th>Horario</th>${esAdmin() ? '<th>Acciones</th>' : ''}</tr></thead>
            <tbody>${estado.sedes.map(sede => `
                <tr>
                    <td><strong>${texto(sede.nombre)}</strong></td>
                    <td>${texto(sede.ciudad)}</td>
                    <td>${texto(sede.direccion)}</td>
                    <td>${soloHora(sede.horaApertura)} a ${soloHora(sede.horaCierre)}</td>
                    ${esAdmin() ? `<td class="acciones">
                        <button class="boton chico secundario" data-accion="editar-sede" data-id="${sede.id}">Editar</button>
                        <button class="boton chico peligro" data-accion="eliminar-sede" data-id="${sede.id}">Eliminar</button>
                    </td>` : ''}
                </tr>`).join('')}
            </tbody>
        </table>
    `;
}

function abrirFormularioSede(sede) {
    const esEdicion = Boolean(sede);
    abrirFormularioDialogo(esEdicion ? 'Editar sede' : 'Nueva sede', `
        <form>
            <label>Nombre<input name="nombre" required value="${texto(sede?.nombre)}"></label>
            <label>Ciudad<input name="ciudad" required value="${texto(sede?.ciudad)}"></label>
            <label>Direccion<input name="direccion" required value="${texto(sede?.direccion)}"></label>
            <label>Hora de apertura<input type="time" name="horaApertura" required value="${soloHora(sede?.horaApertura) === '-' ? '08:00' : soloHora(sede.horaApertura)}"></label>
            <label>Hora de cierre<input type="time" name="horaCierre" required value="${soloHora(sede?.horaCierre) === '-' ? '20:00' : soloHora(sede.horaCierre)}"></label>
            ${accionesDialogo()}
        </form>
    `, async (formulario) => {
        const datos = {
            nombre: formulario.nombre.value.trim(),
            ciudad: formulario.ciudad.value.trim(),
            direccion: formulario.direccion.value.trim(),
            horaApertura: formulario.horaApertura.value,
            horaCierre: formulario.horaCierre.value
        };
        await solicitar(`/api/sedes${esEdicion ? `/${sede.id}` : ''}`, {
            method: esEdicion ? 'PUT' : 'POST',
            body: JSON.stringify(datos)
        });
        await cargarCatalogos();
        renderSedes();
        mostrarAviso('Sede guardada.', 'exito');
    });
}

/* ===== Salas ===== */

async function cargarSalasVista() {
    const sedeId = $('#filtro-sede-salas').value;
    estado.salasVista = await solicitar(`/api/salas${sedeId ? `?sedeId=${sedeId}` : ''}`);
    renderSalas();
}

function renderSalas() {
    const contenedor = $('#tabla-salas');
    if (estado.salasVista.length === 0) {
        contenedor.innerHTML = '<p class="vacio">No hay salas para la sede elegida.</p>';
        return;
    }
    contenedor.innerHTML = `
        <table>
            <thead><tr><th>Sala</th><th>Sede</th><th>Tipo</th><th>Capacidad</th><th>Tarifa</th><th>Estado</th><th>Equipamiento</th>${esAdmin() ? '<th>Acciones</th>' : ''}</tr></thead>
            <tbody>${estado.salasVista.map(sala => `
                <tr>
                    <td><strong>${texto(sala.nombre)}</strong></td>
                    <td>${texto(sala.sede)}</td>
                    <td>${texto(ETIQUETAS_TIPO[sala.tipo] ?? sala.tipo)}</td>
                    <td>${sala.capacidad}</td>
                    <td>${moneda(sala.tarifaPorHora)} / h</td>
                    <td>${sala.activa ? '<span class="insignia ACTIVE">Activa</span>' : '<span class="insignia SUSPENDED">Inactiva</span>'}</td>
                    <td>${sala.equipamientos.map(equipo => texto(equipo.nombre)).join(', ') || '-'}</td>
                    ${esAdmin() ? `<td class="acciones">
                        <button class="boton chico secundario" data-accion="editar-sala" data-id="${sala.id}">Editar</button>
                        <button class="boton chico peligro" data-accion="eliminar-sala" data-id="${sala.id}">Eliminar</button>
                    </td>` : ''}
                </tr>`).join('')}
            </tbody>
        </table>
    `;
}

function abrirFormularioSala(sala) {
    const esEdicion = Boolean(sala);
    const equipos = estado.equipamientos.map(equipo => {
        const marcado = sala?.equipamientos?.some(item => item.id === equipo.id) ? 'checked' : '';
        return `<label><input type="checkbox" name="equipamientoIds" value="${equipo.id}" ${marcado}> ${texto(equipo.nombre)}</label>`;
    }).join('');

    abrirFormularioDialogo(esEdicion ? 'Editar sala' : 'Nueva sala', `
        <form>
            <label>Sede<select name="sedeId" required>
                ${opciones(estado.sedes, 'id', sede => sede.nombre, sala?.sedeId)}
            </select></label>
            <label>Nombre<input name="nombre" required value="${texto(sala?.nombre)}"></label>
            <label>Tipo<select name="tipo">
                ${opciones([{ id: 'MEETING_ROOM', nombre: 'Sala de reuniones' },
                            { id: 'PRIVATE_OFFICE', nombre: 'Oficina privada' },
                            { id: 'DESK', nombre: 'Escritorio' }], 'id', item => item.nombre, sala?.tipo)}
            </select></label>
            <label>Capacidad<input type="number" name="capacidad" min="1" required value="${sala?.capacidad ?? 1}"></label>
            <label>Tarifa por hora<input type="number" name="tarifaPorHora" min="0.01" step="0.01" required value="${sala?.tarifaPorHora ?? 10}"></label>
            <label class="chips-titulo"><input type="checkbox" name="activa" ${sala?.activa !== false ? 'checked' : ''}> Sala activa</label>
            <div class="campo-ancho">
                <span class="etiqueta">Equipamiento</span>
                <div class="chips">${equipos || '<span class="nota">No hay equipamiento cargado.</span>'}</div>
            </div>
            ${accionesDialogo()}
        </form>
    `, async (formulario) => {
        const equipamientoIds = [...formulario.querySelectorAll('input[name="equipamientoIds"]:checked')]
            .map(equipo => Number(equipo.value));
        const datos = {
            sedeId: Number(formulario.sedeId.value),
            nombre: formulario.nombre.value.trim(),
            tipo: formulario.tipo.value,
            capacidad: Number(formulario.capacidad.value),
            tarifaPorHora: Number(formulario.tarifaPorHora.value),
            activa: formulario.activa.checked,
            equipamientoIds
        };
        await solicitar(`/api/salas${esEdicion ? `/${sala.id}` : ''}`, {
            method: esEdicion ? 'PUT' : 'POST',
            body: JSON.stringify(datos)
        });
        await cargarCatalogos();
        await cargarSalasVista();
        mostrarAviso('Sala guardada.', 'exito');
    });
}

/* ===== Equipamientos ===== */

function renderEquipamientos() {
    const contenedor = $('#tabla-equipamientos');
    if (estado.equipamientos.length === 0) {
        contenedor.innerHTML = '<p class="vacio">No hay equipamiento registrado.</p>';
        return;
    }
    contenedor.innerHTML = `
        <table>
            <thead><tr><th>Nombre</th>${esAdmin() ? '<th>Acciones</th>' : ''}</tr></thead>
            <tbody>${estado.equipamientos.map(equipo => `
                <tr>
                    <td>${texto(equipo.nombre)}</td>
                    ${esAdmin() ? `<td class="acciones">
                        <button class="boton chico secundario" data-accion="editar-equipamiento" data-id="${equipo.id}">Editar</button>
                        <button class="boton chico peligro" data-accion="eliminar-equipamiento" data-id="${equipo.id}">Eliminar</button>
                    </td>` : ''}
                </tr>`).join('')}
            </tbody>
        </table>
    `;
}

function abrirFormularioEquipamiento(equipamiento) {
    const esEdicion = Boolean(equipamiento);
    abrirFormularioDialogo(esEdicion ? 'Editar equipamiento' : 'Nuevo equipamiento', `
        <form>
            <label>Nombre<input name="nombre" required value="${texto(equipamiento?.nombre)}"></label>
            ${accionesDialogo()}
        </form>
    `, async (formulario) => {
        await solicitar(`/api/equipamientos${esEdicion ? `/${equipamiento.id}` : ''}`, {
            method: esEdicion ? 'PUT' : 'POST',
            body: JSON.stringify({ nombre: formulario.nombre.value.trim() })
        });
        await cargarCatalogos();
        renderEquipamientos();
        mostrarAviso('Equipamiento guardado.', 'exito');
    });
}

/* ===== Servicios adicionales ===== */

function renderServicios() {
    const contenedor = $('#tabla-servicios');
    if (estado.servicios.length === 0) {
        contenedor.innerHTML = '<p class="vacio">No hay servicios adicionales.</p>';
        return;
    }
    contenedor.innerHTML = `
        <table>
            <thead><tr><th>Servicio</th><th>Precio unitario</th>${esAdmin() ? '<th>Acciones</th>' : ''}</tr></thead>
            <tbody>${estado.servicios.map(servicio => `
                <tr>
                    <td>${texto(servicio.nombre)}</td>
                    <td>${moneda(servicio.precioUnitario)}</td>
                    ${esAdmin() ? `<td class="acciones">
                        <button class="boton chico secundario" data-accion="editar-servicio" data-id="${servicio.id}">Editar</button>
                        <button class="boton chico peligro" data-accion="eliminar-servicio" data-id="${servicio.id}">Eliminar</button>
                    </td>` : ''}
                </tr>`).join('')}
            </tbody>
        </table>
    `;
}

function abrirFormularioServicio(servicio) {
    const esEdicion = Boolean(servicio);
    abrirFormularioDialogo(esEdicion ? 'Editar servicio' : 'Nuevo servicio', `
        <form>
            <label>Nombre<input name="nombre" required value="${texto(servicio?.nombre)}"></label>
            <label>Precio unitario<input type="number" name="precioUnitario" min="0.01" step="0.01" required
                value="${servicio?.precioUnitario ?? 10}"></label>
            ${accionesDialogo()}
        </form>
    `, async (formulario) => {
        const datos = {
            nombre: formulario.nombre.value.trim(),
            precioUnitario: Number(formulario.precioUnitario.value)
        };
        await solicitar(`/api/servicios-adicionales${esEdicion ? `/${servicio.id}` : ''}`, {
            method: esEdicion ? 'PUT' : 'POST',
            body: JSON.stringify(datos)
        });
        await cargarCatalogos();
        renderServicios();
        mostrarAviso('Servicio guardado.', 'exito');
    });
}

/* ===== Planes ===== */

function renderPlanes() {
    const contenedor = $('#tabla-planes');
    if (estado.planes.length === 0) {
        contenedor.innerHTML = '<p class="vacio">No hay planes registrados.</p>';
        return;
    }
    contenedor.innerHTML = `
        <table>
            <thead><tr><th>Plan</th><th>Horas incluidas</th><th>Descuento excedente</th><th>Reservas activas</th>${esAdmin() ? '<th>Acciones</th>' : ''}</tr></thead>
            <tbody>${estado.planes.map(plan => `
                <tr>
                    <td><strong>${texto(plan.nombre)}</strong></td>
                    <td>${plan.horasIncluidasMes} h</td>
                    <td>${plan.descuentoExcedente} %</td>
                    <td>${plan.maxReservasActivas}</td>
                    ${esAdmin() ? `<td class="acciones">
                        <button class="boton chico secundario" data-accion="editar-plan" data-id="${plan.id}">Editar</button>
                        <button class="boton chico peligro" data-accion="eliminar-plan" data-id="${plan.id}">Eliminar</button>
                    </td>` : ''}
                </tr>`).join('')}
            </tbody>
        </table>
    `;
}

function abrirFormularioPlan(plan) {
    const esEdicion = Boolean(plan);
    abrirFormularioDialogo(esEdicion ? 'Editar plan' : 'Nuevo plan', `
        <form>
            <label>Nombre<input name="nombre" required value="${texto(plan?.nombre)}"></label>
            <label>Horas incluidas por mes<input type="number" name="horasIncluidasMes" min="0" required
                value="${plan?.horasIncluidasMes ?? 10}"></label>
            <label>Descuento sobre excedentes (%)<input type="number" name="descuentoExcedente" min="0" max="100" required
                value="${plan?.descuentoExcedente ?? 0}"></label>
            <label>Maximo de reservas activas<input type="number" name="maxReservasActivas" min="1" required
                value="${plan?.maxReservasActivas ?? 2}"></label>
            ${accionesDialogo()}
        </form>
    `, async (formulario) => {
        const datos = {
            nombre: formulario.nombre.value.trim(),
            horasIncluidasMes: Number(formulario.horasIncluidasMes.value),
            descuentoExcedente: Number(formulario.descuentoExcedente.value),
            maxReservasActivas: Number(formulario.maxReservasActivas.value)
        };
        await solicitar(`/api/planes${esEdicion ? `/${plan.id}` : ''}`, {
            method: esEdicion ? 'PUT' : 'POST',
            body: JSON.stringify(datos)
        });
        await cargarCatalogos();
        renderPlanes();
        mostrarAviso('Plan guardado.', 'exito');
    });
}

/* ===== Miembros ===== */

async function cargarMiembros() {
    const parametros = new URLSearchParams({
        page: estado.paginaMiembros,
        size: estado.tamanoMiembros,
        sort: 'nombre,asc'
    });
    if (estado.filtroMiembros.nombre) parametros.append('nombre', estado.filtroMiembros.nombre);
    if (estado.filtroMiembros.estado) parametros.append('estado', estado.filtroMiembros.estado);

    const pagina = await solicitar(`/api/miembros?${parametros.toString()}`);
    renderTablaMiembros(pagina);
}

function renderTablaMiembros(pagina) {
    const contenedor = $('#tabla-miembros');
    if (!pagina.contenido || pagina.contenido.length === 0) {
        contenedor.innerHTML = '<p class="vacio">No hay miembros para el filtro elegido.</p>';
    } else {
        contenedor.innerHTML = `
            <table>
                <thead><tr><th>Documento</th><th>Nombre</th><th>Email</th><th>Plan</th><th>Estado</th><th>Usuario</th><th>Acciones</th></tr></thead>
                <tbody>${pagina.contenido.map(miembro => `
                    <tr>
                        <td>${texto(miembro.documento)}</td>
                        <td><strong>${texto(miembro.nombre)}</strong></td>
                        <td>${texto(miembro.email)}</td>
                        <td>${texto(miembro.plan)}</td>
                        <td><span class="insignia ${texto(miembro.estado)}">${miembro.estado === 'ACTIVE' ? 'Activo' : 'Suspendido'}</span></td>
                        <td>${texto(miembro.nombreUsuario ?? '-')}</td>
                        <td class="acciones">
                            <button class="boton chico secundario" data-accion="editar-miembro" data-id="${miembro.id}">Editar</button>
                            <button class="boton chico secundario" data-accion="ver-consumo" data-id="${miembro.id}">Consumo</button>
                            ${miembro.estado === 'ACTIVE'
                                ? `<button class="boton chico secundario" data-accion="suspender-miembro" data-id="${miembro.id}">Suspender</button>`
                                : `<button class="boton chico secundario" data-accion="activar-miembro" data-id="${miembro.id}">Activar</button>`}
                            <button class="boton chico peligro" data-accion="eliminar-miembro" data-id="${miembro.id}">Eliminar</button>
                        </td>
                    </tr>`).join('')}
                </tbody>
            </table>
        `;
    }
    const totalPaginas = Math.max(1, pagina.totalPaginas);
    $('#etiqueta-paginacion').textContent = `Pagina ${pagina.pagina + 1} de ${totalPaginas} (${pagina.totalElementos} miembros)`;
    $('#boton-pagina-anterior').disabled = pagina.pagina === 0;
    $('#boton-pagina-siguiente').disabled = pagina.pagina + 1 >= totalPaginas;
}

function abrirFormularioMiembro(miembro) {
    const esEdicion = Boolean(miembro);
    const camposCuenta = esEdicion ? '' : `
        <label>Usuario<input name="nombreUsuario" required minlength="3" maxlength="30"></label>
        <label>Contrasena<input name="contrasena" type="password" required minlength="6"></label>
    `;
    abrirFormularioDialogo(esEdicion ? 'Editar miembro' : 'Nuevo miembro', `
        <form>
            <label>Documento<input name="documento" required ${esEdicion ? 'disabled' : ''} value="${texto(miembro?.documento)}"></label>
            <label>Nombre completo<input name="nombre" required value="${texto(miembro?.nombre)}"></label>
            <label>Email<input name="email" type="email" required value="${texto(miembro?.email)}"></label>
            <label>Plan<select name="planId" required>
                ${opciones(estado.planes, 'id', plan => plan.nombre, miembro?.planId)}
            </select></label>
            ${camposCuenta}
            ${accionesDialogo()}
        </form>
    `, async (formulario) => {
        const datos = {
            nombre: formulario.nombre.value.trim(),
            email: formulario.email.value.trim(),
            planId: Number(formulario.planId.value)
        };
        if (esEdicion) {
            await solicitar(`/api/miembros/${miembro.id}`, { method: 'PUT', body: JSON.stringify(datos) });
        } else {
            datos.documento = formulario.documento.value.trim();
            datos.nombreUsuario = formulario.nombreUsuario.value.trim();
            datos.contrasena = formulario.contrasena.value;
            await solicitar('/api/miembros', { method: 'POST', body: JSON.stringify(datos) });
        }
        await cargarMiembros();
        await cargarMiembrosSeleccion();
        mostrarAviso('Miembro guardado.', 'exito');
    });
}

async function abrirConsumo(miembroId, mes) {
    const parametros = mes ? `?mes=${encodeURIComponent(mes)}` : '';
    const resumen = await solicitar(`/api/miembros/${miembroId}/consumo${parametros}`);
    abrirDialogo(`Consumo de ${resumen.miembro}`, `
        <div class="detalle-reserva">
            <div class="fila">
                <label>Mes<input type="month" id="mes-consumo" value="${resumen.mes}"></label>
                <button class="boton secundario" data-accion="cambiar-mes-consumo" data-id="${miembroId}">Consultar</button>
            </div>
            <div class="metricas">
                <div class="metrica"><span>Plan</span><strong>${texto(resumen.plan)}</strong></div>
                <div class="metrica"><span>Horas incluidas</span><strong>${resumen.horasIncluidas}</strong></div>
                <div class="metrica"><span>Horas usadas</span><strong>${resumen.horasUsadas}</strong></div>
                <div class="metrica"><span>Horas restantes</span><strong>${resumen.horasRestantes}</strong></div>
                <div class="metrica"><span>Horas excedentes</span><strong>${resumen.horasExcedentes}</strong></div>
            </div>
            <p class="nota">El consumo se calcula con los minutos de las reservas del mes elegido.</p>
        </div>
    `);
}

/* ===== Dialogos ===== */

function abrirDialogo(titulo, html) {
    $('#dialogo-titulo').textContent = titulo;
    $('#dialogo .dialogo-cuerpo').innerHTML = html;
    const dialogo = $('#dialogo');
    if (!dialogo.open) dialogo.showModal();
}

function abrirFormularioDialogo(titulo, html, alEnviar) {
    abrirDialogo(titulo, html);
    const formulario = $('#dialogo .dialogo-cuerpo form');
    formulario.addEventListener('submit', async (evento) => {
        evento.preventDefault();
        const boton = formulario.querySelector('button[type="submit"]');
        if (boton) boton.disabled = true;
        try {
            await alEnviar(formulario);
            cerrarDialogo();
        } catch (error) {
            mostrarAviso(error.message, 'error');
        } finally {
            if (boton) boton.disabled = false;
        }
    });
}

function accionesDialogo() {
    return `<div class="acciones-dialogo">
        <button type="button" class="boton plano" onclick="cerrarDialogo()">Cancelar</button>
        <button type="submit" class="boton primario">Guardar</button>
    </div>`;
}

function cerrarDialogo() {
    $('#dialogo').close();
}

window.cerrarDialogo = cerrarDialogo;

/* ===== Acciones delegadas ===== */

async function manejarAccion(boton) {
    const accion = boton.dataset.accion;
    const id = Number(boton.dataset.id);

    switch (accion) {
        case 'editar-sede':
            abrirFormularioSede(estado.sedes.find(sede => sede.id === id));
            break;
        case 'eliminar-sede':
            if (!confirm('¿Eliminar la sede seleccionada?')) return;
            await solicitar(`/api/sedes/${id}`, { method: 'DELETE' });
            await cargarCatalogos();
            renderSedes();
            mostrarAviso('Sede eliminada.', 'exito');
            break;

        case 'editar-sala':
            abrirFormularioSala(estado.salas.find(sala => sala.id === id));
            break;
        case 'eliminar-sala':
            if (!confirm('¿Eliminar la sala seleccionada?')) return;
            await solicitar(`/api/salas/${id}`, { method: 'DELETE' });
            await cargarCatalogos();
            await cargarSalasVista();
            mostrarAviso('Sala eliminada.', 'exito');
            break;

        case 'editar-equipamiento':
            abrirFormularioEquipamiento(estado.equipamientos.find(equipo => equipo.id === id));
            break;
        case 'eliminar-equipamiento':
            if (!confirm('¿Eliminar el equipamiento seleccionado?')) return;
            await solicitar(`/api/equipamientos/${id}`, { method: 'DELETE' });
            await cargarCatalogos();
            renderEquipamientos();
            mostrarAviso('Equipamiento eliminado.', 'exito');
            break;

        case 'editar-servicio':
            abrirFormularioServicio(estado.servicios.find(servicio => servicio.id === id));
            break;
        case 'eliminar-servicio':
            if (!confirm('¿Eliminar el servicio seleccionado?')) return;
            await solicitar(`/api/servicios-adicionales/${id}`, { method: 'DELETE' });
            await cargarCatalogos();
            renderServicios();
            mostrarAviso('Servicio eliminado.', 'exito');
            break;

        case 'editar-plan':
            abrirFormularioPlan(estado.planes.find(plan => plan.id === id));
            break;
        case 'eliminar-plan':
            if (!confirm('¿Eliminar el plan seleccionado?')) return;
            await solicitar(`/api/planes/${id}`, { method: 'DELETE' });
            await cargarCatalogos();
            renderPlanes();
            mostrarAviso('Plan eliminado.', 'exito');
            break;

        case 'editar-miembro':
            abrirFormularioMiembro(estado.miembrosSeleccion.find(miembro => miembro.id === id));
            break;
        case 'suspender-miembro':
            await solicitar(`/api/miembros/${id}/suspender`, { method: 'PATCH' });
            await cargarMiembros();
            await cargarMiembrosSeleccion();
            mostrarAviso('Miembro suspendido.', 'exito');
            break;
        case 'activar-miembro':
            await solicitar(`/api/miembros/${id}/activar`, { method: 'PATCH' });
            await cargarMiembros();
            await cargarMiembrosSeleccion();
            mostrarAviso('Miembro activado.', 'exito');
            break;
        case 'eliminar-miembro':
            if (!confirm('¿Eliminar el miembro y su cuenta de usuario?')) return;
            await solicitar(`/api/miembros/${id}`, { method: 'DELETE' });
            await cargarMiembros();
            await cargarMiembrosSeleccion();
            mostrarAviso('Miembro eliminado.', 'exito');
            break;
        case 'ver-consumo':
            await abrirConsumo(id);
            break;

        case 'ver-reserva':
            mostrarDetalleReserva(await solicitar(`/api/reservas/${id}`));
            break;
        case 'gestionar-servicios':
            await abrirServicios(id);
            break;
        case 'confirmar-reserva':
            await solicitar(`/api/reservas/${id}/confirmar`, { method: 'POST' });
            await refrescarReservas();
            mostrarAviso('Reserva confirmada.', 'exito');
            break;
        case 'completar-reserva':
            await solicitar(`/api/reservas/${id}/completar`, { method: 'POST' });
            await refrescarReservas();
            mostrarAviso('Reserva completada.', 'exito');
            break;
        case 'cancelar-reserva':
            if (!confirm('¿Cancelar la reserva? Si faltan menos de 24 horas puede aplicar un cargo del 50 %.')) return;
            await solicitar(`/api/reservas/${id}/cancelar`, { method: 'POST' });
            await refrescarReservas();
            mostrarAviso('Reserva cancelada.', 'exito');
            break;
        case 'agregar-servicio': {
            const servicioId = Number($('#servicio-nuevo').value);
            const cantidad = Number($('#cantidad-nuevo').value);
            await solicitar(`/api/reservas/${id}/servicios`, {
                method: 'POST',
                body: JSON.stringify({ servicioId, cantidad })
            });
            await refrescarReservas();
            await abrirServicios(id);
            mostrarAviso('Servicio agregado.', 'exito');
            break;
        }
        case 'quitar-servicio': {
            const servicioId = Number(boton.dataset.servicio);
            await solicitar(`/api/reservas/${id}/servicios/${servicioId}`, { method: 'DELETE' });
            await refrescarReservas();
            await abrirServicios(id);
            mostrarAviso('Servicio quitado.', 'exito');
            break;
        }
        case 'cambiar-mes-consumo':
            await abrirConsumo(id, $('#mes-consumo').value);
            break;

        case 'usar-sala': {
            const formulario = $('#formulario-reserva');
            const fecha = boton.dataset.fecha;
            formulario.salaId.value = String(id);
            formulario.inicio.value = `${fecha}T${boton.dataset.inicio}`;
            formulario.fin.value = `${fecha}T${boton.dataset.fin}`;
            await mostrarVista('reservas');
            formulario.scrollIntoView({ behavior: 'smooth', block: 'start' });
            mostrarAviso('Sala y horario cargados en el formulario de reserva.');
            break;
        }

        case 'quitar-servicio-temporal': {
            const indice = Number(boton.dataset.indice);
            estado.serviciosReserva.splice(indice, 1);
            renderServiciosReserva();
            break;
        }
    }
}

/* ===== Eventos ===== */

function configurarEventos() {
    $('#formulario-login').addEventListener('submit', iniciarSesion);
    $('#boton-salir').addEventListener('click', cerrarSesion);

    document.querySelectorAll('#menu button[data-vista]').forEach(boton => {
        boton.addEventListener('click', () => mostrarVista(boton.dataset.vista));
    });
    $('#boton-mi-consumo').addEventListener('click', () => abrirConsumo(estado.perfil.miembroId));

    $('#boton-nueva-sede').addEventListener('click', () => abrirFormularioSede(null));
    $('#boton-nueva-sala').addEventListener('click', () => abrirFormularioSala(null));
    $('#boton-nuevo-equipamiento').addEventListener('click', () => abrirFormularioEquipamiento(null));
    $('#boton-nuevo-servicio').addEventListener('click', () => abrirFormularioServicio(null));
    $('#boton-nuevo-plan').addEventListener('click', () => abrirFormularioPlan(null));
    $('#boton-nuevo-miembro').addEventListener('click', () => abrirFormularioMiembro(null));

    $('#filtro-sede-salas').addEventListener('change', () => cargarSalasVista().catch(error => mostrarAviso(error.message, 'error')));

    $('#formulario-reserva').addEventListener('submit', crearReserva);
    $('#boton-limpiar-reserva').addEventListener('click', () => {
        $('#formulario-reserva').reset();
        estado.serviciosReserva = [];
        renderServiciosReserva();
    });
    $('#boton-agregar-servicio').addEventListener('click', () => {
        const servicioId = Number($('#select-servicio-reserva').value);
        const cantidad = Number($('#cantidad-servicio-reserva').value);
        const servicio = estado.servicios.find(item => item.id === servicioId);
        if (!servicio || !cantidad || cantidad < 1) {
            mostrarAviso('Elige un servicio y una cantidad valida.', 'error');
            return;
        }
        const existente = estado.serviciosReserva.find(item => item.servicioId === servicioId);
        if (existente) {
            existente.cantidad += cantidad;
        } else {
            estado.serviciosReserva.push({
                servicioId,
                nombre: servicio.nombre,
                precio: Number(servicio.precioUnitario),
                cantidad
            });
        }
        renderServiciosReserva();
    });

    $('#formulario-busqueda-reservas').addEventListener('submit', async (evento) => {
        evento.preventDefault();
        const formulario = evento.target;
        try {
            if (formulario.modo.value === 'miembro') {
                await consultarReservas({ miembroId: formulario.miembroId.value });
            } else if (formulario.modo.value === 'estado') {
                await consultarReservas({ estado: formulario.estado.value });
            } else {
                await consultarReservas({ salaId: formulario.salaId.value, fecha: formulario.fecha.value });
            }
        } catch (error) {
            mostrarAviso(error.message, 'error');
        }
    });

    $('#modo-busqueda-reservas').addEventListener('change', (evento) => {
        const modo = evento.target.value;
        $('#bloque-busqueda-miembro').hidden = modo !== 'miembro';
        $('#bloque-busqueda-estado').hidden = modo !== 'estado';
        $('#bloque-busqueda-sala').hidden = modo !== 'sala';
        $('#bloque-busqueda-fecha').hidden = modo !== 'sala';
    });

    $('#formulario-disponibilidad').addEventListener('submit', consultarDisponibilidad);

    $('#formulario-filtro-miembros').addEventListener('submit', async (evento) => {
        evento.preventDefault();
        const formulario = evento.target;
        estado.filtroMiembros = {
            nombre: formulario.nombre.value.trim(),
            estado: formulario.estado.value
        };
        estado.paginaMiembros = 0;
        try {
            await cargarMiembros();
        } catch (error) {
            mostrarAviso(error.message, 'error');
        }
    });

    $('#boton-pagina-anterior').addEventListener('click', async () => {
        if (estado.paginaMiembros > 0) {
            estado.paginaMiembros -= 1;
            await cargarMiembros();
        }
    });
    $('#boton-pagina-siguiente').addEventListener('click', async () => {
        estado.paginaMiembros += 1;
        await cargarMiembros();
    });

    document.addEventListener('click', async (evento) => {
        const boton = evento.target.closest('[data-accion]');
        if (!boton) return;
        try {
            await manejarAccion(boton);
        } catch (error) {
            mostrarAviso(error.message, 'error');
        }
    });
}

/* ===== Arranque ===== */

document.addEventListener('DOMContentLoaded', async () => {
    configurarEventos();
    renderServiciosReserva();
    const tokenGuardado = sessionStorage.getItem(CLAVE_TOKEN);
    if (tokenGuardado) {
        estado.token = tokenGuardado;
        try {
            await entrar();
        } catch {
            cerrarSesion();
        }
    }
});
