import java.util.Locale;
import java.util.Objects;

/** Casos de prueba: imprime datos de entrada, resultado esperado y resultado obtenido. */
public final class PruebasLab3 {
    private static int verificadas = 0;
    private static int fallidas = 0;

    private PruebasLab3() { }

    public static void main(String[] args) {
        System.out.println("CASO | ENTRADA | ESPERADO | OBTENIDO | ESTADO");
        probarCobrosYTiempos();
        probarValidaciones();
        probarDespachos();
        probarReintentos("Sobre", new Sobre("TS", "A", "B", 0.2, Destino.URBANO), 3);
        probarReintentos("Paquete", new Paquete("TP", "A", "B", 1.1,
                Destino.URBANO, 10, 10, 10), 3);
        probarReintentos("Fragil", new PaqueteFragil("TF", "A", "B", 1.1,
                Destino.URBANO, 10, 10, 10, 100), 2);
        probarReintentos("Refrigerada", new CargaRefrigerada("TR", "A", "B", 1.1,
                Destino.URBANO, 0), 1);
        probarEntregaExitosa();
        System.out.printf(Locale.US, "RESUMEN | %d casos | %d correctos | %d fallidos%n",
                verificadas, verificadas - fallidas, fallidas);
        if (fallidas > 0) System.exit(1);
    }

    private static void probarCobrosYTiempos() {
        SistemaEnvios sistema = new SistemaEnvios();
        sistema.cargarDatosIniciales();
        caso("Datos iniciales", "8 envios / 3 vehiculos",
                "8 / 3", sistema.consultarEnvios().size() + " / " + sistema.consultarVehiculos().size());
        caso("Ingresos iniciales", "precarga sin cobros", 0.0, sistema.ingresoTotal());
        Envio s1 = sistema.buscarEnvio("S001");
        Envio s2 = sistema.buscarEnvio("S002");
        Paquete paquete = (Paquete) sistema.buscarEnvio("P001");
        Envio p2 = sistema.buscarEnvio("P002");
        Envio f1 = sistema.buscarEnvio("F001");
        Envio f2 = sistema.buscarEnvio("F002");
        Envio r1 = sistema.buscarEnvio("R001");
        caso("Sobre urbano", "0.2 kg / urbano", 20.0, s1.costo());
        caso("Sobre departamental", "0.4 kg / departamental", 35.0, s2.costo());
        caso("Volumetrico", "40 x 30 x 20 / 5000", 4.8, paquete.pesoVolumetricoKg());
        caso("Fraccion volumetrica", "max(3.2, 4.8)", 5, paquete.kgFacturables());
        caso("Paquete urbano", "3.2 kg / 40 x 30 x 20", 65.0, paquete.costo());
        caso("Paquete departamental", "1.1 kg / 10 x 10 x 10", 56.0, p2.costo());
        caso("Seguro y embalaje", "Q65 + Q15 + 3% de Q1200", 116.0, f1.costo());
        caso("Fragil departamental", "1.6 kg / Q500 declarado", 86.0, f2.costo());
        caso("Refrigerada decimal", "2.5 kg / urbano", 115.0, r1.costo());
        caso("Kg por fraccion real", "3.2 kg / 10 x 10 x 10", 57.0,
                new Paquete("TX", "A", "B", 3.2, Destino.URBANO, 10, 10, 10).costo());
        caso("Dias sobres", "urbano / departamental", "1 / 2",
                s1.diasHabiles() + " / " + s2.diasHabiles());
        caso("Dias paquetes", "urbano / departamental", "2 / 4",
                paquete.diasHabiles() + " / " + p2.diasHabiles());
        caso("Dias fragiles", "urbano / departamental", "3 / 5",
                f1.diasHabiles() + " / " + f2.diasHabiles());
        caso("Dias refrigerada", "urbano", 1, r1.diasHabiles());
        double previo = sistema.ingresoTotal();
        Cotizacion cotizacion = sistema.cotizar(new Sobre("COT", "A", "B", 0.1, Destino.URBANO));
        caso("Cotizacion", "sobre urbano 0.1 kg", 20.0, cotizacion.costo());
        caso("Cotizacion no cobra", "consultar cotizacion", previo, sistema.ingresoTotal());
        caso("Cotizacion no registra", "codigo COT", 8, sistema.consultarEnvios().size());
        sistema.registrarEnvio(new Sobre("NUEVO", "A", "B", 0.1, Destino.URBANO));
        caso("Registro cobra", "sobre NUEVO", 20.0, sistema.ingresoTotal());
        caso("Ingreso por registro", "sin reintentos", 20.0,
                sistema.ingresosPorTipo().get(TipoIngreso.REGISTRO));
        caso("Ingreso por categoria", "categoria Sobre", 20.0,
                sistema.ingresosPorCategoria().get("Sobre"));
        rechazo("Codigo repetido", "NUEVO", () -> sistema.registrarEnvio(
                new Sobre("NUEVO", "A", "B", 0.1, Destino.URBANO)));
        caso("Rechazo no cobra", "codigo repetido", 20.0, sistema.ingresoTotal());
    }

    private static void probarValidaciones() {
        rechazo("Sobre fuera de rango", "0.51 kg", () -> new Sobre("A", "R", "D", 0.51, Destino.URBANO));
        rechazo("Peso no positivo", "0 kg", () -> new Sobre("A", "R", "D", 0, Destino.URBANO));
        rechazo("Dimension no positiva", "ancho 0 cm", () -> new Paquete(
                "A", "R", "D", 1, Destino.URBANO, 10, 0, 10));
        rechazo("Seguro sin valor", "valor declarado 0", () -> new PaqueteFragil(
                "A", "R", "D", 1, Destino.URBANO, 10, 10, 10, 0));
        rechazo("Refrigerada departamental", "destino departamental", () -> new CargaRefrigerada(
                "A", "R", "D", 1, Destino.DEPARTAMENTAL, 0));
        rechazo("Temperatura inferior", "-26 C", () -> new CargaRefrigerada(
                "A", "R", "D", 1, Destino.URBANO, -26));
        rechazo("Temperatura superior", "9 C", () -> new CargaRefrigerada(
                "A", "R", "D", 1, Destino.URBANO, 9));
        caso("Temperatura 0 valida", "0 C", 0.0, new CargaRefrigerada(
                "A", "R", "D", 1, Destino.URBANO, 0).temperaturaCelsius());
        caso("Temperatura -25 valida", "-25 C", -25.0, new CargaRefrigerada(
                "A", "R", "D", 1, Destino.URBANO, -25).temperaturaCelsius());
        caso("Temperatura 8 valida", "8 C", 8.0, new CargaRefrigerada(
                "A", "R", "D", 1, Destino.URBANO, 8).temperaturaCelsius());
        rechazo("Codigo vacio", "espacios", () -> new Sobre("   ", "R", "D", 0.2, Destino.URBANO));
        rechazo("Placa vacia", "espacios", () -> new Vehiculo(" ", 10, false));
        rechazo("Capacidad no positiva", "0 kg", () -> new Vehiculo("X", 0, false));
        SistemaEnvios sistema = new SistemaEnvios();
        sistema.registrarVehiculo(new Vehiculo("X", 10, false));
        rechazo("Placa repetida", "x", () -> sistema.registrarVehiculo(new Vehiculo("x", 10, true)));
    }

    private static void probarDespachos() {
        SistemaEnvios sistema = new SistemaEnvios();
        sistema.cargarDatosIniciales();
        Despacho vacio = sistema.crearDespacho("V001");
        rechazo("Despacho vacio", "salir sin envios", () -> sistema.salirDespacho(vacio.id()));
        caso("Rechazo no cambia despacho", "salida vacia", EstadoDespacho.PREPARACION, vacio.estado());
        rechazo("Vehiculo ocupado", "V001 con despacho abierto", () -> sistema.crearDespacho("V001"));
        sistema.asignar(vacio.id(), "P001");
        sistema.retirar(vacio.id(), "P001");
        caso("Retiro devuelve a bodega", "P001 retirado", EstadoEnvio.BODEGA, sistema.buscarEnvio("P001").estado());
        caso("Retiro no cobra", "P001 retirado", 0.0, sistema.ingresoTotal());
        sistema.asignar(vacio.id(), "P001");
        sistema.asignar(vacio.id(), "S001");
        sistema.asignar(vacio.id(), "F002");
        caso("Capacidad peso real", "3.2 + 0.2 + 1.6", 5.0, vacio.pesoCargadoKg());
        // V001 admite 10 kg; se ensaya el limite de 5 kg con V003 en otro despacho.
        Despacho limitado = sistema.crearDespacho("V003");
        sistema.retirar(vacio.id(), "F002");
        sistema.asignar(limitado.id(), "F002");
        sistema.asignar(limitado.id(), "P002");
        rechazo("Exceso capacidad", "V003: 1.6 + 1.1 + 3.2 > 5", () -> sistema.asignar(
                limitado.id(), "F001"));
        caso("Rechazo conserva peso", "V003", 2.7, limitado.pesoCargadoKg());
        rechazo("Vehiculo no refrigerado", "R001 en V003", () -> sistema.asignar(
                limitado.id(), "R001"));
        caso("Compatibilidad no cobra", "R001 rechazado", 0.0, sistema.ingresoTotal());
        rechazo("Resultado antes de salir", "P001 / entregado", () -> sistema.registrarResultado(
                vacio.id(), "P001", ResultadoEntrega.ENTREGADO));
        sistema.salirDespacho(vacio.id());
        caso("Salida primer intento", "P001 en ruta", 1, sistema.buscarEnvio("P001").intentosRealizados());
        caso("Primer intento sin recargo", "salida V001", 0.0, sistema.ingresoTotal());
        rechazo("Retiro en ruta", "S001 en ruta", () -> sistema.retirar(vacio.id(), "S001"));
        rechazo("Asignacion en ruta", "S002 a despacho en ruta", () -> sistema.asignar(vacio.id(), "S002"));
        sistema.registrarResultado(vacio.id(), "P001", ResultadoEntrega.ENTREGADO);
        rechazo("Resultado duplicado", "P001 otra vez", () -> sistema.registrarResultado(
                vacio.id(), "P001", ResultadoEntrega.ENTREGADO));
        caso("Despacho sigue en ruta", "S001 pendiente", EstadoDespacho.EN_RUTA, vacio.estado());
        sistema.registrarResultado(vacio.id(), "S001", ResultadoEntrega.FALLIDO);
        caso("Cierre automatico", "todos con resultado", EstadoDespacho.CERRADO, vacio.estado());
        caso("Fallo devuelve a bodega", "S001 intento 1 de 3", EstadoEnvio.BODEGA,
                sistema.buscarEnvio("S001").estado());
        caso("Vehiculo queda libre", "V001 disponible", 3, sistema.crearDespacho("V001").id());
        caso("Ingresos inalterados", "rechazos y resultados", 0.0, sistema.ingresoTotal());
    }

    private static void probarReintentos(String nombre, Envio envio, int maximo) {
        SistemaEnvios sistema = new SistemaEnvios();
        sistema.registrarEnvio(envio);
        sistema.registrarVehiculo(new Vehiculo("TEST", 20, true));
        double costoInicial = envio.costo();
        for (int intento = 1; intento <= maximo; intento++) {
            Despacho despacho = sistema.crearDespacho("TEST");
            sistema.asignar(despacho.id(), envio.codigo());
            caso(nombre + " sin cobro al asignar " + intento, "intento " + intento,
                    costoInicial + 15.0 * Math.max(0, intento - 2), sistema.ingresoTotal());
            sistema.salirDespacho(despacho.id());
            caso(nombre + " cobro al salir " + intento, "intento " + intento,
                    costoInicial + 15.0 * Math.max(0, intento - 1), sistema.ingresoTotal());
            caso(nombre + " cuenta intento " + intento, "salida", intento, envio.intentosRealizados());
            sistema.registrarResultado(despacho.id(), envio.codigo(), ResultadoEntrega.FALLIDO);
            caso(nombre + " estado tras fallo " + intento, "entrega fallida",
                    intento == maximo ? EstadoEnvio.DEVUELTO : EstadoEnvio.BODEGA, envio.estado());
            caso(nombre + " cierre " + intento, "resultado registrado",
                    EstadoDespacho.CERRADO, despacho.estado());
        }
        Despacho siguiente = sistema.crearDespacho("TEST");
        rechazo(nombre + " sin mas intentos", "envio devuelto", () -> sistema.asignar(
                siguiente.id(), envio.codigo()));
        caso(nombre + " ingresos final", "registro + reintentos",
                costoInicial + 15.0 * (maximo - 1), sistema.ingresoTotal());
        caso(nombre + " ingresos por reintentos", "reintentos reales",
                15.0 * (maximo - 1), sistema.ingresosPorTipo().get(TipoIngreso.REINTENTO));
        caso(nombre + " ingresos categoria", "registro + reintentos",
                costoInicial + 15.0 * (maximo - 1), sistema.ingresosPorCategoria().get(envio.categoria()));
    }

    private static void probarEntregaExitosa() {
        SistemaEnvios sistema = new SistemaEnvios();
        Envio envio = new Sobre("OK", "A", "B", 0.1, Destino.DEPARTAMENTAL);
        sistema.registrarEnvio(envio);
        sistema.registrarVehiculo(new Vehiculo("OK", 1, false));
        Despacho primero = sistema.crearDespacho("OK");
        sistema.asignar(primero.id(), "OK");
        sistema.salirDespacho(primero.id());
        sistema.registrarResultado(primero.id(), "OK", ResultadoEntrega.ENTREGADO);
        caso("Entrega terminal", "sobre departamental", EstadoEnvio.ENTREGADO, envio.estado());
        Despacho otro = sistema.crearDespacho("OK");
        rechazo("Entregado no se reasigna", "estado ENTREGADO", () -> sistema.asignar(otro.id(), "OK"));
        caso("Entrega no recobra", "un solo intento", 35.0, sistema.ingresoTotal());
    }

    private static void caso(String nombre, String entrada, Object esperado, Object obtenido) {
        verificadas++;
        boolean correcto;
        if (esperado instanceof Number && obtenido instanceof Number) {
            correcto = Math.abs(((Number) esperado).doubleValue()
                    - ((Number) obtenido).doubleValue()) < 1e-7;
        } else {
            correcto = Objects.equals(esperado, obtenido);
        }
        if (!correcto) fallidas++;
        System.out.println(nombre + " | " + entrada + " | " + esperado + " | " + obtenido
                + " | " + (correcto ? "OK" : "FALLO"));
    }

    private static void rechazo(String nombre, String entrada, Runnable operacion) {
        String resultado = "aceptado";
        try {
            operacion.run();
        } catch (IllegalArgumentException ex) {
            resultado = "rechazado";
        }
        caso(nombre, entrada, "rechazado", resultado);
    }
}