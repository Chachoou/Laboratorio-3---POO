import java.util.Locale;

public final class PaqueteFragil extends Paquete {
    private final double valorDeclarado;

    public PaqueteFragil(String codigo, String remitente, String destinatario,
                         double pesoRealKg, Destino destino,
                         double largoCm, double anchoCm, double altoCm, double valorDeclarado) {
        super(codigo, remitente, destinatario, pesoRealKg, destino, largoCm, anchoCm, altoCm);
        validarPositivo(valorDeclarado, "El valor declarado");
        this.valorDeclarado = valorDeclarado;
        costo();
    }

    public double valorDeclarado() { return valorDeclarado; }
    @Override public String categoria() { return "Paquete fragil"; }
    @Override public double costo() { return redondearMonto(super.costo() + 15.0 + valorDeclarado * 0.03); }
    @Override public int diasHabiles() { return super.diasHabiles() + 1; }
    @Override public int maxIntentos() { return 2; }

    @Override
    protected String detalleAdicional() {
        return super.detalleAdicional() + String.format(Locale.US, " | valor Q%.2f", valorDeclarado);
    }
}