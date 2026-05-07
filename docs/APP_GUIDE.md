# Guia completa de uso de la aplicacion

Esta guia describe la aplicacion SaaS fiscal completa, apartado por apartado. Sirve para revisar la demo local o la preproduccion compartida.

## Acceso

Abre la URL de la aplicacion:

- Local: `http://127.0.0.1:8080`.
- Demo compartida: la URL HTTPS publica entregada para la revision.

Si hay Basic Auth delante de Nginx, introduce el usuario y contrasena temporal facilitados fuera de Git. Si OIDC esta activo, aparece la pantalla **Acceso seguro**.

- **Iniciar sesion**: redirige al proveedor OIDC.
- **Salir**: cierra la sesion OIDC cuando el usuario esta autenticado.

En modo demo local la aplicacion entra directamente con una identidad de prueba.

## Navegacion principal

La barra lateral izquierda contiene enlaces internos de la misma pantalla.

- **Panel**: vuelve al panel superior.
- **Empresas**: salta a busqueda, listado y alta de empresas.
- **Documentos**: salta a documentos de la empresa seleccionada.
- **Facturas**: salta al ciclo de facturacion.
- **E-invoice**: salta a la evidencia local de factura electronica.
- **SIF local**: salta al registro local SIF/Verifactu.

Estos enlaces no cambian el tenant ni la empresa. Solo mueven la vista dentro del workspace actual.

## Barra superior

La cabecera muestra **SaaS fiscal operativo** y controles globales.

- **Buscar empresa**: filtra empresas por nombre legal o NIF/CIF en el backend.
- **Actualizar**: recarga datos del tenant: empresas, relaciones, documentos, facturas, e-invoices, SIF y declaraciones.
- **Salir**: aparece solo si OIDC esta activo.

## Estado de servicios

La franja de estado confirma el contexto operativo:

- estado del backend;
- tenant activo y rol;
- aviso de preproduccion local sin envio legal externo;
- usuario actual.

## Contexto de tenant

El bloque de tenant muestra la sesion activa, una vista de branding y el selector de tenants.

- **Branding configurable por tenant**: muestra iniciales y nombre del tenant.
- **Botones de tenant**: cambian el tenant activo y recargan datos acotados a ese tenant.

Comportamiento esperado: lo creado en un tenant no debe aparecer al cambiar a otro.

## Control de preproduccion

Este bloque resume si el entorno esta preparado para una revision controlada.

### Roles y permisos

Muestra roles efectivos y capacidades. Distingue operaciones fiscales, configuracion y evidencia.

### Estado del sistema

Muestra frontend, backend, MySQL validado desde backend, proxy, entorno, version, ultimo smoke test y modo de autenticacion.

### Datos demo controlados

Indica si hay tenant demo, empresa demo, clientes, facturas, pagos y evidencias. Todo debe ser dato demo o local/preprod.

### Buscador global

- **Buscar en tenant**: busca empresas, clientes, facturas y documentos ya cargados.
- Las filas de resultado son atajos informativos.

## Resumen SaaS

Las tarjetas de metricas resumen el tenant activo:

- **Empresas**: empresas visibles.
- **Relaciones**: relaciones B2B.
- **Documentos**: documentos del tenant y de la empresa seleccionada.
- **Facturas**: numero de facturas e importe total.
- **E-invoices**: evidencias locales de factura electronica.
- **SIF**: registros SIF locales.

## Empresas

Es la entrada principal al trabajo diario.

### Lista de empresas

Cada fila muestra nombre legal, NIF/CIF, estado y tipo de relacion. Al pulsar una fila se abre en el detalle.

### Nueva empresa

Campos:

- **Nombre legal**.
- **NIF/CIF**.
- **Pais**.
- **Relacion**: `CLIENT`, `SUPPLIER` u `OWNER`.

Boton:

- **Crear**: crea la empresa en el tenant activo y la selecciona.

## Detalle de empresa

Es el centro operativo de la empresa seleccionada.

### Datos basicos

Campos:

- **Nombre legal**.
- **NIF/CIF**.
- **Pais**.
- **Relacion**.
- **Estado**.

Botones:

- **Guardar**: actualiza los datos de la empresa.
- **Desactivar**: marca la empresa como inactiva.

## Resumen operativo de empresa

Muestra actividad fiscal y cobros de la empresa.

- **Emitidas**: facturas emitidas.
- **Borradores**: facturas en borrador.
- **Cobrado**: importe cobrado.
- **Pendiente**: importe pendiente.
- **Dashboard grafico de facturas**: barras por estado de factura.
- **Notificaciones internas**: avisos operativos.
- **Exportar CSV**: descarga un CSV resumen de facturas.

El CSV es auxiliar. La evidencia fiscal local se genera desde el ZIP de evidencias.

## Fiscalidad

Configura los datos de emision de la empresa.

Campos:

- **Razon fiscal**.
- **NIF fiscal**.
- **Direccion fiscal**.
- **Ciudad**.
- **Provincia**.
- **Codigo postal**.
- **Moneda**.
- **Dias pago**.
- **IVA por defecto**.
- **Plantilla PDF**: `standard`, `compact` o `detailed`.
- **Modo SIF**: modo local/preprod.

Botones:

- **Guardar fiscalidad**: guarda configuracion fiscal.
- **Crear serie**: crea una serie desde **Serie**, **Prefijo** y **Siguiente**.

Las insignias de serie muestran el prefijo y el siguiente numero.

## Clientes

Los clientes son destinatarios de factura. Se usan para congelar snapshots de facturacion.

Controles:

- **Buscar cliente**: filtra por nombre, NIF o email.
- **CSV**: descarga clientes en CSV.

Campos de alta:

- **Cliente**.
- **NIF cliente**.
- **Email cliente**.
- **Direccion cliente**.
- **Ciudad cliente**.
- **CP cliente**.

Boton:

- **Crear cliente**: crea el cliente y lo selecciona en el formulario de factura.

Chips de cliente:

- Al pulsar un chip se selecciona ese cliente como destinatario de la factura.

## Datos operativos por empresa

Son enlaces internos del detalle:

- **Clientes**.
- **Documentos**.
- **Facturas**.
- **E-invoice**.
- **SIF local**.

## Documentos

Gestiona archivos asociados a la empresa seleccionada. Subir un documento no crea una factura fiscal.

Controles:

- **Buscar documento**: filtra por titulo, tipo o hash.

Campos:

- **Titulo**.
- **Tipo**: `INVOICE_RECEIVED`, `INVOICE_ISSUED`, `CONTRACT` o `EVIDENCE`.
- **Archivo**.

Botones:

- **Subir**: sube el archivo a la empresa.
- **Descargar**: descarga el documento.
- **Eventos**: carga eventos del documento.

Cada fila muestra titulo, fichero, tipo y prefijo de hash SHA-256.

## Facturas

Gestiona borradores, edicion, emision, cobros, PDF, rectificativas y anulacion local.

### Filtros

- **Estado**: filtra por `DRAFT`, `ISSUED`, `RECTIFIED` o `CANCELLED_LOCAL`.
- **Buscar factura**: filtra por numero, numero fiscal o empresa.

### Lista de facturas

Cada fila muestra numero, emisor, cliente, estado de cobro, pendiente, estado fiscal e importe.

Botones:

- **Ver**: selecciona la factura y muestra operaciones, trazabilidad y timeline.
- **Editar**: carga un borrador en el formulario. Se desactiva tras emitir.
- **Emitir**: emite un borrador y asigna numero fiscal.
- **PDF**: descarga PDF local. Se desactiva si la factura es borrador.
- **Rectificar**: crea un borrador rectificativo desde una factura emitida.
- **Anular**: ejecuta anulacion local/preprod.

Una factura emitida no se edita libremente. Se usa rectificativa o anulacion local.

### Cobros y estado de factura

Aparece al seleccionar una factura.

- **Pago**: importe a registrar.
- **Registrar pago**: crea un pago manual sobre facturas emitidas.
- **Buscar pago**: filtra pagos.

La lista de pagos muestra fecha, importe y metodo.

### Trazabilidad

Muestra numero fiscal, hash de PDF, hash de e-invoice, hash SIF, snapshot de emisor y snapshot de totales.

### Timeline de factura

Muestra eventos de ciclo de vida: borrador, emision, pagos, e-invoice local y SIF local.

### Nueva factura / Editar borrador

Campos:

- **Emisor**.
- **Cliente**.
- **Destinatario**.
- **Numero**.
- **Fecha**.
- **Vencimiento**.
- **Moneda**.

Lineas:

- **Descripcion**.
- **Cantidad**.
- **Precio**.
- **IVA %**.
- **Dto %**.
- **Ret %**.

Botones:

- **Linea**: anade otra linea.
- **Quitar**: elimina una linea si hay mas de una.
- **Guardar borrador**: crea o actualiza un borrador.
- **Cancelar**: sale del modo edicion de borrador.

La tarjeta de totales calcula base, IVA, retencion y total antes de guardar.

## E-invoice

Genera evidencia local UBL desde una factura emitida seleccionada.

Boton:

- **Generar**: crea la evidencia local. Se desactiva si no hay factura emitida seleccionada o si ya existe evidencia.

La evidencia muestra estado, `LOCAL STUB`, sintaxis, estado comercial, artifact ID y hash. No envia nada a un proveedor externo.

## SIF / Verifactu local

Crea un registro local encadenado SIF/Verifactu para la factura emitida seleccionada.

Boton:

- **Registrar**: crea el registro SIF local. Se desactiva si no hay factura emitida seleccionada o si ya existe registro.

La evidencia muestra secuencia, `LOCAL STUB`, tipo, artifact ID y hash. No es presentacion legal ante AEAT.

## Evidencia

Agrupa auditoria y exportacion local/preprod.

Controles:

- **Buscar auditoria**: filtra por evento, actor o hash.
- **CSV**: descarga auditoria en CSV.
- **Generar ZIP**: crea paquete local de evidencias.

Tarjetas:

- **Eventos auditados**: eventos recientes y hashes.
- **Exports**: paquetes generados.

Boton dentro de exports:

- Pulsar un export descarga el ZIP.

### Timeline de empresa

Muestra eventos de empresa y documentos ordenados temporalmente.

## Plataforma SaaS

Resume gobierno de SaaS.

### Planes

Muestra limites de plan: usuarios, documentos, facturas y precio.

### Gobierno

Muestra reglas clave:

- el tenant activo viaja por path y `X-Tenant-Id`;
- la busqueda de empresas esta acotada en backend;
- las facturas emitidas bloquean edicion libre;
- SIF/e-invoice solo nacen desde facturas emitidas.

Si el usuario es administrador de plataforma, se muestra una lista resumida de tenants.

## Flujo completo recomendado

1. Abrir la app.
2. Revisar estado de servicios y control de preproduccion.
3. Buscar una empresa en el buscador global.
4. Filtrar la lista de empresas.
5. Crear una empresa.
6. Guardar fiscalidad y verificar o crear serie.
7. Crear y seleccionar cliente.
8. Subir o revisar documentos.
9. Crear borrador de factura.
10. Editar el borrador.
11. Emitir la factura.
12. Registrar pago.
13. Descargar PDF y comprobar hash visible.
14. Generar e-invoice local.
15. Registrar SIF local.
16. Generar ZIP de evidencias.
17. Cambiar de tenant y comprobar aislamiento.

## Limites legales y de entorno

Esta aplicacion esta lista para revision local/preprod, no para produccion legal.

- E-invoice es local/stub.
- SIF/Verifactu es local/stub.
- No hay envio real a AEAT.
- No hay certificados reales ni firma legal.
- No se deben usar datos reales de clientes en este entorno.
