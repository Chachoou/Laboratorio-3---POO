import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Despacho {
    private final int id;
    private final Vehiculo vehiculo;
    private final Map<Envio, ResultadoEntrega> envios;
    private EstadoDespacho estado;

    Despacho(int id, Vehiculo vehiculo) {
        if (id <= 0 || vehiculo == null) {
            throw new IllegalArgumentException("Identificador y vehiculo validos son obligatorios.");
        }
        this.id = id;
        this.vehiculo = vehiculo;
        this.envios = new LinkedHashMap<>();
        this.estado = EstadoDespacho.PREPARACION;
    }

    public int id() { return id; }
    public Vehiculo vehiculo() { return vehiculo; }
    public EstadoDespacho estado() { return estado; }

    public Map<Envio, ResultadoEntrega> enviosYResultados() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(envios));
    }

    public double pesoCargadoKg() {
        double total = 0.0;
        for (Envio envio : envios.keySet()) {
            total += envio.pesoRealKg();
        }
        return total;
    }

    public double capacidadDisponibleKg() {
        return Math.max(0.0, vehiculo.capacidadKg() - pesoCargadoKg());
    }

    void asignar(Envio envio) {
        if (estado != EstadoDespacho.PREPARACION) {
            throw new IllegalArgumentException("Solo se puede asignar a un despacho en preparacion.");
        }
        if (envio == null || envios.containsKey(envio) || !envio.puedeAsignarse()) {
            throw new IllegalArgumentException("El envio no esta disponible para este despacho.");
        }
        if (!envio.compatibleCon(vehiculo)) {
            throw new IllegalArgumentException("El vehiculo no es compatible con este envio.");
        }
        double nuevoPeso = pesoCargadoKg() + envio.pesoRealKg();
        double toleranciaRepresentacion = 2.0 * Math.ulp(Math.max(nuevoPeso, vehiculo.capacidadKg()));
        if (nuevoPeso > vehiculo.capacidadKg() + toleranciaRepresentacion) {
            throw new IllegalArgumentException("La asignacion supera la capacidad del vehiculo.");
        }
        envio.asignar();
        envios.put(envio, ResultadoEntrega.PENDIENTE);
    }

    void retirar(Envio envio) {
        if (estado != EstadoDespacho.PREPARACION || envio == null || !envios.containsKey(envio)) {
            throw new IllegalArgumentException("Solo se pueden retirar envios durante la preparacion.");
        }
        envio.retirar();
        envios.remove(envio);
    }

    List<Envio> salir() {
        if (estado != EstadoDespacho.PREPARACION || envios.isEmpty()) {
            throw new IllegalArgumentException("Solo puede salir un despacho en preparacion y no vacio.");
        }
        List<Envio> reintentos = new ArrayList<>();
        for (Envio envio : envios.keySet()) {
            if (envio.estado() != EstadoEnvio.ASIGNADO || envio.intentosRealizados() >= envio.maxIntentos()) {
                throw new IllegalArgumentException("Hay un envio que no puede salir en este despacho.");
            }
            if (envio.intentosRealizados() > 0) {
                reintentos.add(envio);
            }
        }
        for (Envio envio : envios.keySet()) {
            envio.iniciarIntento();
        }
        estado = EstadoDespacho.EN_RUTA;
        return reintentos;
    }

    void registrarResultado(Envio envio, ResultadoEntrega resultado) {
        if (estado != EstadoDespacho.EN_RUTA || envio == null
                || envios.get(envio) != ResultadoEntrega.PENDIENTE) {
            throw new IllegalArgumentException("El envio no tiene un resultado pendiente en este despacho.");
        }
        if (resultado == null || resultado == ResultadoEntrega.PENDIENTE) {
            throw new IllegalArgumentException("El resultado debe ser entregado o fallido.");
        }
        envio.registrarResultado(resultado);
        envios.put(envio, resultado);
        if (!envios.containsValue(ResultadoEntrega.PENDIENTE)) {
            estado = EstadoDespacho.CERRADO;
        }
    }
}