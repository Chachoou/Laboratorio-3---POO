public final class Cotizacion {
    private final double costo;
    private final int diasHabiles;

    public Cotizacion(double costo, int diasHabiles) {
        Envio.validarPositivo(costo, "El costo");
        if (diasHabiles <= 0) {
            throw new IllegalArgumentException("Los dias habiles deben ser positivos.");
        }
        this.costo = costo;
        this.diasHabiles = diasHabiles;
    }

    public double costo() { return costo; }
    public int diasHabiles() { return diasHabiles; }
}