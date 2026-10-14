import java.util.Locale;

public abstract class Envio {
    private final String codigo;
    private final String remitente;
    private final String destinatario;
    private final double pesoRealKg;
    private final Destino destino;
    private EstadoEnvio estado;
    private int intentosRealizados;

    protected Envio(String codigo, String remitente, String destinatario,
                    double pesoRealKg, Destino destino) {
        this.codigo = textoObligatorio(codigo, "El codigo").toUpperCase(Locale.ROOT);
        this.remitente = textoObligatorio(remitente, "El remitente");
        this.destinatario = textoObligatorio(destinatario, "El destinatario");
        validarPositivo(pesoRealKg, "El peso real");
        if (destino == null) {
            throw new IllegalArgumentException("El destino es obligatorio.");
        }
        this.pesoRealKg = pesoRealKg;
        this.destino = destino;
        this.estado = EstadoEnvio.BODEGA;
        this.intentosRealizados = 0;
    }

    static String textoObligatorio(String valor, String nombre) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(nombre + " no puede estar vacio.");
        }
        return valor.trim();
    }

    static void validarPositivo(double valor, String nombre) {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(nombre + " debe ser un numero positivo y finito.");
        }
    }

    protected static double redondearMonto(double monto) {
        if (!Double.isFinite(monto) || monto < 0 || monto > Long.MAX_VALUE / 100.0) {
            throw new IllegalArgumentException("El monto esta fuera de rango.");
        }
        return Math.round(monto * 100.0) / 100.0;
    }

    protected double tarifaBase() {
        return destino == Destino.URBANO ? 25.0 : 40.0;
    }

    public final String codigo() { return codigo; }
    public final String remitente() { return remitente; }
    public final String destinatario() { return destinatario; }
    public final double pesoRealKg() { return pesoRealKg; }
    public final Destino destino() { return destino; }
    public final EstadoEnvio estado() { return estado; }
    public final int intentosRealizados() { return intentosRealizados; }

    public abstract String categoria();
    public abstract double costo();
    public abstract int diasHabiles();
    public abstract int maxIntentos();

    public boolean compatibleCon(Vehiculo vehiculo) {
        return vehiculo != null;
    }

    public final boolean puedeAsignarse() {
        return estado == EstadoEnvio.BODEGA && intentosRealizados < maxIntentos();
    }

    final void asignar() {
        if (!puedeAsignarse()) {
            throw new IllegalArgumentException("El envio no esta disponible para asignarlo.");
        }
        estado = EstadoEnvio.ASIGNADO;
    }

    final void retirar() {
        if (estado != EstadoEnvio.ASIGNADO) {
            throw new IllegalArgumentException("Solo se puede retirar un envio asignado.");
        }
        estado = EstadoEnvio.BODEGA;
    }

    final boolean iniciarIntento() {
        if (estado != EstadoEnvio.ASIGNADO || intentosRealizados >= maxIntentos()) {
            throw new IllegalArgumentException("El envio no puede iniciar otro intento.");
        }
        intentosRealizados++;
        estado = EstadoEnvio.EN_RUTA;
        return intentosRealizados > 1;
    }

    final void registrarResultado(ResultadoEntrega resultado) {
        if (estado != EstadoEnvio.EN_RUTA || resultado == null || resultado == ResultadoEntrega.PENDIENTE) {
            throw new IllegalArgumentException("El resultado no es valido para este envio.");
        }
        if (resultado == ResultadoEntrega.ENTREGADO) {
            estado = EstadoEnvio.ENTREGADO;
        } else {
            estado = intentosRealizados < maxIntentos() ? EstadoEnvio.BODEGA : EstadoEnvio.DEVUELTO;
        }
    }

    protected String detalleAdicional() {
        return "";
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "%s | %s | %s -> %s | %.3f kg | %s | %s | intentos %d%s",
                codigo, categoria(), remitente, destinatario, pesoRealKg,
                destino, estado, intentosRealizados, detalleAdicional());
    }
}