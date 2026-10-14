import java.util.Locale;

public class Paquete extends Envio {
    private final double largoCm;
    private final double anchoCm;
    private final double altoCm;

    public Paquete(String codigo, String remitente, String destinatario,
                   double pesoRealKg, Destino destino,
                   double largoCm, double anchoCm, double altoCm) {
        super(codigo, remitente, destinatario, pesoRealKg, destino);
        validarPositivo(largoCm, "El largo");
        validarPositivo(anchoCm, "El ancho");
        validarPositivo(altoCm, "El alto");
        this.largoCm = largoCm;
        this.anchoCm = anchoCm;
        this.altoCm = altoCm;
        if (!Double.isFinite(pesoVolumetricoKg())) {
            throw new IllegalArgumentException("Las dimensiones estan fuera de rango.");
        }
        kgFacturables();
    }

    public final double largoCm() { return largoCm; }
    public final double anchoCm() { return anchoCm; }
    public final double altoCm() { return altoCm; }

    public final double pesoVolumetricoKg() {
        return (largoCm * anchoCm * altoCm) / 5000.0;
    }

    public final int kgFacturables() {
        double peso = Math.ceil(Math.max(pesoRealKg(), pesoVolumetricoKg()));
        if (!Double.isFinite(peso) || peso > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("El peso facturable esta fuera de rango.");
        }
        return (int) peso;
    }

    @Override public String categoria() { return "Paquete"; }
    @Override public double costo() { return redondearMonto(tarifaBase() + 8.0 * kgFacturables()); }
    @Override public int diasHabiles() { return destino() == Destino.URBANO ? 2 : 4; }
    @Override public int maxIntentos() { return 3; }

    @Override
    protected String detalleAdicional() {
        return String.format(Locale.US, " | %.1f x %.1f x %.1f cm", largoCm, anchoCm, altoCm);
    }
}