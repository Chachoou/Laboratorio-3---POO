import java.util.Locale;

public final class CargaRefrigerada extends Envio {
    private final double temperaturaCelsius;

    public CargaRefrigerada(String codigo, String remitente, String destinatario,
                            double pesoRealKg, Destino destino, double temperaturaCelsius) {
        super(codigo, remitente, destinatario, pesoRealKg, destino);
        if (destino != Destino.URBANO) {
            throw new IllegalArgumentException("La carga refrigerada solo admite destino urbano.");
        }
        if (!Double.isFinite(temperaturaCelsius) || temperaturaCelsius < -25 || temperaturaCelsius > 8) {
            throw new IllegalArgumentException("La temperatura debe estar entre -25 y 8 grados C.");
        }
        this.temperaturaCelsius = temperaturaCelsius;
        costo();
    }

    public double temperaturaCelsius() { return temperaturaCelsius; }
    @Override public String categoria() { return "Carga refrigerada"; }
    @Override public double costo() { return redondearMonto(tarifaBase() + 12.0 * pesoRealKg() + 60.0); }
    @Override public int diasHabiles() { return 1; }
    @Override public int maxIntentos() { return 1; }
    @Override public boolean compatibleCon(Vehiculo vehiculo) { return vehiculo != null && vehiculo.refrigerado(); }

    @Override
    protected String detalleAdicional() {
        return String.format(Locale.US, " | %.1f grados C", temperaturaCelsius);
    }
}