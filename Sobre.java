public final class Sobre extends Envio {
    public Sobre(String codigo, String remitente, String destinatario,
                 double pesoRealKg, Destino destino) {
        super(codigo, remitente, destinatario, pesoRealKg, destino);
        if (pesoRealKg > 0.5) {
            throw new IllegalArgumentException("Un sobre no puede superar 0.5 kg.");
        }
    }

    @Override public String categoria() { return "Sobre"; }
    @Override public double costo() { return destino() == Destino.URBANO ? 20.0 : 35.0; }
    @Override public int diasHabiles() { return destino() == Destino.URBANO ? 1 : 2; }
    @Override public int maxIntentos() { return 3; }
}