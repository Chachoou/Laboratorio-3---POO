import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class Aplicacion {
    private final SistemaEnvios sistema;
    private final Scanner teclado;

    public Aplicacion(SistemaEnvios sistema, Scanner teclado) {
        if (sistema == null || teclado == null) {
            throw new IllegalArgumentException("Se necesita el sistema y un lector de entradas.");
        }
        this.sistema = sistema;
        this.teclado = teclado;
    }

    public static void main(String[] args) {
        SistemaEnvios sistema = new SistemaEnvios();
        sistema.cargarDatosIniciales();
        new Aplicacion(sistema, new Scanner(System.in)).ejecutar();
    }

    public void ejecutar() {
        while (true) {
            mostrarMenu();
            if (!teclado.hasNextLine()) return;
            String opcion = teclado.nextLine().trim();
            if (opcion.equals("0")) {
                System.out.println("Hasta luego.");
                return;
            }
            try {
                atenderOpcion(opcion);
            } catch (NoSuchElementException ex) {
                return;
            } catch (IllegalArgumentException ex) {
                System.out.println("Operacion rechazada: " + ex.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("\n=== EnviaGT ===");
        System.out.println("1. Cotizar y, si acepta, registrar envio");
        System.out.println("2. Registrar vehiculo");
        System.out.println("3. Crear despacho");
        System.out.println("4. Asignar envio a despacho");
        System.out.println("5. Retirar envio de despacho");
        System.out.println("6. Hacer salir un despacho");
        System.out.println("7. Registrar resultado de entrega");
        System.out.println("8. Consultar envios");
        System.out.println("9. Consultar vehiculos");
        System.out.println("10. Generar reportes");
        System.out.println("0. Salir");
        System.out.print("Opcion: ");
    }

    private void atenderOpcion(String opcion) {
        switch (opcion) {
            case "1": {
                Envio propuesta = leerEnvio();
                Cotizacion cotizacion = sistema.cotizar(propuesta);
                System.out.printf(Locale.US, "Cotizacion: %s, %d dia(s) habil(es).%n",
                        dinero(cotizacion.costo()), cotizacion.diasHabiles());
                String respuesta = leerTexto("Acepta y registra el envio? (s/n): ");
                if (respuesta.equalsIgnoreCase("s") || respuesta.equalsIgnoreCase("si")) {
                    sistema.registrarEnvio(propuesta);
                    System.out.println("Envio registrado en bodega. Cobrado: " + dinero(propuesta.costo()));
                } else if (respuesta.equalsIgnoreCase("n") || respuesta.equalsIgnoreCase("no")) {
                    System.out.println("Cotizacion sin registro ni cobro.");
                } else {
                    throw new IllegalArgumentException("Responda s o n; no se registro el envio.");
                }
                break;
            }
            case "2": {
                String placa = leerTexto("Placa: ");
                double capacidad = leerDouble("Capacidad maxima (kg): ");
                String respuesta = leerTexto("Tiene refrigeracion? (s/n): ");
                if (!respuesta.equalsIgnoreCase("s") && !respuesta.equalsIgnoreCase("si")
                        && !respuesta.equalsIgnoreCase("n") && !respuesta.equalsIgnoreCase("no")) {
                    throw new IllegalArgumentException("La refrigeracion se responde con s o n.");
                }
                boolean refrigerado = respuesta.equalsIgnoreCase("s") || respuesta.equalsIgnoreCase("si");
                sistema.registrarVehiculo(new Vehiculo(placa, capacidad, refrigerado));
                System.out.println("Vehiculo registrado.");
                break;
            }
            case "3": {
                Despacho despacho = sistema.crearDespacho(leerTexto("Placa del vehiculo: "));
                System.out.println("Despacho " + despacho.id() + " creado en preparacion.");
                break;
            }
            case "4": {
                sistema.asignar(leerEntero("ID del despacho: "), leerTexto("Codigo del envio: "));
                System.out.println("Envio asignado.");
                break;
            }
            case "5": {
                sistema.retirar(leerEntero("ID del despacho: "), leerTexto("Codigo del envio: "));
                System.out.println("Envio devuelto a bodega.");
                break;
            }
            case "6": {
                sistema.salirDespacho(leerEntero("ID del despacho: "));
                System.out.println("Despacho en ruta. Se registraron los cargos de reintento aplicables.");
                break;
            }
            case "7": {
                int id = leerEntero("ID del despacho: ");
                String codigo = leerTexto("Codigo del envio: ");
                int resultado = leerEntero("1. Entregado / 2. Entrega fallida: ");
                if (resultado != 1 && resultado != 2) {
                    throw new IllegalArgumentException("El resultado debe ser 1 o 2.");
                }
                sistema.registrarResultado(id, codigo,
                        resultado == 1 ? ResultadoEntrega.ENTREGADO : ResultadoEntrega.FALLIDO);
                System.out.println("Resultado registrado: " + sistema.buscarEnvio(codigo).estado());
                break;
            }
            case "8":
                System.out.println("Envios:");
                for (Envio envio : sistema.consultarEnvios()) System.out.println("  " + envio);
                break;
            case "9":
                System.out.println("Vehiculos:");
                for (Vehiculo vehiculo : sistema.consultarVehiculos()) System.out.println("  " + vehiculo);
                break;
            case "10":
                mostrarReportes();
                break;
            default:
                throw new IllegalArgumentException("Opcion de menu desconocida.");
        }
    }

    private Envio leerEnvio() {
        System.out.println("1. Sobre / 2. Paquete / 3. Paquete fragil / 4. Carga refrigerada");
        int categoria = leerEntero("Categoria: ");
        if (categoria < 1 || categoria > 4) {
            throw new IllegalArgumentException("Categoria desconocida.");
        }
        String codigo = leerTexto("Codigo: ");
        String remitente = leerTexto("Remitente: ");
        String destinatario = leerTexto("Destinatario: ");
        double peso = leerDouble("Peso real (kg): ");
        int opcionDestino = leerEntero("Destino 1. Urbano / 2. Departamental: ");
        if (opcionDestino != 1 && opcionDestino != 2) {
            throw new IllegalArgumentException("Destino desconocido.");
        }
        Destino destino = opcionDestino == 1 ? Destino.URBANO : Destino.DEPARTAMENTAL;
        switch (categoria) {
            case 1:
                return new Sobre(codigo, remitente, destinatario, peso, destino);
            case 2:
            case 3: {
                double largo = leerDouble("Largo (cm): ");
                double ancho = leerDouble("Ancho (cm): ");
                double alto = leerDouble("Alto (cm): ");
                if (categoria == 2) {
                    return new Paquete(codigo, remitente, destinatario, peso, destino, largo, ancho, alto);
                }
                double valor = leerDouble("Valor declarado (Q): ");
                return new PaqueteFragil(codigo, remitente, destinatario, peso,
                        destino, largo, ancho, alto, valor);
            }
            case 4:
                double temperatura = leerDouble("Temperatura requerida (grados C): ");
                return new CargaRefrigerada(codigo, remitente, destinatario, peso, destino, temperatura);
            default:
                throw new IllegalArgumentException("Categoria desconocida.");
        }
    }

    private void mostrarReportes() {
        System.out.println("Envios por categoria y estado:");
        for (Map.Entry<String, Map<EstadoEnvio, Integer>> categoria
                : sistema.contarPorCategoriaYEstado().entrySet()) {
            System.out.println("  " + categoria.getKey() + ": " + categoria.getValue());
        }
        System.out.println("Ingreso total: " + dinero(sistema.ingresoTotal()));
        for (Map.Entry<TipoIngreso, Double> tipo : sistema.ingresosPorTipo().entrySet()) {
            System.out.println("  " + tipo.getKey() + ": " + dinero(tipo.getValue()));
        }
        System.out.println("Ingresos por categoria:");
        for (Map.Entry<String, Double> categoria : sistema.ingresosPorCategoria().entrySet()) {
            System.out.println("  " + categoria.getKey() + ": " + dinero(categoria.getValue()));
        }
        System.out.println("Despachos sin cerrar:");
        for (Despacho despacho : sistema.despachosAbiertos()) {
            System.out.printf(Locale.US,
                    "  #%d | %s | %s | peso %.3f kg | disponibles %.3f kg%n",
                    despacho.id(), despacho.estado(), despacho.vehiculo().placa(),
                    despacho.pesoCargadoKg(), despacho.capacidadDisponibleKg());
        }
    }

    private static String dinero(double monto) {
        return String.format(Locale.US, "Q%.2f", monto);
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return teclado.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        String texto = leerTexto(mensaje);
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Se esperaba un numero entero.");
        }
    }

    private double leerDouble(String mensaje) {
        String texto = leerTexto(mensaje);
        try {
            double valor = Double.parseDouble(texto);
            if (!Double.isFinite(valor)) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Se esperaba un numero decimal valido (use punto decimal).");
        }
    }
}