import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SistemaEnvios {
    private static final double CARGO_REINTENTO = 15.0;
    private final Map<String, Envio> envios = new LinkedHashMap<>();
    private final Map<String, Vehiculo> vehiculos = new LinkedHashMap<>();
    private final Map<Integer, Despacho> despachos = new LinkedHashMap<>();
    private final List<MovimientoIngreso> ingresos = new ArrayList<>();
    private int siguienteIdDespacho = 1;

    public SistemaEnvios() { }

    public void cargarDatosIniciales() {
        if (!envios.isEmpty() || !vehiculos.isEmpty() || !despachos.isEmpty() || !ingresos.isEmpty()) {
            throw new IllegalArgumentException("Los datos iniciales solo se cargan en un sistema vacio.");
        }
        List<Envio> iniciales = List.of(
                new Sobre("S001", "Ana", "Luis", 0.2, Destino.URBANO),
                new Sobre("S002", "Luis", "Ana", 0.4, Destino.DEPARTAMENTAL),
                new Paquete("P001", "Eva", "Mario", 3.2, Destino.URBANO, 40, 30, 20),
                new Paquete("P002", "Mario", "Eva", 1.1, Destino.DEPARTAMENTAL, 10, 10, 10),
                new PaqueteFragil("F001", "Lina", "Raul", 3.2, Destino.URBANO, 40, 30, 20, 1200),
                new PaqueteFragil("F002", "Raul", "Lina", 1.6, Destino.DEPARTAMENTAL, 10, 10, 10, 500),
                new CargaRefrigerada("R001", "Marta", "Hospital", 2.5, Destino.URBANO, 4),
                new CargaRefrigerada("R002", "Hospital", "Marta", 1.0, Destino.URBANO, -25));
        List<Vehiculo> inicialesVehiculos = List.of(
                new Vehiculo("V001", 10.0, false),
                new Vehiculo("V002", 15.0, true),
                new Vehiculo("V003", 5.0, false));
        for (Envio envio : iniciales) {
            envios.put(envio.codigo(), envio);
        }
        for (Vehiculo vehiculo : inicialesVehiculos) {
            vehiculos.put(vehiculo.placa(), vehiculo);
        }
    }

    public Cotizacion cotizar(Envio propuesta) {
        if (propuesta == null) {
            throw new IllegalArgumentException("Los datos de la cotizacion son obligatorios.");
        }
        return new Cotizacion(propuesta.costo(), propuesta.diasHabiles());
    }

    public void registrarEnvio(Envio envio) {
        if (envio == null || envio.estado() != EstadoEnvio.BODEGA || envio.intentosRealizados() != 0) {
            throw new IllegalArgumentException("El envio debe ser nuevo y estar en bodega.");
        }
        if (envios.containsKey(envio.codigo())) {
            throw new IllegalArgumentException("Ya existe un envio con ese codigo.");
        }
        MovimientoIngreso movimiento = new MovimientoIngreso(envio, TipoIngreso.REGISTRO, envio.costo());
        envios.put(envio.codigo(), envio);
        ingresos.add(movimiento);
    }

    public void registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehiculo es obligatorio.");
        }
        if (vehiculos.containsKey(vehiculo.placa())) {
            throw new IllegalArgumentException("Ya existe un vehiculo con esa placa.");
        }
        vehiculos.put(vehiculo.placa(), vehiculo);
    }

    public Despacho crearDespacho(String placa) {
        Vehiculo vehiculo = buscarVehiculo(placa);
        for (Despacho despacho : despachos.values()) {
            if (despacho.vehiculo().placa().equals(vehiculo.placa())
                    && despacho.estado() != EstadoDespacho.CERRADO) {
                throw new IllegalArgumentException("El vehiculo ya tiene un despacho sin cerrar.");
            }
        }
        Despacho despacho = new Despacho(siguienteIdDespacho, vehiculo);
        despachos.put(siguienteIdDespacho++, despacho);
        return despacho;
    }

    public void asignar(int id, String codigo) {
        buscarDespacho(id).asignar(buscarEnvio(codigo));
    }

    public void retirar(int id, String codigo) {
        buscarDespacho(id).retirar(buscarEnvio(codigo));
    }

    public void salirDespacho(int id) {
        Despacho despacho = buscarDespacho(id);
        List<Envio> reintentos = despacho.salir();
        for (Envio envio : reintentos) {
            ingresos.add(new MovimientoIngreso(envio, TipoIngreso.REINTENTO, CARGO_REINTENTO));
        }
    }

    public void registrarResultado(int id, String codigo, ResultadoEntrega resultado) {
        buscarDespacho(id).registrarResultado(buscarEnvio(codigo), resultado);
    }

    public Envio buscarEnvio(String codigo) {
        String clave = Envio.textoObligatorio(codigo, "El codigo").toUpperCase(Locale.ROOT);
        Envio envio = envios.get(clave);
        if (envio == null) throw new IllegalArgumentException("No existe ese envio.");
        return envio;
    }

    public Vehiculo buscarVehiculo(String placa) {
        String clave = Envio.textoObligatorio(placa, "La placa").toUpperCase(Locale.ROOT);
        Vehiculo vehiculo = vehiculos.get(clave);
        if (vehiculo == null) throw new IllegalArgumentException("No existe ese vehiculo.");
        return vehiculo;
    }

    public Despacho buscarDespacho(int id) {
        Despacho despacho = despachos.get(id);
        if (despacho == null) throw new IllegalArgumentException("No existe ese despacho.");
        return despacho;
    }

    public Collection<Envio> consultarEnvios() {
        return Collections.unmodifiableList(new ArrayList<>(envios.values()));
    }

    public Collection<Vehiculo> consultarVehiculos() {
        return Collections.unmodifiableList(new ArrayList<>(vehiculos.values()));
    }

    public Map<String, Map<EstadoEnvio, Integer>> contarPorCategoriaYEstado() {
        Map<String, Map<EstadoEnvio, Integer>> conteos = new LinkedHashMap<>();
        for (Envio envio : envios.values()) {
            Map<EstadoEnvio, Integer> porEstado = conteos.computeIfAbsent(
                    envio.categoria(), categoria -> {
                        Map<EstadoEnvio, Integer> ceros = new EnumMap<>(EstadoEnvio.class);
                        for (EstadoEnvio estado : EstadoEnvio.values()) ceros.put(estado, 0);
                        return ceros;
                    });
            porEstado.put(envio.estado(), porEstado.get(envio.estado()) + 1);
        }
        return conteos;
    }

    public Map<TipoIngreso, Double> ingresosPorTipo() {
        Map<TipoIngreso, Double> totales = new EnumMap<>(TipoIngreso.class);
        for (TipoIngreso tipo : TipoIngreso.values()) totales.put(tipo, 0.0);
        for (MovimientoIngreso ingreso : ingresos) {
            totales.put(ingreso.tipo(), totales.get(ingreso.tipo()) + ingreso.monto());
        }
        return totales;
    }

    public Map<String, Double> ingresosPorCategoria() {
        Map<String, Double> totales = new LinkedHashMap<>();
        for (Envio envio : envios.values()) totales.putIfAbsent(envio.categoria(), 0.0);
        for (MovimientoIngreso ingreso : ingresos) {
            totales.merge(ingreso.categoria(), ingreso.monto(), Double::sum);
        }
        return totales;
    }

    public double ingresoTotal() {
        double total = 0.0;
        for (MovimientoIngreso ingreso : ingresos) total += ingreso.monto();
        return total;
    }

    public List<Despacho> despachosAbiertos() {
        List<Despacho> abiertos = new ArrayList<>();
        for (Despacho despacho : despachos.values()) {
            if (despacho.estado() != EstadoDespacho.CERRADO) abiertos.add(despacho);
        }
        return Collections.unmodifiableList(abiertos);
    }
}