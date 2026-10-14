import java.util.Locale;

public final class Vehiculo {
    private final String placa;
    private final double capacidadKg;
    private final boolean refrigerado;

    public Vehiculo(String placa, double capacidadKg, boolean refrigerado) {
        this.placa = Envio.textoObligatorio(placa, "La placa").toUpperCase(Locale.ROOT);
        Envio.validarPositivo(capacidadKg, "La capacidad");
        this.capacidadKg = capacidadKg;
        this.refrigerado = refrigerado;
    }

    public String placa() { return placa; }
    public double capacidadKg() { return capacidadKg; }
    public boolean refrigerado() { return refrigerado; }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s | capacidad %.3f kg | refrigeracion: %s",
                placa, capacidadKg, refrigerado ? "si" : "no");
    }
}