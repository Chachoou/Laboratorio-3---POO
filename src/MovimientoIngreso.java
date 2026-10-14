public final class MovimientoIngreso {
    private final Envio envio;
    private final TipoIngreso tipo;
    private final double monto;

    public MovimientoIngreso(Envio envio, TipoIngreso tipo, double monto) {
        if (envio == null || tipo == null) {
            throw new IllegalArgumentException("El envio y el tipo de ingreso son obligatorios.");
        }
        Envio.validarPositivo(monto, "El monto");
        this.envio = envio;
        this.tipo = tipo;
        this.monto = monto;
    }

    public String categoria() { return envio.categoria(); }
    public TipoIngreso tipo() { return tipo; }
    public double monto() { return monto; }
}